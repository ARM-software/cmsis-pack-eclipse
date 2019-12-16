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
import com.arm.cmsis.pack.generic.IAttributedItem;

/**
 * Interface defining memory access permissions, security and privilege flags 
 */
public interface IMemorySecurity extends IAttributedItem {
	
	// security flags 
	final static char SECURE_ACCESS			= 's';
	final static char NON_SECURE_ACCESS		= 'n';
	final static char CALLABLE_ACCESS		= 'c';
	final static String SECURITY_FLAGS = "snc";  //$NON-NLS-1$
	

	/**
	 * Returns security string
	 * @return security permissions as string 
	 */
	default String getSecurityString() { return getAttribute(CmsisConstants.SECURITY);}
	

	/**
	 * Sets security string
	 * @param security security permissions as string 
	 */
	default void setSecurityString(String security) { updateAttribute(CmsisConstants.SECURITY, security);}


	/**
	 * Returns security access as corresponding enum  
	 * @return EMemorySecurity
	 */
	default EMemorySecurity getSecurity() {
		return EMemorySecurity.fromString(getSecurityString());
	}
	
	/**
	 * Returns mask : a string restricting allowed security values 
	 * @return mask String
	 */
	default String getSecurityMask() {
		return getSecurity().getMask(null); // returns own mask not respecting access rights
	}

	/**
	 * Sets security access  
	 * @param security EMemorySecurity
	 * @return true if security is changed
	 */
	default boolean setSecurity(EMemorySecurity security) {
		if(security != null && security != getSecurity()) {
			setSecurityString(security.toString());
			return true;
		}
		return false;
	}

	/**
	 * Adjusts this security to parent one  
	 * @param parent IMemorySecurity to adjust to
	 * @return true if security is changed
	 */
	default public boolean adjustSecurity(IMemorySecurity parent) {
		if(parent == null)
			return false;
		return setSecurity(getSecurity().adjust(parent.getSecurity()));
	}
	
	
	/**
	 * Checks if memory access is explicitly defined via one of secure attributes
	 * @return true if memory has secure access is explicitly defined
	 */
	default boolean isSecureAccessDefined() {
		return !getSecurityString().isEmpty();
	}

	/**
	 * Checks if memory is secure or callable
	 * @return true if memory has secure or callable access
	 */
	default boolean isSecure() {
		return getSecurity().isSecure();
	}
	
	/**
	 * Checks if memory has or can have non-secure access 
	 * @return true if memory is non-secure or can be made it
	 */
	default boolean isNonSecure() {
		return getSecurity().isSecure();
	}

	
	/**
	 * Checks if memory has strong secure access is set
	 * @return true if memory has secure access
	 */
	default boolean isSecureAccess() {
		return getSecurity() == EMemorySecurity.SECURE;
	}

	/**
	 * Checks if memory has non-secure access is set
	 * @return true if memory has non-secure access
	 */
	default boolean isNonSecureAccess() {
		return getSecurity() == EMemorySecurity.NON_SECURE;
	}
	
	/**
	 * Checks if memory has non-secure callable access is set
	 * @return true if memory has non-secure callable access
	 */
	default boolean isCallableAccess() {
		return getSecurity() == EMemorySecurity.CALLABLE;
	}

}
