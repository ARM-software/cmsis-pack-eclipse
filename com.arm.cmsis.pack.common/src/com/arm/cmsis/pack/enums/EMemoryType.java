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
 * Enumeration of memory type : RAM, ROM, peripheral and unknown 
 */
public enum EMemoryType{
	
	UNKNOWN,
	RAM,
	ROM,
	PERIPHERAL;
	
	@Override
	public String toString() {
		return toString(this);
	}

	/**
	 * Converts enum value to string 
	 * @param value enum value to convert
	 * @return string representation of the enum value
	 */
	public static String toString(EMemoryType type) {
		switch( type) {
		case RAM:
			return CmsisConstants.RAM;
		case ROM:
			return CmsisConstants.ROM;
		case PERIPHERAL:
			return CmsisConstants.PERIPHERAL;
		case UNKNOWN:
		default:
			break;
		}
		return CmsisConstants.EMPTY_STRING;
	}

	public static EMemoryType fromString(final String str) {
		if(str == null || str.isEmpty())
			return UNKNOWN;
		switch(str) {
		case CmsisConstants.RAM:
			return RAM;
		case CmsisConstants.ROM:
			return ROM;
		case CmsisConstants.PERIPHERAL:
			return PERIPHERAL;
		default:
			break;
		}
		if(str.startsWith(CmsisConstants.IRAM))
			return RAM;
		if(str.startsWith(CmsisConstants.IROM))
			return ROM;
		return UNKNOWN;
	}
	
}
