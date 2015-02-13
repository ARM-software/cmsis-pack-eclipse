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

package com.arm.cmsis.pack.wizards;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;
import com.arm.cmsis.pack.widgets.RteDeviceSelectorWidget;

/**
 *
 */
public class RteDeviceSelectorPage extends WizardPage implements RteDeviceSelectorWidget.IStatusListener  {

	private RteDeviceSelectorWidget deviceWidget = null;
	private IRteDeviceItem devices = null;
	private ICpDeviceInfo fDeviceInfo = null;
	private boolean fbInitialized = false;
	/**
	 * @param pageName
	 */
	public RteDeviceSelectorPage(String pageName, IRteDeviceItem devices) {
		super(pageName);
		setDevices(devices);
	}

	/**
	 * @param pageName
	 * @param title
	 * @param titleImage
	 */
	public RteDeviceSelectorPage(String pageName, String title,
			ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	@Override
	public void createControl(Composite parent) {
		deviceWidget = new RteDeviceSelectorWidget(parent, SWT.NONE);
		deviceWidget.addListener(this);
		deviceWidget.setDevices(devices);
		
		setControl(deviceWidget);
		updateStatus("Select device");
	}

	/**
	 * Returns internal device tree
	 * @return the devices
	 */
	public IRteDeviceItem getDevices() {
		return devices;
	}

	/**
	 * Assigns device tree 
	 * @param devices the devices to set
	 */
	public void setDevices(IRteDeviceItem devices) {
		this.devices = devices;
		if(deviceWidget != null) {
			deviceWidget.setDevices(devices);
		}
	}
	
	@Override
	public void setVisible(boolean visible) {
		deviceWidget.setDeviceInfo(fDeviceInfo);
		fbInitialized = true;
		super.setVisible(visible);
	}

	@Override
	public void dispose() {
		super.dispose();
		devices = null;
		deviceWidget = null;
	}

	@Override
	public void updateStatus(String message) {
		setErrorMessage(message);
		if(fbInitialized )
			fDeviceInfo = deviceWidget.getDeviceInfo();
		setPageComplete(fDeviceInfo!= null);
	}
	
	/**
	 * Returns selected device if any
	 * @return the selected device
	 */
	public IRteDeviceItem getDevice() {
		if(deviceWidget != null)
			return deviceWidget.getSelectedDeviceItem();
		return null;
	}

	/**
	 * Returns selected device info
	 * @return
	 */
	public ICpDeviceInfo getDeviceInfo() {
		if(fbInitialized )
			fDeviceInfo = deviceWidget.getDeviceInfo();
		return fDeviceInfo;
	}

	/**
	 * Makes initial device selection 
	 * @param deviceInfo ICpDeviceInfo to make initial selection 
	 */
	public void setInitialSelection(ICpDeviceInfo deviceInfo) {
		fDeviceInfo = deviceInfo;
		if(deviceWidget != null)
			deviceWidget.setDeviceInfo(deviceInfo);
	}
}
