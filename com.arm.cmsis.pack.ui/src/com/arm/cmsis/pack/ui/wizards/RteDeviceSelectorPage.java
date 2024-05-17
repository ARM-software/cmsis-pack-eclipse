/*******************************************************************************
* Copyright (c) 2022 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.ui.wizards;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

import com.arm.cmsis.pack.info.ICpBoardInfo;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.rte.boards.IRteBoardItem;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.CpStringsUI;
import com.arm.cmsis.pack.ui.IStatusMessageListener;
import com.arm.cmsis.pack.ui.widgets.RteDeviceSelectorWidget;

/**
 * Wizard page that wraps device selector widget
 */
public class RteDeviceSelectorPage extends WizardPage implements IStatusMessageListener {

    private RteDeviceSelectorWidget fDeviceWidget = null;
    private IRteDeviceItem fDevices = null;
    private IRteBoardItem fBoards = null;
    private ICpDeviceInfo fDeviceInfo = null;
    private ICpBoardInfo fBoardInfo = null;
    private boolean fbInitialized = false;
    protected boolean fbShowProcessors = true;

    /**
     * @wbp.parser.constructor
     */
    public RteDeviceSelectorPage() {
        this(true);
    }

    public RteDeviceSelectorPage(boolean bShowProcessors) {
        this(CpStringsUI.RteDeviceWizard_PageName, CpStringsUI.RteDeviceWizard_SelectDevice,
                CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_CHIP_48), bShowProcessors);
    }

    /**
     * @param pageName
     * @param title
     * @param titleImage
     */
    public RteDeviceSelectorPage(String pageName, String title, ImageDescriptor titleImage, boolean bShowProcessors) {
        super(pageName, title, titleImage);
        fbShowProcessors = bShowProcessors;
        setPageComplete(false);
    }

    @Override
    public void createControl(Composite parent) {
        fDeviceWidget = new RteDeviceSelectorWidget(parent, fbShowProcessors);
        fDeviceWidget.addListener(this);
        fDeviceWidget.setDevices(fDevices);
        fDeviceWidget.setBoards(fBoards);
        setControl(fDeviceWidget);
    }

    /**
     * Returns internal device tree
     *
     * @return the devices
     */
    public IRteDeviceItem getDevices() {
        return fDevices;
    }

    /**
     * Assigns device tree
     *
     * @param devices the devices to set
     */
    public void setDevices(IRteDeviceItem devices) {
        fDevices = devices;
        if (fDeviceWidget != null) {
            fDeviceWidget.setDevices(fDevices);
        }
    }

    /**
     * Assigns boards tree
     *
     * @param boards the boards to set
     */
    public void setBoards(IRteBoardItem boards) {
        fBoards = boards;
        if (fDeviceWidget != null) {
            fDeviceWidget.setBoards(fBoards);
        }
    }

    @Override
    public void setVisible(boolean visible) {
        if (!fbInitialized) {
            fDeviceWidget.setDeviceInfo(fDeviceInfo);
            fDeviceWidget.setBoardInfo(fBoardInfo);
            fbInitialized = true;
        }
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

    public void updateStatus(String message) {
        setErrorMessage(message);
        if (message == null || message.isEmpty()) {
            if (fbInitialized) {
                fDeviceInfo = fDeviceWidget.getDeviceInfo();
                fBoardInfo = fDeviceWidget.getBoardInfo();
            }
            setPageComplete(fDeviceInfo != null);
        } else
            setPageComplete(false);

    }

    /**
     * Returns selected device if any
     *
     * @return the selected device
     */
    public IRteDeviceItem getDevice() {
        if (fDeviceWidget != null) {
            return fDeviceWidget.getSelectedDeviceItem();
        }
        return null;
    }

    /**
     * Returns selected device info
     *
     * @return
     */
    public ICpDeviceInfo getDeviceInfo() {
        if (fbInitialized) {
            fDeviceInfo = fDeviceWidget.getDeviceInfo();
        }
        return fDeviceInfo;
    }

    /**
     * Returns selected board info
     *
     * @return
     */
    public ICpBoardInfo getBoardInfo() {
        if (fbInitialized) {
            fBoardInfo = fDeviceWidget.getBoardInfo();
        }
        return fBoardInfo;
    }

    /**
     * Makes initial device selection
     *
     * @param deviceInfo ICpDeviceInfo to make initial selection
     */
    public void setDeviceInfo(ICpDeviceInfo deviceInfo) {
        fDeviceInfo = deviceInfo;
        if (fDeviceWidget != null) {
            fDeviceWidget.setDeviceInfo(deviceInfo);
        }
    }

    /**
     * Makes initial board selection
     *
     * @param boardInfo ICpBoardInfo to make initial selection
     */
    public void setBoardInfo(ICpBoardInfo boardInfo) {
        fBoardInfo = boardInfo;
        if (fDeviceWidget != null) {
            fDeviceWidget.setBoardInfo(boardInfo);
        }
    }
}
