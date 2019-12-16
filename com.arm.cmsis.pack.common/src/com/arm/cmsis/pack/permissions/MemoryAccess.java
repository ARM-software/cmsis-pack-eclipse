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

package com.arm.cmsis.pack.permissions;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.generic.AttributedItem;

/**
 * A simple implementation of IMemoryPermissions interface 
 * 
 * <p>An access permission specification holds information about permissions
 * granted to access a certain memory area.</p> 
 *  
 */
public class MemoryAccess extends AttributedItem implements IMemoryAccess {
	
	/**
	 * Creates an access specification with default permissions, 
	 * i.e. read, write, execute and non secure.
	 */
	public MemoryAccess() {
	}
	
	/**
	 * Creates an access specification from access string.
	 * @param access The access string to initialize the spec with.
	 */
	public MemoryAccess(String access) {
		if(access != null) {
			setAttribute(CmsisConstants.ACCESS, IMemoryAccess.normalize(access));
		}
	}
	
	/**
	 * Copy constructor
	 * @param access The access to copy
	 */
	public MemoryAccess(IMemoryAccess access) {
		setAccess(access);
	}


	@Override
	public void setAttribute(String key, String value) {
		if(value == null || value.isEmpty())
			value = null;  // treat empty as null to remove them from map
		attributes().setAttribute(key, value);
	}

	/**
	 * Returns the string representation of this access specification.
	 * @return Normalized access string representing the permissions.
	 */
	@Override
	public String toString() {
		return getAccessString();
	}
}
