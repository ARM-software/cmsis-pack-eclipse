/*******************************************************************************
* Copyright (c) 2017 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.build;

/**
 * A simple implementation of IMemoryAccess interface 
 * 
 * <p>An access permission specification holds information about permissions
 * granted to access a certain memory area.</p> 
 *  
 */
public class MemoryAccess implements IMemoryAccess {
	
	protected String fAccess = null;
	
	/**
	 * Creates an access specification with default permissions, 
	 * i.e. read, write, execute and non secure.
	 */
	public MemoryAccess() {
		fAccess = DEFAULT_ACCESS;
	}
	
	/**
	 * Creates an access specification from access string.
	 * @param access The access string to initialize the spec with.
	 */
	public MemoryAccess(String access) {
		if(access != null) {
			fAccess = IMemoryAccess.normalize(access);
		} else {
			fAccess = DEFAULT_ACCESS;
		}
	}

	/**
	 * Returns the string representation of this access specification.
	 * @return Normalized access string representing the permissions.
	 */
	public String toString() {
		return getAccessString();
	}

	@Override
	public String getAccessString() {
		return fAccess;
	}
}
