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

package com.arm.cmsis.pack.ui.tree;

import java.util.Collection;
import java.util.ArrayList;

import com.arm.cmsis.pack.rte.boards.IRteBoardDeviceItem;
import com.arm.cmsis.pack.rte.boards.IRteBoardItem;

public class BoardViewContentProvider extends TreeObjectContentProvider {

    private boolean fbInstallerContext = true;

    public BoardViewContentProvider(boolean installerContext) {
        super();
        this.fbInstallerContext = installerContext;
    }

    IRteBoardItem getRteBoardItem(Object obj) {
        if (obj instanceof IRteBoardItem) {
            return (IRteBoardItem) obj;
        }
        return null;
    }

    IRteBoardDeviceItem getRteBoardDeviceItem(Object obj) {
        if (obj instanceof IRteBoardDeviceItem) {
            return (IRteBoardDeviceItem) obj;
        }
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        IRteBoardItem rteBoardItem = getRteBoardItem(element);
        if (rteBoardItem != null) {
            return rteBoardItem.isRoot() || rteBoardItem.getBoard() != null;
        }
        return super.hasChildren(element);
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        IRteBoardItem rteBoardItem = getRteBoardItem(parentElement);
        if (rteBoardItem != null) {
            if (rteBoardItem.isRoot()) { // All boards
                return rteBoardItem.getChildArray();
            }
            // Normal board
            Collection<IRteBoardDeviceItem> children = new ArrayList<>();

            // Get mounted devices
            IRteBoardDeviceItem mountedDevices = rteBoardItem.getMountedDevices();
            if (mountedDevices != null && mountedDevices.hasChildren()) {
                children.add(mountedDevices);
            }

            // Get compatible devices
            if (fbInstallerContext) {
                IRteBoardDeviceItem compatibleDevices = rteBoardItem.getCompatibleDevices();
                if (compatibleDevices != null && compatibleDevices.hasChildren()) {
                    children.add(compatibleDevices);
                }
            }
            return children.toArray();
        }
        return super.getChildren(parentElement);
    }
}