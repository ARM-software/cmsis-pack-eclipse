/*******************************************************************************
* Copyright (c) 2017 ARM Ltd. and others
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *  Class to support a small sorted collection with selected item
 */
public class ItemSelector<K, T> {

	protected Map<K, T> fItems;
	protected T fSelectedItem;
	protected K fSelectedKey;
	protected int fSelectedIndex;
	
	/**
	 * Default constructor
	 */
	public ItemSelector() {
		fItems = null;
		clearSelection();
	}
	
	/**
	 * Returns internal map storing items
	 * @return Map<K, T> 
	 */
	public Map<K, T> getMap(){
		if(fItems == null) {
			fItems = createMap();
		}
		return fItems;
	}
	/**
	 * Creates a map object to be used as internal map to store items
	 * @return Map<K, T>, default creates  LinkedHashMap<K, T>
	 */
	protected Map<K, T> createMap() {
		return new LinkedHashMap<>();
	}

	/**
	 * Return item for a supplied key, if key is null selected item is returned
	 * @param key item key
	 * @return item
	 */
	public T getItem(K key) {
		if(key == null)
			return getSelectedItem();
		return getMap().get(key);
	}

	/** 
	 * Returns item for given index. If index < 0 selected item is returned 
	 * @param index item index
	 * @return item
	 */
	public T getItem(int index) {
		if(fSelectedIndex == index)
			return getSelectedItem();
		if(index < 0)
			return getSelectedItem();
		if(index >= getMap().size())
			return null; // out of bounds 
		int i = 0;
		for( Entry<K, T> entry: getMap().entrySet()){
			if(index == i) {
				return entry.getValue();
			}
			i++;
		}
		return null;
	}

	/**
	 * Returns number of stored items
	 * @return number of stored items
	 */
	public int getCount(){
		return getMap().size();
	}
	
	
	/**
	 * Returns collection of stored items
	 * @return Collection<T>
	 */
	public Collection<T> getItems(){
		return getMap().values();
	}

	/**
	 * Returns collection of item keys
	 * @return Collection<K> 
	 */
	public Collection<K> getKeys(){
		return getMap().keySet();
	}
	
	/**
	 *  Cleares current selection
	 */
	public void clearSelection() {
		fSelectedIndex = -1;
		fSelectedItem = null;
		fSelectedKey = null;
	}
	
	/**
	 * Returns selected item
	 * @return selected item or null if items map is empty or no selection available
	 */
	public T getSelectedItem() {
		return fSelectedItem;
	}

	/**
	 * Returns selected key
	 * @return selected key or null if items map is empty or no selection available
	 */
	public K getSelectedKey() {
		return fSelectedKey;
	}

	/**
	 * Returns selected index
	 * @return selected index or -1 if items map is empty or no selection available
	 */
	public int getSelectedIndex() {
		return fSelectedIndex;
	}

	/**
	 * Selects an item corresponding given index
	 * @param index item index to select
	 * @return true if selection has changed 
	 */
	public boolean selectIndex(int index) {
		if(fSelectedIndex == index)
			return false;
		int i = 0;
		for( Entry<K, T> entry: getMap().entrySet()){
			if(index == i) {
				fSelectedKey = entry.getKey();
				fSelectedItem = entry.getValue();
				fSelectedIndex = index;
				return true;
			}
			i++;
		}
		clearSelection();
		return true;
	}

	/**
	 * Selects an item corresponding given key
	 * @param key item key to select
	 * @return true if selection has changed 
	 */
	public boolean selectKey(K key)
	{
		if(key != null) {
			if(key.equals(fSelectedKey))
				return false;
			int i = 0;
			for( Entry<K, T> entry: getMap().entrySet()){
				if(key.equals(entry.getKey())) {
					fSelectedKey = key;
					fSelectedItem = entry.getValue();
					fSelectedIndex = i;
					return true;
				}
				i++;
			}
		}
		clearSelection();
		return true;
	}
	/**
	 * Selects item
	 * @param item item to select
	 * @return true if selection has changed 
	 */
	public boolean selectItem(T item)
	{
		if(item != null) {
			if(item.equals( fSelectedItem))
				return false;
			int i = 0;
			for( Entry<K, T> entry: getMap().entrySet()){
				if(item.equals(entry.getValue())) {
					fSelectedKey = entry.getKey();
					fSelectedItem = entry.getValue();
					fSelectedIndex = i;
					return true;
				}
				i++;
			}
		}
		clearSelection();
		return true;
	}
	
	public void addItem(K key, T item) {
		getMap().put(key, item); 
		if(fSelectedItem == null) {
			fSelectedItem = item;
			fSelectedKey = key;
			fSelectedIndex = 0;
		}
	}
}
