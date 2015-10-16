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

package com.arm.cmsis.pack.data;

import java.util.Collection;
import java.util.Map;

import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.enums.EVersionMatchMode;
import com.arm.cmsis.pack.generic.IAttributedItem;
import com.arm.cmsis.pack.item.ICmsisTreeItem;

/**
 * Base for all items in CMSIS packs 
 *   
 */
public interface ICpItem extends IAttributedItem, ICpItemFactory, ICmsisTreeItem<ICpItem> {
	
	public static final ICpItem[] EMPTY_CPITEM_ARRAY = new ICpItem[0];
	
	/**
	 * Items can have IDs constructed out of tag, text and attributes  
	 * @return element ID
	 */
	String getId();
	
	/**
	 * Returns pack item containing this item as ICpPack   
	 * @return pack item owning the item tree
	 */
	ICpPack getPack();     

	/**
	 * Returns ID of pack containing this item
	 * @return pack id
	 */
	String getPackId();

	/**
	 * Returns family ID of pack containing this item (pack ID without version in form Vendor.Name)
	 * @return pack family ID
	 */
	String getPackFamilyId();
	
	
	/**
	 * Returns parent item in the hierarchy that has corresponding tag  
	 * @param tag parent's tag to search  
	 * @return parent item in the hierarchy with corresponding tag or null 
	 */
	ICpItem getParent(final String tag);

	/**
	 * Returns parent component or API item   
	 * @return parent component item in the hierarchy if any 
	 */
	ICpComponent getParentComponent();
	
	/**
	 * Returns grand children of a child with specified tag 
	 * @param tag child's tag
	 * @return collection of child's children or null if no child is found 
	 */
	Collection<? extends ICpItem> getGrandChildren(final String tag);
	

	/**
	 * Returns collection of children that have specified tag 
	 * @param tag child's tag
	 * @return collection of children having specified tag 
	 */
	Collection<ICpItem> getChildren(final String tag);

	
	/**
	 * Checks if an attribute exists in the internal collection
	 * @param key attribute key to search for
	 * @return true if attribute exists
	 */
	boolean hasAttribute(final String key);

	/**
	 * Retrieves an attribute value from internal collection
	 * @param key attribute key to search for
	 * @return attribute value or empty string if attribute not found
	 */
	String getAttribute(final String key);
	
	/**
	 * Returns value of an attribute present in this element or in attributes of parent items.
	 * Search is performed until attribute is found or parent is null or parent implementation stops search
	 * @param key attribute key to search for
	 * @return attribute value or null if attribute not found
	 */
	String getEffectiveAttribute(final String key);

	/**
	 * Returns collected attributes from this item and parent items. 
	 * Search is performed until parent is null or parent implementation stops search
	 * @param m attribute map to fill. If null, the collection is allocated.
	 * @return filled attribute collection
	 */
	Map<String, String> getEffectiveAttributes(Map<String, String> m);
	
	
	/**
	 * Checks if item has "condition" attribute 
	 * @return true if item has condition
	 */
	boolean hasCondition();

	
	/**
	 * Returns item's condition ID 
	 * @return condition ID if exists, otherwise null
	 */
	String getConditionId();
	

	/**
	 * Items like components and files can have condition
	 * @return condition object for this item or null if item has no condition 
	 */
	ICpItem getCondition();
	
	/**
	 * Evaluates underlying condition for given context and returns its result  
	 * @param context condition evaluation context
	 * @return evaluation result for underlying condition or IGNORED if item has no condition   
	 */
	EEvaluationResult evaluate(ICpConditionContext context);
	

	/**
	 * Searches child collection for the first item corresponding to the given ID string
	 * @param id implementation-dependent ID to search for
	 * @param child item if found, null otherwise 
	 */
	ICpItem getProperty(final String id);

	
	/**
	 * Checks if availability of this item depends on selected device
	 * @return true if item is device dependent
	 */
	boolean isDeviceDependent();
	
	/**
	 * Merges property to the child list: adds if the property with same ID does not yet exist
	 * @param property item to merge 
	 */
	void mergeProperty(ICpItem property); 

	/**
	 * Merges content of supplied property to the child item whose ID equals to supplied property one 
	 * @param property property which content to merge 
	 */
	void mergeEffectiveContent(ICpItem property); 
	
	
	/**
	 * Returns true if the property provides effective content and must collect it   
	 * @param true if the property provides effective content and must collect it 
	 */
	boolean providesEffectiveContent(); 
	
	/**
	 * Device properties: returns if item is unique => appears only once in effective properties 
	 * @return true if property is unique
	 */
	boolean isUnique();
	
	
	/**
	 * Gets the item containing collection of effective sub-properties merged with corresponding properties in higher levels in device description hierarchy 
	 * @return collection of effective sub-properties
	 */
	ICpItem getEffectiveContent();
	
	/**
	 * Returns vendor of the element   
	 * @return vendor name of this element
	 */
	String getVendor();

	/**
	 * Returns version of the element   
	 * @return version of this element
	 */
	String getVersion();
	
	/**
	 * Returns "Pname" attribute of the element representing device property 
	 * @return processor name or empty string if "pname" attribute not found
	 */
	String getProcessorName();

	/**
	 * Returns full device name in form "Name:Pname" 
	 * @return full device name or null if this element does not represent device
	 */
	String getDeviceName();
	
	/**
	 * Returns name of component's bundle  
	 * @return bundle name or empty string if component has no bundle 
	 */
	String getBundleName();
	
	/**
	 * Returns version match mode to be used when resolving the item (component, api or pack)
	 * @return version match mode as EVersionMatchMode value 
	 */
	EVersionMatchMode getVersionMatchMode();

	/**
	 * Sets version match mode that should be use when resolving the item 
	 * @param mode version match mode to set
	 */
	void setVersionMatchMode(EVersionMatchMode mode);
	

	/**
	 * Checks if fixed version match mode to be used when resolving the item 
	 * @return true if fixed version match mode is to be used 
	 */
	boolean isVersionFixed();

	/**
	 * Returns absolute path of supplied relative one, if supplied path is an URL or absolute, returns it 
	 * @param relPath path to convert to absolute
	 * @return absolute path
	 */
	String getAbsolutePath(String relPath);

	
	/**
	 * Return collection of documents associated with the device or board (items with "book" tag) 
	 * @return collection of ICpItem objects representing books  
	 */
	Collection<ICpItem> getBooks();
	
}
