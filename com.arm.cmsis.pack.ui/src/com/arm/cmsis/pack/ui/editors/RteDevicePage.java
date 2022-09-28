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

package com.arm.cmsis.pack.ui.editors;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.info.ICpBoardInfo;
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
        if (getModelController() != null) {
            if (getModelController().isDeviceModified() || getModelController().isBoardModified())
                return true;
        }
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

    @Override
    public void handle(RteEvent event) {
        super.handle(event);
        switch (event.getTopic()) {
        case RteEvent.PACKS_UPDATED:
        case RteEvent.FILTER_MODIFIED:
            refresh(); // Refresh UI without changing configuration
            return;
        default:
            super.handle(event);
        }
    }

    @Override
    public void updateActions() {
        super.updateActions();
    }

    protected void changeDevice() {
        IRteModelController model = getModelController();
        if (model != null) {
            RteDeviceSelectorWizard wizard = new RteDeviceSelectorWizard(CpStringsUI.RteDeviceSelectorPage_SelectDevice,
                    model.getDevices(), model.getDeviceInfo(), model.getBoards(), model.getBoardInfo());
            OkWizardDialog dlg = new OkWizardDialog(getFocusWidget().getShell(), wizard);
            dlg.setPageSize(600, 600); // limit initial size

            if (dlg.open() == Window.OK) {
                ICpDeviceInfo deviceInfo = wizard.getDeviceInfo();
                model.setDeviceInfo(deviceInfo);

                // Get board info from RteDeviceSelectorWizard
                ICpBoardInfo boardInfo = wizard.getBoardInfo();
                // Set board info to model controller (IRteModelController)
                model.setBoardInfo(boardInfo);
            }
        }
    }
}
