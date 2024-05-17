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

package com.arm.cmsis.pack.info;

import java.util.Collection;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpItem;
import com.arm.cmsis.pack.data.CpPackFilter;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPackFilter;
import com.arm.cmsis.pack.enums.EVersionMatchMode;

/**
 *
 */
public class CpPackFilterInfo extends CpItem implements ICpPackFilterInfo {

    public CpPackFilterInfo(ICpItem parent) {
        this(parent, CmsisConstants.PACKAGES_TAG);
    }

    public CpPackFilterInfo(ICpItem parent, String tag) {
        super(parent, tag);
    }

    @Override
    public ICpPackFilter createPackFilter() {
        ICpPackFilter packFilter = new CpPackFilter();

        boolean bUseLatest = attributes().getAttributeAsBoolean(CmsisConstants.USE_ALL_LATEST_PACKS, true);
        packFilter.setUseAllLatestPacks(bUseLatest);
        if (bUseLatest) {
            return packFilter;
        }
        Collection<? extends ICpItem> packInfos = getChildren();
        if (packInfos == null)
            return packFilter;
        for (ICpItem item : packInfos) {
            if (!(item instanceof ICpPackInfo))
                continue;
            ICpPackInfo packInfo = (ICpPackInfo) item;
            String packId = packInfo.getId();
            EVersionMatchMode mode = packInfo.getVersionMatchMode();
            switch (mode) {
            case EXCLUDED:
                packFilter.setExcluded(packId, true);
                break;
            case FIXED:
                packFilter.setFixed(packId, true);
                break;
            case LATEST:
                packFilter.setUseLatest(packId);
                break;
            default:
                break;

            }
        }
        return packFilter;
    }

    @Override
    public boolean isUseAllLatestPacks() {
        return attributes().getAttributeAsBoolean(CmsisConstants.USE_ALL_LATEST_PACKS, true);
    }

    @Override
    public void setUseAllLatestPacks(boolean bUseLatest) {
        attributes().setAttribute(CmsisConstants.USE_ALL_LATEST_PACKS, bUseLatest);
    }
}
