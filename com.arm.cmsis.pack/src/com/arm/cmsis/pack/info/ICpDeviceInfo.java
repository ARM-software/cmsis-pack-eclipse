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

package com.arm.cmsis.pack.info;

import com.arm.cmsis.pack.data.ICpDeviceItem;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;

/**
 *
 */
public interface ICpDeviceInfo extends ICpItemInfo {

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
	 * @param IRteDeviceItem to access to actual device
	 */
	void setDevice(IRteDeviceItem device);

}
