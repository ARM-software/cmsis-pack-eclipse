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

import com.arm.cmsis.pack.CpStrings;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.enums.EEvaluationResult;

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
	
	/**
	 * Returns short evaluation result description
	 * @param result EEvaluationResult to get description for 
	 * @return short description
	 */
	static public String getDescription(EEvaluationResult result) {
		switch(result) {
		case CONFLICT:
			return CpStrings.EvalResult_Confilct;
		case INCOMPATIBLE:
			return CpStrings.EvalResult_Incompatible;
		case INCOMPATIBLE_API:
			return CpStrings.EvalResult_IncompatibleApi;
		case INCOMPATIBLE_BUNDLE:
			return CpStrings.EvalResult_IncompatibleBundle;
		case INCOMPATIBLE_VARIANT:
			return CpStrings.EvalResult_IncompatibleVariant;
		case INCOMPATIBLE_VENDOR:
			return CpStrings.EvalResult_IncompatibleVendor;
		case INCOMPATIBLE_VERSION:
			return CpStrings.EvalResult_IncompatibleVersion;
		case MISSING:
			return CpStrings.EvalResult_MissingComponent;
		case MISSING_API:
			return CpStrings.EvalResult_MissingApi;
		case MISSING_BUNDLE:
			return CpStrings.EvalResult_MissingBundle;
		case MISSING_VARIANT:
			return CpStrings.EvalResult_MissingVariant;
		case MISSING_VENDOR:
			return CpStrings.EvalResult_MissingVendor;
		case MISSING_VERSION:
			return CpStrings.EvalResult_MissingVersion;
		case SELECTABLE:
			return CpStrings.EvalResult_SelectComponent;
		case UNAVAILABLE:
			return CpStrings.EvalResult_UnavailableComponent;
		case UNAVAILABLE_PACK:
			return CpStrings.EvalResult_UnavaliablePack;

		case UNDEFINED:
		case ERROR:
		case FAILED:
		case FULFILLED:
		case IGNORED:
		case INACTIVE:
		default:
			break;
		}
		return null;
	}


}
