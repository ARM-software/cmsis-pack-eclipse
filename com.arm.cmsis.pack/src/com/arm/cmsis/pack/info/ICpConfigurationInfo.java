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

package com.arm.cmsis.pack.info;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPackFilter;

/**
 * Interface representing root element of instantiated CMSIS pack data 
 */
public interface ICpConfigurationInfo extends ICpItem {
	
	/**
	 * Returns device info stored in the configuration
	 * @return ICpDeviceInfo stored in the configuration
	 */
	ICpDeviceInfo getDeviceInfo();
	
	
	/**
	 * Returns toolchain information as generic IcpItem with "Tcompiler" and "Toutput" attributes
	 * @return ICpItem describing toolchain info 
	 */
	ICpItem getToolChainInfo();
	
	/**
	 * Return item that is parent of components tems 
	 * @return ICpItem owning ICpComponentInfo items
	 */
	ICpItem getComponentsItem();
	
	/**
	 * Return item that is parent of api items 
	 * @return ICpItem owning ICpComponentInfo items representing APIs
	 */
	ICpItem getApisItem();

	/**
	 * Returns stored pack filter info if any
	 * @return ICpPackFilterInfo
	 */
	ICpPackFilterInfo getPackFilterInfo();
	
	/**
	 * Creates pack filter based on information stored in the info 
	 * @return ICpPackFilter
	 */
	ICpPackFilter createPackFilter();
}
