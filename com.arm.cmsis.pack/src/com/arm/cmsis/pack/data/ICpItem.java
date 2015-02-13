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

package com.arm.cmsis.pack.data;

import java.util.Collection;
import java.util.Map;

import com.arm.cmsis.pack.base.ICmsisTreeItem;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.enums.EVersionMatchMode;
import com.arm.cmsis.pack.generic.IAttributedItem;

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
	 * Returns children of a child with specified tag 
	 * @param tag child's tag
	 * @return collection of child's children or null if no child is found 
	 */
	Collection<? extends ICpItem> getChildren(final String tag);
	
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
	 * Checks that only a pack or a component from certain vendor should be used when resolving an item 
	 * @return true if only certain vendor should be considered 
	 */
	boolean isVendorFixed();
	
	/**
	 * Sets flag if only a pack or a component from certain vendor should be used when resolving an item  
	 * @param fixed true if fixed vendor should be used 
	 */
	void setVendorFixed(boolean fixed);
	
}
