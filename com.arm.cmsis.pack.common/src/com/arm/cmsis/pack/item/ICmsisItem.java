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

import com.arm.cmsis.pack.error.ICmsisErrorCollection;
import com.arm.cmsis.pack.generic.ITreeObject;
import com.arm.cmsis.pack.item.ICmsisVisitor.VisitResult;

/**
 * Base interface for CMSIS items with basic tree structure support
 */
public interface ICmsisItem extends ITreeObject, ICmsisErrorCollection {

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
	 * Clears all cashed information if any 
	 */
	default void invalidate() { /*default does nothing */ };
	
	/**
	 * Clears all cashed information recursively for all children and grand Children 
	 */
	default void invalidateAll() { invalidate(); /*default calls invalidate */ };

	
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
	 * Items can have IDs constructed out of tag, text and attributes  
	 * @return element ID
	 */
	default String getId() { 
		return getName();
	}


	/**
	 * Casts a supplied object to a ICmsisItem  
	 * @param obj object to cast
	 * @return casted object if possible, null otherwise
	 */
	static ICmsisItem cast(Object obj){
		if(obj instanceof ICmsisItem) {
			return (ICmsisItem)obj;
		}
		return null;
	}
	
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
