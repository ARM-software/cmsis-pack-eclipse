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

package com.arm.cmsis.pack.data;

import java.util.Collection;
import java.util.ArrayList;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.utils.DeviceVendor;

/**
 * Default implementation of ICpBoard interface
 */
public class CpBoard extends CpItem implements ICpBoard {

    public CpBoard(ICpItem parent) {
        super(parent);
    }

    public CpBoard(ICpItem parent, String tag) {
        super(parent, tag);
    }

    @Override
    public String constructId() {
        return ICpBoard.constructBoardId(this);
    }

    @Override
    public String constructName() {
        String name = getAttribute(CmsisConstants.BNAME);
        if (name == null || name.isEmpty()) {
            name = getAttribute(CmsisConstants.NAME);
        }
        return name;
    }

    @Override
    public String getVersion() {
        return getRevision();
    }

    @Override
    public boolean hasMountedDevice(IAttributes deviceAttributes) {
        return containsDevice(deviceAttributes, true);
    }

    @Override
    public boolean hasCompatibleDevice(IAttributes deviceAttributes) {
        return containsDevice(deviceAttributes, false);
    }

    @Override
    public boolean hasMcu() {
        Collection<? extends ICpItem> children = getChildren();
        if (children == null) {
            return false;
        }
        for (ICpItem item : children) {
            if (CmsisConstants.COMPATIBLE_DEVICE_TAG.equals(item.getTag())) {
                String dname = item.getDeviceName();
                if (!dname.isEmpty() && CmsisConstants.NO_MCU.equals(dname)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean containsDevice(IAttributes deviceAttributes, boolean bOnlyMounted) {
        Collection<? extends ICpItem> children = getChildren();
        if (children == null) {
            return false;
        }
        for (ICpItem item : children) {
            String tag = item.getTag();
            switch (tag) {
            case CmsisConstants.COMPATIBLE_DEVICE_TAG:
                if (bOnlyMounted) {
                    break;
                }
                // fall through
            case CmsisConstants.MOUNTED_DEVICE_TAG:
                String dName = item.getAttribute(CmsisConstants.DNAME);
                if (!dName.isEmpty() && deviceAttributes.containsValue(dName)) { // covers Dvariant
                    return true;
                }
                if ((!dName.isEmpty() || item.hasAttribute(CmsisConstants.DFAMILY)
                        || item.hasAttribute(CmsisConstants.DSUBFAMILY))
                        && (item.attributes().matchAttributes(deviceAttributes, CmsisConstants.D_ATTRIBUTE_PREFIX))) {
                    return true;
                }
                break;
            default:
                break;
            }
        }
        return false;
    }

    @Override
    public synchronized String getUrl() {
        if (fURL == null) {
            fURL = DeviceVendor.getBoardVendorUrl(getVendor());
            if (!fURL.isEmpty()) {
                fURL += '/';
                fURL += DeviceVendor.adjutsToUrl(getName());
            }
        }
        return fURL;
    }

    @Override
    public Collection<ICpItem> getMountedDevices() {
        return getDevices(CmsisConstants.MOUNTED_DEVICE_TAG);
    }

    @Override
    public Collection<ICpItem> getCompatibleDevices() {
        return getDevices(CmsisConstants.COMPATIBLE_DEVICE_TAG);
    }

    protected Collection<ICpItem> getDevices(final String requiredTag) {
        Collection<ICpItem> devices = new ArrayList<>();
        Collection<? extends ICpItem> children = getChildren();
        if (children == null) {
            return devices;
        }

        for (ICpItem item : children) {
            String tag = item.getTag();
            if (tag.equals(requiredTag)) {
                devices.add(item);
            }
        }
        return devices;
    }

}
