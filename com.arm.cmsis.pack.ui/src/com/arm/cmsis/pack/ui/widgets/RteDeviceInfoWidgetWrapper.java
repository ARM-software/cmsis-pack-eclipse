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
package com.arm.cmsis.pack.ui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import com.arm.cmsis.pack.rte.IRteModelController;

/**
 * This class implements functionality of component selector page
 */
public class RteDeviceInfoWidgetWrapper extends RteModelWidget {

    protected RteDeviceInfoWidget deviceWidget = null;

    public RteDeviceInfoWidgetWrapper() {
        super();
    }

    @Override
    public void destroy() {
        deviceWidget = null;
        super.destroy();
    }

    public RteDeviceInfoWidget getDeviceInfoWidget() {
        return deviceWidget;
    }

    @Override
    public Composite getFocusWidget() {
        return deviceWidget;
    }

    @Override
    public void setModelController(IRteModelController model) {
        super.setModelController(model);
        if (deviceWidget != null)
            deviceWidget.setModelController(model);
        update();
    }

    @Override
    public Composite createControl(Composite parent) {
        deviceWidget = new RteDeviceInfoWidget(parent);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        deviceWidget.setLayoutData(gd);
        getFocusWidget().setFocus();
        return deviceWidget;
    }

    @Override
    public void refresh() {
        IRteModelController modelController = getModelController();
        if (deviceWidget != null && !deviceWidget.isDisposed() && modelController != null) {
            deviceWidget.setDeviceInfo(modelController.getDeviceInfo());

            // Set board info from IRteModelController to RteDeviceInfoWidget
            deviceWidget.setBoardInfo(modelController.getBoardInfo());
        }
    }

    @Override
    public void update() {
        refresh();
    }
}
