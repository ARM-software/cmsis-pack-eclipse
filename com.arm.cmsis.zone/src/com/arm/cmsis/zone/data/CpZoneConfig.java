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

package com.arm.cmsis.zone.data;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;

/**
 * Zone configure element
 */
public class CpZoneConfig extends CpZoneItem implements ICpZoneConfig {

    public CpZoneConfig(ICpItem parent, String tag) {
        super(parent, tag);
    }

    @Override
    public ICpZoneConfig getZoneConfig() {
        return this;
    }

    @Override
    public String getZoneMode() {
        ICpZoneConfig config = getZoneConfig();
        if (config != null)
            return getZoneConfig().attributes().getAttribute(CmsisConstants.MODE, CmsisConstants.PROJECT);
        return CmsisConstants.PROJECT;
    }

    protected ICpItem ensureZoneConfigType(String type) {
        ICpItem item = getFirstChild(type);
        if (item == null) {
            item = createChildItem(CmsisConstants.TYPE);
            item.setAttribute(CmsisConstants.NAME, type);
            addChild(item);
        }
        return item;
    }

    @Override
    public boolean getZoneOption(String type, String key) {
        boolean defaultValue = getDefaultOption(type, key);
        ICpItem item = getZoneConfigOptionItem(type);
        if (item == null) {
            return defaultValue;
        }
        return item.attributes().getAttributeAsBoolean(key, defaultValue);
    }

    @Override
    public boolean setZoneOption(String type, String key, boolean value) {
        boolean bOldValue = getZoneOption(type, key);
        if (bOldValue == value)
            return false;
        ICpItem item = ensureZoneConfigType(type);
        item.setAttribute(key, value);
        return true;
    }

    protected boolean getDefaultOption(String type, String key) {
        // this version ignores type
        if (CmsisConstants.SHOW.equals(key))
            return true;
        return false;
    }

}
