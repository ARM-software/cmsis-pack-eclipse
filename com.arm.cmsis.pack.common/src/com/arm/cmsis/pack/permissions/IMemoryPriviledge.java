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
import com.arm.cmsis.pack.enums.EMemoryPrivilege;
import com.arm.cmsis.pack.generic.IAttributedItem;

/**
 * Interface defining memory access permissions, security and privilege flags 
 */
public interface IMemoryPriviledge extends IAttributedItem {
	
	// security flags 
	final static char UNPRIVILEGED_ACCESS	= 'u';
	final static char PRIVILEGED_ACCESS	    = 'p';
	final static String PRIVILEGE_FLAGS = "pu";  //$NON-NLS-1$
	

	/**
	 * Returns privilege string
	 * @return privilege permissions as string 
	 */
	default String getPrivilegeString() { return getAttribute(CmsisConstants.PRIVILEGE);}
	
	/**
	 * Sets privilege string
	 * @param privilege privilege permissions as string 
	 */
	default void setPrivilegeString(String privilege) { updateAttribute(CmsisConstants.PRIVILEGE, privilege);}
	

	/**
	 * Returns privilege access as corresponding enum  
	 * @return EMemoryPrivilege
	 */
	default EMemoryPrivilege getPrivilege() {
		return EMemoryPrivilege.fromString(getPrivilegeString());
	}

	
	/**
	 * Returns mask : a string listing allowed privilege values 
	 * @return mask String
	 */
	default String getPrivilegeMask() {
		return getPrivilege().getMask();
	}
	
	/**
	 * Sets security access  
	 * @param privilege EMemoryPrivilege
	 * 	@return true if changed 
	 */
	default boolean setPrivilige(EMemoryPrivilege privilege) {
		if(privilege != null && privilege != getPrivilege()) {
			setPrivilegeString(privilege.toString());
			return true;
		}
		return false;
	}
	
	/**
	 * Adjusts this privilege to parent one  
	 * @param parent IMemoryPriviledge to adjust to
	 * @return true if changed
	 */
	default public boolean adjustPrivilege(IMemoryPriviledge parent) {
		if(parent == null)
			return false;
		return setPrivilige(getPrivilege().adjust(parent.getPrivilege()));
	}

	
	/**
	 * Checks if memory has privileged access
	 * @return true if memory has privileged access
	 */
	default boolean isPrivilegedAccess() {
		return getPrivilege() == EMemoryPrivilege.PRIVILEGED;
	}

	/**
	 * Checks if memory has unprivileged access
	 * @return true if memory has unprivileged access
	 */
	default boolean isUnprivilegedAccess() {
		return getPrivilege() == EMemoryPrivilege.UNPRIVILEGED;
	}

}
