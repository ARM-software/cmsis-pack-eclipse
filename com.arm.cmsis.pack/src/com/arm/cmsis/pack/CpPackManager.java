/*******************************************************************************
* Copyright (c) 2014 ARM Ltd.
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/

package com.arm.cmsis.pack;


import java.io.File;
import java.util.Collection;

import com.arm.cmsis.pack.data.CpPackCollection;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPackCollection;
import com.arm.cmsis.pack.events.IRteEventProxy;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.parser.ICpXmlParser;
import com.arm.cmsis.pack.parser.PdscParser;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;
import com.arm.cmsis.pack.rte.devices.RteDeviceItem;
import com.arm.cmsis.pack.utils.Utils;

/**
 * Default simple CMSIS-Pack manager
 */
public class CpPackManager implements ICpPackManager {

	protected ICpPackCollection packs = null; // global pack collection  
	protected ICpXmlParser pdscParser = null;
	protected IRteDeviceItem allDevices = null;
	protected String defaultPackDirectory = null;
	protected boolean bPacksLoaded = false;
	protected IRteEventProxy fRteEventProxy = null;
	
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
		if(pdscParser == null)
			pdscParser = new PdscParser(xsdFile);
		else
			pdscParser.setXsdFile(xsdFile);
		return pdscParser.init();
	}

	@Override
	public void clear() {
		packs = null;
		allDevices = null;
		if(pdscParser != null)
			pdscParser.clear();
	}
	
	@Override
	public void destroy() {
		clear();
		pdscParser = null;
	}
	
	@Override
	public ICpPackCollection getPacks() {
		if(packs == null) {
			bPacksLoaded = loadPacks(defaultPackDirectory);
		}
		return packs;
	}
	
	@Override
	public IRteDeviceItem getDevices() {
		getPacks(); // ensure packs are loade
		if(allDevices == null && bPacksLoaded)  {
			allDevices = RteDeviceItem.createTree(packs.getLatestPacks());
		}
		return allDevices;
	}

	@Override
	public boolean loadPacks(final String rootDirectory){
		if(rootDirectory == null || rootDirectory.isEmpty())
			return false;
		
		File root = new File(rootDirectory);
		if(root == null || !root.exists())
			return false;
		
		setDefaultPackDirectory(rootDirectory);
		
		Collection<String> fileNames = Utils.findPdscFiles(root, null, 3); 
		return loadPacks(fileNames);
	}
	
	
	@Override
	public boolean loadPacks(final Collection<String> fileNames){
		if(fileNames == null || fileNames.isEmpty())
			return true; // nothing to load => success 
		
		boolean success = true;
		for(String f : fileNames) {
	    	if(loadPack(f) == false)
	    		success = false;
	    }
		if(fRteEventProxy != null)
			fRteEventProxy.processRteEvent(new RteEvent(RteEvent.PACK_ALL_LOADED));
		return success;
	}
	
	@Override
	public boolean loadPack(String file){
		if(packs == null)
			packs = new CpPackCollection();
		
		ICpPack pack  = (ICpPack)pdscParser.parseFile(file);
		if(pack != null) {
			packs.addChild(pack);
		}
		return pack != null;
	}

	@Override
	public String getDefaultPackDirectory() {
		return defaultPackDirectory;
	}

	@Override
	public void setDefaultPackDirectory(String defaultPackDirectory) {
		this.defaultPackDirectory = defaultPackDirectory;
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
