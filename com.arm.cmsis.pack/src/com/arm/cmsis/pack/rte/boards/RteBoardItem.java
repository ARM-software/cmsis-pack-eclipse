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

package com.arm.cmsis.pack.rte.boards;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpBoard;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;
import com.arm.cmsis.pack.utils.VersionComparator;

/**
 * Default implementation of {@link RteBoardItem}
 */
public class RteBoardItem extends RteBoardDeviceItem implements IRteBoardItem {

    protected Map<String, ICpBoard> fBoards = null; // packId -> board
    protected IRteBoardDeviceItem fMountedDevices = null;
    protected IRteBoardDeviceItem fCompatibleDevices = null;
    protected boolean fRoot;

    public RteBoardItem() {
        super(null, CmsisConstants.ALL_BOARDS);
        fRoot = true;
    }

    public RteBoardItem(IRteBoardItem parent, String name) {
        super(parent, name);
        fRoot = false;
    }

    @Override
    public void invalidate() {
        fAllDeviceNames = null;
        removeChild(fMountedDevices);
        fMountedDevices = null;
        removeChild(fCompatibleDevices);
        fCompatibleDevices = null;

        super.invalidate();
    }

    /**
     * Creates board tree from list of Packs
     *
     * @param packs collection of packs to use
     * @return device tree as root IRteBoardDeviceItem
     */
    public static IRteBoardItem createTree(Collection<ICpPack> packs) {
        IRteBoardItem root = new RteBoardItem();
        if (packs == null || packs.isEmpty()) {
            return root;
        }

        for (ICpPack pack : packs) {
            root.addBoards(pack);
        }
        return root;
    }

    @Override
    public boolean isRoot() {
        return fRoot;
    }

    @Override
    public boolean isBoard() {
        return true;
    }

    @Override
    public IRteBoardItem getRteBoard() {
        return this;
    }

    @Override
    public void addBoard(ICpBoard item) {
        if (item == null) {
            return;
        }

        if (fRoot) {
            addBoardItem(item, item.getId());
            return;
        }
        ICpPack pack = item.getPack();
        String packId = pack.getId();
        if (fBoards == null) {
            fBoards = new TreeMap<>(new VersionComparator());
        }

        ICpBoard board = fBoards.get(packId);
        if (board == null ||
        // new item's pack is installed/downloaded and the one in the tree is not
                (item.getPack().getPackState().ordinal() < board.getPack().getPackState().ordinal())) {
            fBoards.put(packId, item);
            invalidate();
        }
    }

    protected void addBoardItem(ICpBoard item, final String itemName) {
        IRteBoardItem bi = (IRteBoardItem) getChild(itemName);
        if (bi == null) {
            bi = new RteBoardItem(this, itemName);
            addChild(bi);
        }
        bi.addBoard(item);
    }

    @Override
    public void addBoards(ICpPack pack) {
        if (pack == null) {
            return;
        }
        Collection<? extends ICpItem> boards = pack.getGrandChildren(CmsisConstants.BOARDS_TAG);
        if (boards == null) {
            return;
        }
        for (ICpItem item : boards) {
            if (!(item instanceof ICpBoard)) {
                continue;
            }
            ICpBoard boardItem = (ICpBoard) item;
            addBoard(boardItem);
        }
    }

    @Override
    public void removeBoard(ICpBoard item) {
        if (item == null) {
            return;
        }

        if (fRoot) {
            IRteBoardItem b = (IRteBoardItem) getChild(item.getId());
            if (b == null) {
                return;
            }
            b.removeBoard(item);
        } else {
            String packId = item.getPackId();
            if (fBoards == null) {
                return;
            }

            fBoards.remove(packId);

            if (fBoards.size() == 0) {
                getParent().removeChild(this);
                setParent(null);
                return;
            }
            invalidate();
        }
    }

    @Override
    public void removeBoards(ICpPack pack) {
        if (pack == null) {
            return;
        }
        Collection<? extends ICpItem> boards = pack.getGrandChildren(CmsisConstants.BOARDS_TAG);
        if (boards != null) {
            for (ICpItem item : boards) {
                if (!(item instanceof ICpBoard)) {
                    continue;
                }
                ICpBoard currentBoard = (ICpBoard) item;
                removeBoard(currentBoard);
            }
        }
    }

    @Override
    public ICpBoard getBoard() {
        if (fBoards != null && !fBoards.isEmpty()) {
            // Return the latest INSTALLED pack's board
            for (ICpBoard board : fBoards.values()) {
                if (board.getPack().getPackState().isInstalledOrLocal()) {
                    return board;
                }
            }
            // Otherwise return the latest pack's board
            return fBoards.entrySet().iterator().next().getValue();
        }
        return null;
    }

