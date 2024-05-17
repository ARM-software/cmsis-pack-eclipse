/*******************************************************************************
* Copyright (c) 2021 ARM Ltd. and others
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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnViewer;

import com.arm.cmsis.pack.events.IRteController;
import com.arm.cmsis.pack.ui.CpStringsUI;
import com.arm.cmsis.pack.ui.tree.ColumnAdvisor;

/**
 * Extends ColumnAdvisor with IRteCobdelController support
 */
public abstract class RteColumnAdvisor<TController extends IRteController> extends ColumnAdvisor
        implements IRteColumnAdvisor<TController> {

    private TController fRteModelController = null;

    public RteColumnAdvisor(ColumnViewer columnViewer) {
        super(columnViewer);
    }

    public RteColumnAdvisor(ColumnViewer columnViewer, TController modelController) {
        this(columnViewer);
        fRteModelController = modelController;
    }

    @Override
    public void setModelController(TController modelController) {
        fRteModelController = modelController;
    }

    @Override
    public TController getModelController() {
        return fRteModelController;
    }

    @Override
    public void openUrl(String url) {
        if (fRteModelController != null) {
            String msg = fRteModelController.openUrl(url);
            if (msg != null) {
                String message = CpStringsUI.CannotOpenURL + url;
                message += "\n"; //$NON-NLS-1$
                message += msg;
                MessageDialog.openError(this.control != null ? this.control.getShell() : null,
                        CpStringsUI.CannotOpenURL, message);
            }
        } else {
            super.openUrl(url);
        }
    }

}
