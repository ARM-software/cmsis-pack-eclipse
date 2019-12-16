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


/**
 * Interface for tree-like structures based on generic Object.
 * <p/>
 * Intended to be called from ITreeContentProvider 
 */
public interface ITreeObject {

	public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0]; 

	/**
	 *  Clears child collection 
	 */
	void clear(); 
	
	/**
	 * Destroys the object clearing it and by removing all references from it to other objects
	 */
	void destroy();

	/**
	 * Removes "dead" branches those that do not have end-leaves 
	 * @return true if this items has been destroyed and should be removed from parent's children
	 */
	boolean purge();

	/**
	 * Checks if this item is marked for removal and therefore must be purged
	 * @return true if item is marked for removal
	 */
	default boolean isRemoved() {
		return false; // default is not marked as removed
	}
	
	
	/**
	 * Returns immediate parent of this item
	 * @return immediate parent item or null if this item has no parent or parent is unknown 
	 */
	Object getParent();

	/**
	 * Tells if item has children 
	 * @return true if this item has children 
	 */
	boolean hasChildren();
	
	/**
	 * Returns number of children 
	 * @return child count  
	 */
	int getChildCount();

	/**
	 * Returns array of child items as generic Objects
	 * @return array of child items or empty array if item has no children 
	 */
	Object[] getChildArray();
	
	
	/**
	 * Casts this object to the given type if possible
	 * @param type lass type to cast to
	 * @return casted object if possible, null otherwise 
	 */
	default <C> C castTo(Class<C> type) {
		return castTo(this, type);
	}

	/**
	 * Helper function to cast a supplied object to a given type
	 * @return casted object if possible, null otherwise
	 */
	static <C> C castTo(Object obj, Class<C> type) {
		if(type.isInstance(obj))
			return type.cast(obj); 
		return null;
	}
	
	/**
	 * Casts a supplied object to a ITreeObject  
	 * @param obj object to cast
	 * @return casted object if possible, null otherwise
	 */
	static ITreeObject cast(Object obj){
		if(obj instanceof ITreeObject) {
			return (ITreeObject)obj;
		}
		return null;
	}
	
}
