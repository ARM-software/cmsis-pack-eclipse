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

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.rte.IRteModelController;

/**
 * This class implements functionality of component selector page
 */
public class RteComponentManagerWidget extends RteModelWidget {
    private SashForm sashForm = null;

    RteComponentSelectorWidget rteComponentTreeWidget = null;
    RteValidateWidget rteValidateWidget = null;

    public RteComponentManagerWidget() {
        super();
        rteComponentTreeWidget = new RteComponentSelectorWidget();
        rteValidateWidget = new RteValidateWidget();
    }

    public SashForm getSashForm() {
        return sashForm;
    }

    @Override
    public void destroy() {
        rteComponentTreeWidget.destroy();
        rteValidateWidget.destroy();
        rteComponentTreeWidget = null;
        rteValidateWidget = null;
        super.destroy();
    }

    @Override
    public Composite getFocusWidget() {
        TreeViewer viewer = rteComponentTreeWidget.getViewer();
        return viewer.getTree();
    }

    @Override
    public void setModelController(IRteModelController model) {
        super.setModelController(model);
        rteComponentTreeWidget.setModelController(model);
        rteValidateWidget.setModelController(model);
        update();
    }

    @Override
    public Composite createControl(Composite parent) {
        sashForm = new SashForm(parent, SWT.VERTICAL);
        sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
        sashForm.setSashWidth(3);
        rteComponentTreeWidget.createControl(sashForm);
        rteValidateWidget.createControl(sashForm);
        sashForm.setWeights(new int[] { 3, 1 });
        getFocusWidget().setFocus();

        return sashForm;
    }

    @Override
    public void handle(RteEvent event) {
    }

    @Override
    public void refresh() {
        // does nothing : component and validation views refresh themselves
    }

    @Override
    public void update() {
        if (sashForm != null && !sashForm.isDisposed()) {
            rteComponentTreeWidget.update();
            rteValidateWidget.update();
        }
    }
}
