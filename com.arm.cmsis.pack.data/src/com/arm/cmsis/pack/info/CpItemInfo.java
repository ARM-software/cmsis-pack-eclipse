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

package com.arm.cmsis.pack.info;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.enums.IEvaluationResult;

/**
 * Default implementation of ICpDeviceInfo interface
 */
public abstract class CpItemInfo extends CpItem implements ICpItemInfo, IEvaluationResult {

    protected ICpPackInfo fPackInfo = null;
    protected EEvaluationResult fResolveResult = EEvaluationResult.UNDEFINED;

    /**
     *
     * @Override protected ICpItem createChildItem(String tag) { return
     *           CpConfigurationInfo.createChildItem(this, tag); }
     *
     *           /** Constructs CpDeviceInfo from parent and tag
     *
     * @param parent parent ICpItem
     * @param tag
     */
    protected CpItemInfo(ICpItem parent, String tag) {
        super(parent, tag);
    }

    @Override
    protected ICpItem createChildItem(String tag) {
        return CpConfigurationInfo.createChildItem(this, tag);
    }

    @Override
    public ICpPack getPack() {
        if (getPackInfo() != null) {
            return getPackInfo().getPack();
        }
        return null;
    }

    @Override
    public String getPackId() {
        if (getPackInfo() != null) {
            return getPackInfo().getPackId();
        }
        return CmsisConstants.EMPTY_STRING;
    }

    @Override
    public ICpPackInfo getPackInfo() {
        if (fPackInfo == null) {
            fPackInfo = getFirstChildOfType(ICpPackInfo.class);
        }
        return fPackInfo;
    }

    public void updatePackInfo(ICpPack pack) {
        if (pack != null) {
            if (fPackInfo != null && fPackInfo.getPack() == pack) {
                return;
            }
            fPackInfo = new CpPackInfo(this, pack);
            fPackInfo.attributes().setAttribute(CmsisConstants.INFO, pack.getDescription());
            replaceChild(fPackInfo);
        } else if (fPackInfo != null) {
            fPackInfo.setPack(null);
        }
    }

    @Override
    public void addChild(ICpItem item) {
        if (item instanceof ICpPackInfo) {
            fPackInfo = (ICpPackInfo) item;
        }
        super.addChild(item);
    }

    @Override
    public EEvaluationResult getEvaluationResult() {
        return fResolveResult;
    }

    @Override
    public void setEvaluationResult(EEvaluationResult result) {
        fResolveResult = result;
    }
}
