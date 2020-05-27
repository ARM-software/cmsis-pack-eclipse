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

package com.arm.cmsis.pack.item;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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
		super(parent);
	}

	/**
	 * Hierarchical constructor
	 * @param parent parent item in the hierarchy
	 * @param name item name 
	 */
	public CmsisMapItem(T parent, String name) {
		super(parent);
		fName = name;
	}

	@Override
	public void clear() {
		super.clear();
		fChildMap = null;
	}
	
	
	@Override
	public synchronized Collection<? extends T> getChildren() {
		if(fChildMap == null) {
			return Collections.emptyList();
		}

		if(fChildren == null) {
			fChildren = fChildMap.values();
		}
		return fChildren;
	}
	
	
	@Override
	public Collection<String> getKeys() {
		Map<String, ? extends T> childMap = getChildMap();
		if(childMap != null) {
			return childMap.keySet();
		}
		return Collections.emptyList();
	}

	
	@Override
	public T getChild(String key) {
		if(key != null) {
			Map<String, ? extends T> childMap = getChildMap();
			if(childMap != null) {
				return childMap.get(key);
			}
		} else {
			return getFirstChild();
		}
		return null;
	}
	

	@Override
	public T findChild(List<String> keyPath, boolean useFullPath) {
		if(keyPath == null || keyPath.isEmpty())
			return null;
		T previousChild = getThisItem();
		T child = null;
		for(String key : keyPath) {
			child = previousChild.getChild(key);
			if(child == null) {
				return useFullPath ? previousChild : null;
			}
			previousChild = child; 
		}
		return child;
	}
	

	@Override
	public List<String> getKeyPath() {
		List<String> keyPath = new LinkedList<>();
		T child = getThisItem();
		for(T parent = getParent(); parent != null; parent = parent.getParent()) {
			String key = parent.getItemKey(child);
			keyPath.add(0, key);
			child = parent;
		}
		return keyPath;
	}

	@Override
	public T getFirstChild() {
		Map<String, ? extends T> childMap = getChildMap();
		if(childMap != null && !childMap.isEmpty()) {
			return fChildMap.entrySet().iterator().next().getValue();
		}
		return null;
	}

	@Override
	public String getFirstChildKey() {
		Map<String, ? extends T> childMap = getChildMap();
		if(childMap != null && !childMap.isEmpty()) {
			return fChildMap.entrySet().iterator().next().getKey();
		}
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
		if(childMap != null) {
			return childMap.containsKey(key);
		}
		return false;
	}

	@Override
	public void addChild(T item) {
		if(item != null) {
			 // invalidate cached collections
			cachedChildArray = null; 
			fChildren = null; 
			String key = getItemKey(item);
			if(key != null) {
				childMap().put(getItemKey(item), item);
			}
		}
	}

	
	@Override
	public T removeChild(String key) {
		if(hasChild(key)) {
			 // invalidate cached collections
			cachedChildArray = null; 
			fChildren = null; 
			return childMap().remove(key);
		}
		return null;
	}

	@Override
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
		if(fChildMap == null ) {
			fChildMap = createMap();
		}
		return fChildMap;
	}
	
	/**
	 * Creates map suitable to store child items.
	 * Implementation can use HashMap as well as TreeMap with Comparator or  natural sorting     
	 * @return created map
	 */
	protected Map<String, T> createMap() {
		// default implementation creates TreeMap with natural sorting
		return new TreeMap<>();
	}

	@Override
	public boolean hasChildren() {
		Map<String, ? extends T> children = getChildMap();  
		return children != null && !children.isEmpty();
	}

	@Override
	public int getChildCount() {
		Map<String, ? extends T> children = getChildMap();
		if(children != null) {
			return children.size();
		}
		return 0;
	}
	
	@Override
	public Map<String, ? extends T> getEffectiveChildMap() {
		return getEffectiveItem().getChildMap();
	}

	@Override
	public Collection<String> getEffectiveKeys() {
		Map<String, ? extends T> childMap = getEffectiveChildMap();
		if(childMap != null) {
			return childMap.keySet();
		}
		return Collections.emptyList();
	}
	
	@Override
	public Collection<? extends T> getEffectiveChildren() {
		Map<String, ? extends T> childMap = getEffectiveChildMap();
		if(childMap != null) {
			return childMap.values();
		}
		return Collections.emptyList();
	}

	@Override
	public int getEffectiveChildCount() {
		Map<String, ? extends T> children = getEffectiveChildMap();
		if(children != null) {
			return children.size();
		}
		return 0;
	}

	@Override
	public boolean hasEffectiveChildren() {
		Map<String, ? extends T> children = getEffectiveChildMap();  
		return children != null && !children.isEmpty();
	}
}
