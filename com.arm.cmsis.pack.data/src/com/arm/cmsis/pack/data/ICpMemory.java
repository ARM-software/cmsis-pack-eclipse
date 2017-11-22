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

import com.arm.cmsis.pack.common.CmsisConstants;

/**
 *  Interface representing memory element in pdsc 
 */
public interface ICpMemory extends ICpDeviceProperty {

	final static char READ_ACCESS 		= 'r';
	final static char WRITE_ACCESS		= 'w';
	final static char EXECUTE_ACCESS	= 'x';
	final static char SECURE_ACCESS		= 's';
	final static char NON_SECURE_ACCESS	= 'n';
	final static char CALLABLE_ACCESS	= 'c';
	final static char PERIPHERAL_ACCESS	= 'p';
	
	
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
	default String getAccess() {
		return getEffectiveAttribute(CmsisConstants.ACCESS);
	}
	
	/**
	 * Checks if memory has specified access
	 * @param access : one of <code>rwxpsnc</code> characters
	 * @return true if memory provides specified access
	 */
	default boolean isAccess(char access) {
		return getAccess().indexOf(access) >= 0;
	}
	
	/**
	 * Checks if the memory region represents RAM ("rwx")
	 * @return true if RAM
	 */
	default boolean isRAM() {
		String access = getAccess();
		return access.indexOf(READ_ACCESS) >= 0 && access.indexOf(WRITE_ACCESS) >= 0 && access.indexOf(EXECUTE_ACCESS) >= 0;   
	}

	/**
	 * Checks if the memory region represents ROM ("rx")
	 * @return true if ROM
	 */
	default boolean isROM() {
		String access = getAccess();
		return access.indexOf(WRITE_ACCESS) < 0 && access.indexOf(READ_ACCESS) >= 0 && access.indexOf(EXECUTE_ACCESS) >= 0;   
	}
	
	
	/**
	 * Checks if memory has read access
	 * @return true if memory has read access
	 */
	default boolean isReadAccess() {
		return isAccess(READ_ACCESS);
	}
	
	/**
	 * Checks if memory has write access
	 * @return true if memory has write access
	 */
	default boolean isWriteAccess() {
		return isAccess(WRITE_ACCESS);
	}

	/**
	 * Checks if memory has execute access
	 * @return true if memory has execute access
	 */
	default boolean isExecuteAccess() {
		return isAccess(EXECUTE_ACCESS);
	}
	
	/**
	 * Checks if memory has secure access
	 * @return true if memory has secure access
	 */
	default boolean isSecureAccess() {
		return isAccess(SECURE_ACCESS);
	}
	
	/**
	 * Checks if memory has non-secure access
	 * @return true if memory has non-secure access
	 */
	default boolean isNonSecureAccess() {
		return isAccess(NON_SECURE_ACCESS) && !isAccess(SECURE_ACCESS);
	}
	
	/**
	 * Checks if memory has callable access
	 * @return true if memory has callable access
	 */
	default boolean isCallableAccess() {
		return isAccess(CALLABLE_ACCESS);
	}

	/**
	 * Checks if memory has peripheral access
	 * @return true if memory has peripheral access
	 */
	default boolean isPeripheralAccess() {
		return isAccess(PERIPHERAL_ACCESS);
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

}
