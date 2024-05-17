/*******************************************************************************
 * Copyright (c) 2022 ARM Ltd. and others
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

package com.arm.cmsis.pack.ui.tree;

import org.eclipse.jface.viewers.ColumnViewer;

import com.arm.cmsis.pack.enums.EDeviceHierarchyLevel;
import com.arm.cmsis.pack.info.CpDeviceInfo;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;
import com.arm.cmsis.pack.ui.CpStringsUI;

public class DevicesViewColumnAdvisor extends ColumnAdvisor {
    protected static final int COLURL = 1;

    public DevicesViewColumnAdvisor(ColumnViewer columnViewer) {
        super(columnViewer);
    }

    @Override
    public CellControlType getCellControlType(Object obj, int columnIndex) {
        if (columnIndex == COLURL) {
            IRteDeviceItem item = getDeviceTreeItem(obj);
            if (item != null) {
                if (item.getLevel() == EDeviceHierarchyLevel.VARIANT.ordinal()
                        || (item.getLevel() == EDeviceHierarchyLevel.DEVICE.ordinal() && stopAtCurrentLevel(item))) {
                    return CellControlType.URL;
                }
            }
        }
        return CellControlType.TEXT;
    }

    @Override
    public String getString(Object obj, int columnIndex) {
        if (getCellControlType(obj, columnIndex) == CellControlType.URL) {
            IRteDeviceItem item = getDeviceTreeItem(obj);
            if (item != null) {
                ICpDeviceInfo deviceInfo = new CpDeviceInfo(null, item.getDevice(), item.getName());
                return deviceInfo.getSummary();
            }
        } else if (columnIndex == COLURL) {
            IRteDeviceItem item = getDeviceTreeItem(obj);
            int nrofDevices = item.getAllDeviceNames().size();
            if (nrofDevices == 1) {
                return CpStringsUI.DevicesViewColumnAdvisor_1Device;
            } else if (nrofDevices == 0) {
                return CpStringsUI.DevicesViewColumnAdvisor_Processor;
            } else {
                return nrofDevices + CpStringsUI.DevicesViewColumnAdvisor_Devices;
            }
        }
        return null;
    }

    @Override
    public String getUrl(Object obj, int columnIndex) {
        if (getCellControlType(obj, columnIndex) == CellControlType.URL) {
            IRteDeviceItem item = getDeviceTreeItem(obj);
            return item.getUrl();
        }
        return null;
    }

    @Override
    public String getTooltipText(Object obj, int columnIndex) {
        return getUrl(obj, columnIndex);
    }

    static IRteDeviceItem getDeviceTreeItem(Object obj) {
        if (obj instanceof IRteDeviceItem) {
            return (IRteDeviceItem) obj;
        }
        return null;
    }

    static boolean stopAtCurrentLevel(IRteDeviceItem rteDeviceItem) {
        IRteDeviceItem firstChild = rteDeviceItem.getFirstChild();
        if (firstChild == null || firstChild.getLevel() == EDeviceHierarchyLevel.PROCESSOR.ordinal()) {
            return true;
        }
        return false;
    }
}