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

/* (non-Javadoc)
 * @see com.arm.cmsis.zone.data.ICpProcessorUnit
 */
public class CpProcessorUnit extends CpResourceItem implements ICpProcessorUnit {

	
	public CpProcessorUnit(ICpItem parent, String tag) {
		super(parent, tag);
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
	}

	@Override
	protected String constructName() {
		if(hasAttribute(CmsisConstants.PNAME)) {
			return getAttribute(CmsisConstants.PNAME);
		}
		if(hasAttribute(CmsisConstants.DCORE)) {
			return getAttribute(CmsisConstants.DCORE);
		}
		return getTag();
	}
	
	@Override
	 public String getDeviceName() {
		return getParentDeviceUnit().getDeviceName();
	}
}
