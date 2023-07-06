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

package com.arm.cmsis.pack.installer.ui;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackManager;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpBoard;
import com.arm.cmsis.pack.data.ICpExample;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPack.PackState;
import com.arm.cmsis.pack.data.ICpPackCollection;
import com.arm.cmsis.pack.data.ICpPackFamily;
import com.arm.cmsis.pack.installer.ui.views.BoardsView;
import com.arm.cmsis.pack.installer.ui.views.DevicesView;
import com.arm.cmsis.pack.installer.ui.views.PackInstallerView;
import com.arm.cmsis.pack.item.ICmsisItem;
import com.arm.cmsis.pack.rte.boards.IRteBoardDeviceItem;
import com.arm.cmsis.pack.rte.boards.IRteBoardItem;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;
import com.arm.cmsis.pack.rte.examples.IRteExampleItem;
import com.arm.cmsis.pack.utils.Utils;

/**
 * The filter used to filter the packs and examples
 */
public class PackInstallerViewFilter extends ViewerFilter {

    protected IRteDeviceItem fDeviceItem = null;
    protected IRteBoardDeviceItem fBoardItem = null;

    protected boolean bAllDevices = false;
    protected boolean bAllBoards = false;

    protected String fBoardName = null;
    protected Set<String> fSelectedDeviceNames = null;

    protected PackInstallerView fSelectionView = null;

    protected ICmsisItem fSelectedItem = null;
    protected List<String> fSelectionPath = null;

    protected boolean fbInstalledExamplesOnly = true;
    protected String filterString = null;

    protected Set<ICpPackFamily> fFilteredDevicePackFamilies = new HashSet<>();

    /**
     * Default constructor
     */
    public PackInstallerViewFilter() {
        fbInstalledExamplesOnly = true;
    }

    public void clear() {
        fSelectionView = null;
        clearSelection();
    }

    public void clearSelection() {
        fSelectionPath = null;
        fSelectedItem = null;
        fFilteredDevicePackFamilies.clear();
        fSelectedDeviceNames = null;
        fDeviceItem = null;
        fBoardItem = null;
    }

    public Set<ICpPackFamily> getFilteredDevicePackFamilies() {
        return fFilteredDevicePackFamilies;
    }

    public List<String> getSelectionPath() {
        return fSelectionPath;
    }

    public PackInstallerView getSelectionView() {
        return fSelectionView;
    }

    /**
     * Sets filter selection
     *
     * @param part
     * @param fSelection
     */
    public boolean setSelection(PackInstallerView view, ICmsisItem selectedItem, List<String> selectionPath) {
        fSelectionView = view;
        fSelectedItem = selectedItem;
        if (selectionPath.equals(fSelectionPath)) {
            return false;
        }
        fSelectionPath = selectionPath;
        boolean bFilterChanged = updateFilter();

        if (bFilterChanged) {
            updateFilterdPacks();
        }
        return bFilterChanged;
    }

    protected void updateFilterdPacks() {
        fFilteredDevicePackFamilies.clear();
        ICpPackManager pm = CpPlugIn.getPackManager();
        ICpPackCollection dfps = pm.getDevicePacks();
        if (dfps == null || !dfps.hasChildren())
            return;
        for (ICpItem item : dfps.getChildren()) {
            if (item instanceof ICpPackFamily) {
                ICpPackFamily f = (ICpPackFamily) item;
                if (isDevicePackFamilyFiltered(f))
                    fFilteredDevicePackFamilies.add(f);
            }
        }
    }

    protected boolean updateFilter() {
        filterString = createFilterString();
        if (fSelectionView instanceof DevicesView) {
            return setDeviceSelection();
        } else if (fSelectionView instanceof BoardsView) {
            return setBoardSelection();
        }
        return false;
    }

    protected boolean setDeviceSelection() {
        IRteDeviceItem item = null;
        if (fSelectedItem != null) {
            item = (IRteDeviceItem) fSelectedItem;
            bAllDevices = CmsisConstants.ALL_DEVICES.equals(fSelectedItem.getName());
        } else {
            bAllDevices = false;
        }
        return setDeviceItem(item);
    }

    protected boolean setDeviceItem(IRteDeviceItem item) {
        if (fDeviceItem == item)
            return false;
        fBoardItem = null;
        fBoardName = null;
        fDeviceItem = item;
        if (fDeviceItem != null && !bAllDevices) {
            fSelectedDeviceNames = item.getAllDeviceNames();
        } else {
            fSelectedDeviceNames = null;
        }
        return true;
    }

