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


/**
 * Interface for tree-like structures based on generic Object.
 * <p/>
 * Intended to be called from ITreeContentProvider 
 */
public interface ITreeObject {

	public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0]; 
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
}
