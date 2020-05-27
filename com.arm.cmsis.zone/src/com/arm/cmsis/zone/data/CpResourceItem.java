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

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;

/**
 *  Resource item
 */
public class CpResourceItem extends CpZoneItem implements ICpResourceItem {

	public CpResourceItem(ICpItem parent) {
		super(parent);
	}

	public CpResourceItem(ICpItem parent, String tag) {
		super(parent, tag);
	}

	@Override
	protected ICpItem createChildItem(String tag) {
		switch (tag) {
		case CmsisConstants.MEMORY_TAG:
			return new CpMemoryBlock(this, tag);
		case CmsisConstants.PERIPHERAL:
			return new CpPeripheral(this, tag);
		case CmsisConstants.GROUP:
			return new CpPeripheralGroup(this, tag);
		case CmsisConstants.PERIPHERALS:
		case CmsisConstants.MEMORIES:
			return new CpResourceGroup(this, tag);
		case CmsisConstants.INTERRUPT:
			return new CpInterrupt(this, tag);
		case CmsisConstants.SETUP:
			return new CpPeripheralSetup(this, tag);
		case CmsisConstants.SLOT:
			return new CpSlot(this, tag);
		case CmsisConstants.SAU_INIT:
			return new CpSauInit(this, tag);
		case CmsisConstants.MPC:
			return new CpMpc(this, tag);
		default:
			break;
		}
		return new CpResourceItem(this, tag);
	}

	@Override
	public String getDescription() {
		return super.getDescription();
	}
}
