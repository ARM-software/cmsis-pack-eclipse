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

import org.eclipse.jface.viewers.ColumnViewer;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackInstaller;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.rte.boards.IRteBoardItem;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;
import com.arm.cmsis.pack.ui.CpStringsUI;

public class BoardsViewColumnAdvisor extends ColumnAdvisor {
    protected static final int COLURL = 1;
    private static final String ALL_BOARDS = CmsisConstants.ALL_BOARDS;

    private DevicesViewColumnAdvisor devicesViewColumnAdvisor = new DevicesViewColumnAdvisor(null);

    public BoardsViewColumnAdvisor(ColumnViewer columnViewer) {
        super(columnViewer);
    }

    @Override
    public boolean isEnabled(Object obj, int columnIndex) {
        ICpPackInstaller packInstaller = getPackInstaller();
        if (packInstaller == null) {
            return false;
        }
        if (packInstaller.isUpdatingPacks()) {
            return false;
        }
        return super.isEnabled(obj, columnIndex);
    }

    @Override
    public CellControlType getCellControlType(Object obj, int columnIndex) {
        if (columnIndex == COLURL) {
            IRteBoardItem item = getBoardDeviceTreeItem(obj);
            if (item != null && item.getBoard() != null) {
                return CellControlType.URL;
            } else if (obj instanceof IRteDeviceItem) {
                return devicesViewColumnAdvisor.getCellControlType(obj, columnIndex);
            }
        }
        return CellControlType.TEXT;
    }

    @Override
    public String getString(Object obj, int columnIndex) {
        if (getCellControlType(obj, columnIndex) == CellControlType.URL) {
            IRteBoardItem item = getBoardDeviceTreeItem(obj);
            if (item != null) {
                if (item.getMountedDevices() != null) {
                    return item.getMountedDevices().getFirstChildKey();
                } else if (item.getCompatibleDevices() != null) {
                    return item.getCompatibleDevices().getFirstChildKey();
                } else if (item.getBoard() != null) {
                    return item.getBoard().getName();
                }
            } else if (obj instanceof IRteDeviceItem) {
                return devicesViewColumnAdvisor.getString(obj, columnIndex);
            }
        } else if (columnIndex == COLURL) {
            IRteBoardItem item = getBoardDeviceTreeItem(obj);
            if (item != null) {
                if (ALL_BOARDS.equals(item.getName())) {
                    int nrofBoards = item.getChildCount();
                    return nrofBoards + CpStringsUI.BoardsViewColumnAdvisor_Boards;
                }
            } else if (obj instanceof IRteDeviceItem) {
                return devicesViewColumnAdvisor.getString(obj, columnIndex);
            }
        }
        return null;
    }

    @Override
    public String getUrl(Object obj, int columnIndex) {
        if (getCellControlType(obj, columnIndex) == CellControlType.URL) {
            IRteBoardItem item = getBoardDeviceTreeItem(obj);
            if (item != null) {
                return item.getUrl();
            } else if (obj instanceof IRteDeviceItem) {
                return devicesViewColumnAdvisor.getTooltipText(obj, columnIndex);
            }
        }
        return null;
    }

    @Override
    public String getTooltipText(Object obj, int columnIndex) {
        if (getCellControlType(obj, columnIndex) == CellControlType.URL) {
            IRteBoardItem item = getBoardDeviceTreeItem(obj);
            if (item != null) {
                return item.getUrl();
            } else if (obj instanceof IRteDeviceItem) {
                return devicesViewColumnAdvisor.getTooltipText(obj, columnIndex);
            }
        }
        return null;
    }

    public ICpPackInstaller getPackInstaller() {
        if (CpPlugIn.getPackManager() == null) {
            return null;
        }
        return CpPlugIn.getPackManager().getPackInstaller();
    }

    IRteBoardItem getBoardDeviceTreeItem(Object obj) {
        if (obj instanceof IRteBoardItem) {
            return (IRteBoardItem) obj;
        }
        return null;

    }
}
