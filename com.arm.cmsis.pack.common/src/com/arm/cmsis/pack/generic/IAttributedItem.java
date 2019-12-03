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

package com.arm.cmsis.pack.generic;

import java.util.Map;

import com.arm.cmsis.pack.common.CmsisConstants;

/**
 * Interface describing an item that has attributes
 */
public interface IAttributedItem {
	
	/**
	 * Returns the item's internal collection of attributes  
	 * @return IAttributes element that contains item attributes
	 */
	IAttributes attributes(); 

	/**
	 * Checks if an attribute exists in the internal collection
	 * @param key attribute key to search for
	 * @return true if attribute exists
	 */
	default boolean hasAttribute(final String key) {
		return attributes().hasAttribute(key);
	}

	/**
	 * Retrieves an attribute value from internal collection
	 * @param key attribute key to search for
	 * @return attribute value or empty string if attribute not found
	 */
	default String getAttribute(final String key) {
		return attributes().getAttribute(key, CmsisConstants.EMPTY_STRING);
	}

	/**
	 * Returns integer representation of an attribute value  
	 * @param key attribute key
	 * @param nDefault value to return if attribute is not found
	 * @return attribute value as integer or nDefault  
	 */
	default int getAttributeAsInt(String key, int nDefault) {
		return attributes().getAttributeAsInt(key, nDefault);
	}

	/**
	 * Returns boolean representation of an attribute value  
	 * @param key attribute key
	 * @param bDefault value to return if attribute is not found
	 * @return attribute value as boolean or bDefault  
	 */
	default boolean getAttributeAsBoolean(String key, boolean bDefault) {
		return attributes().getAttributeAsBoolean(key, bDefault);
	}

	/**
	 * Adds attribute key-value pair to this element, overwriting existing one  
	 * @param key attribute key
	 * @param value attribute value
	 */
	default void setAttribute(String key, String value) {
		attributes().setAttribute(key, value);	
	}
	
	/**
	 * Adds attribute key-value pair to this element, overwriting existing one  
	 * @param key attribute key
	 * @param value boolean attribute value
	 */
	default void setAttribute(String key, boolean value) {
		setAttribute(key, value ? "1" : "0"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Adds attribute key-value pair to this element, overwriting existing one  
	 * @param key attribute key
	 * @param value integer attribute value
	 */
	default void setAttribute(String key, int value) {
		setAttribute(key, Integer.toString(value));
	}

	/**
	 * Updates existing attribute key-value pair or adds one if attribute does not exist  
	 * @param key attribute key
	 * @param value attribute value
	 * @return true if value has changes or attribute added
	 */
	default boolean updateAttribute(String key, String value) {
		return attributes().updateAttribute(key, value);
	}

	/**
	 * Updates existing attributes with supplied ones, adds attributes if do not exist  
	 * @param attributes IAttributes with new attribute values 
	 * @return true if a single value has changes or an attribute added
	 */
	default boolean updateAttributes(IAttributes attributes) {
		return attributes().updateAttributes(attributes);
	}

	/**
	 * Updates existing attributes with supplied ones, adds attributes if do not exist  
	 * @param attributes Map<String, String> with new key-value pairs 
	 * @return true if a single value has changes or an attribute added
	 */
	default boolean updateAttributes(Map<String, String> attributes) {
		return attributes().updateAttributes(attributes);
	}
	
	/**
	 * Removes attribute from the internal collection
	 * @param key attribute key
	 */
	default void removeAttribute(String key) {
		attributes().removeAttribute(key);
	}
	
	/**
	 * Sets "removed" boolean attribute to the item 
	 * @param removed true to set the attribute, false to reset it 
	 */
	default void setRemoved(boolean removed) {
		if(removed)
			setAttribute(CmsisConstants.REMOVED, true);
		else 
			removeAttribute(CmsisConstants.REMOVED);
	}
	
	/**
	 * Sets "valid" boolean attribute to the item 
	 * @param removed true to set the attribute, false to reset it 
	 */
	default void setValid(boolean valid) {
		if(!valid)
			setAttribute(CmsisConstants.VALID, false);
		else 
			removeAttribute(CmsisConstants.VALID);
	}
	
	/**
	 * Checks if item is valid 
	 * @return true if valid
	 */
	default public boolean isValid() {
		return getAttributeAsBoolean(CmsisConstants.VALID, true);
	}


	/**
	 * Checks if item is a custom one  
	 * @return true if custom
	 */
	default public boolean isCustom() {
		return getAttributeAsBoolean(CmsisConstants.CUSTOM, true);
	}

}

