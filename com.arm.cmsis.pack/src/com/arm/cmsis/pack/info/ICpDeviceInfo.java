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

import com.arm.cmsis.pack.data.ICpDeviceItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.enums.IEvaluationResult;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;

/**
 *  Interface representing device used  in the configuration
 */
public interface ICpDeviceInfo extends ICpItemInfo, IEvaluationResult {

	/**
	 * Returns actual device represented by this info 
	 * @return actual device
	 */
	ICpDeviceItem getDevice(); 
	
	/**
	 * Sets actual device to this info
	 * @param device actual device to set
	 */
	void setDevice(ICpDeviceItem device);

	/**
	 * Sets actual device to this info using supplied IRteDeviceItem
	 * @param IRteDeviceItem to access actual device
	 */
	void setRteDevice(IRteDeviceItem device);

	
	/**
	 * Returns effective properties of the first device stored in the item or properties with associated processor
	 * @return effective device properties if any 
	 * @see #getProcessorName()
	 */
	ICpItem getEffectiveProperties();


	/**
	 * Returns brief device description that includes core, clock and memory 
	 * @return brief device description
	 */
	String getSummary();
	
	/**
	 * Returns brief clock description 
	 * @return brief clock description
	 */
	String getClockSummary();
	
	/**
	 * Returns brief description of device memory 
	 * @return brief memory description
	 */
	String getMemorySummary();
}
