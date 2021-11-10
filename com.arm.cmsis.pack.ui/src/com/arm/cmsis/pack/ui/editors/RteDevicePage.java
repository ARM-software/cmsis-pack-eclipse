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

package com.arm.cmsis.pack.ui.editors;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.rte.IRteModelController;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.CpStringsUI;
import com.arm.cmsis.pack.ui.IHelpContextIds;
import com.arm.cmsis.pack.ui.widgets.RteDeviceInfoWidget;
import com.arm.cmsis.pack.ui.widgets.RteDeviceInfoWidgetWrapper;
import com.arm.cmsis.pack.ui.widgets.RteWidget;
import com.arm.cmsis.pack.ui.wizards.OkWizardDialog;
import com.arm.cmsis.pack.ui.wizards.RteDeviceSelectorWizard;

/**
 * Editor page that wraps RteManagerWidget
 *
 */
public class RteDevicePage extends RteModelEditorPage {

    public RteDevicePage() {
    }

    @Override
    protected RteWidget<IRteModelController> createContentWidget() {
        return new RteDeviceInfoWidgetWrapper();
    }

    @Override
    protected String getHelpID() {
        return IHelpContextIds.DEVICE_PAGE;
    }

    @Override
    protected Image getImage() {
        return CpPlugInUI.getImage(CpPlugInUI.ICON_DEVICE);
    }

    @Override
    protected String getLabel() {
        return CpStringsUI.RteDevicePage_Device;
    }

    @Override
    public boolean isModified() {
        if (getModelController() != null)
            return getModelController().isDeviceModified();
        return false;
    }

    @Override
    public void createPageContent(Composite parent) {
        RteDeviceInfoWidgetWrapper wrapper = (RteDeviceInfoWidgetWrapper) getContentWidget();
        wrapper.createControl(parent);
        RteDeviceInfoWidget deviceWidget = wrapper.getDeviceInfoWidget();
        deviceWidget.setSelectionAdapter(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                changeDevice();
            }
        });
    }

    protected void changeDevice() {
        IRteModelController model = getModelController();
        if (model != null) {
            RteDeviceSelectorWizard wizard = new RteDeviceSelectorWizard(CpStringsUI.RteDeviceSelectorPage_SelectDevice,
                    model.getDevices(), model.getDeviceInfo());
            OkWizardDialog dlg = new OkWizardDialog(getFocusWidget().getShell(), wizard);
            dlg.setPageSize(600, 400); // limit initial size

            if (dlg.open() == Window.OK) {
                ICpDeviceInfo deviceInfo = wizard.getDeviceInfo();
                // deviceWidget.setDeviceInfo(deviceInfo);
                model.setDeviceInfo(deviceInfo);
            }
        }
    }
}
