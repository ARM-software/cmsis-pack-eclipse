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

package com.arm.cmsis.pack.build.settings;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;

/**
 * Responsible for managing toolchain adapters contributed through the extension point
 */
public class RteToolChainAdapterFactory {

	private static RteToolChainAdapterFactory theManager = null;
	
	public static final String ID = "id"; //$NON-NLS-1$
	public static final String TOOLCHAIN = "toolChain"; //$NON-NLS-1$
	public static final String TOOLCHAIN_ADAPTER = "toolChainAdapter"; //$NON-NLS-1$
	public static final String TOOLCHAIN_ADAPTER_EXTENSION_ID = Activator.PLUGIN_ID + ".ToolChainAdapter"; //$NON-NLS-1$
	public static final String TOOLCHAIN_ADAPTER_ASSOCIATIONS_EXTENSION_ID = Activator.PLUGIN_ID + ".ToolChainAdapterAssociation"; //$NON-NLS-1$
	
	
	private Map<String, RteToolChainAdapterInfo> adapterInfos= new HashMap<String, RteToolChainAdapterInfo>();  
	/**
	 *  Internal constructor 
	 */
	private RteToolChainAdapterFactory() {
		initialize();
	}
	
	/**
	 * Singleton method that returns RteToolChainAdapterManager instance
	 * @return RteToolChainAdapterManager instance
	 */
	public static RteToolChainAdapterFactory getInstance() {
		if(theManager == null)
			theManager = new RteToolChainAdapterFactory();
		return theManager; 
	}
	
	
	/**
	 *  Destroys RteToolChainAdapterManager singleton 
	 */
	public static void destroy() {
		theManager = null;
	}
	
	
	/**
	 * Returns toolchain adapter for given ID 
	 * @param adapterId unique toolchain adapter ID 
	 * @return IRteToolChainAdapter object or null if not found
	 */
	public IRteToolChainAdapter getAdapter(final String adapterId) {
		RteToolChainAdapterInfo info = getAdapterInfo(adapterId);
		if(info != null)
			return info.getToolChainAdapter();
		
		return null;
	}
	
	/**
	 * Returns toolchain adapter info for given ID 
	 * @param adapterId unique toolchain adapter ID 
	 * @return RteToolChainAdapterInfo object or null if not found
	 */
	public RteToolChainAdapterInfo getAdapterInfo(final String adapterId) {
		return adapterInfos.get(adapterId);
	}
	

	/**
	 * Returns best matching adapter for given toolchain
	 * @param toolChain toolchain to get adapter for 
	 * @return RteToolChainAdapterInfo best matching given toolchain
	 */
	public RteToolChainAdapterInfo getBestAdapterInfo(IToolChain toolChain) {
		RteToolChainAdapterInfo bestInfo = null;
		int bestMatch = 99; 
		for(RteToolChainAdapterInfo info :  adapterInfos.values()) {
			int match = info.matchToolChain(toolChain);
			if(match != -1 && match < bestMatch)
				bestInfo = info;
		}
		return bestInfo;
	}


	/**
	 * Returns complete collection of adapters matching registed by the factory 
	 * @return entire collection of adapter infos 
	 */
	public Collection<RteToolChainAdapterInfo> getAdapterInfos() {
		return adapterInfos.values();
	}

	
	/**
	 * Returns complete collection of adapters matching supplied toolchain ordered by relevance 
	 * @param toolChain toolchain to get adapters for
	 * @return collection of adapter infos matching given toolchain
	 */
	public Collection<RteToolChainAdapterInfo> getAdapterInfos(IToolChain toolChain) {
		Map<String, RteToolChainAdapterInfo> infos = new TreeMap<String, RteToolChainAdapterInfo>();
		// first search only adapters with associations
		for(RteToolChainAdapterInfo info :  adapterInfos.values()) {
			int match = info.matchToolChain(toolChain);
			if(match <= 0  || match == 99)
				continue;
			String wheightId = String.valueOf(match); 
			wheightId += info.getId();
			infos.put(wheightId, info);
		}
		return infos.values();
	}

	/**
	 *  Loads extension point data
	 */
	private void initialize() {
		IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint(TOOLCHAIN_ADAPTER_EXTENSION_ID).getExtensions();
		for(IExtension extension : extensions) {
			IConfigurationElement[] configElements = extension.getConfigurationElements();
			for(IConfigurationElement config : configElements) {
				String configName = config.getName();
				if (configName.equals(TOOLCHAIN_ADAPTER)) { 
					RteToolChainAdapterInfo info = new RteToolChainAdapterInfo(config);
					String id = info.getId();
					if(getAdapterInfo(id) != null) {
						//TODO: issue warning
					}
					adapterInfos.put(id, info);
					
					// add toolchain associations
					IConfigurationElement[] toolChainConfigs = config.getChildren(TOOLCHAIN);
					for (IConfigurationElement toolChainConfig : toolChainConfigs) {
						info.addToolChainAssociation(toolChainConfig.getAttribute(ID));
					}
				}
			}
		}
		addToolChainAssociations();
	}

	private void addToolChainAssociations() {
		IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint(TOOLCHAIN_ADAPTER_ASSOCIATIONS_EXTENSION_ID).getExtensions();
		for(IExtension extension : extensions) {
			IConfigurationElement[] configElements = extension.getConfigurationElements();
			for(IConfigurationElement config : configElements) {
				String id = config.getAttribute(ID); 
				RteToolChainAdapterInfo info = getAdapterInfo(id);
				if(info == null)
					continue;
				
				IConfigurationElement[] toolChainConfigs = config.getChildren(TOOLCHAIN);
				for (IConfigurationElement toolChainConfig : toolChainConfigs) {
					info.addToolChainAssociation(toolChainConfig.getAttribute(ID));
				}
			}
		}
	}
}
