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
 * 
 */
public class CpPartitionGroup extends CpResourceGroup implements ICpPartitionGroup {

	
	public CpPartitionGroup(ICpItem parent, String tag) {
		super(parent, tag);
	}
	
	@Override
	protected ICpItem createChildItem(String tag) {
		switch (tag) {
		case CmsisConstants.MEMORY_TAG: 
			return new CpMemoryRegion(this, tag);
		case CmsisConstants.SLOT:
			return new CpSlot(this, tag);
		default:
			break;
		}
		return super.createChildItem(tag);
	}

}
