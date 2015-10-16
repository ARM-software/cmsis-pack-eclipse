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
import java.util.TreeMap;
import java.util.Map.Entry;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.utils.WildCards;

/**
 * Default implementation of IAttributes interface
 */
public class Attributes implements IAttributes {

	protected Map<String, String> fAttributes = null;

	/**
	 * Default constructor 
	 */
	public Attributes() {
	}


	/**
	 * Copy constructor
	 * @param copyFrom
	 */
	public Attributes(final IAttributes copyFrom) {
		setAttributes(copyFrom.getAttributesAsMap());
	}
	

	@Override
	synchronized public boolean hasAttribute(String key) {
		return fAttributes != null && fAttributes.containsKey(key);
	}

	@Override
	synchronized public String getAttribute(String key) {
		if(fAttributes != null)
			return fAttributes.get(key);
		return null;
	}

	@Override
	synchronized public String getAttribute(String key, String defaultValue) {
		String value = getAttribute(key);
		if(value != null)
			return value;
		return defaultValue;
	}

	
	@Override
	public int getAttributeAsInt(String key, int nDefault) {
		String value = getAttribute(key);
		if(value != null && !value.isEmpty()) {
			try {
				return Integer.decode(value);
			} catch (NumberFormatException e) {
				// do nothing, return default  
			}
		}
		return nDefault;
	}

	@Override
	public long getAttributeAsLong(String key, long nDefault) {
		String value = getAttribute(key);
		if(value != null && !value.isEmpty()) {
			try {
				return Long.decode(value);
			} catch (NumberFormatException e) {
				// do nothing, return default  
			}
		}
		return nDefault;
	}

	
	@Override
	public boolean getAttributeAsBoolean(String key, boolean bDefault) {
		String value = getAttribute(key);
		if(value != null && !value.isEmpty()) {
			if(value.equals("1") || value.equals("true")) //$NON-NLS-1$ //$NON-NLS-2$
				return true;
			else 
				return false;
		}
		return bDefault;
	}
	
	
	@Override
	synchronized public void setAttribute(String key, String value) {
		if(key == null)
			return;
		if(fAttributes == null) {
			fAttributes = new TreeMap<String, String>(); // use natural sorting to get consistent string representation
		}
		if(value != null)
			fAttributes.put(key, value);
		else if(fAttributes != null)
			fAttributes.remove(key);
	}
	
 
	@Override
	public void setAttribute(String key, boolean value) {
		setAttribute(key, value ? "1" : "0"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public void setAttribute(String key, int value) {
		setAttribute(key, Integer.toString(value));
	}

	@Override
	synchronized public void removeAttribute(String key) {
		if(fAttributes != null ) {
			fAttributes.remove(key);
		}
	}

	@Override
	synchronized public void mergeAttribute(String key, String value) {
		if(!hasAttribute(key))
			setAttribute(key, value);
	}

		
	@Override
	synchronized public boolean hasAttributes() {
		return fAttributes != null && !fAttributes.isEmpty();
	}

	@Override
	synchronized public Map<String, String> getAttributesAsMap() {
		return fAttributes;
	}

	@Override
	synchronized public void setAttributes(Map<String, String> attributes) {
		if(attributes == null) {
			fAttributes = null;
		} else {
			// make copy
			fAttributes = new TreeMap<String, String>();
			fAttributes.putAll(attributes);
		}
	}

	@Override
	synchronized public void setAttributes(IAttributes attributes) {
		if(attributes != null && attributes.hasAttributes())
			setAttributes(attributes.getAttributesAsMap());	
		else
			fAttributes = null;
		
	}

	@Override
	synchronized public void mergeAttributes(final IAttributes attributes) {
		if(attributes == null || !attributes.hasAttributes())
			return; // nothing to merge
		Map<String, String> attributesMap = attributes.getAttributesAsMap();
		if(fAttributes == null)
			setAttributes(attributesMap);
		for(Entry<String, String> e: attributesMap.entrySet()) {
			mergeAttribute(e.getKey(), e.getValue());
		}
	}

	/**
	 * Returns string containing all attributes in the form "key0"="value0", "key1"=value1,...
	 * @return string containing keys and values of all attributes
	 */
	synchronized public String getAttributesAsString() {
		String s = CmsisConstants.EMPTY_STRING;
		if(hasAttributes()) {
			for(Entry<String, String> e : fAttributes.entrySet()){
				if(!s.isEmpty())
					s += ", "; //$NON-NLS-1$
				s += e.getKey();
				s += "=\""; //$NON-NLS-1$
				s += e.getValue();
				s += "\""; //$NON-NLS-1$
			}
		}
		return s;
	}

	@Override
	synchronized public boolean containsAttribute(String pattern) {
		if(fAttributes == null)
			return false;
		for(String key : fAttributes.keySet()){
			if(WildCards.match(pattern, key))
				return true;
		}
		return false;
	}


	@Override
	public boolean matchAttributes(final IAttributes attributes) {
		return matchAttributes(attributes, false);
	}


	@Override
	public boolean matchCommonAttributes(final IAttributes attributes) {
		return matchAttributes(attributes, true);
	}

	
	/**
	 * Checks if attributes found in this element and supplied map match    
	 * using wild card match  
	 * @param attributes to match to
	 * @param bCommon match mode: true - compare only attributes found in both maps, false - match all
	 * @return true if matches, false otherwise
	 */
	protected boolean matchAttributes(final IAttributes attributes, boolean bCommon){
		if(attributes == null)
			return fAttributes == null || fAttributes.isEmpty();
		for(Entry<String, String> e : fAttributes.entrySet()){
			String key = e.getKey();
			String val = e.getValue();
			String pattern =attributes.getAttribute(key);
			if(pattern == null) {
				if(bCommon)
					continue;
				else
					return false;
			}
			if(!matchAttribute(key, val, pattern))
				return false;
		}
		return true;
	}
	

	@Override
	public boolean matchAttribute(String key, String value, String pattern) {
		return WildCards.match(pattern, value);
	}


	@Override
	public String getUrl() {
		return getAttribute(CmsisConstants.URL);
	}

	@Override
	public String getDoc() {
		return getAttribute(CmsisConstants.DOC);
	}


	@Override
	public boolean equals(Object obj) {
		if(obj instanceof IAttributes) {
			IAttributes other = (IAttributes)obj;
			return other.toString().equals(toString());
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		return getAttributesAsString();
	}
	
	
	
}
