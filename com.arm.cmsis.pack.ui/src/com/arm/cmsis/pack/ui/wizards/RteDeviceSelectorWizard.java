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

import org.eclipse.jface.wizard.Wizard;

import com.arm.cmsis.pack.info.ICpBoardInfo;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.rte.boards.IRteBoardItem;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;

/**
 * Wizard to select a device for configuration
 */
public class RteDeviceSelectorWizard extends Wizard {

    private RteDeviceSelectorPage fDevicePage;
    private IRteDeviceItem fDevices;
    private IRteBoardItem fBoards;
    private ICpDeviceInfo fDeviceInfo;
    private ICpBoardInfo fBoardInfo;

    public RteDeviceSelectorWizard(String name, IRteDeviceItem devices, ICpDeviceInfo deviceInfo, IRteBoardItem boards,
            ICpBoardInfo boardInfo) {
        fDevices = devices;
        fDeviceInfo = deviceInfo;
        fBoards = boards;
        fBoardInfo = boardInfo;
        setWindowTitle(name);
    }

    public ICpDeviceInfo getDeviceInfo() {
        return fDeviceInfo;
    }

    public void setDeviceInfo(ICpDeviceInfo deviceInfo) {
        fDeviceInfo = deviceInfo;
    }

    public ICpBoardInfo getBoardInfo() {
        return fBoardInfo;
    }

    public void setBoardInfo(ICpBoardInfo boardInfo) {
        this.fBoardInfo = boardInfo;
    }

    @Override
    public boolean performFinish() {
        fDeviceInfo = fDevicePage.getDeviceInfo();
        fBoardInfo = fDevicePage.getBoardInfo();
        return fDeviceInfo != null;
    }

    @Override
    public void addPages() {
        fDevicePage = new RteDeviceSelectorPage();
        fDevicePage.setDevices(fDevices);
        fDevicePage.setDeviceInfo(fDeviceInfo);
        fDevicePage.setBoards(fBoards);
        fDevicePage.setBoardInfo(fBoardInfo);
        addPage(fDevicePage);
    }

}
