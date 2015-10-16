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

package com.arm.cmsis.pack.rte.packs;

import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.enums.EVersionMatchMode;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.generic.ITreeObject;
import com.arm.cmsis.pack.info.ICpPackInfo;

/**
 *  Base interface for RTE view on ICpPack and ICpPackInfo and ICpPackFamily  
 */
public interface IRtePackItem extends ITreeObject {
	
	/**
	 * Returns item ID 
	 * @return item ID  
	 */
	String getId();

	/** 
	 * Returns version string, for Pack ist version, for Pack family - effectively used versions   
	 * @return version string
	 */
	String getVersion();

	/**
	 * Returns pack version mode
	 * @return EVersionMatchMode
	 */
	EVersionMatchMode getVersionMatchMode();

	/**
	 * Check is to latest versions of all installed packs 
	 * @return true if the latest versions of packs should be used
	 */
	boolean isUseAllLatestPacks();
	
	/**
	 *  Adds ICpItem to the item
	 * @param item ICpItem to add (IcpPack, ICpPackInfo or ICpPackFamily )
	 */
	void addCpItem(ICpItem item);

	/**
	 * Returns ICpItem associated with this item
	 * @return ICpItem associated with this item (IcpPack, ICpPackInfo or ICpPackFamily )
	 */
	ICpItem getCpItem();

	
	/**
	 * Returns parent IRtePacktem if any 
	 * @return parent IRtePacktem if any 
	 */
	IRtePackItem getParent();	

	/**
	 * Checks if the item is explicitly or implicitly selected  
	 * @return true if the item is selected  
	 */
	boolean isSelected(); 

	/**
	 * Checks if the pack item is used in an ICpConfigurationInfo  
	 * @return true if the item is used   
	 */
	boolean isUsed(); 

	/**
	 * Checks if all pack corresponding to this item are installed
	 * @return true if installed, false if not
	 */
	boolean isInstalled();

	
	/**
	 * Checks if the pack family is excluded  
	 * @return true if the item is excluded   
	 */
	boolean isExcluded();
	
	/**
	 * Returns URL associated with the item if any
	 * @return URL associated with the item
	 */
	String getUrl();
	
	/** 
	 * Return item description text of the element if any 
	 * @return description or empty string 
	 */
	String getDescription();
	
	/**
	 * Returns corresponding ICpPack if installed
	 * @return ICpPack if installed or null 
	 */
	ICpPack getPack();

	/**
	 * Returns corresponding ICpPackInfo if assigned
	 * @return ICpPack if assigned or null 
	 */
	ICpPackInfo getPackInfo();
	
	/**
	 * Returns pack attributes taken either from underlying pack or ICPackItem 
	 * @returns pack attributes
	 */
	IAttributes getAttributes();

	/**
	 * Returns the first child item 
	 * @return the first child IRtePackItem
	 */
	IRtePackItem getFirstChild();
	
	/**
	 * Returns root pack collection
	 * @return root IRtePackCollection 
	 */
	IRtePackCollection getRoot();

	
	/**
	 * Returns parent pack family if any 
	 * @return IRtePackFamily 
	 */
	IRtePackFamily getFamily();

	
}
