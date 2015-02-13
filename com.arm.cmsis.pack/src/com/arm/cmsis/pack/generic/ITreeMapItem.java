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
import java.util.Map;

/**
 * Extension to ITreeItem interface for trees that use maps for child collections.
 * 
 * @param <K> key to be used for sorting (usually String) 
 * @param <E> type of items to store in the tree, must implement ITreeItem interface
 * @see ITreeItem 
 */
public interface ITreeMapItem<K, E extends ITreeItem<E> > extends ITreeItem<E> {

	/**
	 * Returns children as a map of key-value pairs
	 * @return map of child items
	 */
	Map<K, ? extends E> getChildMap();
	
	
	/**
	 * Returns collection of the keys in child map
	 * @return collection of keys    
	 */
	Collection<K> getKeys();
	
	
	/** 
	 * Returns child item corresponding to given key 
	 * @param key key to search for
	 * @return child item corresponding to the key or null if key is not found
	 */
	E getChild(final K key);
	
	/**
	 * Checks if the child item exists for the given key
	 * @param key key to search for
	 * @return true if child exists 
	 */
	boolean hasChild(final K key);

	/**
	 * Removes child for the given key if exists
	 * @param key key to search for
	 * @return removed child item if existed, null otherwise 
	 */
	E removeChild(final K key);

	
	
	/**
	 * Returns effective children as a map of key-value pairs
	 * @return map of child items
	 * @see ITreeItem.getEffectiveChildren
	 */
	Map<K, ? extends E> getEffectiveChildMap();
	
	
	/**
	 * Returns collection of the keys in child map
	 * @return collection of keys    
	 */
	Collection<K> getEffectiveKeys();
	
	
	
}
