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

package com.arm.cmsis.pack.rte.components;

import java.util.Collection;

import com.arm.cmsis.pack.data.ICpComponent;
import com.arm.cmsis.pack.data.ICpGenerator;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.enums.EComponentAttribute;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.info.ICpComponentInfo;
import com.arm.cmsis.pack.item.ICmsisMapItem;
import com.arm.cmsis.pack.rte.dependencies.IRteDependency;

/**
 * Base interface for component hierarchy items  
 */
public interface IRteComponentItem extends ICmsisMapItem<IRteComponentItem> {
	
	/**
	 * Adds a component to the hierarchy  
	 * @param cpComponent ICpComponent or ICpComponentInfo object to add 
	 * @param flags COMPONENT_IGNORE_* flags describing how to resolve components when adding ICpComponentInfo objects to the hierarchy
	 * @see RteConstants 
	 */
	void addComponent(ICpComponent cpComponent, int flags);

	/**
	 * Adds a component to the hierarchy  
	 * @param cpComponent ICpComponent object to add 
	 */
	void addComponent(ICpComponent cpComponent);

	
	/**
	 * Add ICpItem (API or taxonomy) to already created component hierarchy.<br>
	 * In contrast to addComponent() this function does not add new hierarchy items .   
	 * @param cpItem item to add
	 * @see #addComponent(ICpComponent, int)   
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
	 * Returns default child name 
	 * The name does not need to be an actual key, but a symbolic one, for instance component version can have "latest" special name  
	 * @return special string name, otherwise null
	 */
	String getDefaultChildName();

	/**
	 * Check if the active child is selected using default name 
	 * @return true if active child is default one
	 */
	boolean isActiveChildDefault();
	

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
	 * @see #getKeyAttribute() 
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
	 * Returns default version  
	 * @return default version
	 */
	String getDefaultVersion();

	
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
	 * Returns collection of currently used active components (those that have associated {@link ICpComponentInfo})
	 * @param components collection to fill, if null the new collection is allocated
	 * @return collection of {@link IRteComponent} components
	 */
	Collection<IRteComponent> getUsedComponents(Collection<IRteComponent> components);

	/**
	 * Returns collection of components associated with given generator, includes generated and bootstrap ones
	 * @param generatorId {@link ICpGenerator} id, if null all generator-related components are returned 
	 * @param components collection to fill, if null the new collection is allocated
	 * @return collection of {@link IRteComponent} components
	 */
	Collection<IRteComponent> getGeneratorComponents(String generatorId, Collection<IRteComponent> components);

	
	/**
	 * Searches the hierarchy for components matching supplied criteria
	 * @param dependency {@link IRteDependency} describing search criteria and accumulating results 
	 * @return result of search as EEvaluationResult value 
	 */
	EEvaluationResult findComponents(IRteDependency dependency);

	
	/**
	 * Returns string key used by parent to insert into map<br>
	 * Standard component representing component hierarchy ICpItems items must return getName()
	 * Artificial item might return a different string to allow desirable sorting
	 * @return item key 
	 */
	String getKey();

}
