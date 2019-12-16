/*******************************************************************************
* Copyright (c) 2019 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.zone.data;

import java.util.Collection;

import com.arm.cmsis.pack.common.CmsisConstants;

/**
 *  Preripheral's slot (channel or pin) item 
 */
public interface ICpSlot extends ICpResourceItem {

	/**
	 * Check if slot is secure 
	 * @return true if secure
	 */
	default boolean isSecure() {
		return getAttributeAsBoolean(CmsisConstants.secure, false);
	}
	
	/**
	 * Sets security flag   
	 * @param security boolean flag secure/non-secure
	 * @return true if security is changed
	 */
	default boolean setSecure(boolean security) {
		return updateAttribute(CmsisConstants.secure, security ? CmsisConstants.ONE : CmsisConstants.ZERO);
	}
	
	
	/**
	 * Check if slot is privileged 
	 * @return true if privileged
	 */
	default boolean isPrivileged() {
		return getAttributeAsBoolean(CmsisConstants.PRIVILEGE, false);
	}
	
	
	/**
	 * Sets privilege flag   
	 * @param privelege boolean flag privileged/unprivileged 
	 * @return true if privilege is changed
	 */
	default boolean setPrivileged(boolean privielege) {
		return updateAttribute(CmsisConstants.PRIVILEGE, privielege ? CmsisConstants.ONE : CmsisConstants.ZERO);
	}
	
	
	/**
	 * Returns collection of interrupts associated with the peripheral
	 * @return Collection<ICpInterrupt>
	 */
	default Collection<ICpInterrupt> getInterrupts() {
		return getChildrenOfType(ICpInterrupt.class);
	}

	/**
	 * Returns collection of register setup elements associated with peripheral  
	 * @return Collection<ICpPeripheralSetup>
	 */
	default Collection<ICpPeripheralSetup> getPeripheralSetups() {
		return getChildrenOfType(ICpPeripheralSetup.class);
	}

}
