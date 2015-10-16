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

import java.util.Map;


import com.arm.cmsis.pack.generic.IAttributes;

/**
 * Interface abstracting memory settings from ICpDevieInfo 
 */
public interface IMemorySettings {
	
	/**
	 * Returns collection of memory regions
	 * @return map of memory regions (name to IAttributes) 
	 */
	Map<String, IAttributes> getRegions();
	
	/**
	 * Sets collection of memory setting entries 
	 * @param regions collection of memory setting regions to set
	 */
	void setRegions(Map<String, IAttributes> regions);
	
	
	/**
	 * Get single memory region
	 * @param id memory region ID : usually one of <code>"IRAM1".."IRAM8"</code> or <code>"IROM1".."IROM8"</code>  
	 * @return entry corresponding to given name
	 */
	IAttributes getRegion(String id);
	
	
	/**
	 * Returns ID of memory region that shall be used for the startup by linker 
	 * @return string ID
	 */
	String getStartupRegionId();
	
}
