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

package com.arm.cmsis.pack.rte.boards;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.arm.cmsis.pack.data.ICpBoard;
import com.arm.cmsis.pack.item.CmsisMapItem;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;

/**
 * Default implementation of {@link RteBoardDeviceItem}
 */
public class RteBoardDeviceItem extends CmsisMapItem<IRteBoardDeviceItem> implements IRteBoardDeviceItem {

    protected IRteDeviceItem fDeviceItem = null; // referenced deviceItem
    protected Set<String> fAllDeviceNames = null;

    public RteBoardDeviceItem(IRteBoardDeviceItem parent, String name) {
        super(parent, name);
    }

    public RteBoardDeviceItem(IRteBoardDeviceItem parent, IRteDeviceItem deviceItem) {
        super(parent);
        fName = deviceItem.getName();
        fDeviceItem = deviceItem;
        addDeviceChildren(deviceItem);

    }

    @Override
    public ICpBoard getBoard() {
        IRteBoardItem rteBoard = getRteBoard();
        if (rteBoard != null) {
            return rteBoard.getBoard();
        }
        return null;
    }

    @Override
    public IRteBoardItem getRteBoard() {
        return getParentOfType(IRteBoardItem.class);
    }

    @Override
    public IRteDeviceItem getRteDeviceItem() {
        return fDeviceItem;
    }

    @Override
    public IRteDeviceItem getRteDeviceLeaf() {
        if (hasChildren()) {
            return getChildCount() == 1 ? getFirstChild().getRteDeviceItem() : null;
        }
        return fDeviceItem;
    }

    @Override
    public void addDeviceChildren(IRteDeviceItem deviceItem) {
        Collection<? extends IRteDeviceItem> deviceChildren = deviceItem.getChildren();
        for (IRteDeviceItem childDevice : deviceChildren) {
            addChild(new RteBoardDeviceItem(this, childDevice));
        }
    }

    @Override
    protected Map<String, IRteBoardDeviceItem> createMap() {
        // create TreeMap with Alpha-Numeric case-insensitive ascending sorting
        return new TreeMap<>(new RteBoardDeviceItemComparator());
    }

    @Override
    public Set<String> getAllDeviceNames() {
        if (fAllDeviceNames == null) {
            if (fDeviceItem != null) {
                fAllDeviceNames = fDeviceItem.getAllDeviceNames();
            } else {
                fAllDeviceNames = new HashSet<>();
                if (fChildMap != null) {
                    for (IRteBoardDeviceItem item : fChildMap.values()) {
                        fAllDeviceNames.addAll(item.getAllDeviceNames());
                    }
                }
            }
        }
        return fAllDeviceNames;
    }

}
