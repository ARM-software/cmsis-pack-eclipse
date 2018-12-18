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

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;


/**
 * Generic template-based interface for tree like structures
 * 
 * @param <T> type of items to store in the tree, must implement ITreeItem interface itself 
 */
public interface ITreeItem<T extends ITreeItem<T>> extends ITreeObject {
	
	/**
	 * Returns immediate parent of this item
	 * @return immediate parent item or null if this item is top-level item 
	 */
	@Override
	T getParent();

	/**
	 * Sets parent item for this item. 
	 * @param T parent item
	 */
	void setParent(T parent);

	
	/**
	 * Returns top-level parent of the hierarchy (the item that has no parent above) 
	 * @return top parent item   
	 */
	default T getRoot() {
		if(getParent() == null) {
			return getThisItem();
		}
		return getParent().getRoot();
	}
	
	
	/**
	 * Returns this item
	 * @return this item
	 */
	@SuppressWarnings("unchecked")
	default T getThisItem() {
		return (T)this; // we know that this item type is T : T extends ITreeItem<T>
	}

	
	/**
	 * Returns object of type T effectively associated with this item  
	 * @return effective object of type T
	 * @see #getEffectiveHierarchyItem() 
	 */
	T getEffectiveItem();

	/**
	 * Function symmetric to <code>getEffectiveItem()</code> fulfilling condition:
	 * <p/>
	 * <code>getEffectiveItem().getEffectiveHierarchyItem() == this</code> 
	 * <p/>
	 * @return item that represents a node in effective tree. That could be:
	 * <ul> 
	 * <li> the item itself
	 * <li> item's effective parent
	 * </ul>
	 * @see #getEffectiveItem() 
	 */
	T getEffectiveHierarchyItem();

	/**
	 * Returns effective parent of this item which might be the immediate parent or higher-level parent in the hierarchy 
	 * @return effective parent item 
	 */
	T getEffectiveParent();


    /**
     * Returns closest parent of given type
     * @param type class type to search
     * @return parent of given type or null if not found
     */
	default <C> C getParentOfType(Class<C> type) {
    	if(type == null)
    		return null;
    	for(T parent = getParent(); parent != null; parent = parent.getParent()) {
    		if(type.isInstance(parent))
    			return type.cast(parent);
    	}
    	return null;
    }
	
	/**
	 * Returns collection of children of specified type
	 * @param type type class type to search
	 * @return collection of sub-items matching given type, empty if none is found
	 */
	default <C> Collection<C> getChildrenOfType(Class<C> type) {
		if(!hasChildren())
			return new LinkedList<C>();
		return getChildren().stream()
			.filter(c -> type.isInstance(c))
			.map(c -> type.cast(c))
			.collect(Collectors.toList());
	}

	/**
	 * Returns list of of child items
	 * @return list of child items or null if item has no child elements 
	 */
	Collection<? extends T> getChildren(); 
	
	
	/**
	 * Returns list of of effective child items.
	 * <p> Effective child items can be a subset of own children (filtering) or grand children ( level is skipped)</p>
	 * Default should return all own children
	 * @return list of effective child items or null if item has no effective child elements 
	 */
	Collection<? extends T> getEffectiveChildren(); 

	/**
	 * Returns number of effective child items.
	 * @return effective child count 
	 */
	int getEffectiveChildCount(); 

	/**
	 * Checks if the item has effective children
	 * @return true if item has effective children 
	 */
	boolean hasEffectiveChildren(); 

	/**
	 * Adds item to children list 
	 * @param item child item to add 
	 */
	void addChild(T item);

	
	/**
	 * Returns implementation-dependent string key corresponding to the item 
	 * @param item item to get key from
	 * @return implementation-dependent key that can be used in functions using key parameter
	 */
	String getItemKey(T item);
	
	
	/**
	 * Returns the first child item 
	 * @return first child item  
	 */
	T getFirstChild();
		
	/**
	 * Returns implementation-depended string key of the first child 
	 * @return key of the very first child
	 * @see #getItemKey(ITreeItem) 
	 */
	String getFirstChildKey(); 
	

	/**
	 * Searches child collection for the first item corresponding to the given string key
	 * @param key implementation-dependent string to search for
	 * @return child item if found, null otherwise
	 * @see #getItemKey(ITreeItem) 
	 */
	T getFirstChild(final String key);

	
	/**
	 * Returns first child's text
	 * @param key implementation-dependent string to search for
	 * @return child item's text if found, null otherwise
	 * @see #getFirstChild(ITreeItem) 
	 * @see #getItemKey(ITreeItem)
	 */
	String getFirstChildText(final String key);

	/**
	 * Searches child collection for the first item corresponding to the given class type
     * @param type class type to search
	 * @return child item if found, null otherwise
	 */
	 default <C> C getFirstChildOfType(Class<C> type) {
		 if(type == null)
			 return null;
		 Collection<? extends T> children = getChildren();
		 if(children == null)
			 return null;
		 for(T child : children ) {
			 if(type.isInstance(child))
				 return type.cast(child);
		 }
		 return null;
	 }


	/**
	 * Removes child from the collection
	 * @param childToRemove child to remove
	 */
	void removeChild(T childToRemove);

	/**
	 * Removes first child corresponding to the given string
	 * @param key implementation-dependent string to search for
	 * @return removed child item if existed, null otherwise
	 * @see #getItemKey(ITreeItem) 
	 */
	T removeFirstChild(final String key);

	
	/**
	 * Removes all children corresponding to the given string
	 * @param key implementation-dependent string to search for
	 * @param first removed child if existed, null otherwise
	 * @see #getItemKey(ITreeItem)
	 */
	T removeAllChildren(final String key);

	/**
	 * Adds item to children list and removes all other children with the same key 
	 * @param item item to replace others
	 * @see #getItemKey(ITreeItem) 
	 */
	void replaceChild(T item);

	
	 /**
	 * Returns first item in the hierarchy that matches given wildcard pattern  
	 * @param pattern wildcard string to search for 
	 * @return first item matching given pattern if found, null otherwise
	 */
	T getFirstItem(final String pattern);
	

	/**
	 * Returns collection of segments from root to this 
	 * @return collection of items from root to this  
	 */
	Object[] getHierachyPath(); 
	
	/**
	 * Returns collection of segments from root to this item as effective hierarchy path 
	 * @return collection of items from root to this  
	 */
	Object[] getEffectiveHierachyPath(); 

	/**
	 * Returns array of effective child items as generic Objects
	 * @return array of effective child items or empty array if item has no children 
	 */
	Object[] getEffectiveChildArray();
	
}
