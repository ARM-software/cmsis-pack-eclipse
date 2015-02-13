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
import com.arm.cmsis.pack.data.ICpDeviceItem;
import com.arm.cmsis.pack.data.ICpItem;

/**
 *
 */
public class CpDeviceInfo extends CpItem implements ICpDeviceInfo {

	protected ICpDeviceItem fDevice = null;
	protected ICpPackInfo   fPackInfo = null;

	/**
	 * Constructs CpDeviceInfo from supplied ICpDeviceItem 
	 * @param parent parent ICpItem
	 * @param device ICpDeviceItem to construct from 
	 */
	public CpDeviceInfo(ICpItem parent, ICpDeviceItem device) {
		super(parent, "device");
		setDevice(device);
	}

	
	/**
	 * Default constructor   
	 * @param parent parent ICpItem
	 */
	public CpDeviceInfo(ICpItem parent) {
		super(parent, "device");
	}

	/**
	 * Constructs CpDeviceInfo from parent and tag
	 * @param parent parent ICpItem
	 * @param tag
	 */
	public CpDeviceInfo(ICpItem parent, String tag) {
		super(parent, tag);
	}

	@Override
	public ICpDeviceItem getDevice() {
		return fDevice;
	}


	@Override
	public ICpPackInfo getPackInfo() {
		return fPackInfo;  
	}

	@Override
	public void setDevice(ICpDeviceItem device) {
		fDevice = device;
		if(fDevice != null) {
			attributes().setAttributes(device.getEffectiveAttributes(null));
			fPackInfo = new CpPackInfo(this, device.getPack());
			replaceChild(fPackInfo);			
		} else {
			fPackInfo = null;
			removeAllChildren("package");
		}
	}

	@Override
	public void addChild(ICpItem item) {
		if(item instanceof ICpPackInfo) {
			fPackInfo = (ICpPackInfo)item;
		} 
		super.addChild(item);
	}


	@Override
	public String getName() {
		return getDeviceName(attributes());
	}
	
}