    @Override
    public Collection<ICpBoard> getBoards() {
        if (fBoards != null) {
            return fBoards.values();
        }
        return Collections.emptyList();
    }

    @Override
    public IRteDeviceItem getRteDeviceLeaf() {
        if (getMountedDevices() != null) {
            return getMountedDevices().getRteDeviceLeaf();
        }
        return null;
    }

    @Override
    public IRteBoardDeviceItem getMountedDevices() {

        if (fMountedDevices == null) {
            fMountedDevices = collectDevices(CmsisConstants.MOUNTED_DEVICES);
        }
        return fMountedDevices;
    }

    @Override
    public IRteBoardDeviceItem getCompatibleDevices() {
        if (fCompatibleDevices == null) {
            fCompatibleDevices = collectDevices(CmsisConstants.COMPATIBLE_DEVICES);
        }
        return fCompatibleDevices;
    }

    protected IRteBoardDeviceItem collectDevices(String devicesType) {
        IRteBoardDeviceItem rootDeviceItem = new RteBoardDeviceItem(this, devicesType);
        addChild(rootDeviceItem);
        ICpBoard board = getBoard();
        if (board == null) {
            return rootDeviceItem;
        }

        Collection<ICpItem> devices;
        if (devicesType.equals(CmsisConstants.MOUNTED_DEVICES)) {
            devices = board.getMountedDevices();
        } else {
            devices = board.getCompatibleDevices();
        }
        if (devices == null || devices.isEmpty()) {
            return rootDeviceItem;
        }

        IRteDeviceItem allDevices = CpPlugIn.getPackManager().getDevices();
        if (allDevices == null) {
            return rootDeviceItem;
        }

        for (ICpItem device : devices) {
            String vendorName = device.getVendor();
            String deviceName = getDeviceName(device);
            IRteDeviceItem rteDeviceItem = allDevices.findItem(deviceName, vendorName, false);
            if (rteDeviceItem == null) {
                continue;
            }
            if (rootDeviceItem.getChild(rteDeviceItem.getName()) == null) {
                rootDeviceItem.addChild(new RteBoardDeviceItem(rootDeviceItem, rteDeviceItem));
            }
        }
        return rootDeviceItem;
    }

    protected String getDeviceName(ICpItem device) {
        String deviceName = CmsisConstants.EMPTY_STRING;
        if (device.hasAttribute(CmsisConstants.DFAMILY)) {
            deviceName = device.getAttribute(CmsisConstants.DFAMILY);
        } else if (device.hasAttribute(CmsisConstants.DSUBFAMILY)) {
            deviceName = device.getAttribute(CmsisConstants.DSUBFAMILY);
        } else if (device.hasAttribute(CmsisConstants.DNAME)) {
            deviceName = device.getAttribute(CmsisConstants.DNAME);
        } else if (device.hasAttribute(CmsisConstants.DVARIANT)) {
            deviceName = device.getAttribute(CmsisConstants.DVARIANT);
        }
        return deviceName;
    }

    @Override
    public IRteBoardItem findBoard(String boardId) {
        if (fRoot) {
            return (IRteBoardItem) getChild(boardId);
        } else if (fName.equals(boardId)) {
            return this;
        }
        return null;
    }

    @Override
    public Collection<IRteBoardItem> findBoards(String partialBoardId) {
        List<IRteBoardItem> boards = new ArrayList<>();
        if (partialBoardId != null) {
            for (Entry<String, IRteBoardDeviceItem> entry : childMap().entrySet()) {
                if (entry.getKey().contains(partialBoardId) && entry.getValue() instanceof IRteBoardItem) {
                    boards.add((IRteBoardItem) entry.getValue());
                }
            }
        }
        return boards;
    }

    @Override
    public String getDescription() {
        ICpBoard board = getBoard();
        if (board != null) {
            String description = board.getDescription();
            if (description != null && !description.isEmpty()) {
                return description;
            }
        }
        if (getParent() != null) {
            return getParent().getDescription();
        }
        return CmsisConstants.EMPTY_STRING;
    }

    @Override
    public String getUrl() {
        ICpBoard board = getBoard();
        if (board != null) {
            return board.getUrl();
        }
        return null;
    }

    @Override
    public String getDoc() {
        ICpBoard board = getBoard();
        if (board != null) {
            return board.getDoc(); // TODO: return a collection of documents
        }
        return null;
    }

    @Override
    public Set<String> getAllDeviceNames() {
        if (fAllDeviceNames == null) {
            fAllDeviceNames = new HashSet<>();
            IRteBoardDeviceItem di = getMountedDevices();
            if (di != null) {
                fAllDeviceNames.addAll(di.getAllDeviceNames());
            }
            di = getCompatibleDevices();
            if (di != null) {
                fAllDeviceNames.addAll(di.getAllDeviceNames());
            }
        }
        return fAllDeviceNames;
    }

}
