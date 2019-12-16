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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.utils.WildCards;

/**
 *  Generic tree of Cmsis items
 */
public class CmsisTreeItem<T extends ICmsisTreeItem<T>> extends CmsisItem implements  ICmsisTreeItem<T> {

	protected T fParent = null;
	protected String fName = null;
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
		fParent = parent;
	}

	/**
	 * Hierarchical constructor
	 * @param parent parent item in the hierarchy
	 * @param name item name
	 */
	public CmsisTreeItem(T parent, String name) {
		fParent = parent;
		fName = name;
	}


	@Override
	public void clear() {
		super.clear();
		fChildren = null;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		fName = null;
	}

	@Override
	public void invalidateAll() {
		invalidate();
		Collection<? extends T> children = getChildren();
		if(children == null || children.isEmpty())
			return;
		for(T item : children) {
			item.invalidateAll();
		}
	}
	
	@Override
	public void destroy() {
		super.destroy();
		fParent = null;
		fName = null;
	}

	@Override
	public boolean purge() {
		if(isRemoved())
			return true;
		
		Collection<? extends T> children = getChildren();
		if(children == null) {
			return false;
		}

		for (Iterator<? extends T> iterator = children.iterator(); iterator.hasNext();) {
			T child = iterator.next();
			if(child.purge()) {
				iterator.remove();
				cachedChildArray = null;
			}
		}
		return false;
	}


	@Override
	public void setParent(T parent) {
		if(fParent == parent) {
			return;
		}
		if(fParent != null) {
			fParent.removeChild(getThisItem());
		}
		fParent = parent;
	}

	@Override
	public String getName() {
		if(fName == null) {
			fName = constructName();
			if(fName == null || fName.isEmpty()) {
				fName = super.getName();
			}
		}
		return fName;
	}

	/**
	 * Constructs item name
	 * @return constructed item name
	 */
	protected String constructName() {
		return CmsisConstants.EMPTY_STRING;
	}

	@Override
	public T getParent() {
		return fParent;
	}


	@Override
	public Object[] getHierachyPath() {
		List<Object> segments =  new LinkedList<Object>();
		segments.add(this);
		for(T item = getParent(); item != null; item = item.getParent()){
			if(item.getParent() != null) {
				segments.add(0, item);
			}
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
			invalidate();
			children().add(item);
		}
	}

	/**
	 * Returns child collection, creates one if not created yet
	 * @return child
	 */
	protected Collection<T> children() {
		if(fChildren == null ) {
			fChildren = createCollection();
		}
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
		if(children != null) {
			return children.size();
		}
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
			if(item.getParent() != null) {
				segments.add(0, item);
			}
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
		if(children != null) {
			return children.size();
		}
		return 0;	}

	@Override
	public boolean hasEffectiveChildren() {
		Collection<? extends T> children = getEffectiveChildren();
		return children != null && !children.isEmpty();
	}


	@Override
	public String getItemKey(T item) {
		if(item == null) {
			return null;
		}
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
		if(key == null) {
			return null;
		}
		Collection<? extends T> children = getChildren();
		if(children != null) {
			for(T child : children) {
				if(getItemKey(child).equals(key)) {
					return child;
				}
			}
		}
		return null;
	}


	@Override
	public String getFirstChildText(String key) {
		T item = getFirstChild(key);
		if(item != null) {
			return item.getText();
		}
		return null;
	}


	@Override
	public void removeChild(T childToRemove) {
		Collection<? extends T> children = getChildren();
		if(children == null) {
			return;
		}
		for (Iterator<? extends T> iterator = children.iterator(); iterator.hasNext();) {
			T child = iterator.next();
			if(child.equals(childToRemove)) {
				iterator.remove();
				invalidate();
			}
		}
	}


	@Override
	public T removeFirstChild(String key) {
		if(key == null) {
			return null;
		}
		Collection<? extends T> children = getChildren();
		if(children != null) {
			for(T child : children) {
				if(getItemKey(child).equals(key)) {
					children.remove(child);
					invalidate();
					return child;
				}
			}
		}
		return null;
	}


	@Override
	public T removeAllChildren(String key) {
		if(key == null) {
			return null;
		}
		Collection<? extends T> children = getChildren();
		if(children == null) {
			return null;
		}
		T firstRemovedChild = null;
		for (Iterator<? extends T> iterator = children.iterator(); iterator.hasNext();) {
			T child = iterator.next();
			if(getItemKey(child).equals(key)) {
				if(firstRemovedChild == null) {
					firstRemovedChild = child;
				}
				iterator.remove();
				cachedChildArray = null;
			}
		}
		return firstRemovedChild;
	}


	@Override
	public void replaceChild(T item) {
		if(item == null) {
			return;
		}
		if(item == this) {
			return;
		}
		removeAllChildren(getItemKey(item));
		addChild(item);
	}

	@Override
	public T getFirstItem(String pattern) {
		if(WildCards.matchNoCase(pattern, getName())) {
			return getThisItem();
		}

		Collection<? extends T> children = getChildren();
		if(children == null) {
			return null;
		}
		for(T item : children) {
			T matchingItem = item.getFirstItem(pattern);
			if(matchingItem != null) {
				return matchingItem;
			}
		}
		return null;
	}
}
