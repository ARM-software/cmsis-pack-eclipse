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
	boolean isStartup();
	
	/**
	 * Returns access string corresponding following regular expression pattern: "[rwxpsnc]+"
	 * @return "access" attribute value if present or default derived from ID for deprecated elements 
	 */
	String getAccess();

	/**
	 * Checks if the memory region represents RAM ("rwx")
	 * @return true if RAM
	 */
	boolean isRAM();

	/**
	 * Checks if the memory region represents ROM ("rx")
	 * @return true if ROM
	 */
	boolean isROM();

	
	/**
	 * Checks if memory has specified access
	 * @param access : one of <code>rwxpsnc</code> characters
	 * @return true if memory provides specified access
	 */
	boolean isAccess(char access);
	
	/**
	 * Checks if memory has read access
	 * @return true if memory has read access
	 */
	boolean isReadAccess();
	
	/**
	 * Checks if memory has write access
	 * @return true if memory has write access
	 */
	boolean isWriteAccess();

	/**
	 * Checks if memory has execute access
	 * @return true if memory has execute access
	 */
	boolean isExecuteAccess();

	
	/**
	 * Checks if memory has secure access
	 * @return true if memory has secure access
	 */
	boolean isSecureAccess();
	
	/**
	 * Checks if memory has non-secure access
	 * @return true if memory has non-secure access
	 */
	boolean isNonSecureAccess();
	
	/**
	 * Checks if memory has callable access
	 * @return true if memory has callable access
	 */
	boolean isCallableAccess();

	/**
	 * Checks if memory has peripheral access
	 * @return true if memory has peripheral access
	 */
	boolean isPeripheralAccess(); 

	
}
