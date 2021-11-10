/*******************************************************************************
* Copyright (c) 2021 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.ui.wizards;

import org.eclipse.jface.wizard.Wizard;

import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;

/**
 * Wizard to select a device for configuration
 */
public class RteDeviceSelectorWizard extends Wizard {

    private RteDeviceSelectorPage fDevicePage;
    private IRteDeviceItem fDevices;
    private ICpDeviceInfo fDeviceInfo;

    public RteDeviceSelectorWizard(String name, IRteDeviceItem devices, ICpDeviceInfo deviceInfo) {
        fDevices = devices;
        fDeviceInfo = deviceInfo;
        setWindowTitle(name);
    }

    public ICpDeviceInfo getDeviceInfo() {
        return fDeviceInfo;
    }

    public void setDeviceInfo(ICpDeviceInfo deviceInfo) {
        fDeviceInfo = deviceInfo;
    }

    @Override
    public boolean performFinish() {
        fDeviceInfo = fDevicePage.getDeviceInfo();
        return fDeviceInfo != null;
    }

    @Override
    public void addPages() {
        fDevicePage = new RteDeviceSelectorPage();
        fDevicePage.setDevices(fDevices);
        fDevicePage.setDeviceInfo(fDeviceInfo);
        addPage(fDevicePage);
    }

}
