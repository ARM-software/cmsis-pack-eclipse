/*******************************************************************************
 * Copyright (c) 2015 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/

package com.arm.cmsis.pack;


import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpPackCollection;
import com.arm.cmsis.pack.data.ICpBoard;
import com.arm.cmsis.pack.data.ICpExample;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPack.PackState;
import com.arm.cmsis.pack.data.ICpPackCollection;
import com.arm.cmsis.pack.events.IRteEventProxy;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.parser.ICpXmlParser;
import com.arm.cmsis.pack.parser.PdscParser;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;
import com.arm.cmsis.pack.rte.devices.RteDeviceItem;
import com.arm.cmsis.pack.utils.Utils;
import com.arm.cmsis.pack.utils.VersionComparator;

/**
 * Default simple CMSIS-Pack manager
 */
public class CpPackManager implements ICpPackManager {

	protected ICpPackCollection allPacks = null; // global pack collection  
	protected ICpPackCollection allGenericPacks = null; // generic pack collection
	protected ICpPackCollection allDevicePacks = null; // device-specific pack collection
	protected ICpXmlParser pdscParser = null;
	protected IRteDeviceItem allDevices = null;
	protected Map<String, ICpBoard> allBoards = null;
	protected Map<String, ICpExample> allExamples = null;
	protected String cmsisPackRootDirectory = null;
	protected boolean bPacksLoaded = false;
	protected IRteEventProxy fRteEventProxy = null;

	private ICpPack.PackState packState = PackState.UNKNOWN;

	/**
	 *  Default pack manager implementation
	 */
	public CpPackManager() {
	}


	@Override
	public IRteEventProxy getRteEventProxy() {
		return fRteEventProxy;
	}

	@Override
	public void setRteEventProxy(IRteEventProxy rteEventProxy) {
		fRteEventProxy = rteEventProxy;
	}


	@Override
	public boolean initParser(String xsdFile){
		if(pdscParser == null) {
			pdscParser = new PdscParser(xsdFile);
		} else {
			pdscParser.setXsdFile(xsdFile);
		}
		return pdscParser.init();
	}

	@Override
	synchronized public void clear() {
		allPacks = null;
		allGenericPacks = null;
		allDevicePacks = null;
		allDevices = null;
		allBoards = null;
		allExamples = null;
		bPacksLoaded = false;
		if(pdscParser != null) {
			pdscParser.clear();
		}
	}


	@Override
	synchronized public void reload() {
		clear();
		getPacks(); // triggers load
		if(fRteEventProxy != null) {
			fRteEventProxy.notifyListeners(new RteEvent(RteEvent.PACKS_RELOADED));
		}
	}

	@Override
	public void destroy() {
		clear();
		pdscParser = null;
	}

	@Override
	synchronized public ICpPackCollection getPacks() {
		if(allPacks == null) {
			bPacksLoaded = loadPacks(cmsisPackRootDirectory);
		}
		return allPacks;
	}

	@Override
	synchronized public ICpPackCollection getDevicePacks() {
		if(allPacks == null) {
			bPacksLoaded = loadPacks(cmsisPackRootDirectory);
		}
		return allDevicePacks;
	}

	@Override
	synchronized public ICpPackCollection getGenericPacks() {
		if(allPacks == null) {
			bPacksLoaded = loadPacks(cmsisPackRootDirectory);
		}
		return allGenericPacks;
	}

	@Override
	synchronized public IRteDeviceItem getDevices() {
		getPacks(); // ensure allPacks are loaded
		if(allDevices == null && bPacksLoaded && allPacks != null)  {
			allDevices = RteDeviceItem.createTree(allPacks.getLatestPacks());
		}
		return allDevices;
	}

	@Override
	synchronized public Map<String, ICpBoard> getBoards() {
		getPacks(); // ensure allPacks are loaded
		if(allBoards == null && bPacksLoaded && allPacks != null)  {
			collectBoards();
		}
		return allBoards;
	}

	protected void collectBoards() {
		allBoards = new HashMap<String, ICpBoard>();
		Collection<ICpPack> packs = allPacks.getPacks();
		for(ICpPack p: packs) {
			Collection<? extends ICpItem> boards = p.getGrandChildren(CmsisConstants.BOARDS_TAG);
			if(boards == null || boards.isEmpty()) {
				continue;
			}

			for(ICpItem item : boards) {
				if(!(item instanceof ICpBoard)) {
					continue;
				}
				ICpBoard b = (ICpBoard)item;
				String id = b.getId();
				if(allBoards.containsKey(id) && VersionComparator.versionCompare(
								allBoards.get(id).getPack().getVersion(),
								b.getPack().getVersion()) > 0) {
					continue;
				}
				allBoards.put(id, b);
			}
		}
	}

