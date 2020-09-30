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
import com.arm.cmsis.pack.generic.Attributes;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.utils.DeviceVendor;

/**
 * Class that overrides some methods from generic Attributes
 */
public class CpAttributes extends Attributes {

	public CpAttributes() {
		super();
	}

	/**
	 * Copy constructor
	 * @param copyFrom
	 */
	public CpAttributes(IAttributes copyFrom) {
		super(copyFrom);
	}


	@Override
	public boolean matchAttribute(String key, String value, String pattern) {
		if(CmsisConstants.DVENDOR.equals(key)) {
			return DeviceVendor.match(value, pattern);
		}
		if (CmsisConstants.DCDECP.equals(key)) { // CDE support
			long lval = IAttributes.stringToLong(value, 0L);
			long lpat = IAttributes.stringToLong(pattern, 0L);
			return (lval & lpat) != 0; // alternatively considered (lval & lpat) == lpat
		}
		return super.matchAttribute(key, value, pattern);
	}

}
