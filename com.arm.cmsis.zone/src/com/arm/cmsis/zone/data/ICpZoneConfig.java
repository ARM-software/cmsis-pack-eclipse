/*******************************************************************************
* Copyright (c) 2019 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.zone.data;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;

/**
 * Interface for a zone configuration element
 */
public interface ICpZoneConfig extends ICpZoneItem {

	/**
	 * Returns the actual ICpZoneConfig item 
	 * @return ICpZoneConfig, may not be null
	 */
	ICpZoneConfig getZoneConfig();

	/**
	 * Retrieves zone mode string
	 * @return mode string
	 */
	default String getZoneMode() { 
		return getZoneConfig().attributes().getAttribute(CmsisConstants.MODE, CmsisConstants.PROJECT);
	}
	
	/**
	 * Sets zone mode 
	 * @param mode zone mode to set
	 * @return true if mode changed 
	 */
	default boolean setZoneMode(String mode) {
		return getZoneConfig().updateAttribute(CmsisConstants.MODE, mode);
	}
	
	/**
	 * Checks if the zone has MPU mode  
	 * @return true if mode == "MPU"
	 */
	default boolean isZoneModeMPU() { return CmsisConstants.MPU.equals(getZoneMode());}
	
	/**
	 * Checks if the zone has Project mode  
	 * @return true if mode == "project"
	 */
	default boolean isZoneModeProject() { return CmsisConstants.PROJECT.equals(getZoneMode());}

	/**
	 * Gets zone option value
	 * @param type option type string
	 * @param key flag to change
	 * @return option value as boolean flag
	 */
	default boolean getZoneOption(String type, String key) {
		return getZoneConfig().getZoneOption(type, key);
	}

	
	/**
	 * Sets zone option flag
	 * @param type option type string
	 * @param key flag to change
	 * @param value flag value
	 * @return true if modified
	 */
	default boolean setZoneOption(String type, String key, boolean value) {
		return getZoneConfig().setZoneOption(type, key, value);
	}


	
	/**
	 * Returns ICpItem for given option type  
	 * @param type option type string 
	 * @return ICpItem or null
	 */
	default ICpItem getZoneConfigOptionItem(String type) {
		return getZoneConfig().getFirstChild(type);
	};
	
	
}
