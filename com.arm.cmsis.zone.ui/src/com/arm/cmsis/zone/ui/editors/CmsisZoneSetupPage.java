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

package com.arm.cmsis.zone.ui.editors;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.zone.ui.IZoneHelpContextIds;
import com.arm.cmsis.zone.ui.Messages;
import com.arm.cmsis.zone.widgets.CmsisZoneSetupWidget;

/**
 * Editor page that wraps RteManagerWidget
 *
 */
public class CmsisZoneSetupPage extends CmsisZonePage {

    public CmsisZoneSetupPage() {
        super(Messages.CmsisZoneSetupPage_ZoneSetup);
    }

    @Override
    protected CmsisZoneSetupWidget createContentWidget() {
        return new CmsisZoneSetupWidget();
    }

    @Override
    public void createPageContent(Composite parent) {
        CmsisZoneSetupWidget widget = (CmsisZoneSetupWidget) getContentWidget();
        widget.createControl(parent);
    }

    @Override
    protected String getHelpID() {
        return IZoneHelpContextIds.SETUP_PAGE;
    }

    @Override
    protected Image getImage() {
        return CpPlugInUI.getImage(CpPlugInUI.ICON_CMSIS_ZONE);
    }

    @Override
    protected void createShowActions() {
        // do nothing
    }

}
