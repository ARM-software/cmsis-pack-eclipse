/*******************************************************************************
* Copyright (c) 2016 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.rte;

import com.arm.cmsis.pack.common.CmsisConstants;

/**
 *  Utility class to define constants comment to RTE framework
 */
public class RteConstants {

	public static final int NONE = 0; 
	// Component RTE flags describing how to resolve components 
	// when adding ICpComponentInfo objects to the hierarchy  
	public static final int COMPONENT_IGNORE_NONE = 0;
	public static final int COMPONENT_IGNORE_VERSION = 1;
	public static final int COMPONENT_IGNORE_VENDOR  = 2;
	public static final int COMPONENT_IGNORE_VARIANT = 4;
	public static final int COMPONENT_IGNORE_BUNDLE  = 8;
	public static final int COMPONENT_IGNORE_VVV  = COMPONENT_IGNORE_VERSION | COMPONENT_IGNORE_VENDOR | COMPONENT_IGNORE_VARIANT;
	public static final int COMPONENT_IGNORE_ALL  = COMPONENT_IGNORE_VVV | COMPONENT_IGNORE_BUNDLE;

	/**
	 * Gets flag corresponding a component attribute  
	 * @param attribute attribute to get flag for 
	 * @return flag value
	 */
	static public int flagForAttribute(String attribute) {
		switch (attribute) {
		case CmsisConstants.CVARIANT:
			return COMPONENT_IGNORE_VARIANT;
		case CmsisConstants.CVENDOR:
			return COMPONENT_IGNORE_VENDOR;
		case CmsisConstants.CVERSION:
			return COMPONENT_IGNORE_VERSION;
		case CmsisConstants.CBUNDLE:
			return COMPONENT_IGNORE_BUNDLE;
		}
		return NONE;
	}

}
