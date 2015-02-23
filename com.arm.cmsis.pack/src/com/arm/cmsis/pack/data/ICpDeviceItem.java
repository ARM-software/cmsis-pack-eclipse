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

package com.arm.cmsis.pack.data;

import java.util.Collection;
import java.util.Map;

import com.arm.cmsis.pack.enums.EDeviceHierarchyLevel;

/**
 * Interface that represent devise description hierarchy: family-subfamily-device-variant 
 */
public interface ICpDeviceItem extends ICpItem {
	/**
	 * Returns device parent of this item if any
	 * @return device item parent or null if parent if not device item (family parent is not a device item)
	 */
	ICpDeviceItem getDeviceItemParent();
	
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
	 * Returns effective processor count for this device item 
	 * @return map of processor name (pname) - processor property entries 
	 */
	int getProcessorCount();

	
	/**
	 * Returns list of effective properties for this device merged with properties from upper levels in device description hierarchy 
	 * @param processorName processor name for which to get properties
	 * @return list of device effective properties for supplied processor name 
	 */
	ICpItem getEffectiveProperties(String processorName); 

	
}
