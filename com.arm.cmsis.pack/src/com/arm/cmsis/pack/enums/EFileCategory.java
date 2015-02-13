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
	OTHER;

	/**
	 * @param str value of <code>"category"</code> attribute 
	 * @return corresponding enumeration value
	 */
	public static EFileCategory fromString(final String str) {
		if(str == null)
			return OTHER;
		switch(str) {
		case  "doc":
			return DOC;
		case  "header":
			return HEADER;
		case  "include":
			return INCLUDE;
		case  "library":
			return LIBRARY;
		case  "object":
			return OBJECT;
		case  "source":
			return SOURCE;
		case  "sourceAsm":
			return SOURCE_ASM;
		case  "sourceC":
			return SOURCE_C;
		case  "sourceCpp":
			return SOURCE_CPP;
		case  "linkerScript":
			return LINKER_SCRIPT;
		case  "utility":
			return UTILITY;
		case  "image":
			return IMAGE;
		default:
			return OTHER;
		}
	}

}
