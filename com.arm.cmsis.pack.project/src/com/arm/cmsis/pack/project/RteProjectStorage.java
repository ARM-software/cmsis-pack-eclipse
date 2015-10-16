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

package com.arm.cmsis.pack.project;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.core.runtime.CoreException;

import com.arm.cmsis.pack.build.settings.IMemorySettings;
import com.arm.cmsis.pack.build.settings.IRteToolChainAdapter;
import com.arm.cmsis.pack.build.settings.MemorySettings;
import com.arm.cmsis.pack.build.settings.RteToolChainAdapterFactory;
import com.arm.cmsis.pack.build.settings.RteToolChainAdapterInfo;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpDeviceItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.generic.Attributes;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.info.ICpDeviceInfo;


/**
 * The class responsible for storing/restoring RTE-related information in ICStorageElement 
 */
public class RteProjectStorage {

	public static final String RTE_STORAGE 			 = "com.arm.cmsis.project"; //$NON-NLS-1$
	public static final String RTE_TOOLCHAIN_ADAPTER = "toolChainAdapter"; //$NON-NLS-1$
	public static final String RTE_CONFIG 		 	 = "rteConfig"; //$NON-NLS-1$

	protected String fRteConfigurationName = null;  // associated IRteConfiguration name (by default equals project name)
	protected IAttributes fDeviceAttributes = null; // device used by configuration

	protected String fToolChainAdapterId = null;    // associated IRteToolchainAdapter id
	protected String fToolChainAdapterName = null;  // associated IRteToolchainAdapter name
	protected RteToolChainAdapterInfo fToolChainAdapterInfo = null;

	protected Map<String, String> fConfigFileVersions = new HashMap<String, String>(); // config file: name to version
	

	public RteProjectStorage() {
	}


	public RteProjectStorage( String rteConfigurationName, RteToolChainAdapterInfo adapterInfo) {
		setRteConfigurationName(rteConfigurationName);
		setToolChainAdapterInfo(adapterInfo);
	}
	
	/**
	 * Return RTE configuration name associated with the build configuration
	 * @return RTE configuration name
	 */
	public String getRteConfigurationName() {
		return fRteConfigurationName;
	}

	/**
	 * Associates RTE configuration with build configuration 
	 * @param rteConfigurationName name of RTE configuration to associate
	 */
	public void setRteConfigurationName(String rteConfigurationName) {
		fRteConfigurationName = rteConfigurationName;
	}

	/**
	 * Returns toolchain adapter info associated with configuration 
	 * @return RteToolChainAdapterInfo
	 */
	public RteToolChainAdapterInfo getToolChainAdapterInfo() {
		return fToolChainAdapterInfo;
	}

	
	/**
	 * Associates toolchain adapter with the configuration  
	 * @param info RteToolChainAdapterInfo
	 */
	public void setToolChainAdapterInfo(RteToolChainAdapterInfo info) {
		fToolChainAdapterInfo = info;
		if(info != null) {
			fToolChainAdapterId = info.getId();
			fToolChainAdapterName = info.getName();
		} else {
			fToolChainAdapterId = null;
			fToolChainAdapterName = null;
		}
	}
	
	
	/**
	 * Returns human-readable name of toolchain adapter associated with configuration 
	 * @return toolchain adapter name
	 */
	public String getToolChainAdapterName() {
		return fToolChainAdapterName;
	}

	/**
	 * Returns id of toolchain adapter associated with configuration 
	 * @return toolchain adapter id
	 */
	public String getToolChainAdapterId() {
		return fToolChainAdapterId;
	}

	/**
	 * Returns toolchain adapter associated with configuration 
	 * @return IRteToolChainAdapter
	 */
	public IRteToolChainAdapter getToolChainAdapter() {
		if(fToolChainAdapterInfo != null)
			return fToolChainAdapterInfo.getToolChainAdapter();
		return null;
	}


	/**
	 * Returns attributes of selected device
	 * @return IAttributes 
	 */
	public IAttributes getDeviceAttributes() {
		return fDeviceAttributes;
	}
	
	/**
	 * Sets device information 
	 * @param deviceInfo ICpDeviceInfo object
	 */
	public void setDeviceInfo(ICpDeviceInfo deviceInfo) {
		if(deviceInfo == null){
			fDeviceAttributes = null;
		} else { 
			fDeviceAttributes = new Attributes(deviceInfo.attributes());
		}
	}
	
	/**
	 * Creates memory settings from device information
	 * @param deviceInfo ICpDeviceInfo object
	 */
	protected IMemorySettings createMemorySettings(ICpDeviceInfo deviceInfo) {

		ICpDeviceItem d = deviceInfo.getDevice();
		if(d == null) {
			return null;
		}
		String processorName = deviceInfo.getProcessorName();
		ICpItem props = d.getEffectiveProperties(processorName);
		if(props == null)
			return null;

		Map<String, IAttributes> entries = new TreeMap<String, IAttributes>(); 		
		
		Collection<? extends ICpItem> children = props.getChildren();
		for(ICpItem p : children) {
			String tag = p.getTag();
			if(!tag.equals(CmsisConstants.MEMORY_TAG))
				continue;
			String id = p.getId();
			IAttributes a = new Attributes(p.attributes());
			entries.put(id, a);
		}
		
		return new MemorySettings(entries);
	}

