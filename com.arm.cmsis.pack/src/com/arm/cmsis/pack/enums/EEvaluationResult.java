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

import com.arm.cmsis.pack.CpStrings;

/**
 * Enumeration for condition evaluation result and dependency resolve result:
 * <dl>
 * 	<dt>UNDEFINED</dt>            <dd> not evaluated yet
 *  <dt>ERROR</dt>                <dd> error evaluating condition ( recursion detected or condition is missing)
 *  <dt>FAILED</dt>               <dd> device or toolchain not match; selected component is denied 
 *  <dt>MISSING</dt>              <dd> no component is installed
 *  <dt>MISSING_BUNDLE</dt>  	  <dd> no matching bundle is installed 
 *  <dt>MISSING_VARIANT</dt> 	  <dd> no component of specified variant is installed 
 *  <dt>MISSING_VENDOR</dt>  	  <dd> no component from specified vendor is installed 
 *	<dt>MISSING_VERSION</dt> 	  <dd> no component of specified version is installed 
 *  <dt>MISSING_API</dt>          <dd> no api of required version is installed 
 *  <dt>UNAVAILABLE</dt>          <dd> component is installed but filtered out
 *  <dt>UNAVAILABLE_PACK</dt>     <dd> component is installed but pack is not selected
 *	<dt>INCOMPATIBLE</dt>         <dd> incompatible component is selected (result of deny expression) 
 *  <dt>INCOMPATIBLE_BUNDLE</dt>  <dd> incompatible bundle is selected 
 *  <dt>INCOMPATIBLE_VARIANT</dt> <dd> incompatible variant of component is selected 
 *  <dt>INCOMPATIBLE_VENDOR</dt>  <dd> incompatible component vendor is selected 
 *	<dt>INCOMPATIBLE_VERSION</dt> <dd> incompatible version of component is selected 
 *	<dt>INCOMPATIBLE_API</dt>     <dd> incompatible API version is selected 
 *	<dt>CONFLICT</dt>             <dd> more than one exclusive component selected 
 *	<dt>INACTIVE</dt>             <dd> matching component belongs to an inactive bundle 
 *	<dt>INSTALLED</dt>            <dd> more than one matching component entity is installed, choice is ambiguous 
 *  <dt>SELECTABLE</dt>           <dd> matching components are installed but not selected
 *	<dt>FULFILLED                 <dd> required component(s) selected
 *  <dt>IGNORED              	  <dd> condition or expression is irrelevant for supplied context, ignored for further evaluation
 * </dl>
 * </p>
 * @see ICpConditionContext
 */
public enum EEvaluationResult {
	/**
	 *  not evaluated yet
	 */
	UNDEFINED,
	/**
	 * error evaluating condition ( recursion detected, condition is missing)
	 */
    ERROR,
    /**
     *  device or toolchain not match;  selected component is denied
     */
    FAILED,
    /**
     * required GPDSC file is missing of failed to load
     */
    MISSING_GPDSC,
    /**
     * no matching component is installed
     */
    MISSING,
    /**
     *  no matching bundle is installed
     */
    MISSING_BUNDLE, 
    /**
     * no component of specified variant is installed
     */
    MISSING_VARIANT,
    /**
     *  no component from specified vendor is installed
     */
    MISSING_VENDOR, 
    /**
     *  no component of specified version is installed
     */
    MISSING_VERSION,
    /**
     *  no api of required version is installed
     */
    MISSING_API,
    /**
     *  component is installed, but filtered out
     */
    UNAVAILABLE,
    /**
     * component is installed, pack is not selected
     */
    UNAVAILABLE_PACK,
    /**
     * incompatible component is selected (result of deny expression)
     */
	INCOMPATIBLE,  
	/** 
	 * incompatible bundle is selected
	 */
	INCOMPATIBLE_BUNDLE, 
	/** 
	 * incompatible variant of component is selected
	 */
    INCOMPATIBLE_VARIANT,
    /**
     * incompatible vendor of component is selected /*
     */
    INCOMPATIBLE_VENDOR, 
    /** 
     * incompatible version of component is selected
     */
	INCOMPATIBLE_VERSION, 
	/**
	 * incompatible version of API is selected
	 */
	INCOMPATIBLE_API,
	/** 
	 * more than one exclusive component selected
	 */
	CONFLICT,
	/**
	 *  matching component belongs to an inactive bundle
	 */
    INACTIVE,
    /**
     *  more than one matching component entity is installed, choice is ambiguous
     */
	INSTALLED,
    /**
     *  matching components are installed, but not selected
     */
	SELECTABLE,
    /** 
     * required component(s) selected
     */
	FULFILLED,
    /**
     *  condition or expression is irrelevant for supplied context, ignored for further evaluation
     */
    IGNORED; 
	
	
	private static EEvaluationResult[] cachedValues = null;

	/**
	 * Converts integer value to corresponding enum value
	 * @param value integer value to convert
	 * @return enum value
	 */
	public static EEvaluationResult valueOf(int value){
		if(cachedValues == null)
			cachedValues = values();
		if(value < 0 || value >= cachedValues.length)
			return UNDEFINED;
		return 	cachedValues[value];
	}
	
	/**
	 * Calculates enum value corresponding to given base and component attribute  
	 * @param base enum value to use as offset  
	 * @param componentAttribute 
	 * @return calculated enum value
	 */
	public static EEvaluationResult valueOf(EEvaluationResult base, EComponentAttribute componentAttribute ){
	
		if(base.equals(MISSING) || base.equals(INCOMPATIBLE)) {
			switch(componentAttribute){
			case CBUNDLE:
				if(base.equals(MISSING))
					return MISSING_BUNDLE;
				else if(base.equals(INCOMPATIBLE))
					return INCOMPATIBLE_BUNDLE;
				break;
			case CCONDITION:
			case CNONE:
				return UNDEFINED;
			case CVARIANT:
			case CVENDOR:
			case CVERSION:
			case CAPIVERSION:
				int n = componentAttribute.ordinal() - EComponentAttribute.CVARIANT.ordinal() + 1;
				if(base.equals(MISSING) || base.equals(INCOMPATIBLE))
					return valueOf( base.ordinal() + n);
				break;
			case CCLASS:
			case CGROUP:
			case CSUB:
			default:
				break;
			}
		}
		return base;
	}
	
	
	/**
	 * Returns if result is FULFILLED or IGNORED 
	 * @return true if result >= FULFILLED 
	 */
	public boolean isFulfilled() {
		return this == FULFILLED || this == IGNORED;     
	}
	

	
	/**
	 * Returns short evaluation result description
	 * @return short description
	 */
	public String getDescription() {
		switch(this) {
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
