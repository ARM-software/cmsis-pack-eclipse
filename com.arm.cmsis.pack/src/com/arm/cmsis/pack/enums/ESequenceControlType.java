package com.arm.cmsis.pack.enums;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpDataPatch;
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
public enum ESequenceControlType {
	IF,
	WHILE;

	/**
	 * @param str value of <code>"attr"</code> attribute 
	 * @return corresponding enumeration value
	 */
	public static ESequenceControlType fromString(final String str) {
		if(CmsisConstants.WHILE.equals(str))
			return WHILE;
		else
			return IF;
	}
	
	public static String toString(ESequenceControlType type) {
		if(type == WHILE)
			return CmsisConstants.WHILE;
		else
			return CmsisConstants.IF;
	}

	@Override
	public String toString() {
		return toString(this);
	}
		
}
