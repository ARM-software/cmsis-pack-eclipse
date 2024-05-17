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

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.enums.EDeviceHierarchyLevel;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.CpStringsUI;

public class DevicesViewLabelProvider extends ColumnLabelProvider {

    static IRteDeviceItem getDeviceTreeItem(Object obj) {
        if (obj instanceof IRteDeviceItem) {
            return (IRteDeviceItem) obj;
        }
        return null;
    }

    @Override
    public String getText(Object obj) {
        IRteDeviceItem rteDeviceItem = getDeviceTreeItem(obj);
        if (rteDeviceItem != null) {
            // added spaces at last of text as a workaround to show the complete text in the
            // views
            String name = removeColon(rteDeviceItem.getName()) + ' ';
            if (!rteDeviceItem.hasChildren() && rteDeviceItem.getDevice() != null
                    && rteDeviceItem.getDevice().isDeprecated()) {
                name += CpStringsUI.DevicesViewLabelProvider_DeprecatedDevice + ' ';
            }
            return name;
        }
        return CmsisConstants.EMPTY_STRING;
    }

    private String removeColon(String string) {
        if (string.indexOf(':') != -1) {
            return string.substring(0, string.indexOf(':'));
        }
        return string;
    }

    @Override
    public Image getImage(Object obj) {
        IRteDeviceItem rteDeviceItem = getDeviceTreeItem(obj);
        if (rteDeviceItem != null) {
            if (rteDeviceItem.getLevel() == EDeviceHierarchyLevel.VENDOR.ordinal()) {
                return CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT);
            } else if (rteDeviceItem.hasChildren() && !stopAtCurrentLevel(rteDeviceItem)) {
                return CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT_CLASS);
            } else if (rteDeviceItem.getDevice() != null && rteDeviceItem.getDevice().isDeprecated()) {
                return CpPlugInUI.getImage(CpPlugInUI.ICON_DEVICE_DEPR);
            } else if (packInstalledAndContainsDevice(rteDeviceItem)) {
                return CpPlugInUI.getImage(CpPlugInUI.ICON_DEVICE);
            } else {
                return CpPlugInUI.getImage(CpPlugInUI.ICON_DEVICE_GREY);
            }
        }

        return null;
    }

    private boolean packInstalledAndContainsDevice(IRteDeviceItem rteDeviceItem) {
        IRteDeviceItem deviceItem;
        if (rteDeviceItem.getDevice() == null) {
            IRteDeviceItem parent = getClosestParentRteDeviceItem(rteDeviceItem);
            deviceItem = parent.findItem(rteDeviceItem.getName(), rteDeviceItem.getVendorName(), false);
            if (deviceItem == null) {
                return false;
            }
        } else {
            deviceItem = rteDeviceItem;
        }
        return deviceItem.getDevice().getPack().getPackState().isInstalledOrLocal();
    }

    @Override
    public String getToolTipText(Object obj) {
        IRteDeviceItem item = getDeviceTreeItem(obj);
        IRteDeviceItem parent = getClosestParentRteDeviceItem(item);
        IRteDeviceItem rteDeviceItem;
        if (parent == item) {
            rteDeviceItem = item;
        } else {
            rteDeviceItem = parent.findItem(item.getName(), item.getVendorName(), false);
        }
        if (rteDeviceItem != null && rteDeviceItem.getDevice() != null) {
            return NLS.bind(CpStringsUI.DevicesViewLabelProvider_AvailableInPack, rteDeviceItem.getDevice().getPackId());
        }
        return null;
    }

    private IRteDeviceItem getClosestParentRteDeviceItem(IRteDeviceItem item) {
        IRteDeviceItem parent = item;
        while (parent.getParent() != null && parent.getParent().getAllDeviceNames().isEmpty()) {
            parent = parent.getParent();
        }
        return parent;
    }

    static boolean stopAtCurrentLevel(IRteDeviceItem rteDeviceItem) {
        IRteDeviceItem firstChild = rteDeviceItem.getFirstChild();
        if (firstChild == null || firstChild.getLevel() == EDeviceHierarchyLevel.PROCESSOR.ordinal()) {
            return true;
        }
        return false;
    }
}