	/**
	 * Returns version of a config file last copied to the project
	 * @param name project-relative filename 
	 * @return file version string
	 */
	public String getConfigFileVersion(String name){
		return fConfigFileVersions.get(name);
	}

	/**
	 * Sets version of config file copied to the project 
	 * @param name project-relative filename
	 * @param version file version
	 */
	public void setConfigFileVersion(String name, String version){
		fConfigFileVersions.put(name, version);
	}

	/**
	 * Removes  config file version information
	 * @param name project-relative filename
	 */
	public void removeConfigFileVersion(String name){
		fConfigFileVersions.remove(name);
	}
	
	
	/**
	 * Loads RTE-related information from ICConfigurationDescription  
	 * @throws CoreException
	 */
	public void load(ICProjectDescription projDesc) throws CoreException {
		ICStorageElement storage = projDesc.getStorage(RTE_STORAGE, false);
		if(storage == null) {
			//project not initialized jet => ignore
			return;
		}
		fDeviceAttributes = null;
		ICStorageElement[] elements = storage.getChildren();
		for(ICStorageElement e : elements) {
			String name = e.getName();
			switch(name){
			case RTE_CONFIG:
				fRteConfigurationName = e.getAttribute(CmsisConstants.NAME); 
				break;
			case RTE_TOOLCHAIN_ADAPTER:
				fToolChainAdapterId = e.getAttribute(CmsisConstants.ID); 
				fToolChainAdapterName = e.getAttribute(CmsisConstants.NAME); 
				break;
			case CmsisConstants.DEVICE_TAG: {
				fDeviceAttributes = loadAttributes(e);
				break;
				}
			case CmsisConstants.FILES_TAG:
				loadConfigFileInfos(e);
			}
		}
		initializeToolChainAdapter();	
	}
	
	private IAttributes loadAttributes(ICStorageElement e) {
		IAttributes attributes = null;
		String[] names = e.getAttributeNames();
		if(names != null && names.length > 0) {
			attributes = new Attributes();
			for(String key : names) {
				String value = e.getAttribute(key);
				attributes.setAttribute(key, value);
			}
		}
		return attributes;
	}

	
	private void saveAttributes(ICStorageElement e, IAttributes attributes) {
		if(e == null || attributes == null)
			return;
		Map<String, String> attrMap = attributes.getAttributesAsMap();
		for(Entry<String, String> a : attrMap.entrySet()){
			e.setAttribute(a.getKey(), a.getValue());
		}
	}
	
	
	protected void loadConfigFileInfos(ICStorageElement element) {
		ICStorageElement[] elements = element.getChildren();
		fConfigFileVersions.clear();
		for(ICStorageElement e : elements) {
			if(e.getName().equals(CmsisConstants.FILE_TAG)){
				String name = e.getAttribute(CmsisConstants.NAME);
				String version = e.getAttribute(CmsisConstants.VERSION);
				fConfigFileVersions.put(name, version);
			}
		}
	}

	protected RteToolChainAdapterInfo initializeToolChainAdapter() {
		fToolChainAdapterInfo = null;
		if(fToolChainAdapterId != null) {
			RteToolChainAdapterFactory adapterFactory = RteToolChainAdapterFactory.getInstance();
			fToolChainAdapterInfo = adapterFactory.getAdapterInfo(fToolChainAdapterId);
		}
		return fToolChainAdapterInfo;
	}


	
	/**
	 * Loads RTE-related information from ICConfigurationDescription  
	 * @param configDesc ICConfigurationDescription to load RTE info from
	 * @throws CoreException
	 */
	public void save(ICProjectDescription projDesc) throws CoreException {
		ICStorageElement storage = projDesc.getStorage(RTE_STORAGE, true);
		
		storage.clear(); // clear last values
		
		if(fRteConfigurationName != null && !fRteConfigurationName.isEmpty()) {
			ICStorageElement se = storage.createChild(RTE_CONFIG);
			se.setAttribute("name", fRteConfigurationName); //$NON-NLS-1$
		}
		
		if(fToolChainAdapterId != null && !fToolChainAdapterId.isEmpty()) {
			ICStorageElement se = storage.createChild(RTE_TOOLCHAIN_ADAPTER);
			se.setAttribute("id", fToolChainAdapterId); //$NON-NLS-1$
			if(fToolChainAdapterName != null)
				se.setAttribute("name", fToolChainAdapterName); //$NON-NLS-1$
		}
		
		if(fDeviceAttributes != null && fDeviceAttributes.hasAttributes()){
			ICStorageElement deviceSe = storage.createChild(CmsisConstants.DEVICE_TAG);
			saveAttributes(deviceSe, fDeviceAttributes);
		}
		
		if(fConfigFileVersions !=  null && !fConfigFileVersions.isEmpty()) {
			ICStorageElement files = storage.createChild(CmsisConstants.FILES_TAG);
			for(Entry<String, String> f : fConfigFileVersions.entrySet()){
				ICStorageElement e = files.createChild(CmsisConstants.FILE_TAG);
				e.setAttribute(CmsisConstants.NAME, f.getKey());
				e.setAttribute(CmsisConstants.VERSION, f.getValue());
			}
		}
	}
}
