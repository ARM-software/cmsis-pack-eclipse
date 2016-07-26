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

package com.arm.cmsis.pack.data;

import java.util.Collection;
import java.util.Map;

import com.arm.cmsis.pack.enums.EDeviceHierarchyLevel;

/**
 * Interface that represent device description hierarchy: family-subfamily-device-variant 
 */
public interface ICpDeviceItem extends ICpItem {
	/**
	 * Returns device parent of this item if any
	 * @return device item parent or null if parent if not device item (family parent is not a device item)
	 */
	ICpDeviceItem getDeviceItemParent();
	
	/**
	 * Check is this item has device item children
	 * @return true if the item has device item children
	 */
	boolean hasDeviceItems();
	
	/** Returns list of child device items: sub-families, devices, variants
	 * @return list of child device items
	 */
	Collection<ICpDeviceItem> getDeviceItems();	
	
	/**
	 * Returns hierarchy level of this item: family, sub-family, device or variant 
	 * @return item hierarchy level
	 */
	EDeviceHierarchyLevel getLevel();
	
	/**
	 * Returns effective processor properties for this device item (can be defined at higher level) 
	 * @return map of processor name  - processor property entries 
	 */
	Map<String, ICpItem> getProcessors();

	/**
	 * Returns effective processor property for given processor 
	 * @param processorName processor name for which to get property
	 * @return processor property as ICpItem 
	 */
	ICpItem getProcessor(String processorName);
	
	
	/**
	 * Returns effective processor count for this device item 
	 * @return map of processor name (pname) - processor property entries 
	 */
	int getProcessorCount();

	/**
	 * Returns item containing list of effective properties for this device merged with properties from upper levels in device hierarchy 
	 * @param processorName processor name for which to get properties
	 * @return ICpItem containing list of device effective properties for supplied processor name 
	 */
	ICpItem getEffectiveProperties(String processorName); 

	/**
	 * Returns device debug configuration for given processor 
	 * @param processorName processor name for which to get debug configuration
	 * @return ICpDebugConfiguration for supplied processor name 
	 */
	ICpDebugConfiguration getDebugConfiguration(String processorName);

	/**
	 * @param deviceName
	 * @param eDeviceHierarchyLevel 
	 * @return the {@link ICpDeviceItem}, or null if no such device exists
	 */
	ICpDeviceItem findDeviceByName(String deviceName, int eDeviceHierarchyLevel);
	
}
