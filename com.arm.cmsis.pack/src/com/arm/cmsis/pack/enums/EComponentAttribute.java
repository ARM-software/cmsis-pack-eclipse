/*******************************************************************************
* Copyright (c) 2014 ARM Ltd.
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/

package com.arm.cmsis.pack.enums;

import com.arm.cmsis.pack.base.CmsisConstants;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.utils.Vendor;
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
	CNONE		(IAttributes.EMPTY_STRING);
	
	private String fName = IAttributes.EMPTY_STRING;
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
			return Vendor.match(pattern,  attributeValue);
		case CNONE:
			return true;
		default:
			break;
		}
		return WildCards.match(pattern, attributeValue);
	}
	
}