	@Override
	public Collection<ICpBoard> getCompatibleBoards(IAttributes deviceAttributes) {
		List<ICpBoard> boards =  new LinkedList<ICpBoard>();
		getBoards();
		if(allBoards == null || allBoards.isEmpty()) {
			return boards;
		}
		for(ICpBoard b : allBoards.values()){
			if(b.hasCompatibleDevice(deviceAttributes)) {
				boards.add(b);
			}
		}
		return boards;
	}

	@Override
	synchronized public Collection<ICpExample> getExamples() {
		getPacks(); // ensure allPacks are loaded
		if(allExamples == null && bPacksLoaded && allPacks != null)  {
			collectExamples();
		}
		return allExamples.values();
	}

	protected void collectExamples() {
		allExamples = new HashMap<String, ICpExample>();
		Collection<ICpPack> packs = allPacks.getPacks();
		for(ICpPack p: packs) {
			// TODO: If all packs are there, it becomes super slow...
			if (p.getPackState() == PackState.INSTALLED) {
				Collection<? extends ICpItem> examples = p.getGrandChildren(CmsisConstants.EXAMPLES_TAG);
				if(examples == null || examples.isEmpty()) {
					continue;
				}

				for(ICpItem item : examples) {
					if(!(item instanceof ICpExample)) {
						continue;
					}
					ICpExample e = (ICpExample)item;
					String id = e.getId();
					if(allExamples.containsKey(id) && VersionComparator.versionCompare(
							allExamples.get(id).getPack().getVersion(),
							e.getPack().getVersion()) > 0) {
						continue;
					}
					allExamples.put(id, e);
				}
			}
		}
	}

	@Override
	synchronized public boolean loadPacks(final String rootDirectory){
		if(rootDirectory == null || rootDirectory.isEmpty()) {
			return false;
		}

		File root = new File(rootDirectory);
		if(root == null || !root.exists()) {
			return false;
		}

		setCmsisPackRootDirectory(rootDirectory);
//
//      Reserved for future: 		
//		packState = PackState.AVAILABLE;
//		Collection<String> availableFileNames = Utils.findPdscFiles(
//				new File(rootDirectory + File.separator + ".Web"), null, 0); //$NON-NLS-1$
//		if (!loadPacks(availableFileNames)) {
//			return false;
//		}
//
//		packState = PackState.DOWNLOADED;
//		Collection<String> downloadedFileNames = Utils.findPdscFiles(
//				new File(rootDirectory + File.separator + ".Download"), null, 0); //$NON-NLS-1$
//		if (!loadPacks(downloadedFileNames))
//			return false;

// for now only load installed packs		
		packState = PackState.INSTALLED;
		Collection<String> installedFileNames = Utils.findPdscFiles(root, null, 3); 
		if (!loadPacks(installedFileNames)) {
			return false;
		}
		packState = PackState.UNKNOWN;
		return true;
	}


	@Override
	public boolean loadPacks(final Collection<String> fileNames){
		if(fileNames == null || fileNames.isEmpty()) {
			return true; // nothing to load => success 
		}

		boolean success = true;
		for(String f : fileNames) {
			if(loadPack(f) == false) {
				success = false;
			}
		}
		return success;
	}

	@Override
	public boolean loadPack(String file){
		if(allPacks == null) {
			allPacks = new CpPackCollection();
		}
		if (allGenericPacks == null) {
			allGenericPacks = new CpPackCollection();
		}
		if (allDevicePacks == null) {
			allDevicePacks = new CpPackCollection();
		}

		ICpPack pack  = (ICpPack)pdscParser.parseFile(file);
		if(pack != null) {
			pack.setPackState(packState);
			allPacks.addChild(pack);
			if (pack.isDevicelessPack()) {
				allGenericPacks.addChild(pack);
			} else {
				allDevicePacks.addChild(pack);
			}
		}
		return pack != null;
	}

	@Override
	public String getCmsisPackRootDirectory() {
		return cmsisPackRootDirectory;
	}

	@Override
	public void setCmsisPackRootDirectory(String packRootDirectory) {
		if(packRootDirectory == null) {
			cmsisPackRootDirectory = null;
		} else {
			// normalize and convert to Unix format 
			IPath p = new Path(Utils.removeTrailingSlash(packRootDirectory));
			cmsisPackRootDirectory = p.toString();
		}
	}

	@Override
	public boolean arePacksLoaded() {
		return bPacksLoaded;
	}

	@Override
	public void setParser(ICpXmlParser xmlParser) {
		pdscParser = xmlParser;
	}

	@Override
	public ICpXmlParser getParser() {
		return pdscParser;
	}


}
