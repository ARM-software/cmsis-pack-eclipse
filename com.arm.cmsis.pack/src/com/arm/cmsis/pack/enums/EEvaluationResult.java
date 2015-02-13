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
	

	
	public String getDescription() {
		switch(this) {
		case CONFLICT:
			return "Component conflicts with other selected components";
		case INCOMPATIBLE:
			return "Component is incompatible with other selected components";
		case INCOMPATIBLE_API:
			return "Incompatible API version is selected";
		case INCOMPATIBLE_BUNDLE:
			return "Incompatible bundle is selected";
		case INCOMPATIBLE_VARIANT:
			return "Incompatible variant is selected";
		case INCOMPATIBLE_VENDOR:
			return "Incompatible vendor is selected";
		case INCOMPATIBLE_VERSION:
			return "Incompatible version is selected";
		case MISSING:
			return "Required commponent is missing";
		case MISSING_API:
			return "Required API is missing";
		case MISSING_BUNDLE:
			return "Required bundle is missing";
		case MISSING_VARIANT:
			return "Required variant is missing";
		case MISSING_VENDOR:
			return "Required vendor is missing";
		case MISSING_VERSION:
			return "Required vendor is missing";
		case SELECTABLE:
			return "Select required component";
		case UNAVAILABLE:
			return "Required component is not available for current device or toolchain";
		case UNAVAILABLE_PACK:
			return "Required pack is not selected";

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
