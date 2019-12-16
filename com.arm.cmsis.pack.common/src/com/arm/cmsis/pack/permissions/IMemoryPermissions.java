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
import com.arm.cmsis.pack.enums.EMemorySecurity;

/**
 * Interface combining  memory access, security and privilege flags 
 */
public interface IMemoryPermissions extends IMemoryAccess, IMemorySecurity, IMemoryPriviledge {

	/**
	 * Copies access from the supplied object
	 * @param other IMemoryPermissions to copy from
	 * @return this 
	 */
	
	default IMemoryPermissions setPermissions(IMemoryPermissions other) {
		if(other != null) {
			updateAttribute(CmsisConstants.ACCESS, other.getAccessString());
			updateAttribute(CmsisConstants.SECURITY, other.getSecurityString());
			updateAttribute(CmsisConstants.PRIVILEGE, other.getPrivilegeString());
		}
		return this;
	}

	
	/**
	 * Copies access from the supplied object and updates it case if not equal 
	 * @param other IMemoryPermissions to copy from
	 * @return this 
	 */
	default boolean updatePermissions(IMemoryPermissions other) {
		if(other == null || arePermissionsEqual(other))
			return false;
		IMemoryPermissions parentPermissions = getParentPermissions(); // ensures cache of parent permissions
		setPermissions(other);
		if(parentPermissions != null)
			adjustPermissions(parentPermissions);
		return true;
	}

	/**
	 * Adjust this permissions to the parent's ones
	 * @param parent parent IMemoryPermissions
	 * @return this adjusted permissions   
	 */
	default IMemoryPermissions adjustPermissions(IMemoryPermissions other) {
		adjustSecurity(other);
		adjustPrivilege(other);
		mergeAccess(other);
		return this;
	}

	
	/**
	 * Returns parent memory permissions if any
	 * @return parent IMemoryPermissions or null
	 */
	default IMemoryPermissions getParentPermissions() {
		return null; // default implementation has no parent permissions
	}
	
	
	/**
	 * Creates a simple memory permission object from supplied string. 
	 * @param access permissions string (contains only "prwx"), no security, no privilege
	 * @return IMemoryAccess object
	 */
	static IMemoryPermissions fromString(String access) {
		return new MemoryPermissions(access);
	}
	
	
	/**
	 * Returns combined permissions string in the format access[,security[,privilege]], e.g rwx,s,u
	 * <p> Note: the string does not contain peripheral permission 'p' to avoid confusion with peivilege  
	 * @return combined permission String
	 */
	default String getPermissionsString() {
		String permissions = CmsisConstants.EMPTY_STRING;
		if(isReadAccess())
			permissions += READ_ACCESS; 
		if(isWriteAccess())
			permissions += WRITE_ACCESS; 
		if(isExecuteAccess())
			permissions += EXECUTE_ACCESS; 
		
		String security = getSecurityString();
		if(security == null )
			security = CmsisConstants.EMPTY_STRING;
		String privilege = getPrivilegeString();				
		if(privilege == null )
			privilege = CmsisConstants.EMPTY_STRING;
		
		if(!security.isEmpty() || !privilege.isEmpty()) {
			permissions += ',' + security;
		}
		if(!privilege.isEmpty()) {
			permissions += ',' + privilege;
		}
		return permissions;	
	}
	
	/**
	 * Checks if memory permissions equals to the supplied object 
	 * @param other an IMemoryPermissions object to compare.
	 * @return true if all permissions are equal
	 */
	default boolean arePermissionsEqual(IMemoryPermissions other) {
		if(other == this)
			return true;
		if(other == null)
			return false;
		return 	isAccessEqual(other) && 
				getSecurity() == other.getSecurity() &&
				getPrivilege() == other.getPrivilege();
	}
	
	
	@Override
	default String getSecurityMask() {
		EMemorySecurity security = getSecurity();
		return security.getMask(this);
	}
}
