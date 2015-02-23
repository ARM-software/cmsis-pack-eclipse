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

import com.arm.cmsis.pack.base.CmsisConstants;
import com.arm.cmsis.pack.data.CpItem;
import com.arm.cmsis.pack.data.ICpDeviceItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;

/**
 * Default implementation of ICpDeviceInfo interface
 */
public class CpDeviceInfo extends CpItem implements ICpDeviceInfo {

	protected ICpDeviceItem fDevice = null;
	protected ICpPackInfo   fPackInfo = null;

	
	/**
	 * Constructs CpDeviceInfo from supplied ICpDeviceItem 
	 * @param parent parent ICpItem
	 * @param device IRteDeviceItem to construct from 
	 */
	public CpDeviceInfo(ICpItem parent, IRteDeviceItem device) {
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
	public void setDevice(IRteDeviceItem device) {
		setDevice(device.getDevice());
		if(fDevice != null) {
			attributes().setAttributes(fDevice.getEffectiveAttributes(null));
			String processorName = device.getProcessorName();
			ICpItem props = fDevice.getEffectiveProperties(processorName);
			if(props != null) {
				ICpItem proc = props.getFirstChild(CmsisConstants.PROCESSOR_TAG);
				if(proc != null) {
					attributes().mergeAttributes(proc.attributes());
				}
				if(processorName != null && !processorName.isEmpty())
					attributes().setAttribute(CmsisConstants.PNAME, processorName);
			}
		}
	}


	@Override
	public void setDevice(ICpDeviceItem device) {
		fDevice = device;
		if(fDevice != null) {
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
