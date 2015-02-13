/*******************************************************************************
 * Copyright (c) 2014 ARM Ltd and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/

package com.arm.cmsis.pack.rte;

import java.util.Collection;

import com.arm.cmsis.pack.base.IEvaluationResult;
import com.arm.cmsis.pack.data.ICpDeviceItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPackFilter;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.events.IRteEventProxy;
import com.arm.cmsis.pack.info.ICpConfigurationInfo;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.rte.components.IRteComponent;
import com.arm.cmsis.pack.rte.components.IRteComponentItem;

/**
 * Interface responsible for Run-Time-Configuration of a project for selected device and toolchain.
 * It can be considered as controller that connects CMSIS-Pack items with  project configuration 
 *    
 */
public interface IRteConfiguration extends IEvaluationResult{

	
	/**
	 * Sets IRteEventProxy to be used by this configuration to fire notifications 
	 * @param rteEventProxy IRteEventProxy object 
	 */
	void setRteEventProxy(IRteEventProxy rteEventProxy);
	
	
	/**
	 * Returns IRteEventProxy object set by setRteEventProxy()
	 * @return IRteEventProxy object or null if none has been set
	 * @see #setRteEventProxy(IRteEventProxy)
	 */
	IRteEventProxy getRteEventProxy();

	
	/**
	 *  Clears the configuration
	 */
	public void clear();

	/**
	 * Returns pack filter associated with configuration  
	 * @return ICpPackFilter 
	 */
	public ICpPackFilter getPackFilter();


	/**
	 * Returns actual device item used by the configuration 
	 * @return actual device item used by the configuration
	 */
	public ICpDeviceItem getDevice();

	/**
	 * Returns device info used by in this configuration
	 * @return device info IAttributes used by this configuration
	 */
	public ICpDeviceInfo getDeviceInfo();

	/**
	 * Sets device info to be used by the configuration  
	 * @param deviceInfo device info to set
	 */
	public void setDeviceInfo(ICpDeviceInfo deviceInfo);

	/**
	 * Returns toolchain information as generic IcpItem with "Tcompiler" and "Toutput" attributes
	 * @return ICpItem describing toolchain info 
	 */
	ICpItem getToolchainInfo();

	/**
	 * Sets toolchain info to be used by the configuration  
	 * @param toolchainInfo toolchain info to set
	 */
	public void setToolchainInfo(ICpItem toolchainInfo);

	/**
	 * Sets all filter attributes: device and toolchain
	 * @param deviceInfo item defining device attributes
	 * @param toolchainInfo item defining toolchain attributes
	 */
	public void setFilterAttributes(ICpDeviceInfo deviceInfo, ICpItem toolchainInfo);
	
	
	/**
	 * Returns serializable configuration object 
	 * @return configuration info
	 */
	public ICpConfigurationInfo getConfigurationInfo();
		
	/**
	 * Sets configuration info to the configuration
	 * @param info ICpConfigurationInfo to set
	 */
	public void setConfigurationInfo(ICpConfigurationInfo info);
		
	/**
	 *  Applies configuration changes 
	 */
	public void apply();
	

	/**
	 * Returns filtered component tree  
	 * @return IRteComponentItem representing component tree root 
	 */
	public IRteComponentItem getComponents();

	/**
	 * Evaluates dependencies of selected components
	 * @return dependency evaluation result 
	 */
	EEvaluationResult evaluateDependencies();

	/**
	 * Returns dependency evaluation result for given item (class, group or component) 
	 * @param item IRteComponentItem for which to get result 
	 * @return condition result or IGNORED if item has no result
	 */
	EEvaluationResult getEvaluationResult(IRteComponentItem item); 

	
	/**
	 * Tries to resolve component dependencies
	 * @return evaluation result after dependency resolving 
	 */
	EEvaluationResult resolveDependencies();
	
	 /**
	 * Returns collection of selected components
	 * @return collection of selected components
	 */
	Collection<IRteComponent> getSelectedComponents();

	 /**
	 * Returns collection of used components
	 * @return collection of used components
	 */
	Collection<IRteComponent> getUsedComponents();
	
	/**
	 * Sets, resets or changes component selection.
	 * <br>
	 * If selection state has changed re-evaluates dependencies and emits notification  
	 * @param component to set, reset or change selection selection 
	 * @param nInstances number of instances to select, 0 to reset selection
	 */
	void selectComponent(IRteComponent component, int nInstances);
	
	/**
	 * Sets active child (bundle, variant, vendor or version ) for given parent item.
	 * <br> 
	 * If selection state has changed re-evaluates dependencies and emits notification  
	 * @param item for which to set active child.
	 * @param childName new active child name to set
	 * @see IRteComponentItem#setActiveChild(String)
	 */
	void selectActiveChild(IRteComponentItem item, final String childName);
	
	/**
	 * Sets active variant or bundle for given parent item.
	 * <br> 
	 * If selection state has changed re-evaluates dependencies and emits notification  
	 * @param item for which to set active variant.
	 * @param variant new active variant name to set
	 * @see IRteComponentItem#setActiveVariant(String)
	 */
	void selectActiveVariant(IRteComponentItem item, final String variant);
	
	/**
	 * Sets active vendor for given parent item.
	 * <br> 
	 * If selection state has changed re-evaluates dependencies and emits notification  
	 * @param item for which to set active variant.
	 * @param variant new active vendor name to set
	 * @see IRteComponentItem#setActiveVendor(String)
	 */
	void selectActiveVendor(IRteComponentItem item, final String vendor);

	/**
	 * Sets active version for given parent item.
	 * <br> 
	 * If selection state has changed re-evaluates dependencies and emits notification  
	 * @param item for which to set active version.
	 * @param variant new active version name to set
	 * @see IRteComponentItem#setActiveVersion(String)
	 */
	void selectActiveVersion(IRteComponentItem item, final String version);

	
	/**
	 * Returns collection of dependency results (items and dependencies)
	 * @return collection of dependency results
	 */
	Collection<? extends IRteDependencyItem> getDependencyItems();
	
}