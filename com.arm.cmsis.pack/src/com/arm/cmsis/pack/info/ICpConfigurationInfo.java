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
import com.arm.cmsis.pack.data.ICpItem;

/**
 * Interface representing root element of instantiated CMSIS pack data 
 */
public interface ICpConfigurationInfo extends ICpItem {
	
	/**
	 * Returns device info stored in the configuration info
	 * @return ICpDeviceInfo stored in the configuration
	 */
	ICpDeviceInfo getDeviceInfo();
	
	
	/**
	 * Returns toolchain information as generic IcpItem with "Tcompiler" and "Toutput" attributes
	 * @return ICpItem describing toolchain info 
	 */
	ICpItem getToolchainInfo();
}