    protected boolean setBoardSelection() {
        if (fSelectedItem != null) {
            bAllBoards = CmsisConstants.ALL_BOARDS.equals(fSelectedItem.getName());
        } else {
            bAllBoards = false;
        }
        if (fSelectedItem instanceof IRteDeviceItem) {
            return setDeviceItem((IRteDeviceItem) fSelectedItem);
        }
        return setBoardItem((IRteBoardDeviceItem) fSelectedItem);
    }

    protected boolean setBoardItem(IRteBoardDeviceItem item) {
        if (fBoardItem == item)
            return false;
        fBoardItem = item;
        fDeviceItem = null;
        if (!bAllBoards && fBoardItem != null && fBoardItem.getRteBoard() != null) {
            fBoardName = fBoardItem.getRteBoard().getName();
            fSelectedDeviceNames = fBoardItem.getAllDeviceNames();
        } else {
            fBoardName = null;
            fSelectedDeviceNames = null;
        }
        return true;
    }

    public String getFilterString() {
        return filterString;
    }

    protected String createFilterString() {
        if (fSelectedItem == null) {
            if (fSelectionView instanceof DevicesView) {
                return Messages.PacksExamplesViewFilter_NoDevices;
            } else if (fSelectionView instanceof BoardsView) {
                return Messages.PacksExamplesViewFilter_NoBoards;
            }
            return null;
        }
        if (fSelectionView instanceof BoardsView && fSelectedItem.hasChildren()
                && (CmsisConstants.MOUNTED_DEVICES.equals(fSelectedItem.getName())
                        || CmsisConstants.COMPATIBLE_DEVICES.equals(fSelectedItem.getName()))) {
            return fSelectedItem.getChildren().iterator().next().getName() + " (" + fSelectedItem.getName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return fSelectedItem.getName();
    }

    /**
     * Set to filter out uninstalled examples
     *
     * @param installedOnly
     */
    public void setShowExamplesInstalledOnly(boolean installedOnly) {
        fbInstalledExamplesOnly = installedOnly;
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (element instanceof IRteExampleItem) {
            return selectExamples((IRteExampleItem) element);
        } else if (element instanceof ICpPackFamily) {
            return selectPacks((ICpPackFamily) element);
        }
        return true;
    }

    protected boolean isDevicePackFamilyFiltered(ICpPackFamily f) {
        ICpPack pack = f.getPack();
        if (pack == null) {
            return false;
        }

        if (pack.isDevicelessPack()) {
            return false;
        }

        if (fDeviceItem != null) {
            if (bAllDevices)
                return true;
            if (fSelectedDeviceNames == null)
                return false;
            return packContainsRteDevice(fDeviceItem, pack);
        } else if (fBoardItem != null) {
            if (bAllBoards)
                return true;
            if (fSelectedDeviceNames == null)
                return false;
            if (fBoardName == null)
                return false;
            return packContainsBoard(fBoardItem, pack);
        }
        return false;
    }

    protected boolean selectPacks(ICpPackFamily f) {
        ICpPack pack = f.getPack();
        if (pack == null) {
            return false;
        }

        if (pack.isDevicelessPack()) {
            return true;
        }

        return fFilteredDevicePackFamilies.contains(f);
    }

    /**
     * @param board The board
     * @param pack  The pack
     * @return true if pack contains this board, otherwise false
     */
    private boolean packContainsBoard(IRteBoardDeviceItem board, ICpPack pack) {

        // check if the pack contains a board
        Set<String> boardNames = pack.getBoardNames();
        if (boardNames != null && boardNames.contains(fBoardName))
            return true;

        Set<String> devicesContainedInPack = pack.getAllDeviceNames();
        if (Utils.checkIfIntersect(fSelectedDeviceNames, devicesContainedInPack)) {
            return true;
        }
        IRteDeviceItem deviceItem = board != null ? board.getRteDeviceLeaf() : null;
        return packContainsRteDevice(deviceItem, pack);
    }

    /**
     * Convert the ICpItem to IRteDeviceItem
     *
     * @param item
     * @return the corresponding IRteDeviceItem, null if no matching IRteDeviceItem
     *         is found
     */
    private IRteDeviceItem convertCpItemToRteDeviceItem(ICpItem item) {

        String vendorName = item.getVendor();
        String deviceName = CmsisConstants.EMPTY_STRING;
        if (item.hasAttribute(CmsisConstants.DFAMILY)) {
            deviceName = item.getAttribute(CmsisConstants.DFAMILY);
        } else if (item.hasAttribute(CmsisConstants.DSUBFAMILY)) {
            deviceName = item.getAttribute(CmsisConstants.DSUBFAMILY);
        } else if (item.hasAttribute(CmsisConstants.DNAME)) {
            deviceName = item.getAttribute(CmsisConstants.DNAME);
        } else if (item.hasAttribute(CmsisConstants.DVARIANT)) {
            deviceName = item.getAttribute(CmsisConstants.DVARIANT);
        }

        if (vendorName.isEmpty()) {
            return null;
        }

        IRteDeviceItem allRteDevices = CpPlugIn.getPackManager().getDevices();
        if (deviceName.isEmpty()) {
            return allRteDevices.getVendorItem(vendorName);
        }

        return allRteDevices.findItem(deviceName, vendorName, false);
    }

    private boolean packContainsRteDevice(IRteDeviceItem deviceItem, ICpPack pack) {
        if (fSelectedDeviceNames.isEmpty()) {
            return false;
        }

        if (deviceItem == fDeviceItem) {
            Set<String> devicesContainedInPack = pack.getAllDeviceNames();
            if (Utils.checkIfIntersect(fSelectedDeviceNames, devicesContainedInPack)) {
                return true;
            }
        }

        // Check if the mounted devices or compatible devices on this pack's board
        // intersect with deviceItem's devices
        Collection<? extends ICpItem> boards = pack.getGrandChildren(CmsisConstants.BOARDS_TAG);
        if (boards == null || boards.isEmpty()) {
            return false;
        }
        for (ICpItem item : boards) {
            if (!(item instanceof ICpBoard)) {
                continue;
            }
            ICpBoard b = (ICpBoard) item;
            Collection<ICpItem> mountedDevices = b.getMountedDevices();
            for (ICpItem mountedDevice : mountedDevices) {
                IRteDeviceItem mountedDeviceItemInPack = convertCpItemToRteDeviceItem(mountedDevice);
                if (mountedDeviceItemInPack == null) {
                    continue;
                }
                Set<String> temp = mountedDeviceItemInPack.getAllDeviceNames();
                if (Utils.checkIfIntersect(temp, fSelectedDeviceNames)) {
                    return true;
                }
            }
            Collection<ICpItem> compatibleDevices = b.getCompatibleDevices();
            for (ICpItem compatibleDevice : compatibleDevices) {
                // Check intersection
                IRteDeviceItem compatibleDeviceItemInPack = convertCpItemToRteDeviceItem(compatibleDevice);
                if (compatibleDeviceItemInPack == null) {
                    continue;
                }
                // Check intersection
                Set<String> temp = compatibleDeviceItemInPack.getAllDeviceNames();
                if (Utils.checkIfIntersect(temp, fSelectedDeviceNames)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean selectExamples(IRteExampleItem exampleItem) {

        if (exampleItem == null) {
            return false;
        }

        ICpExample example = exampleItem.getExample();
        if (example == null)
            return false;

        if (fDeviceItem == null && fBoardItem == null)
            return false;
        PackState packState = example.getPack().getPackState();
        if (fbInstalledExamplesOnly && packState != PackState.INSTALLED && packState != PackState.LOCAL
                && packState != PackState.GENERATED) {
            return false;
        }

        if (fDeviceItem != null) {
            if (bAllDevices) {
                return true;
            }
            return boardContainsDevice(example.getBoardId());
        }
        if (bAllBoards) {
            return true;
        }
        return exampleContainsBoard(example, fBoardItem.getBoard());
    }

    private boolean exampleContainsBoard(ICpExample example, ICpBoard board) {
        if (example == null || board == null)
            return false;
        String exampleBoardId = example.getBoardId();
        if (exampleBoardId == null || exampleBoardId.isEmpty())
            return false;
        String boardId = board.getId();
        if (boardId == null || boardId.isEmpty())
            return false;
        return boardId.contains(exampleBoardId);
    }

    private boolean boardContainsDevice(String boardId) {
        Collection<IRteBoardItem> boards = CpPlugIn.getPackManager().getRteBoards().findBoards(boardId);
        for (IRteBoardItem board : boards) {
            if (Utils.checkIfIntersect(board.getAllDeviceNames(), fSelectedDeviceNames))
                return true;
        }
        return false;
    }
}
