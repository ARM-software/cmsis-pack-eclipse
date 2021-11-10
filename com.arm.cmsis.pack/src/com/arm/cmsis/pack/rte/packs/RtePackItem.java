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

package com.arm.cmsis.pack.rte.packs;

import com.arm.cmsis.pack.CpStrings;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.enums.EVersionMatchMode;
import com.arm.cmsis.pack.info.ICpPackInfo;

/**
 * Base class for RTE pack tree
 */
public abstract class RtePackItem implements IRtePackItem {

    protected IRtePackItem fParent = null;

    public RtePackItem(IRtePackItem parent) {
        fParent = parent;
    }

    @Override
    public void clear() {
        fParent = null;
    }

    @Override
    public IRtePackCollection getRoot() {
        if (fParent != null)
            return fParent.getRoot();
        return null;
    }

    @Override
    public IRtePackFamily getFamily() {
        if (fParent != null)
            return fParent.getFamily();
        return null;
    }

    @Override
    public IRtePackItem getParent() {
        return fParent;
    }

    @Override
    public EVersionMatchMode getVersionMatchMode() {
        if (fParent != null)
            return fParent.getVersionMatchMode();
        return null;
    }

    @Override
    public String getUrl() {
        ICpItem item = getCpItem();
        if (item != null)
            return item.getUrl();
        return null;
    }

    @Override
    public String getDescription() {
        ICpPack pack = getPack();
        if (pack != null)
            return pack.getDescription();
        return CpStrings.RtePackIsNotInstalled;
    }

    @Override
    public void destroy() {
        clear();
        fParent = null;
    }

    @Override
    public ICpPack getPack() {
        return null;
    }

    @Override
    public ICpPackInfo getPackInfo() {
        return null;
    }

    @Override
    public boolean isUseAllLatestPacks() {
        if (fParent != null)
            return fParent.isUseAllLatestPacks();
        return false;
    }
}
