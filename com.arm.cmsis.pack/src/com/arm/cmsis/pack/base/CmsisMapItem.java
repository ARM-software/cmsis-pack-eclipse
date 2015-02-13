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

package com.arm.cmsis.pack.base;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;


/**
 * Base implementation of ICmsisMapItem interface
 */
public class CmsisMapItem<T extends ICmsisMapItem<T>> extends CmsisTreeItem<T> implements ICmsisMapItem<T> {

	protected Map<String, T> fChildMap;
	/**
	 *  Default constructor
	 */
	public CmsisMapItem() {
	}

	/**
	 * Hierarchical constructor
	 * @param parent parent item in the hierarchy 
	 */
	public CmsisMapItem(T parent) {
		setParent(parent);
	}

	/**
	 * Hierarchical constructor
	 * @param parent parent item in the hierarchy
	 * @param name item name 
	 */
	public CmsisMapItem(T parent, String name) {
		setParent(parent);
		fName = name;
	}

	@Override
	public Collection<? extends T> getChildren() {
		if(fChildren == null) {
			if(fChildMap != null)
				fChildren = fChildMap.values();
		}
		return fChildren;
	}
	
	
	@Override
	public Collection<String> getKeys() {
		Map<String, ? extends T> childMap = getChildMap();
		if(childMap != null)
			return childMap.keySet();
		return null;
	}

	
	@Override
	public T getChild(String key) {
		if(key != null) {
			Map<String, ? extends T> childMap = getChildMap();
			if(childMap != null)
				return childMap.get(key);
		}
		return null;
	}
	
	

	@Override
	public T getFirstChild() {
		Map<String, ? extends T> childMap = getChildMap();
		if(childMap != null)
			return fChildMap.entrySet().iterator().next().getValue();
		return null;
	}

	@Override
	public String getFirstChildKey() {
		Map<String, ? extends T> childMap = getChildMap();
		if(childMap != null)
			return fChildMap.entrySet().iterator().next().getKey();
		return null;
	}

	@Override
	public T getFirstChild(String key) {
		return getChild(key);
	}

	
	@Override
	public T removeFirstChild(String key) {
		return removeChild(key);
	}
	
	@Override
	public T removeAllChildren(String key) {
		return removeChild(key);
	}

	@Override
	public void replaceChild(T item) {
		addChild(item);
	}

	@Override
	public boolean hasChild(String key) {
		Map<String, ? extends T> childMap = getChildMap();
		if(childMap != null)
			return childMap.containsKey(key);
		return false;
	}

	@Override
	public void addChild(T item) {
		if(item != null) {
			 // invalidate cached collections
			cachedChildArray = null; 
			fChildren = null; 
			String key = getItemKey(item);
			if(key != null)
				childMap().put(getItemKey(item), item);
		}
	}

	
	@Override
	public T removeChild(String key) {
		if(hasChild(key))
			return childMap().remove(key);
		return null;
	}

	public Map<String, ? extends T> getChildMap() {
		return fChildMap;
	}

	
	@Override
	protected Collection<T> children() {
		return childMap().values();
	}

	@Override
	protected Collection<T> createCollection() {
		return createMap().values();
	}

	/**
	 * Returns child map, creates one if not created yet   
	 * @return child map  
	 */
	protected Map<String, T> childMap() {
		if(fChildMap == null )
			fChildMap = createMap();
		return fChildMap;
	}
	
	/**
	 * Creates map suitable to store child items.
	 * Implementation can use HashMap as well as TreeMap with Comparator or  natural sorting     
	 * @return created map
	 */
	protected Map<String, T> createMap() {
		// default implementation creates TreeMap with natural sorting
		return new TreeMap<String,T>();
	}

	@Override
	public boolean hasChildren() {
		Map<String, ? extends T> children = getChildMap();  
		return children != null && !children.isEmpty();
	}

	@Override
	public int getChildCount() {
		Map<String, ? extends T> children = getChildMap();
		if(children != null)
			return children.size();
		return 0;
	}
	
	@Override
	public Map<String, ? extends T> getEffectiveChildMap() {
		return getEffectiveItem().getChildMap();
	}

	@Override
	public Collection<String> getEffectiveKeys() {
		Map<String, ? extends T> childMap = getEffectiveChildMap();
		if(childMap != null)
			return childMap.keySet();
		return null;
	}
	
	@Override
	public Collection<? extends T> getEffectiveChildren() {
		Map<String, ? extends T> childMap = getEffectiveChildMap();
		if(childMap != null)
			return childMap.values();
		return null;
	}

	@Override
	public int getEffectiveChildCount() {
		Map<String, ? extends T> children = getEffectiveChildMap();
		if(children != null)
			return children.size();
		return 0;
	}

	@Override
	public boolean hasEffectiveChildren() {
		Map<String, ? extends T> children = getEffectiveChildMap();  
		return children != null && !children.isEmpty();
	}
}
