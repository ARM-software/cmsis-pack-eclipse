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

package com.arm.cmsis.pack.enums;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.utils.DeviceVendor;
import com.arm.cmsis.pack.utils.VersionComparator;
import com.arm.cmsis.pack.utils.WildCards;

/**
 * Enumeration of component attributes Cclass, Cbundle, Cgroup, Csub, Cvariant, Cvendor, Cversion, CapiVersion and condition: 
 */
public enum EComponentAttribute{
	
	CCLASS 		(CmsisConstants.CCLASS),
	CBUNDLE		(CmsisConstants.CBUNDLE),
	CGROUP		(CmsisConstants.CGROUP),
	CSUB		(CmsisConstants.CSUB),
	CVARIANT	(CmsisConstants.CVARIANT),
	CVENDOR		(CmsisConstants.CVENDOR),
	CVERSION	(CmsisConstants.CVERSION),
	CAPIVERSION	(CmsisConstants.CAPIVERSION),
	CCONDITION	(CmsisConstants.CONDITION),
	CNONE		(CmsisConstants.EMPTY_STRING);
	
	private String fName = CmsisConstants.EMPTY_STRING;
	private EComponentAttribute(final String attributeName) {
		fName = attributeName;
	}
	
	@Override
	public String toString() {
		return fName;
	}

	/**
	 * Converts enum value to string 
	 * @param value enum value to convert
	 * @return string representation of the enum value
	 */
	public static String toString(EComponentAttribute value) {
		return value.toString();
	}
	
	private static EComponentAttribute[] cachedValues = null;

	/**
	 * Converts integer value to corresponding enum value
	 * @param value integer value to convert
	 * @return enum value
	 */
	public static EComponentAttribute valueOf(int value){
		if(cachedValues == null)
			cachedValues = values();
		if(value < 0 || value >= cachedValues.length)
			return CNONE;
		return 	cachedValues[value];
	}
	

	public boolean match(final String pattern, final String attributeValue) {
		if(pattern == null)
			return true;
		if(attributeValue == null)
			return true;

		switch(this) {
		case CVERSION:
		case CAPIVERSION:
			return VersionComparator.matchVersionRange(attributeValue, pattern);
		case CVENDOR:
			return DeviceVendor.match(pattern,  attributeValue);
		case CNONE:
			return true;
		default:
			break;
		}
		return WildCards.match(pattern, attributeValue);
	}
	
}
