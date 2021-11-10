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

import com.arm.cmsis.pack.ui.widgets.RteWidget;
import com.arm.cmsis.zone.data.ICpZone;
import com.arm.cmsis.zone.widgets.CmsisZoneAssignWidget;

/**
 * Editor page that wraps RteManagerWidget
 *
 */
public class CmsisZoneAssignPage extends CmsisZonePage {

    private ICpZone fZone = null;

    public CmsisZoneAssignPage(ICpZone zone) {
        super(zone.getName());
        fZone = zone;
    }

    @Override
    public ICpZone getZone() {
        return fZone;
    }

    public void setZone(ICpZone zone) {
        if (fZone == zone)
            return;
        fZone = zone;
        RteWidget<CmsisZoneController> w = getContentWidget();
        if (w != null) {
            CmsisZoneAssignWidget assignWidget = (CmsisZoneAssignWidget) w;
            assignWidget.setZone(zone);
        }
    }

    @Override
    public boolean isSingleZonePage() {
        return true;
    }

    @Override
    public void destroy() {
        if (fZone != null) {
            fZone = null;
        }
        super.destroy();
    }

    @Override
    protected RteWidget<CmsisZoneController> createContentWidget() {
        return new CmsisZoneAssignWidget(getZone());
    }

    @Override
    public void update() {
        if (fZone != null) {
            CmsisZoneController controller = getModelController();
            if (controller != null) {
                ICpZone zone = controller.getRootZone().getZone(fZone.getName());
                setZone(zone);
            }
        }
        super.update();
    }

}
