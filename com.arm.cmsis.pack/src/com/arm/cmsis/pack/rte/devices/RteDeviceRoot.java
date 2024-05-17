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

package com.arm.cmsis.pack.rte.devices;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpDeviceItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.utils.VersionComparator;

/**
 * Default implementation of IRteDeviceItem
 */
public class RteDeviceRoot extends RteDeviceItem implements IRteDeviceRoot {

    protected Map<String, ICpPack> packs = new HashMap<>();

    public RteDeviceRoot() {
        super();
    }

    /**
     * Creates device tree from list of Packs
     *
     * @param packs collection of packs to use
     * @return device tree as root IRteDeviceItem
     */
    public static IRteDeviceRoot createTree(Collection<ICpPack> packs) {
        IRteDeviceRoot root = new RteDeviceRoot();
        if (packs == null || packs.isEmpty()) {
            return root;
        }
        for (ICpPack pack : packs) {
            root.addDevices(pack);
        }
        return root;
    }

    @Override
    public void addDevices(ICpPack pack) {
        if (pack == null) {
            return;
        }
        ICpPack p = packs.getOrDefault(pack.getPackFamilyId(), null);
        // if there is a previous pack in the same pack family and has a higher priority
        // over this pack,
        // don't add this pack's devices, otherwise remove the previous pack's devices
        // then add this pack's
        if (p != null) {
            if (p.getPackState().ordinal() < pack.getPackState().ordinal()) {
                return;
            }
            if (p.getPackState().ordinal() == pack.getPackState().ordinal()
                    && VersionComparator.versionCompare(p.getVersion(), pack.getVersion()) > 0) {
                return;
            }
        }
        removeDevices(p);
        packs.put(pack.getPackFamilyId(), pack);
        Collection<? extends ICpItem> devices = pack.getGrandChildren(CmsisConstants.DEVICES_TAG);
        if (devices != null) {
            for (ICpItem item : devices) {
                if (!(item instanceof ICpDeviceItem)) {
                    continue;
                }
                ICpDeviceItem deviceItem = (ICpDeviceItem) item;
                addDevice(deviceItem);
            }
        }
    }

    @Override
    public void removeDevices(ICpPack pack) {
        if (pack == null) {
            return;
        }
        Collection<? extends ICpItem> devices = pack.getGrandChildren(CmsisConstants.DEVICES_TAG);
        if (devices != null) {
            for (ICpItem item : devices) {
                if (!(item instanceof ICpDeviceItem)) {
                    continue;
                }
                ICpDeviceItem deviceItem = (ICpDeviceItem) item;
                removeDevice(deviceItem);
            }
        }
    }
}
