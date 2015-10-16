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
