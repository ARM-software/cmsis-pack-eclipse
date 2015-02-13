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

package com.arm.cmsis.pack.rte.components;

import java.util.Collection;

import com.arm.cmsis.pack.base.ICmsisMapItem;
import com.arm.cmsis.pack.data.ICpComponent;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.enums.EComponentAttribute;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.info.ICpComponentInfo;
import com.arm.cmsis.pack.rte.IRteDependency;

/**
 * Base interface for component hierarchy items  
 */
public interface IRteComponentItem extends ICmsisMapItem<IRteComponentItem> {
	
	/**
	 * Create hierarchy level for given component  
	 * @param ICpComponent component to add
	 */
	void addComponent(ICpComponent cpComponent);

	/**
	 * Add IcpItem (API or taxonomy) to already created component hierarchy.<br>
	 * In contrast to addComponent() this function does not add new hierarchy items .   
	 * @param cpItem item to add
	 * @see #addComponent(ICpComponent)   
	 */
	void addCpItem(ICpItem cpItem);
	
	
	/**
	 * Returns ICpItem associated with this item (bundle, component or API)
	 * @return ICpItem associated with this item if any
	 */
	ICpItem getCpItem();
	
	/**
	 * Returns IcpItem of this item if not null, or from an active child 
	 * @return active IcpItem if any 
	 */
	ICpItem getActiveCpItem();

	/**
	 * Returns active component entity of this item if any  
	 * @return ICpComponent object if any or null
	 */
	ICpComponent getActiveCpComponent();
	
	/**
	 * Returns active component info of this item if any is assigned  
	 * @return ICpComponentInfo object if any or null
	 */
	ICpComponentInfo getActiveCpComponentInfo();
	
	/**
	 * Returns active api version of this item if any  
	 * @return ICpApi object if found or null
	 */
	ICpComponent getApi();

	/**
	 * Returns taxonomy associated with this item
	 * @return taxonomy ICpItem or null if item has no taxonomy
	 */
	ICpItem getTaxonomy();
	
	
	/**
	 * Check is component (child or parent) is selected  
	 * @return true if component is selected
	 */
	boolean isSelected();
	
	/**
	 * Returns if this item is active and all parents are active
	 * @return true if active 
	 */
	boolean isActive();
	
	/**
	 * Returns an active child (bundle, variant, vendor or version )
	 * @return active child name or null for non-exclusive items 
	 */
	IRteComponentItem getActiveChild();
	
	/**
	 * Returns name of an active child (bundle, vendor or version )
	 * @return active child name or null for non-exclusive items 
	 */
	String getActiveChildName();

	/**
	 * Sets active child for this item.
	 * Does nothing for non-exclusive items 
	 * @param name child name to set active, if equals to default name, the first child in the collection is set active
	 * @return true if active child has changed
	 * @see #getSpecialChildName()
	 */
	boolean setActiveChild(final String name);
	
	/**
	 * Returns implicit child name 
	 * The name does not need to be an actual key, but a symbolic one, for instance component version can have "latest" special name  
	 * @return special string name, otherwise null
	 */
	String getImplicitChildName();

	/**
	 * Check if the active child is selected using implicit name 
	 * @return true if active child is implicitly selected
	 */
	boolean isActiveChildImplicit();
	

	/**
	 * Returns component attribute associated with this level of component hierarchy 
	 * @return component attribute associated with child key 
	 */
	EComponentAttribute getKeyAttribute();
	
	/**
	 * Returns component attribute as string representing child key ("Cclass", "Cbundle", "Cgroup", etc.) 
	 * @return component attribute key associated with child key 
	 */
	String getKeyAttributeString();
	
	/**
	 * Returns value of key attribute taken from supplied attributes 
	 * @return component attribute value corresponding key attribute 
	 */
	String getKeyAttributeValue(IAttributes attributes);

	/**
	 * Matches supplied pattern to key of a child item, usually represent a value of key attribute    
	 * @param pattern pattern to match
	 * @param key child key to match to 
	 * @return true if supplied pattern matches supplied child key
	 * @see getKeyAttribute() 
	 */
	boolean matchKey(String pattern, String key);
	

	/**
	 * Returns active child or grand-child that has ICpItem  
	 * That is could be group (can have API), bundle version or component version 
	 * @return active item that has associated ICpItem 
	 */
	IRteComponentItem getActiveItem();
	
	/**
	 * Returns active variant name (bundle name for class)
	 * @return active variant 
	 */
	String getActiveVariant();

	/**
	 * Returns active vendor string
	 * @return active vendor
	 */
	String getActiveVendor();
	
	/**
	 * Returns active version string
	 * @return active version
	 */
	String getActiveVersion();
	
	
	/**
	 * Sets active item variant
	 * @param variant variant to set
	 */
	void setActiveVariant(String variant);
	
	/**
	 * Sets active item vendor
	 * @param vendor version to set
	 */
	void setActiveVendor(String vendor);	

	/**
	 * Sets active item version
	 * @param variant version to set
	 */
	void setActiveVersion(String version);	
	
	
	/**
	 * Returns list of component variant names ( bundle names for class)  
	 * @return list of variant/bundle names
	 */
	Collection<String> getVariantStrings();
	
	/**
	 * Returns list of component/bundle vendors   
	 * @return list of component/bundle vendors
	 */
	Collection<String> getVendorStrings();
	
	/**
	 * Returns list of component/bundle versions   
	 * @return list of component/bundle versions
	 */
	Collection<String> getVersionStrings();

	
	/**
	 * Checks if component or bundle is configured to use the latest version
	 * @return if latest version is to be used 
	 */	
	boolean isUseLatestVersion();
	

	/**
	 * Checks if component or bundle is configured to use any vendor
	 * @return if any vendor is to be used 
	 */	
	boolean isUseAnyVendor();

	
	/**
	 * Returns parent component item in the hierarchy chain  
	 * @return component item
	 */
	IRteComponent getParentComponent();

	
	/**
	 * Returns component group item that is in the parent chain of this item  
	 * @return component group item
	 */
	IRteComponentGroup getParentGroup();

	/**
	 * Returns component class item that is in the parent chain of this item  
	 * @return component class item
	 */
	IRteComponentClass getParentClass();


	/**
	 * Returns component bundle item in the parent hierarchy chain  
	 * @return component bundle item
	 */
	IRteComponentBundle getParentBundle();
	
	/**
	 * Searches for component group in the child hierarchy (default bundle takes precedence) 
	 * @param attributes component attributes to search for
	 * @return component group item
	 */
	IRteComponentGroup getGroup(IAttributes attributes);
	
	/**
	 * Returns collection of currently selected active components
	 * @param components collection to fill, if null the new collection is allocated
	 * @return collection of selected components
	 */
	Collection<IRteComponent> getSelectedComponents(Collection<IRteComponent> components);

	/**
	 * Returns collection of currently used active components (those that have associated ICpComponentInfo)
	 * @param components collection to fill, if null the new collection is allocated
	 * @return collection of used components
	 */
	Collection<IRteComponent> getUsedComponents(Collection<IRteComponent> components);
	
	
	/**
	 * Searches the hierarchy for components matching supplied attributes
	 * @param dependency dependency result  
	 * @return result of search as EEvaluationResult value 
	 */
	EEvaluationResult findComponents(IRteDependency dependency);

}
