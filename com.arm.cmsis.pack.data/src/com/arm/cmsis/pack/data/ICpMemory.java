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

import com.arm.cmsis.pack.build.IMemoryAccess;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.generic.IAttributes;

/**
 *  Interface representing memory element in pdsc 
 */
public interface ICpMemory extends IMemoryAccess, ICpDeviceProperty  {
	
	/**
	 * Checks if the memory shall be used for the startup by linker
	 * @return true if startup memory
	 */
	default boolean isStartup() {
		return attributes().getAttributeAsBoolean(CmsisConstants.STARTUP, false);
	}
	
	/**
	 * Returns access string corresponding following regular expression pattern: "[rwxpsnc]+"
	 * @return "access" attribute value if present or default derived from ID for deprecated elements 
	 */
	@Override
	default String getAccessString() {
		return getEffectiveAttribute(CmsisConstants.ACCESS);
	}
	
	
	/**
	 * Checks if the memory region represents RAM ("rwx")
	 * @return true if RAM
	 */
	default boolean isRAM() {
		String access = getAccessString();
		return access.indexOf(READ_ACCESS) >= 0 && access.indexOf(WRITE_ACCESS) >= 0;   
	}

	/**
	 * Checks if the memory region represents ROM ("rx")
	 * @return true if ROM
	 */
	default boolean isROM() {
		String access = getAccessString();
		return access.indexOf(WRITE_ACCESS) < 0 && access.indexOf(READ_ACCESS) >= 0;   
	}
	
	/**
	 * Returns parent ICpMemory (if parent is ICpMemory)  
	 * @return parent item as ICpMemory 
	 */
	default ICpMemory getParentMemory() {
		return getParentOfType(ICpMemory.class);
	}

	/**
	 * Returns alias name
	 * @return alias name is specified or empty string
	 */
	default String getAlias() {
		return getAttribute(CmsisConstants.ALIAS);
	}
	
	/**
	 * Checks if the memory (RAM) should not be zero-initialized  
	 * @return true if not initialized
	 */
	default boolean isNoInit() { return getAttributeAsBoolean(CmsisConstants.INIT, true) == false;}
	
	
	/**
	 * Returns stop address calculated from start and stop 
	 * @return stop address as long
	 */
	default long getStop() {
		long size = getSize();
		if(size > 0)
			size--;
		return getStart() + size; 
	}

	/**
	 * Returns stop address calculated from start and stop 
	 * @return stop address as String
	 */
	default String getStopString() {
		return IAttributes.longToHexString(getStop());
	}


	/**
	 * Returns start-stop string  
	 * @return start-stop as String
	 */
	default String getStartStopString() {
		return getStartString() + '-' + getStopString(); 
	}


}
