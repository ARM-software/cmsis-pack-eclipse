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
import com.arm.cmsis.pack.permissions.IMemoryAccess;

/**
 * Implementation of ICpPeripheralItem 
 */
public class CpPeripheralItem extends CpMemoryRegion implements ICpPeripheralItem {
	
	public CpPeripheralItem(ICpItem parent, String tag) {
		super(parent, tag);
	}
	
	/**
	 * Copy constructor
	 * @param realItem item to copy
	 */
	public CpPeripheralItem(ICpPeripheralItem realItem) { 
		super(realItem);
	}


	@Override
	public boolean isPeripheral() {
		return true;
	}
	
	@Override
	public boolean isPeripheralAccess() {
		return true;
	}


	@Override
	public String getAccessString() {
		String access = super.getAccessString();
		if (access == null) {
			access = DEFAULT_PERIPHERAL_ACCESS;
		} else if (!IMemoryAccess.isAccessSet(PERIPHERAL_ACCESS, access)) {
			access = IMemoryAccess.normalize(access+'p');
		}
		return access;
	}
}
