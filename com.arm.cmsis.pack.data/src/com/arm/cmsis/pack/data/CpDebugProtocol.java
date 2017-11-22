/*******************************************************************************
* Copyright (c) 2015 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.data;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.enums.EDebugProtocolType;

/**
 *  
 */
public class CpDebugProtocol extends CpDeviceProperty implements
		ICpDebugProtocol {

	public CpDebugProtocol(ICpItem parent, String tag) {
		super(parent, tag);
	}

	@Override
	public EDebugProtocolType getProtocolType() {
		return EDebugProtocolType.fromString(getTag());
	}

	@Override
	public long getIdCode() {
		return attributes().getAttributeAsLong(CmsisConstants.IDCODE, 0);
	}

	@Override
	public long getTargetSel() {
		return attributes().getAttributeAsLong(CmsisConstants.TARGETSEL, 0);
	}

	@Override
	public int getTapIndex() {
		return attributes().getAttributeAsInt(CmsisConstants.TAPINDEX, 0);
	}

	@Override
	public int getIRlen() {
		return attributes().getAttributeAsInt(CmsisConstants.IRLEN, 0);
	}

	
	
	
}
