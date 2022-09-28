/*******************************************************************************
* Copyright (c) 2022 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.project.template;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.ui.templateengine.IWizardDataPage;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackManager;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.project.IHelpContextIds;
import com.arm.cmsis.pack.project.Messages;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;
import com.arm.cmsis.pack.ui.wizards.RteDeviceSelectorPage;

/**
 * Device selector page for new project wizard
 */
public class RteTemplateDeviceSelectorPage extends RteDeviceSelectorPage implements IWizardDataPage {

    protected IWizardPage next;

    public RteTemplateDeviceSelectorPage() {
        super(true);
    }

    @Override
    public Map<String, String> getPageData() {
        Map<String, String> data = null;

        ICpDeviceInfo deviceInfo = getDeviceInfo();
        if (deviceInfo != null) {
            data = deviceInfo.attributes().getAttributesAsMap();
            RteProjectTemplate.setSelectedDeviceInfo(deviceInfo);
        }
        RteProjectTemplate.setSelectedBoardInfo(getBoardInfo());

        if (data == null) {
            data = new HashMap<>();
        }
        return data;
    }

    @Override
    public void createControl(Composite parent) {

        ICpPackManager packManager = CpPlugIn.getPackManager();
        if (packManager == null) {
            updateStatus(Messages.RteTemplateDeviceSelectorPage_NoPackManagerIsAvailble);
            return;
        }

        IRteDeviceItem devices = packManager.getDevices();
        setDevices(devices);
        setBoards(packManager.getRteBoards());

        // always clear the device and board info in a new wizard
        RteProjectTemplate.setSelectedBoardInfo(null);
        setBoardInfo(RteProjectTemplate.getSelectedBoardInfo());

        RteProjectTemplate.setSelectedDeviceInfo(null);
        // this will update status
        setDeviceInfo(RteProjectTemplate.getSelectedDeviceInfo());

        super.createControl(parent);
        if (devices == null || !devices.hasChildren()) {
            updateStatus(Messages.RteTemplateDeviceSelectorPage_NoDevicesAreAvailable);
        } else if (getWizard() instanceof IDeviceInfoProvider) { // the wizard already provides the device info
            IDeviceInfoProvider dp = (IDeviceInfoProvider) getWizard();
            setDeviceInfo(dp.getDeviceInfo());
        }

        // add context-sensitive help
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IHelpContextIds.CMSIS_DEVICE_SELECT_WIZARD);
    }

    @Override
    public void setNextPage(IWizardPage next) {
        this.next = next;
    }

    @Override
    public IWizardPage getNextPage() {
        if (next != null) {
            return next;
        }
        return super.getNextPage();
    }

    @Override
    public void handle(String message) {
        updateStatus(message);
    }

    @Override
    public void updateStatus(String message) {
        setErrorMessage(message);
        if (message == null || message.isEmpty())
            setPageComplete(true);
        else
            setPageComplete(false);
    }
}
