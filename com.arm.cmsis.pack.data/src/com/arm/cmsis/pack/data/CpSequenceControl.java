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
import com.arm.cmsis.pack.enums.ESequenceControlType;

/**
 * 
 */
public class CpSequenceControl extends CpDeviceProperty implements
		ICpSequenceControl {

	public CpSequenceControl(ICpItem parent, String tag) {
		super(parent, tag);
	}

	@Override
	public ESequenceControlType getControlType() {
		if(hasAttribute(CmsisConstants.WHILE))
			return ESequenceControlType.WHILE;
		return ESequenceControlType.IF;
	}

	@Override
	public String getExpression() {
		if(hasAttribute(CmsisConstants.WHILE))
			return getAttribute(CmsisConstants.WHILE);
		return getAttribute(CmsisConstants.IF);
	}
	
	@Override
	public long getTimeout() {
		return attributes().getAttributeAsLong(CmsisConstants.TIMEOUT, 0);
	}
	
}
