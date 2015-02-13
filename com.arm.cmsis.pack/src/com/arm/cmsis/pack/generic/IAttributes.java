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

package com.arm.cmsis.pack.generic;

import java.util.Map;

/**
 * Interface describing an item that has String attributes map. 
 */
public interface IAttributes {
	
	/**
	 * Empty string used as "null-object" to avoid using null strings
	 */
	static public final String EMPTY_STRING = "";
	
	
	/**
	 * Returns element tag
	 * @return XML tag associated with this item if any
	 */
	String getTag(); 
	
	/**
	 * Sets element tag
	 * @param tag tag associated with this item if any
	 */
	void setTag(String tag);

	/**
	 * @return XML text associated with this item if any
	 */
	String getText();  
	
	/**
	 * Sets element text
	 * @param text 
	 */
	void setText(String text);
	
	/**
	 * Elements can have names (for instance specified by "name" attribute)
	 * name is usually only a portion of element's ID 
	 * @return element name taken or constructed from attributes, tag and text    
	 */
	String getName(); 

	/**
	 * Returns item name to be effectively presented to the user, e.g. decorated name  
	 * @return effective item name
	 */
	String getEffectiveName();


	/** 
	 * Return item description text of the element if any 
	 * @return description or empty string 
	 */
	String getDescription();


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
	
	/**
	 * Checks if Element has attributes
	 * @return true if element has non-empty attributes map
	 */
	boolean hasAttributes();

	/**
	 * Returns all attributes of this element as a Map<String,String>  
	 * @return the attributes as a Map
	 */
	Map<String, String> getAttributesAsMap();
	
	/**
	 * Returns string containing all attributes in the form "key0"="value0", "key1"=value1,...
	 * @return string containing keys and values of all attributes
	 */
	String getAttributesAsString();


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
	 * Merges attributes from supplied attributes his element, does not overwrite existing ones  
	 * @param attributes collection to merge
	 */
	void mergeAttributes(final IAttributes attributes);

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
	 * @param otherAttributes to match to
	 * @return true if matches, false otherwise
	 */
	boolean matchAttributes(final IAttributes otherAttributes);


	/**
	 * Checks if attributes found in this item and supplied map match    
	 * using wild card match  
	 * @param attributes to match to
	 * @return true if matches, false otherwise
	 */
	boolean matchCommonAttributes(final IAttributes otherAttributes);

	
	/**
	 * Matches attribute value against pattern 
	 * @param key attribute key, can be used to define match method 
	 * @param value attribute value
	 * @param pattern pattern to match to value 
	 * @return true if attribute value matches pattern 
	 */
	boolean matchAttribute(final String key, final String value, final String pattern);
}
