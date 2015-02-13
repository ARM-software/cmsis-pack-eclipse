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

import com.arm.cmsis.pack.generic.IAttributes;

/**
 * Enumeration to describe device hierarchy from vendor to processor 
 */
public enum EDeviceHierarchyLevel {
	NONE,		// used in RTE context
	ROOT,		// used in RTE context
	VENDOR,		// used in RTE context
	FAMILY,
	SUBFAMILY,
	DEVICE,
	VARIANT,
	PROCESSOR;	// used in RTE context
	
	private static EDeviceHierarchyLevel[] cachedValues = null;
	
	public static EDeviceHierarchyLevel fromString(final String str) {
		if(str == null)
			return NONE;
		switch(str) {
		case  "vendor":
			return VENDOR;
		case  "family":
			return FAMILY;
		case  "subFamily":
			return SUBFAMILY;
		case  "device":
			return DEVICE;
		case  "variant":
			return VARIANT;
		case  "processor":
			return PROCESSOR;
		default:
			return NONE;
		}	
	}
	
	public static EDeviceHierarchyLevel valueOf(int value){
		if(cachedValues == null)
			cachedValues = values();
		if(value < 0 || value >= cachedValues.length)
			return NONE;
		return 	cachedValues[value];
	}

	public static String toString(EDeviceHierarchyLevel level) {
		switch( level) {
		case VENDOR:
			return "vendor";
		case FAMILY:
			return "family";
		case SUBFAMILY:
			return "subFamily";
		case DEVICE:
			return "device";
		case VARIANT:
			return "variant";
		case PROCESSOR:
			return "processor";
		case NONE:
		case ROOT:
		default:
			break;
		}
		return IAttributes.EMPTY_STRING;
	}

	public static String toString(int nLevel) {
		return toString(valueOf(nLevel)); 
	}


	
}
