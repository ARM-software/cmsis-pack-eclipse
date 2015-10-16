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

import java.util.Collection;
import java.util.Set;

import com.arm.cmsis.pack.enums.EVersionMatchMode;

/**
 *  Represents RTE view on a ICpPackFamily 
 */
public interface IRtePackFamily  extends IRtePackItem {

	/**
	 * Returns child IRtePack for given version 
	 * @param version requested pack version or null to get the latest  
	 * @return IRtePack for given packId 
	 */
	IRtePack getRtePack(String version);

	/**
	 * Returns latest IRtePack  
	 * @return latest IRtePack in the family 
	 */
	IRtePack getLatestRtePack();
	
	
	/**
	 * Returns set of selected pack versions 
	 * @return set of selected pack versions   
	 */
	Set<String> getSelectedVersions();
	
	/**
	 * Returns set of selected pack versions 
	 * @return set of selected pack versions   
	 */
	Collection<IRtePack> getSelectedPacks();

	/**
	 * Sets version match mode that should be use when resolving the packs in family 
	 * @param mode version match mode to set
	 */
	void setVersionMatchMode(EVersionMatchMode mode);
	

	/**
	 * Updates family version match mode according to selection of packs 
	 */
	void updateVersionMatchMode();
	
	
}
