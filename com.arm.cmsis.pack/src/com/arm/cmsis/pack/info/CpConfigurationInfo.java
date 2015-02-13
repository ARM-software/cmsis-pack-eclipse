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

import com.arm.cmsis.pack.data.CpItem;
import com.arm.cmsis.pack.data.ICpItem;

/**
 *
 */
public class CpConfigurationInfo extends CpItem implements ICpConfigurationInfo {

	/**
	 * @param parent
	 */
	public CpConfigurationInfo() {
		super(null, "configuration");
	}

	@Override
	protected ICpItem createChildItem(String tag) {
		return createChildItem(this, tag);
	}
	
	
	public static ICpItem createChildItem(ICpItem parent, String tag) {
		switch(tag) {
		case "component":
		case "api":
			return new CpComponentInfo(parent, tag);
		case "device":
			return new CpDeviceInfo(parent, tag);
		case "package":
			return new CpPackInfo(parent, tag);
		case "file":
			return new CpFileInfo(parent, tag);
		default:
			break;
		}
		return new CpItem(parent, tag);
	}

	
	@Override
	public ICpDeviceInfo getDeviceInfo() {
		return (ICpDeviceInfo)getFirstChild("device");
	}

	@Override
	public ICpItem getToolchainInfo() {
		return getFirstChild("toolchain");
	}

	
	
}
