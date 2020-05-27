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

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
	 * Creates attribute collection   
	 * @return IAttributes newly created attributes collection
	 */
	default IAttributes createAttributes() {
		return new Attributes();
	}

	
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
	 * Returns Long representation of an attribute value  
	 * @param key attribute key
	 * @param nDefault value to return if attribute is not found
	 * @return attribute value as long or nDefault  
	 */
	default long getAttributeAsLong(String key, long nDefault) {
		return attributes().getAttributeAsLong(key, nDefault);
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
	 * Checks if an attribute can be modified (updated).    
	 * @param kew attribute key 
	 * @return true if the attribute can be modified 
	 */
	default boolean isAttributeModifiable(final String key) {
		return key != null && !key.isEmpty(); // default returns true for any attribute
	}

	/**
	 * Returns collection of all modifiable attributes   
	 * @return modifiable attributes as IAttributes or null if item has no attributes
	 */
	default IAttributes getModifiableAttributes() {
		if(attributes() == null)
			return null;
		IAttributes modAttributes = createAttributes();
		if(modAttributes == null) {
			return null;
		}
		
		for(Entry<String, String> e : attributes().getAttributesAsMap().entrySet()){
			if(!isAttributeModifiable(e.getKey()))
				continue;
			modAttributes.setAttribute(e.getKey(), e.getValue());
		}
		return modAttributes;
	}
	/**
	 * Updates existing attribute key-value pair or adds one if attribute does not exist  
	 * @param key attribute key
	 * @param value attribute value
	 * @return true if value has changes or attribute added
	 */
	default boolean updateAttribute(String key, String value) {
		if(!isAttributeModifiable(key))
			return false;
		return attributes().updateAttribute(key, value);
	}

	/**
	 * Updates existing attributes with supplied ones, adds attributes if do not exist, does not remove existing ones  
	 * @param newAttributes Map<String, String> with new key-value pairs 
	 * @return true if a single value has changes or an attribute added
	 */
	default boolean updateAttributes(Map<String, String> newAttributes) {
		if(newAttributes == null)
			return false;
		
		boolean bChanged = false;
		for(Entry<String, String> e : newAttributes.entrySet()) {
			if(updateAttribute(e.getKey(), e.getValue())) {
				bChanged = true;
			}
		}
		return bChanged;
	}
	
	/**
	 * Updates existing attributes with supplied ones, adds attributes if do not exist, removes if not in supplied collection   
	 * @param newAttributes IAttributes with new attribute values 
	 * @return true if a single value has changes or an attribute added
	 */
	default boolean updateAttributes(IAttributes newAttributes) {
		if(newAttributes == null)
			return false;
		
		boolean bChanged = updateAttributes(newAttributes.getAttributesAsMap());

		if(attributes().hasAttributes()) {
			// remove attributes that do not exist in the supplied collection
			Set<String> keys = new HashSet<>(attributes().getAttributesAsMap().keySet());
			for(String key : keys) {
				if(isAttributeModifiable(key) && !newAttributes.hasAttribute(key)) {
					attributes().removeAttribute(key);
					bChanged = true;
				}
			}
		}
		return bChanged;
	}

	/**
	 * Updates existing attributes with supplied ones, adds attributes if do not exist, removes if not in supplied collection   
	 * @param item IAttributeItem with new attribute values 
	 * @return true if a single value has changes or an attribute added
	 */
	default boolean updateAttributes(IAttributedItem item) {
		if(item == null)
			return false;
		return updateAttributes(item.attributes());
	}

	/**
	 * Sets new attributes to the item  replacing existing ones   
	 * @param item IAttributeItem with new attribute values 
	 */
	default void setAttributes(IAttributedItem item) {
		if(item == null)
			return;
		setAttributes(item.attributes());
	}

	/**
	 * Sets new attributes to the item  replacing existing ones   
	 * @param otherAttributes attributes to set
	 */
	default void setAttributes(IAttributes otherAttributes ) {
		if(otherAttributes == null)
			return;
		attributes().setAttributes(otherAttributes);
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
	default boolean isValid() {
		return getAttributeAsBoolean(CmsisConstants.VALID, true);
	}
	
	
	/**
	 * Returns value of an attribute present in this element or in attributes of parent items (if any).
	 * Search is performed until attribute is found or parent is null or parent implementation stops search
	 * @param key attribute key to search for
	 * @return attribute value or null if attribute not found
	 */
	default String getEffectiveAttribute(final String key) {
		return getAttribute(key);
	}


	/**
	 * Checks if item is a custom one  
	 * @return true if custom
	 */
	default boolean isCustom() {
		return getAttributeAsBoolean(CmsisConstants.CUSTOM, false);
	}

	/**
	 * Returns a value for "value" attribute 
	 * @return String corresponding "value" attribute 
	 */
	default String getValue() {
		return getAttribute(CmsisConstants.VALUE);
	}
	
	
	/**
	 * Checks attributes of this item match supplied ones. 
	 * @param  item IAttributedItem to compare
	 * @return true if equal, false otherwise
	 */
	default boolean equalsAttributes(final IAttributedItem item) {
		if(item == null)
			return false;
		return equalsAttributes(item.attributes());
	}

	/**
	 * Checks attributes of this item match supplied item's ones. 
	 * @param otherAttributes attributes to compare to
	 * @return true if equal, false otherwise
	 */
	default boolean equalsAttributes(final IAttributes otherAttributes) {
		return attributes().equalsAttributes(otherAttributes);
	}


	
	
	/**
	 * Checks if all attributes of this item exist in supplied map and their values match. 
	 * Wild card match is used  
	 * @param otherAttributes attributes to match to
	 * @return true if matches, false otherwise
	 */
	default boolean matchAttributes(final IAttributes otherAttributes) {
		return attributes().matchAttributes(otherAttributes);
	}


	/**
	 * Merges attributes from supplied attributes to this item, does not overwrite existing ones  
	 * @param attributes collection to merge
	 */
	default void mergeAttributes(final IAttributes attributes) {
		attributes().matchAttributes(attributes);
	}

	
	/**
	 * Merges attributes from supplied attributes to this item, does not overwrite existing ones  
	 * @param item IAttributedItem to merge
	 */
	default void mergeAttributes(final IAttributedItem item) {
		attributes().matchAttributes(item.attributes());
	}

}

