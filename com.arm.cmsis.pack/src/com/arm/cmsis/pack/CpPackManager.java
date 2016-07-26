/*******************************************************************************
 * Copyright (c) 2015 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 * A snippet for file change monitoring is taken from:
 * http://stackoverflow.com/questions/16251273/can-i-watch-for-single-file-change-with-watchservice-not-the-whole-directory
 *******************************************************************************/

package com.arm.cmsis.pack;


import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.arm.cmsis.pack.ICpPackInstaller.ConsoleColor;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpPack;
import com.arm.cmsis.pack.data.CpPackCollection;
import com.arm.cmsis.pack.data.CpPackFamily;
import com.arm.cmsis.pack.data.ICpBoard;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPack.PackState;
import com.arm.cmsis.pack.data.ICpPackCollection;
import com.arm.cmsis.pack.data.ICpPackFamily;
import com.arm.cmsis.pack.events.IRteEventListener;
import com.arm.cmsis.pack.events.IRteEventProxy;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.events.RtePackJobResult;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.parser.ICpXmlParser;
import com.arm.cmsis.pack.parser.PdscParser;
import com.arm.cmsis.pack.preferences.CpPreferenceInitializer;
import com.arm.cmsis.pack.repository.CpRepositoryList;
import com.arm.cmsis.pack.rte.boards.IRteBoardDeviceItem;
import com.arm.cmsis.pack.rte.boards.RteBoardDeviceItem;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;
import com.arm.cmsis.pack.rte.devices.RteDeviceItem;
import com.arm.cmsis.pack.rte.examples.IRteExampleItem;
import com.arm.cmsis.pack.rte.examples.RteExampleItem;
import com.arm.cmsis.pack.utils.Utils;
import com.arm.cmsis.pack.utils.VersionComparator;

/**
 * Default simple CMSIS-Pack manager
 */
public class CpPackManager implements ICpPackManager, IRteEventListener {

