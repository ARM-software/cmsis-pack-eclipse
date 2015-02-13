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

import java.util.Collection;


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
	T getRoot();
	
		
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
