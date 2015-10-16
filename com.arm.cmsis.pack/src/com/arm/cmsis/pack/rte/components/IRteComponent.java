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

import com.arm.cmsis.pack.info.ICpComponentInfo;

/**
 * Class that represents component entity that can be selected 
 */
public interface IRteComponent extends IRteComponentItem {

	/**
	 * Sets/resets component selection  
	 * @param count number of instances to select, 0 to reset the component selection 
	 * @return true is selection state has changed 
	 */
	boolean setSelected(int count); 
	
	/**
	 * Returns number of selected instances  
	 * @return number of selected instances
	 */
	int getSelectedCount();
	

	/**
	 * Returns maximum number of instances that can be selected for the component, default is 1
	 * @return maximum number of component instances  
	 */
	int getMaxInstanceCount();
	
	
	/**
	 * Checks if component belongs to bundle
	 * @return true if the component belongs to a bundle
	 */
	boolean hasBundle();
	

	/**
	 * Returns number of used (instantiated) instances  
	 * @return number of used instances
	 */
	int getUseCount();

	
	/**
	 * Sets/updates active component info, purges all non-active ones  
	 * @param ci ICpComponentInfo
	 */
	void setActiveComponentInfo(ICpComponentInfo ci);
	
	
}