	protected ICpPackCollection allPacks = null; // global pack collection
	protected ICpPackCollection allInstalledPacks = null; // all installed pack collection
	protected ICpPackCollection allGenericPacks = null; // generic pack collection
	protected ICpPackCollection allDevicePacks = null; // device-specific pack collection
	protected ICpPackFamily allErrorPacks = null; // error pack collection
	protected ICpXmlParser pdscParser = null;
	protected IRteDeviceItem allDevices = null;
	protected IRteDeviceItem allInstalledDevices = null;
	protected Map<String, ICpBoard> allBoards = null;
	protected IRteBoardDeviceItem allRteBoardDevices = null;
	protected IRteExampleItem allExamples = null;
	protected String cmsisPackRootDirectory = null;
	protected URI cmsisPackRootURI = null;
	protected boolean bPacksLoaded = false;
	protected IRteEventProxy fRteEventProxy = null;
	protected ICpPackInstaller fPackInstaller = null;
	protected CpRepositoryList fRepoList = null;

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
		fRteEventProxy.addListener(this);
	}

	@Override
	public void setPackInstaller(ICpPackInstaller packInstaller) {
		fPackInstaller = packInstaller;
	}

	@Override
	public ICpPackInstaller getPackInstaller() {
		return fPackInstaller;
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
		allErrorPacks = null;
		allInstalledPacks = null;
		allGenericPacks = null;
		allDevicePacks = null;
		allDevices = null;
		allInstalledDevices = null;
		allBoards = null;
		allRteBoardDevices = null;
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
	synchronized public ICpPackCollection getInstalledPacks() {
		if(allPacks == null) {
			bPacksLoaded = loadPacks(cmsisPackRootDirectory);
		}
		return allInstalledPacks;
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
	synchronized public ICpPackFamily getErrorPacks() {
		return allErrorPacks;
	}

	@Override
	synchronized public IRteDeviceItem getDevices() {
		getPacks(); // ensure allPacks are loaded
		if(allDevices == null && bPacksLoaded && allPacks != null)  {
			allDevices = RteDeviceItem.createTree(allPacks.getPacks());
		}
		return allDevices;
	}

	@Override
	synchronized public IRteDeviceItem getInstalledDevices() {
		getPacks(); // ensure allPacks are loaded
		if(allInstalledDevices == null && bPacksLoaded && allInstalledPacks != null)  {
			allInstalledDevices = RteDeviceItem.createTree(allInstalledPacks.getLatestPacks());
		}
		return allInstalledDevices;
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
		for(ICpPack pack: packs) {
			addBoards(pack);
		}
	}

	@Override
	synchronized public IRteBoardDeviceItem getRteBoardDevices() {
		getPacks(); // ensure allPacks are loaded
		if(allRteBoardDevices == null && bPacksLoaded && allPacks != null)  {
			allRteBoardDevices = RteBoardDeviceItem.createTree(allPacks.getPacks());
		}
		return allRteBoardDevices;
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
	synchronized public IRteExampleItem getExamples() {
		getPacks(); // ensure allPacks are loaded
		if(allExamples == null && bPacksLoaded && allPacks != null)  {
			allExamples = RteExampleItem.createTree(allPacks.getPacks());
		}
		return allExamples;
	}

	@Override
	synchronized public boolean loadPacks(final String rootDirectory){
		if(rootDirectory == null || rootDirectory.isEmpty()) {
			return false;
		}

		File root = new File(rootDirectory);
		if( !root.exists()) {
			return false;
		}

		packState = PackState.AVAILABLE;
		File webFile = new File(rootDirectory + File.separator + ".Web"); //$NON-NLS-1$
		if (!webFile.exists()) {
			webFile.mkdir();
		}
		Collection<String> availableFileNames = Utils.findPdscFiles(webFile, null, 0);
		loadPacks(availableFileNames);

		packState = PackState.DOWNLOADED;
		File downloadFile = new File(rootDirectory + File.separator + ".Download"); //$NON-NLS-1$
		if (!downloadFile.exists()) {
			downloadFile.mkdir();
		}
		Collection<String> downloadedFileNames = Utils.findPdscFiles(downloadFile, null, 0);
		loadPacks(downloadedFileNames);

		packState = PackState.INSTALLED;
		Collection<String> installedFileNames = Utils.findPdscFiles(root, null, 3);
		loadPacks(installedFileNames);

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
			allGenericPacks = new CpPackCollection(CmsisConstants.GENERIC);
		}
		if (allDevicePacks == null) {
			allDevicePacks = new CpPackCollection(CmsisConstants.DEVICE_SPECIFIC);
		}
		if (allInstalledPacks == null) {
			allInstalledPacks = new CpPackCollection();
		}

		ICpPack pack  = (ICpPack)pdscParser.parseFile(file);
		if (pack != null && CmsisConstants.PACKAGE_TAG.equals(pack.getTag())) {
			pack.setPackState(packState);
			allPacks.addChild(pack);
			if (packState == PackState.INSTALLED) {
				allInstalledPacks.addChild(pack);
			}
			if (pack.isDevicelessPack()) {
				allGenericPacks.addChild(pack);
			} else {
				allDevicePacks.addChild(pack);
			}
		} else {
			if (allErrorPacks == null) {
				allErrorPacks = new CpPackFamily(null, CmsisConstants.ERRORS);
			}
			pack = new CpPack(allErrorPacks);
			pack.setFileName(file);
			pack.setText(Utils.extractFileName(file));
			pack.setTag(Utils.extractFileName(file));
			pack.setPackState(PackState.ERROR);
			allErrorPacks.addChild(pack);
			String errorString;
			if (!pdscParser.getErrorStrings().isEmpty()) {
				errorString = pdscParser.getErrorStrings().get(0);
			} else if (!CmsisConstants.PACKAGE_TAG.equals(pack.getTag())) {
				errorString = pack.getFileName().replace('/', '\\') + ": " //$NON-NLS-1$
						+ CpStrings.CpPackManager_UnrecognizedFileFormatError;
			} else {
				errorString = pack.getFileName().replace('/', '\\') + ": " //$NON-NLS-1$
						+ CpStrings.CpPackManager_DefaultError;
			}
			fPackInstaller.printInConsole(CpStrings.CpPackManager_ErrorWhileParsing + errorString,
					ConsoleColor.ERROR);
		}
		return true;
	}

	@Override
	public String getCmsisPackRootDirectory() {
		return cmsisPackRootDirectory;
	}

	@Override
	synchronized public CpRepositoryList getCpRepositoryList() {
		if (fRepoList == null) {
			fRepoList = new CpRepositoryList();
		}
		return fRepoList;
	}

	@Override
	public URI getCmsisPackRootURI() {
		return cmsisPackRootURI;
	}

	@Override
	public void setCmsisPackRootDirectory(String packRootDirectory) {
		String normalizedPackRoot = null;
		String osPackRoot = CmsisConstants.EMPTY_STRING;
		if(packRootDirectory != null) {
			// normalize and convert to Unix format
			IPath p = new Path(Utils.removeTrailingSlash(packRootDirectory));
			normalizedPackRoot = p.toString();
			osPackRoot = p.toOSString();
		}

		if (cmsisPackRootDirectory == null) {
			if (normalizedPackRoot == null || normalizedPackRoot.isEmpty()) {
				return;
			}
		} else if (cmsisPackRootDirectory.equals(normalizedPackRoot) && bPacksLoaded) {
			return;
		}
		fPackInstaller.stopPackWatchThread();

		if (normalizedPackRoot == null || normalizedPackRoot.isEmpty()) {
			cmsisPackRootDirectory = null;
			cmsisPackRootURI = null;
		} else {
			cmsisPackRootDirectory = normalizedPackRoot;
			File f = new File(cmsisPackRootDirectory);
			cmsisPackRootURI = f.toURI();
		}
		CpPreferenceInitializer.setPackRoot(osPackRoot);

		reload();

		if (cmsisPackRootDirectory != null && !cmsisPackRootDirectory.isEmpty()) {
			fPackInstaller.startPackWatchThread();
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

	@Override
	public void handle(RteEvent event) {
		switch (event.getTopic()) {
			case RteEvent.PACK_INSTALL_JOB_FINISHED:
			case RteEvent.PACK_UNPACK_JOB_FINISHED:
				RtePackJobResult result = (RtePackJobResult) event.getData();
				if (result.isSuccess()) {

					ICpPack pack = result.getPack();
					Assert.isTrue(pack.getPackState() == PackState.INSTALLED ||
							pack.getPackState() == PackState.GENERATED);

					// Update pack collection
					allPacks.addChild(pack);
					allInstalledPacks.addChild(pack);
					if (pack.isDevicelessPack()) {
						allGenericPacks.addChild(pack);
					} else {
						allDevicePacks.addChild(pack);
					}

					// Update RteDevice Tree
					if (allDevices != null) {
						allDevices.addDevices(pack);
					}
					if (allInstalledDevices != null) {
						allInstalledDevices.addDevices(pack);
					}

					// Update Board Collection
					if (allRteBoardDevices != null) {
						allRteBoardDevices.addBoards(pack);
					}
					addBoards(pack);

					// Update Examples Collection
					if (allExamples != null) {
						allExamples.addExamples(pack);
					}
				}
				break;
			case RteEvent.PACK_REMOVE_JOB_FINISHED:
			case RteEvent.PACK_DELETE_JOB_FINISHED:
				result = (RtePackJobResult) event.getData();
				if (result.isSuccess()) {
					ICpPack pack = result.getPack();
					// if the deleted pack is an error pack, only need to remove it from allErrorPacks
					if (pack.getPackState() == PackState.ERROR) {
						allErrorPacks.removeChild(pack);
						pack.setParent(null);
						return;
					}

					// Remove Pack from all installed packs
					Collection<ICpPackCollection> packCollections = new LinkedList<>();
					packCollections.add(allInstalledPacks);
					packCollections.add(allPacks);
					if (RteEvent.PACK_DELETE_JOB_FINISHED.equals(event.getTopic())) {
						if (pack.isDevicelessPack()) {
							packCollections.add(allGenericPacks);
						} else {
							packCollections.add(allDevicePacks);
						}
					}
					for (ICpPackCollection packCollection : packCollections) {
						String familyId = pack.getPackFamilyId();
						for (ICpItem packFamily : packCollection.getChildren()) {
							if (familyId.equals(packFamily.getPackFamilyId())) {
								packFamily.removeChild(pack);
							}
						}
					}

					// Remove Device from device tree if pack is not the latest version of this pack family
					if (allDevices != null) {
						allDevices.removeDevices(pack);
					}
					if (allInstalledDevices != null) {
						allInstalledDevices.removeDevices(pack);
					}

					// Remove Board from board tree
					if (allRteBoardDevices != null) {
						allRteBoardDevices.removeBoards(pack);
					}

					// Remove Example from examples tree
					if (allExamples != null) {
						allExamples.removeExamples(pack);
					}

					// Add new pack into the packs, which could be the new pdsc file in the .Web or the .Download folder
					ICpPack newPack = result.getNewPack();
					if (newPack != null) {
						allPacks.addChild(newPack);
						if (newPack.isDevicelessPack()) {
							allGenericPacks.addChild(newPack);
						} else {
							allDevicePacks.addChild(newPack);
						}

						// Update RteDevice Tree for the newly added pack
						if (allDevices != null) {
							allDevices.addDevices(newPack);
						}
						if (allInstalledDevices != null) {
							allInstalledDevices.addDevices(newPack);
						}

						// Update RteBoard Tree for the newly added pack
						if (allRteBoardDevices != null) {
							allRteBoardDevices.addBoards(newPack);
						}
						addBoards(newPack);

						if (allExamples != null) {
							allExamples.addExamples(newPack);
						}
					} else {
						//reload();
					}
				}
				break;
			default:
				break;
		}
	}

	private void addBoards(ICpPack pack) {
		if (pack == null) {
			return;
		}
		Collection<? extends ICpItem> boards = pack.getGrandChildren(CmsisConstants.BOARDS_TAG);
		if (allBoards != null && boards != null) {
			for(ICpItem item : boards) {
				if(!(item instanceof ICpBoard)) {
					continue;
				}
				ICpBoard currentBoard = (ICpBoard)item;
				String id = currentBoard.getId();
				ICpBoard previousBoard = allBoards.get(id);
				if (previousBoard == null ||
						replacePreviousItem(previousBoard, currentBoard)) {
					allBoards.put(id, currentBoard);
				}
			}
		}
	}

	private boolean replacePreviousItem(ICpItem previous, ICpItem current) {
		PackState ps1 = previous.getPack().getPackState();
		PackState ps2 = current.getPack().getPackState();
		if (ps1.ordinal() < ps2.ordinal()) {
			return false;
		} else if (ps1.ordinal() > ps2.ordinal()) {
			return true;
		} else {
			String pv1 = previous.getPack().getVersion();
			String pv2 = current.getPack().getVersion();
			if (VersionComparator.versionCompare(pv1, pv2) < 0) {
				return true;
			}
		}
		return false;
	}


}
