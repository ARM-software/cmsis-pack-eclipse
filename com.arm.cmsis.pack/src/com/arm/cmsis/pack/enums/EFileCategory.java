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

import com.arm.cmsis.pack.data.ICpFile;

/**
 * 	Enumeration value corresponding <code>"category"</code> attribute in pdsc file
 *  That represents 
 *  @see ICpFile
 */
public enum EFileCategory {

	DOC, 
	HEADER, 
	INCLUDE, 
	LIBRARY, 
	OBJECT, 
	SOURCE, 
	SOURCE_ASM, 
	SOURCE_C, 
	SOURCE_CPP, 
	LINKER_SCRIPT, 
	UTILITY, 
	IMAGE, 
	SVD,
	SRC, // library source paths
	OTHER;

	/**
	 * @param str value of <code>"category"</code> attribute 
	 * @return corresponding enumeration value
	 */
	public static EFileCategory fromString(final String str) {
		if(str == null)
			return OTHER;
		switch(str) {
		case  "doc": //$NON-NLS-1$
			return DOC;
		case  "header": //$NON-NLS-1$
			return HEADER;
		case  "include": //$NON-NLS-1$
			return INCLUDE;
		case  "library": //$NON-NLS-1$
			return LIBRARY;
		case  "object": //$NON-NLS-1$
			return OBJECT;
		case  "source": //$NON-NLS-1$
			return SOURCE;
		case  "sourceAsm": //$NON-NLS-1$
			return SOURCE_ASM;
		case  "sourceC": //$NON-NLS-1$
			return SOURCE_C;
		case  "sourceCpp": //$NON-NLS-1$
			return SOURCE_CPP;
		case  "linkerScript": //$NON-NLS-1$
			return LINKER_SCRIPT;
		case  "utility": //$NON-NLS-1$
			return UTILITY;
		case  "image": //$NON-NLS-1$
			return IMAGE;
		case  "svd": //$NON-NLS-1$
			return SVD;
		case  "src": //$NON-NLS-1$
			return SRC;
		default:
			return OTHER;
		}
	}
	
	/**
	 * Checks if file category is header 
	 * @return true if header 
	 */
	public boolean isHeader() {
		return this == HEADER; 
	}

	/**
	 * Checks if file category is source 
	 * @return true if source 
	 */
	public boolean isSource() {
		switch(this) {
		case SOURCE:
		case SOURCE_ASM:
		case SOURCE_C:
		case SOURCE_CPP:
			return true;
		default:
			break;
			
		}
		return false; 
	}

}
