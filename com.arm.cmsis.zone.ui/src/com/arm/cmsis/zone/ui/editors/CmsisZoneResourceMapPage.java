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

package com.arm.cmsis.zone.ui.editors;

import org.eclipse.swt.graphics.Image;

import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.widgets.RteWidget;
import com.arm.cmsis.zone.ui.IZoneHelpContextIds;
import com.arm.cmsis.zone.ui.Messages;
import com.arm.cmsis.zone.widgets.CmsisZoneResourceMapWidget;

/**
 * Editor page that wraps RteManagerWidget
 *
 */
public class CmsisZoneResourceMapPage extends CmsisZonePage {

    public CmsisZoneResourceMapPage() {
        super(Messages.CmsisZoneResourceMapPage_ResourceMap);
    }

    @Override
    protected RteWidget<CmsisZoneController> createContentWidget() {
        return new CmsisZoneResourceMapWidget();
    }

    @Override
    protected String getHelpID() {
        return IZoneHelpContextIds.MEMORY_MAP_PAGE;
    }

    @Override
    protected Image getImage() {
        return CpPlugInUI.getImage(CpPlugInUI.ICON_MEMORY_MAP);
    }
}
