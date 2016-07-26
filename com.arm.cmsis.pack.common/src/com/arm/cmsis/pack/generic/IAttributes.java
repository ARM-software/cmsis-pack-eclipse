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

/**
 * Interface describing an item that has String attributes map. 
 */
public interface IAttributes {
	
	/**
	 * Checks if element has attributes
	 * @return true if element has non-empty attributes map
	 */
	boolean hasAttributes();

	/**
	 * Returns all attributes of this element as a Map<String,String>  
	 * @return the attributes as a Map
	 */
	Map<String, String> getAttributesAsMap();
	

	/**
	 * Parses supplied string into collection of attributes and sets it to this element overwriting existing collection
	 * @param attributesString string in the form <code>key1="value1", key2="vaule2", ...</code>
	 */
	void setAttributes(String attributesString);

	/**
	 * Puts collection of attributes to this element overwriting existing collection
	 * @param attributes collection to set
	 */
	void setAttributes(Map<String, String> attributes);
	
	/**
	 * Copies attributes from supplied attributes to this element overwriting existing collection
	 * @param attributes collection to set
	 */
	void setAttributes(final IAttributes attributes);
	
	/**
	 * Merges attributes from supplied attributes to this element, does not overwrite existing ones  
	 * @param attributes collection to merge
	 */
	void mergeAttributes(final IAttributes attributes);

	/**
	 * Merges attributes from supplied attributes to this element considering only attributes with given prefix  
	 * @param attributes collection to merge
	 * @param prefix attributes prefix, for instance "C"
	 */
	void mergeAttributes(final IAttributes attributes, final String prefix);
	
	
	/**
	 * Checks if element has attribute for given key
	 * @param key attribute key
	 * @return true if attribute for the given key is found, false otherwise
	 */
	boolean hasAttribute(String key);

	/**
	 * Checks if element has at least one attribute matching specified key pattern.
	 * Equivalent to hasAttribute() if argument contains no wild cards  
	 * @param keyPattern to search for
	 * @return true if matching attribute is found, otherwise false
	 */
	boolean containsAttribute(String pattern);

	/**
	 * Returns attribute value stored in object if any
	 * @param key attribute key
	 * @return value or null if attribute does not exist
	 */
	String getAttribute(String key);
	
	/**
	 * Returns attribute value stored in object if or default value if no attribute exists
	 * @param key attribute key
	 * @param defaultValue value to return if attribute is not found
	 * @return attribute value or default value if attribute does not exist
	 */
	String getAttribute(String key, String defaultValue);

	/**
	 * Adds attribute key-value pair to this element, overwriting existing one  
	 * @param key attribute key
	 * @param value attribute value
	 */
	void setAttribute(String key, String value);
	
	/**
	 * Adds attribute key-value pair to this element, overwriting existing one  
	 * @param key attribute key
	 * @param value boolean attribute value
	 */
	void setAttribute(String key, boolean value);

	/**
	 * Adds attribute key-value pair to this element, overwriting existing one  
	 * @param key attribute key
	 * @param value integer attribute value
	 */
	void setAttribute(String key, int value);

	/**
	 * Removes attribute from the map 
	 * @param key attribute key
	 */
	void removeAttribute(String key);
	
	
	/**
	 * Returns integer representation of an attribute value  
	 * @param key attribute key
	 * @param nDefault value to return if attribute is not found
	 * @return attribute value as integer or nDefault  
	 */
	int getAttributeAsInt(String key, int nDefault);

	/**
	 * Returns long integer representation of an attribute value  
	 * @param key attribute key
	 * @param nDefault value to return if attribute is not found
	 * @return attribute value as long or nDefault  
	 */
	long getAttributeAsLong(String key, long nDefault);
	
	
	/**
	 * Returns boolean representation of an attribute value  
	 * @param key attribute key
	 * @param bDefault value to return if attribute is not found
	 * @return attribute value as boolean or bDefault  
	 */
	boolean getAttributeAsBoolean(String key, boolean bDefault);
	
	/**
	 * Adds attribute key-value pair to this element if it does not exist
	 * @param key attribute key
	 * @param value attribute value
	 */
	void mergeAttribute(String key, String value);
	
	
	/**
	 * Checks if all attributes of this element exist in supplied map and their values match. 
	 * Wild card match is used  
	 * @param otherAttributes attributes to match to
	 * @return true if matches, false otherwise
	 */
	boolean matchAttributes(final IAttributes otherAttributes);

	/**
	 * Checks if all attributes with given prefix of this element exist in supplied map and their values match. 
	 * Wild card match is used  
	 * @param otherAttributes attributes to match to
	 * @param prefix attribute key prefix 
	 * @return true if matches, false otherwise
	 */
	boolean matchAttributes(final IAttributes otherAttributes, String prefix);

	/**
	 * Checks if attributes found in this item and supplied map match    
	 * using wild card match  
	 * @param otherAttributes attributes to match to
	 * @return true if matches, false otherwise
	 */
	boolean matchCommonAttributes(final IAttributes otherAttributes);

	/**
	 * Checks if attributes with given prefix found in this item and supplied map match    
	 * using wild card match  
	 * @param otherAttributes attributes to match to
	 * @param prefix attribute key prefix 
	 * @return true if matches, false otherwise
	 */
	boolean matchCommonAttributes(final IAttributes otherAttributes, String prefix);

	
	/**
	 * Matches attribute value against pattern 
	 * @param key attribute key, can be used to define match method 
	 * @param value attribute value
	 * @param pattern pattern to match to value 
	 * @return true if attribute value matches pattern 
	 */
	boolean matchAttribute(final String key, final String value, final String pattern);


	/**
	 * Checks if attributes map contain at least one attribute with value exactly matching supplied string 
	 * @param value attribute value
	 * @return true if map contains attribute value  
	 */
	boolean containsValue(final String value);

	/**
	 * Checks if attributes map contain at least one attribute with value matching given pattern value 
	 * @param pattern attribute value pattern
	 * @return true if map contains attribute value pattern  
	 */
	boolean containsValuePattern(final String pattern);

	
	/**
	 * Returns URL associated with the item if any
	 * @return URL associated with the item
	 */
	String getUrl();
	
	/**
	 * Returns document file or URL associated with the item if any
	 * @return document file or HTTP link associated with the item
	 */
	String getDoc();

}
