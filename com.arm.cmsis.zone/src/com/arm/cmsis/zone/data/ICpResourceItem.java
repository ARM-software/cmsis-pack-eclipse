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
package com.arm.cmsis.zone.data;

import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.generic.IAttributes;

/**
 * Base interface for CMSIS-Zone resources (memory and peripherals)
 *
 */
public interface ICpResourceItem extends ICpZoneItem  {


	/**
	 * Returns memory region for given name
	 * @param name region name
	 * @return ICpMemoryRegion if found
	 */
	default ICpMemoryBlock getMemoryRegion(String name) {
		return getFirstChildOfType(name, ICpMemoryBlock.class);
	}


	/**
	 * Returns peripheral region for given name
	 * @param name peripheral name
	 * @return ICpPeripheral if found
	 */
	default ICpPeripheral getPeripheral(String name) {
		return getFirstChildOfType(name, ICpPeripheral.class);
	}

	/**
	 * Returns parent resource container
	 * @return parent ICpResourceContainer
	 */
	default ICpResourceItem getParentResource() {
		return getParentOfType(ICpResourceItem.class);
	}

	/**
	 * Returns parent resource group
	 * @return parent ICpResourceGroup
	 */
	default ICpResourceGroup getParentGroup() {
		return getParentOfType(ICpResourceGroup.class);
	}

	/**
	 * Returns parent peripheral
	 * @return parent ICpPeripheral
	 */
	default ICpPeripheral getParentPeripheral() {
		return getParentOfType(ICpPeripheral.class);
	}


	/**
	 * Returns parent peripheral item (peripheral item group)
	 * @return parent ICpPeripheralItem
	 */
	default ICpPeripheralItem getParentPeripheralItem() {
		return getParentOfType(ICpPeripheralItem.class);
	}

	/**
	 * Returns parent peripheral group if any
	 * @return ICpPeripheralGroup or null
	 */
	default ICpPeripheralGroup getParentPeripheralGroup() {
		ICpItem parent = getParent();
		if(parent instanceof ICpPeripheralGroup)
			return (ICpPeripheralGroup)parent;
		return null;
	}



	/**
	 * Returns parent slot if any
	 * @return parent ICpSlot
	 */
	default ICpSlot getParentSlot() {
		return getParentOfType(ICpSlot.class);
	}

	/**
	 * Returns parent ICpMemoryBlock
	 * @return parent ICpMemoryBlock item
	 */
	default ICpMemoryBlock getParentBlock() {
		return getParentOfType(ICpMemoryBlock.class);
	}

	/**
	 * Returns the original resource attributes
	 * @return IAttributes
	 */
	default IAttributes getOriginalAttributes() {
		return attributes(); // default returns own attributes
	}

	/**
	 * Checks if attributes modified to compare to its original values
	 * @return true if initial attributes have been modified
	 */
	default boolean areAttributesModified() {
		return !equalsAttributes(getOriginalAttributes());
	}

	/**
	 * Checks if the item has been modified
	 * @return true if modified
	 */
	default boolean isModified() {
		return areAttributesModified(); // default checks only own attributes
	}
}
