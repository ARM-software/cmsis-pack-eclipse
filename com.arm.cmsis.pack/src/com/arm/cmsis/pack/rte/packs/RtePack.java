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

package com.arm.cmsis.pack.rte.packs;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.info.ICpPackInfo;

/**
 *
 */
public class RtePack extends RtePackItem implements IRtePack {

    protected ICpPack fPack = null;
    protected ICpPackInfo fPackInfo = null;
    protected IAttributes fAttributes = null;
    private boolean fbSelected = false;

    public RtePack(IRtePackItem parent, ICpItem packItem) {
        super(parent);
        addCpItem(packItem);
    }

    @Override
    public void clear() {
        super.clear();
        fPack = null;
        fPackInfo = null;
        fbSelected = false;
    }

    @Override
    public boolean purge() {
        if (isRemoved()) {
            clear();
            return true;
        }
        return false;
    }

    @Override
    public boolean isRemoved() {
        return !isUsed() && !isInstalled();
    }

    @Override
    public IAttributes attributes() {
        if (fAttributes == null)
            fAttributes = createAttributes();
        return fAttributes;
    }

    @Override
    public void addCpItem(ICpItem item) {
        if (item instanceof ICpPack) {
            fPack = (ICpPack) item;
            setAttribute(CmsisConstants.NAME, fPack.getName());
            setAttribute(CmsisConstants.URL, fPack.getUrl());
            setAttribute(CmsisConstants.VENDOR, fPack.getVendor());
            setAttribute(CmsisConstants.VERSION, fPack.getVersion());

        } else if (item instanceof ICpPackInfo) {
            fPackInfo = (ICpPackInfo) item;
            if (fPack == null)
                attributes().setAttributes(fPackInfo.attributes());
        }
    }

    @Override
    public ICpItem getCpItem() {
        ICpPack pack = getPack();
        if (pack != null)
            return pack;
        return getPackInfo();
    }

    @Override
    public ICpPackInfo getPackInfo() {
        return fPackInfo;
    }

    @Override
    public ICpPack getPack() {
        return fPack;
    }

    @Override
    public String getId() {
        ICpItem item = getCpItem();
        if (item != null)
            return item.getId();
        return null;
    }

    @Override
    public boolean isSelected() {
        switch (getVersionMatchMode()) {
        case EXCLUDED:
            return false;
        case LATEST:
            return isLatest();
        case FIXED:
        default:
            break;
        }
        return fbSelected;
    }

    @Override
    public boolean isExcluded() {
        return !isSelected();
    }

    @Override
    public boolean isExplicitlySelected() {
        return fbSelected;
    }

    @Override
    public boolean isUsed() {
        IRtePackCollection root = getRoot();
        if (root != null)
            return root.isPackUsed(getId());
        return false;
    }

    @Override
    public String getVersion() {
        ICpItem item = getCpItem();
        if (item != null)
            return item.getVersion();
        return null;
    }

    @Override
    public void setSelected(boolean selected) {
        fbSelected = selected;
    }

    @Override
    public boolean isInstalled() {
        return fPack != null;
    }

    @Override
    public boolean isLatest() {
        return fParent.getFirstChild() == this;
    }

    @Override
    public IRtePackItem getFirstChild() {
        return null;
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public Object[] getChildArray() {
        return EMPTY_OBJECT_ARRAY;
    }

    @Override
    public int getChildCount() {
        return 0;
    }
}
