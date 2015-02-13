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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.utils.WildCards;

/**
 *
 */
public class CmsisTreeItem<T extends ICmsisTreeItem<T>> extends CmsisItem implements  ICmsisTreeItem<T> {
	
	private T fParent = null;
	protected String fName = IAttributes.EMPTY_STRING;
	protected Collection<T> fChildren = null;

	/**
	 *  Default constructor
	 */
	public CmsisTreeItem() {
	}

	
	/**
	 * Hierarchical constructor
	 * @param parent parent item in the hierarchy 
	 */
	public CmsisTreeItem(T parent) {
		setParent(parent);
	}

	/**
	 * Hierarchical constructor
	 * @param parent parent item in the hierarchy
	 * @param name item name 
	 */
	public CmsisTreeItem(T parent, String name) {
		setParent(parent);
		fName = name;
	}

	@Override
	public void setParent(T parent) {
		fParent = parent;
	}

	@Override
	public String getName() {
		if(fName != null && !fName.isEmpty())
			return fName;
		return super.getName();
	}
	
	@Override
	public String getEffectiveName() {
		return getName();
	}

	@Override
	public T getParent() {
		return fParent;
	}

	@SuppressWarnings("unchecked")
	public T getThisItem() {
		return (T)this; // we know that this item type is T : T extends ICmsisTreeItem<T>
	}
	
	@Override
	public T getRoot() {
		if(fParent == null)
			return getThisItem();
		return fParent.getRoot();
	}
	
	@Override
	public Object[] getHierachyPath() {
		List<Object> segments =  new LinkedList<Object>();
		segments.add(this);
		for(T item = getParent(); item != null; item = item.getParent()){
			if(item.getParent() != null)
				segments.add(0, item);
		}
		return segments.toArray();
	}
	

	@Override
	public Collection<? extends T> getChildren() {
		return fChildren;
	}
	
		
	@Override
	public void addChild(T item) {
		if(item != null) {
			cachedChildArray = null; // invalidate
			children().add(item);
		}
	}

	/**
	 * Returns child collection, creates one if not created yet   
	 * @return child  
	 */
	protected Collection<T> children() {
		if(fChildren == null )
			fChildren = createCollection();
		return fChildren;
	}
	
	
	
	/**
	 * Creates collection suitable to store child items.
	 * Implementation can use List, Set or their descendants     
	 * @return created child collection
	 */
	protected Collection<T> createCollection(){
		// default creates linkedList
		return new LinkedList<T>();
	}
	
	
	@Override
	public boolean hasChildren() {
		Collection<? extends T> children = getChildren();
		return children != null && !children.isEmpty();
	}

	@Override
	public int getChildCount() {
		Collection<? extends T> children = getChildren();
		if(children != null)
			return children.size();
		return 0;
	}
	

	@Override
	public T getEffectiveItem() {
		return getThisItem();
	}

	@Override
	public T getEffectiveParent() {
		return getParent();
	}
	
	
	@Override
	public T getEffectiveHierarchyItem() {
		// default returns this item
		return getThisItem();
	}


	@Override
	public Object[] getEffectiveHierachyPath() {
		List<Object> segments =  new LinkedList<Object>();
		for(T item = getEffectiveHierarchyItem(); item != null; item = item.getEffectiveParent()){
			if(item.getParent() != null)
				segments.add(0, item);
		}
		return segments.toArray();
	}

	@Override
	public Object[] getEffectiveChildArray() {
		return getEffectiveItem().getChildArray();
	}

	@Override
	public Collection<? extends T> getEffectiveChildren() {
		return getEffectiveItem().getChildren();
	}

	@Override
	public int getEffectiveChildCount() {
		Collection<? extends T> children = getEffectiveChildren();
		if(children != null)
			return children.size();
		return 0;	}

	@Override
	public boolean hasEffectiveChildren() {
		Collection<? extends T> children = getEffectiveChildren();
		return children != null && !children.isEmpty();
	}

	
	@Override
	public String getItemKey(T item) {
		if(item == null)
			return null;
		return item.getName(); // default returns item name
	}


	@Override
	public T getFirstChild() {
		Collection<? extends T> children = getChildren(); 
		if(children != null) {
			for(T child : children) {
				return child;
			}
		}
		return null;
	}


	@Override
	public String getFirstChildKey() {
		Collection<? extends T> children = getChildren(); 
		if(children != null) {
			for(T child : children) {
				return getItemKey(child);
			}
		}
		return null;	
	}


	@Override
	public T getFirstChild(String key) {
		if(key == null)
			return null;
		Collection<? extends T> children = getChildren();  
		if(children != null) {
			for(T child : children) {
				if(getItemKey(child).equals(key))
					return child;
			}
		}
		return null;
	}
	
	@Override
	public T removeFirstChild(String key) {
		if(key == null)
			return null;
		Collection<? extends T> children = getChildren();  
		if(children != null) {
			for(T child : children) {
				if(getItemKey(child).equals(key)) {
					children.remove(child);
					return child;
				}
			}
		}
		return null;
	}


	@Override
	public T removeAllChildren(String key) {
		if(key == null)
			return null;
		Collection<? extends T> children = getChildren();
		if(children == null)
			return null;
		T firstRemovedChild = null;
		for (Iterator<? extends T> iterator = children.iterator(); iterator.hasNext();) {
			T child = iterator.next();
			if(getItemKey(child).equals(key)) {
				if(firstRemovedChild == null)
					firstRemovedChild = child;
				iterator.remove();
			}
		}	
		return firstRemovedChild;
	}


	@Override
	public void replaceChild(T item) {
		if(item == null)
			return;
		removeAllChildren(getItemKey(item));
		addChild(item);
	}

	@Override
	public T getFirstItem(String pattern) {
		if(WildCards.matchNoCase(pattern, getName()))
			return getThisItem();
		
		Collection<? extends T> children = getChildren();
		if(children == null)
			return null;
		for(T item : children) {
			T matchingItem = item.getFirstItem(pattern);
			if(matchingItem != null)
				return matchingItem;
		}
		return null;
	}
}
