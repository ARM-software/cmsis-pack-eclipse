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
package com.arm.cmsis.zone.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import com.arm.cmsis.pack.ui.widgets.RteWidget;
import com.arm.cmsis.zone.ui.editors.CmsisZoneController;

/**
 * This class to setup root zone options and display info
 */
public class CmsisZoneSetupWidget extends RteWidget<CmsisZoneController> {

    protected CmsisZoneSetupComposite zoneSetupComposite = null;

    public CmsisZoneSetupWidget() {
        super();
    }

    @Override
    public void destroy() {
        zoneSetupComposite = null;
        super.destroy();
    }

    public CmsisZoneSetupComposite getZoneSetupComposite() {
        return zoneSetupComposite;
    }

    @Override
    public Composite getFocusWidget() {
        return getZoneSetupComposite();
    }

    @Override
    public void setModelController(CmsisZoneController model) {
        super.setModelController(model);
        if (zoneSetupComposite != null)
            zoneSetupComposite.setModelController(model);
        update();
    }

    @Override
    public Composite createControl(Composite parent) {
        zoneSetupComposite = new CmsisZoneSetupComposite(parent);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        zoneSetupComposite.setLayoutData(gd);
        getFocusWidget().setFocus();
        return zoneSetupComposite;
    }

    @Override
    public void refresh() {
        CmsisZoneController modelController = getModelController();
        if (zoneSetupComposite != null && !zoneSetupComposite.isDisposed() && modelController != null) {
            zoneSetupComposite.refresh();
        }
    }

    @Override
    public void update() {
        refresh();
    }
}
