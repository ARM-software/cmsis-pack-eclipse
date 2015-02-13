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

import com.arm.cmsis.pack.base.ICmsisVisitor.VisitResult;
import com.arm.cmsis.pack.generic.ITreeObject;

/**
 * Base interface for CMSIS items with basic tree structure support
 */
public interface ICmsisItem extends ITreeObject {

	/**
	 * Returns item tag
	 * @return XML tag associated with this item if any
	 */
	String getTag(); 
	
	/**
	 * Sets item tag
	 * @param tag tag associated with this item if any
	 */
	void setTag(String tag);

	/**
	 * Returns item text
	 * @return text associated with this item if any
	 */
	String getText();  
	
	/**
	 * Sets item text
	 * @param text 
	 */
	void setText(String text);
	
	/**
	 * Items can have names (for instance specified by "name" attribute)
	 * name is usually only a portion of element's ID 
	 * @return item name taken or constructed from attributes, tag and text    
	 */
	String getName(); 

	/**
	 * Returns item name to be effectively presented to the user, e.g. decorated name  
	 * @return effective item name
	 */
	String getEffectiveName();


	/** 
	 * Return item description text of the element if any 
	 * @return description or empty string 
	 */
	String getDescription();


	/**
	 * Returns URL associated with the item if any
	 * @return URL associated with the item
	 */
	String getUrl();
	
	/**
	 * Returns document file or URL associated with the item if any
	 * @return document file or HTTP link associated with the item
	 */
	String getDoc();

	/**
	 * Returns collection of child items
	 * @return collection of child items 
	 */
	Collection<? extends ICmsisItem> getChildren();

	/**
	 * Returns if only one of its children or associated items can be active at a time.
	 * </p>  
	 *  For instance an API is exclusive if  only one component implementing the API can be selected 
	 * @return true if item is exclusive
	 */
	boolean isExclusive();

	
	/**
	 * Visitor pattern to traverse the tree 
	 * @param visitor ICmsisVisitor to visit
	 * @return VisitResult instructing ICpItem on further processing:
	 * <ul>
	 * <li>CONTINUE  	  continue processing the tree
	 * <li>SKIP_CHILDREN  skip visiting child items of this item
	 * <li>SKIP_LEVEL     skip visiting child and remaining sibling items
	 * <li>CANCEL		  cancel further visits
	 * </ul>
	 * @see  ICmsisVisitor
	 */
	VisitResult accept(ICmsisVisitor visitor);
	
}
