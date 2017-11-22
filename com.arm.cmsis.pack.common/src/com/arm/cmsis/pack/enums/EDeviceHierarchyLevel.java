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
		case  CmsisConstants.VENDOR:
			return VENDOR;
		case  CmsisConstants.FAMILY_TAG:
			return FAMILY;
		case  CmsisConstants.SUBFAMILY_TAG:
			return SUBFAMILY;
		case  CmsisConstants.DEVICE_TAG:
			return DEVICE;
		case  CmsisConstants.VARIANT_TAG:
			return VARIANT;
		case  CmsisConstants.PROCESSOR_TAG:
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
			return CmsisConstants.VENDOR;
		case FAMILY:
			return CmsisConstants.FAMILY_TAG;
		case SUBFAMILY:
			return CmsisConstants.SUBFAMILY_TAG;
		case DEVICE:
			return CmsisConstants.DEVICE_TAG;
		case VARIANT:
			return CmsisConstants.VARIANT_TAG;
		case PROCESSOR:
			return CmsisConstants.PROCESSOR_TAG;
		case NONE:
		case ROOT:
		default:
			break;
		}
		return CmsisConstants.EMPTY_STRING;
	}

	public static String toString(int nLevel) {
		return toString(valueOf(nLevel)); 
	}


	
}
