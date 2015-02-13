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
import java.util.TreeMap;
import java.util.Map.Entry;

import com.arm.cmsis.pack.utils.Vendor;
import com.arm.cmsis.pack.utils.WildCards;

/**
 * Default implementation of IAttributes interface
 */
public class Attributes implements IAttributes {

	protected Map<String, String> fAttributes = null;
	protected String fTag  = EMPTY_STRING;    
	protected String fText = EMPTY_STRING;

	/**
	 * Default constructor 
	 */
	public Attributes() {
	}

	/**
	 * Tagged constructor 
	 */
	public Attributes(final String tag) {
		setTag(tag);
	}

	/**
	 * Copy constructor
	 * @param copyFrom
	 */
	public Attributes(final IAttributes copyFrom) {
		setAttributes(copyFrom.getAttributesAsMap());
		setTag(copyFrom.getTag());
		setText(copyFrom.getTag());
	}
	

	@Override
	public boolean hasAttribute(String key) {
		return fAttributes != null && fAttributes.containsKey(key);
	}

	@Override
	public String getAttribute(String key) {
		if(fAttributes != null)
			return fAttributes.get(key);
		return null;
	}

	@Override
	public String getAttribute(String key, String defaultValue) {
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
	public boolean getAttributeAsBoolean(String key, boolean bDefault) {
		String value = getAttribute(key);
		if(value != null && !value.isEmpty()) {
			if(value.equals("1") || value.equals("true"))
				return true;
			else 
				return false;
		}
		return bDefault;
	}
	
	
	@Override
	public void setAttribute(String key, String value) {
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
		setAttribute(key, value ? "1" : "0");
	}

	@Override
	public void setAttribute(String key, int value) {
		setAttribute(key, Integer.toString(value));
	}

	@Override
	public void removeAttribute(String key) {
		if(fAttributes != null ) {
			fAttributes.remove(key);
		}
	}

	@Override
	public void mergeAttribute(String key, String value) {
		if(!hasAttribute(key))
			setAttribute(key, value);
	}

		
	@Override
	public boolean hasAttributes() {
		return fAttributes != null && !fAttributes.isEmpty();
	}

	@Override
	public Map<String, String> getAttributesAsMap() {
		return fAttributes;
	}

	@Override
	public void setAttributes(Map<String, String> attributes) {
		if(attributes == null) {
			fAttributes = null;
		} else {
			// make copy
			fAttributes = new TreeMap<String, String>();
			fAttributes.putAll(attributes);
		}
	}

	@Override
	public void setAttributes(IAttributes attributes) {
		if(attributes != null && attributes.hasAttributes())
			setAttributes(attributes.getAttributesAsMap());	
		else
			fAttributes = null;
		
	}

	@Override
	public void mergeAttributes(final IAttributes attributes) {
		if(attributes == null || !attributes.hasAttributes())
			return; // nothing to merge
		Map<String, String> attributesMap = attributes.getAttributesAsMap();
		if(fAttributes == null)
			setAttributes(attributesMap);
		for(Entry<String, String> e: attributesMap.entrySet()) {
			mergeAttribute(e.getKey(), e.getValue());
		}
	}

	@Override
	public String getAttributesAsString() {
		String s = EMPTY_STRING;
		if(hasAttributes()) {
			for(Entry<String, String> e : fAttributes.entrySet()){
				if(!s.isEmpty())
					s += ", ";
				s += e.getKey();
				s += "=\"";
				s += e.getValue();
				s += "\"";
			}
		}
		return s;
	}

	@Override
	public boolean containsAttribute(String pattern) {
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
		if(key.equals("Dvendor")) {
			if(!Vendor.match(value, pattern))
				return false;
		} else if(!WildCards.match(pattern, value)) {
			return false;
		}
		return true;
	}


	@Override
	public String getTag() {
		return fTag;
	}

	@Override
	public void setTag(String tag) {
		if(tag != null)
			fTag = tag;
		else
			fTag = IAttributes.EMPTY_STRING;
	}


	@Override
	public String getText() {
		return fText;
	}

	@Override
	public void setText(String text) {
		if(text != null)
			fText = text;
		else
			fText = IAttributes.EMPTY_STRING;
	}

	@Override
	public String getName() {
		String name = getAttribute("name");
		if(name != null)
			return name;
		return fTag;
	}
		
	@Override
	public String getEffectiveName() {
		return getName();
	}

	@Override
	public String getUrl() {
		return getAttribute("url");
	}

	@Override
	public String getDoc() {
		return getAttribute("doc");
	}

	@Override
	public String getDescription() {
		return getText();
	}

}
