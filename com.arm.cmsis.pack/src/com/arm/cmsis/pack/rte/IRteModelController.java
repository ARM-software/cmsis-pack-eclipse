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

package com.arm.cmsis.pack.rte;

import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.enums.EVersionMatchMode;
import com.arm.cmsis.pack.events.IRteController;
import com.arm.cmsis.pack.info.ICpConfigurationInfo;
import com.arm.cmsis.pack.rte.components.IRteComponentItem;
import com.arm.cmsis.pack.rte.packs.IRtePack;
import com.arm.cmsis.pack.rte.packs.IRtePackCollection;
import com.arm.cmsis.pack.rte.packs.IRtePackFamily;

/**
 *  Interface represents a  controller to edit underlying RTE Configuration.</br>
 *  The interface also serves as a proxy to IRteModel to simplify its usage   
 */
public interface IRteModelController extends IRteController, IRteModel{

	/**
	 * Returns controllable RTE model 
	 * @return underlying IRteModel  
	 */
	IRteModel getModel();
	
	@Override
	default ICpConfigurationInfo getDataInfo() {
		return getConfigurationInfo();
	}

	@Override
	default void setDataInfo(ICpItem info) {
		if(info instanceof ICpConfigurationInfo)
			setConfigurationInfo((ICpConfigurationInfo)info);
		else
			setConfigurationInfo(null);
	}

	@Override
	default void updateDataInfo() {
		updateConfigurationInfo();
	}

	/**
	 * Returns RTE pack collection
	 * @return IRtePackCollection
	 */
	IRtePackCollection getRtePackCollection();

	
	/**
	 * Check if the component selection in the model has been modified since last load/apply
	 * @return true if modified 
	 */
	boolean isComponentSelectionModified();
	
	/**
	 * Checks if pack filter is modified since lase commit
	 * @return true if modified 
	 */
	boolean isPackFilterModified(); 
	
	/**
	 * Checks if device has been changed or modified 
	 * @return true if modified
	 */
	boolean isDeviceModified();
	
	/**
	 * Updates configuration info based on current selection  
	 */
	void updateConfigurationInfo();

	/**
	 *  Fully reloads packs and updates loaded configuration 
	 */
	void reloadPacks();
	
	/**
	 * Sets active variant or bundle for given parent item.
	 * <br> 
	 * If selection state has changed re-evaluates dependencies and emits notification  
	 * @param item for which to set active variant.
	 * @param variant new active variant name to set
	 * @see IRteComponentItem#setActiveVariant(String)
	 */
	void selectActiveVariant(IRteComponentItem item, final String variant);
	
	/**
	 * Sets active vendor for given parent item.
	 * <br> 
	 * If selection state has changed re-evaluates dependencies and emits notification  
	 * @param item for which to set active variant.
	 * @param variant new active vendor name to set
	 * @see IRteComponentItem#setActiveVendor(String)
	 */
	void selectActiveVendor(IRteComponentItem item, final String vendor);

	/**
	 * Sets active version for given parent item.
	 * <br> 
	 * If selection state has changed re-evaluates dependencies and emits notification  
	 * @param item for which to set active version.
	 * @param variant new active version name to set
	 * @see IRteComponentItem#setActiveVersion(String)
	 */
	void selectActiveVersion(IRteComponentItem item, final String version);

	
	/**
	 * Tries to resolve component dependencies
	 * @return evaluation result after dependency resolving 
	 */
	EEvaluationResult resolveComponentDependencies();
	
	
	/**
	 * Explicitly selects specified pack 
	 * @param pack IRtePack to select
	 * @param select selection flag
	 */
	void selectPack(IRtePack pack, boolean select);


	/**
	 * Explicitly sets version match mode to specified pack family 
	 * @param packFamily pack family to set mode to
	 * @param mode EVersionMatchMode to set
	 */
	void setVesrionMatchMode(IRtePackFamily packFamily, EVersionMatchMode mode);

	/**
	 * Check is to latest versions of all installed packs 
	 * @return true if the latest versions of packs should be used
	 */
	boolean isUseAllLatestPacks();
	
	/**
	 * Sets if the model should use the latest versions of all installed packs 
	 * @param bUseLatest flag if to use latest 
	 */
	void setUseAllLatestPacks(boolean bUseLatest);

	/**
	 * Sets flag to show only used packs in Packs editor page
	 * @param bShowUsed flag to set
	 */
	void setShowUsedPacksOnly(boolean bShowUsed);
	
	/**
	 * Checks if Packs editor page should only show used packs
	 * @return true if only used packs should be shown
	 */
	boolean isShowUsedPacksOnly();
	
	
}
