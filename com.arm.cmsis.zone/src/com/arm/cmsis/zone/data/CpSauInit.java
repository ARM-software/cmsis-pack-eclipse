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

package com.arm.cmsis.zone.data;

import java.util.Collection;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;

/**
 * Class containing SAUI regions
 */
public class CpSauInit extends CpZoneItem implements ICpSauInit {

    public CpSauInit(ICpItem parent) {
        this(parent, CmsisConstants.SAU_INIT);
    }

    public CpSauInit(ICpItem parent, String tag) {
        super(parent, tag);
    }

    @Override
    protected ICpItem createChildItem(String tag) {
        if (CmsisConstants.REGION.equals(tag)) {
            return new CpSauRegion(this, tag);
        }
        return super.createChildItem(tag);
    }

    @Override
    public ICpSauRegion getSauRegion(String name) {
        ICpItem item = getFirstChild(name);
        if (item instanceof ICpSauRegion) {
            return (ICpSauRegion) item;
        }
        return null;
    }

    @Override
    public Collection<ICpSauRegion> getSauRegions() {
        return getChildrenOfType(ICpSauRegion.class);
    }

    @Override
    public ICpItem toFtlModel(ICpItem ftlParent) {
        for (ICpSauRegion sr : getSauRegions()) {
            ICpItem sauItem = sr.toFtlModel(ftlParent);
            if (ftlParent != null)
                ftlParent.addChild(sauItem);
        }
        return null; // does not include itself
    }
}
