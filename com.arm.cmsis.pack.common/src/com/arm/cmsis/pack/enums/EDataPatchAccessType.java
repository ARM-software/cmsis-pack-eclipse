package com.arm.cmsis.pack.enums;

import com.arm.cmsis.pack.common.CmsisConstants;
/*******************************************************************************
 * Copyright (c) 2015 ARM Ltd and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/

/**
 * 	Enumeration value corresponding "DataPatchAccessTypeEnum" in pdsc file schema
 *  @see ICpDataPatch
 */
public enum EDataPatchAccessType {
	Mem,
	DP,
	AP;

	/**
	 * @param str value of <code>"attr"</code> attribute 
	 * @return corresponding enumeration value
	 */
	public static EDataPatchAccessType fromString(final String str) {
		if(str == null)
			return Mem;
		switch(str) {
		case  CmsisConstants.AP:
			return AP;
		case  CmsisConstants.DP:
			return DP;
		case  CmsisConstants.MEM:
		default:
			return Mem;
		}
	}
	
	public static String toString(EDataPatchAccessType type) {
		switch( type) {
		case DP:
			return CmsisConstants.DP;
		case AP:
			return CmsisConstants.AP;
		default:
			return CmsisConstants.MEM;
		}
	}

	@Override
	public String toString() {
		return toString(this);
	}
		
}
