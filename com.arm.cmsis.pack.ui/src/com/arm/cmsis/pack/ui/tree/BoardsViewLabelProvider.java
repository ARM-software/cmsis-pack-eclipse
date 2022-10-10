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

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpBoard;
import com.arm.cmsis.pack.rte.boards.IRteBoardDeviceItem;
import com.arm.cmsis.pack.rte.boards.IRteBoardItem;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.CpStringsUI;

public class BoardsViewLabelProvider extends ColumnLabelProvider {

    protected boolean fbInstallerContext = true;

    public BoardsViewLabelProvider(boolean installerContext) {
        super();
        this.fbInstallerContext = installerContext;
    }

    private DevicesViewLabelProvider devicesViewLabelProvider = new DevicesViewLabelProvider();

    @Override
    public String getText(Object element) {
        String text = CmsisConstants.EMPTY_STRING;
        if (element instanceof IRteBoardDeviceItem) {
            IRteBoardDeviceItem bddItem = (IRteBoardDeviceItem) element;

            if (bddItem.isBoard() && bddItem.getBoard() != null) {
                text = getBoardTitle(bddItem.getBoard());
            } else {
                text = bddItem.getName();
            }

            text += ' '; // append a spaces as a workaround to show the complete text in views
        }
        return text;
    }

    @Override
    public Image getImage(Object element) {
        if (element instanceof IRteBoardItem) {
            IRteBoardItem bdItem = (IRteBoardItem) element;
            if (CmsisConstants.NO_BOARD.equals(bdItem.getName())) {
                return null;
            }
            if (CmsisConstants.ALL_BOARDS.equals(bdItem.getName())) {
                return CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT_CLASS);
            }

            ICpBoard board = bdItem.getBoard();
            if (board == null) {
                return CpPlugInUI.getImage(CpPlugInUI.ICON_BOARD_DEPR);
            }

            if (bdItem.getBoard().isDeprecated()) {
                return CpPlugInUI.getImage(CpPlugInUI.ICON_BOARD_DEPR);
            }

            if (packInstalledAndContainsBoard(bdItem.getBoard())) {
                return CpPlugInUI.getImage(CpPlugInUI.ICON_BOARD);
            }
            return CpPlugInUI.getImage(CpPlugInUI.ICON_BOARD_GREY);
        } else if (element instanceof IRteBoardDeviceItem) {
            IRteBoardDeviceItem bddItem = (IRteBoardDeviceItem) element;
            if (CmsisConstants.MOUNTED_DEVICES.equals(bddItem.getName())
                    || CmsisConstants.COMPATIBLE_DEVICES.equals(bddItem.getName())) {
                return CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT_CLASS);
            }
            return devicesViewLabelProvider.getImage(bddItem.getRteDeviceItem());
        }
        return CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT_CLASS);
    }

    private boolean packInstalledAndContainsBoard(ICpBoard board) {
        if (board == null) {
            return false;
        }
        return board.getPack().getPackState().isInstalledOrLocal();
    }

    @Override
    public String getToolTipText(Object obj) {
        if (obj instanceof IRteBoardItem) {
            IRteBoardItem board = (IRteBoardItem) obj;
            if (board.getBoard() != null) {
                return NLS.bind(CpStringsUI.BoardsViewLabelProvider_AvailableInPack, board.getBoard().getPackId());
            }
        } else if (obj instanceof IRteDeviceItem) {
            return devicesViewLabelProvider.getToolTipText(obj);
        }
        return null;
    }

    String getBoardTitle(ICpBoard cpBoard) {
        String boardTitle = ICpBoard.constructBoardDisplayName(cpBoard);
        if (cpBoard.isDeprecated()) {
            boardTitle += ' ' + CpStringsUI.DeprecatedBoard;
        }
        return boardTitle;
    }
}