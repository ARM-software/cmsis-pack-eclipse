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

/**
 * Enumeration describing behavior when selecting an Item:
 * <dl>
 * <dt>LATEST</dt>
 *    	<dd>use the latest available item (pack, device, component, etc.)
 * <dt>FIXED</dt> 
 *		<dd>use only version defined by getVersion() method</dd>
 * <dt>EXCLUDED</dt> 
 *		<dd>do not use the item</dd>
 * </dl>
 * </p>  
 *  
 */
public enum EVersionMatchMode {
	LATEST,
	FIXED,
	EXCLUDED;
	
	
	public static EVersionMatchMode fromString(final String str) {
		if(str != null) {
			if(str.equals("fixed"))
				return FIXED;
			else if(str.equals("excluded"))
				return EXCLUDED;
		}
		return LATEST;
	}
	
	public static String toString(EVersionMatchMode mode) {
		switch( mode) {
		case FIXED:
			return "fixed";
		case EXCLUDED:
			return "excluded";
		case LATEST:
		default:
			break;
		}
		return null;
	}
}
