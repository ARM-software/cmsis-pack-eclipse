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

package com.arm.cmsis.pack.ui.wizards;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.CpStringsUI;
import com.arm.cmsis.pack.ui.IStatusMessageListener;
import com.arm.cmsis.pack.ui.widgets.RteDeviceSelectorWidget;

/**
 * Wizard page that wraps device selector widget
 */
public class RteDeviceSelectorPage extends WizardPage implements IStatusMessageListener  {

	private RteDeviceSelectorWidget fDeviceWidget = null;
	private IRteDeviceItem fDevices = null;
	private ICpDeviceInfo fDeviceInfo = null;
	private boolean fbInitialized = false;

	public RteDeviceSelectorPage() {
		this(CpStringsUI.RteDeviceWizard_PageName, CpStringsUI.RteDeviceWizard_SelectDevice, CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_DEVICE_48));
	}

	/**
	 * @param pageName
	 * @param title
	 * @param titleImage
	 */
	public RteDeviceSelectorPage(String pageName, String title,	ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
		setPageComplete(false);
	}

	@Override
	public void createControl(Composite parent) {
		fDeviceWidget = new RteDeviceSelectorWidget(parent);
		fDeviceWidget.addListener(this);
		fDeviceWidget.setDevices(fDevices);
		
		setControl(fDeviceWidget);
		updateStatus(CpStringsUI.RteDeviceSelectorPage_SelectDevice);
	}

	/**
	 * Returns internal device tree
	 * @return the devices
	 */
	public IRteDeviceItem getDevices() {
		return fDevices;
	}

	/**
	 * Assigns device tree 
	 * @param devices the devices to set
	 */
	public void setDevices(IRteDeviceItem devices) {
		fDevices = devices;
		if(fDeviceWidget != null) {
			fDeviceWidget.setDevices(fDevices);
		}
	}
	
	@Override
	public void setVisible(boolean visible) {
		fDeviceWidget.setDeviceInfo(fDeviceInfo);
		fbInitialized = true;
		super.setVisible(visible);
	}

	@Override
	public void dispose() {
		super.dispose();
		fDevices = null;
		fDeviceWidget = null;
		fDeviceInfo = null;
	}

	@Override
	public void handle(String message) {
		updateStatus(message);
	}
	
	protected void updateStatus(String message) {
		setErrorMessage(message);
		if(fbInitialized )
			fDeviceInfo = fDeviceWidget.getDeviceInfo();
		setPageComplete(fDeviceInfo!= null);
	}
	
	/**
	 * Returns selected device if any
	 * @return the selected device
	 */
	public IRteDeviceItem getDevice() {
		if(fDeviceWidget != null)
			return fDeviceWidget.getSelectedDeviceItem();
		return null;
	}

	/**
	 * Returns selected device info
	 * @return
	 */
	public ICpDeviceInfo getDeviceInfo() {
		if(fbInitialized )
			fDeviceInfo = fDeviceWidget.getDeviceInfo();
		return fDeviceInfo;
	}

	/**
	 * Makes initial device selection 
	 * @param deviceInfo ICpDeviceInfo to make initial selection 
	 */
	public void setDeviceInfo(ICpDeviceInfo deviceInfo) {
		fDeviceInfo = deviceInfo;
		if(fDeviceWidget != null)
			fDeviceWidget.setDeviceInfo(deviceInfo);
	}
}
