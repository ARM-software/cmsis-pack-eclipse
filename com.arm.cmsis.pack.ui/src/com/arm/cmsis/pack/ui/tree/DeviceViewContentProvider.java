package com.arm.cmsis.pack.ui.tree;

import com.arm.cmsis.pack.enums.EDeviceHierarchyLevel;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;

public class DeviceViewContentProvider extends TreeObjectContentProvider {

    static IRteDeviceItem getDeviceTreeItem(Object obj) {
        if (obj instanceof IRteDeviceItem) {
            return (IRteDeviceItem) obj;
        }
        return null;
    }

    @Override
    public Object getParent(Object child) {
        IRteDeviceItem rteDeviceItem = getDeviceTreeItem(child);
        if (rteDeviceItem != null) {
            return rteDeviceItem.getParent();
        }
        return null;
    }

    @Override
    public Object[] getChildren(Object parent) {
        IRteDeviceItem rteDeviceItem = getDeviceTreeItem(parent);
        if (rteDeviceItem != null) {
            if (!rteDeviceItem.hasChildren() || stopAtCurrentLevel(rteDeviceItem)) {
                return null;
            }
            return rteDeviceItem.getChildArray();
        }
        return super.getChildren(parent);
    }

    @Override
    public boolean hasChildren(Object parent) {
        IRteDeviceItem rteDeviceItem = getDeviceTreeItem(parent);
        if (rteDeviceItem != null) {
            if (stopAtCurrentLevel(rteDeviceItem)) {
                return false;
            }
            return rteDeviceItem.hasChildren();
        }
        return super.hasChildren(parent);
    }

    static boolean stopAtCurrentLevel(IRteDeviceItem rteDeviceItem) {
        IRteDeviceItem firstChild = rteDeviceItem.getFirstChild();
        if (firstChild == null || firstChild.getLevel() == EDeviceHierarchyLevel.PROCESSOR.ordinal()) {
            return true;
        }
        return false;
    }
}