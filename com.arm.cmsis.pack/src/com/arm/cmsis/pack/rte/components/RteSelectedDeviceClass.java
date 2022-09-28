/*******************************************************************************
* Copyright (c) 2022 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.rte.components;

import com.arm.cmsis.pack.CpStrings;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.utils.DeviceVendor;

/**
 * An artificial component that describes selected device.
 */
public class RteSelectedDeviceClass extends RteComponentClass {

    ICpDeviceInfo fDeviceInfo = null;
    String vendorName = null;

    public RteSelectedDeviceClass(IRteComponentItem parent, ICpDeviceInfo deviceInfo) {
        super(parent, deviceInfo.getFullDeviceName());
        fDeviceInfo = deviceInfo;
    }

    @Override
    public boolean purge() {
        return false;
    }

    @Override
    public ICpItem getActiveCpItem() {
        return fDeviceInfo;
    }

    @Override
    public String getUrl() {
        return fDeviceInfo.getUrl();
    }

    @Override
    public String getDescription() {
        if (fDeviceInfo.getDevice() == null) {
            return CpStrings.DeviceNotFound;
        }
        return fDeviceInfo.getSummary();
    }

    @Override
    public String getActiveVendor() {
        if (vendorName == null) {
            vendorName = DeviceVendor.getOfficialVendorName(fDeviceInfo.getVendor());
        }
        return vendorName;
    }

    @Override
    public String getActiveVersion() {
        return fDeviceInfo.getVersion();
    }

    @Override
    public boolean isUseLatestVersion() {
        return true;
    }

    @Override
    public String getKey() {
        // Artificial empty key to make the item always on top (the second one).
        return CmsisConstants.ZERO;
    }

    @Override
    public boolean isSelected() {
        return true; // device is always selected
    }

}
