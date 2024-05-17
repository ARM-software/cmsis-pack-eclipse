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

package com.arm.cmsis.pack.installer.ui.views;

import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackManager;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.installer.ui.IHelpContextIds;
import com.arm.cmsis.pack.installer.ui.Messages;
import com.arm.cmsis.pack.item.CmsisMapItem;
import com.arm.cmsis.pack.item.ICmsisMapItem;
import com.arm.cmsis.pack.rte.boards.IRteBoardDeviceItem;
import com.arm.cmsis.pack.rte.boards.IRteBoardItem;
import com.arm.cmsis.pack.ui.tree.AdvisedCellLabelProvider;
import com.arm.cmsis.pack.ui.tree.BoardTreeColumnComparator;
import com.arm.cmsis.pack.ui.tree.BoardViewContentProvider;
import com.arm.cmsis.pack.ui.tree.BoardsViewColumnAdvisor;
import com.arm.cmsis.pack.ui.tree.BoardsViewLabelProvider;

/**
 * Default implementation of the boards view in pack manager
 */
public class BoardsView extends PackInstallerView {

    public static final String ID = "com.arm.cmsis.pack.installer.ui.views.BoardsView"; //$NON-NLS-1$

    public BoardsView() {
    }

    @Override
    protected String getHelpContextId() {
        return IHelpContextIds.BOARDS_VIEW;
    }

    @Override
    public boolean isFilterSource() {
        return true;
    }

    @Override
    public void createTreeColumns() {

        fTree.setInitialText(Messages.BoardsView_SearchBoard);

        TreeViewerColumn column0 = new TreeViewerColumn(fViewer, SWT.LEFT);
        column0.getColumn().setText(CmsisConstants.BOARD_TITLE);
        column0.getColumn().setWidth(200);
        column0.setLabelProvider(new BoardsViewLabelProvider(true));

        TreeViewerColumn column1 = new TreeViewerColumn(fViewer, SWT.LEFT);
        column1.getColumn().setText(CmsisConstants.SUMMARY_TITLE);
        column1.getColumn().setWidth(300);
        BoardsViewColumnAdvisor columnAdvisor = new BoardsViewColumnAdvisor(fViewer);
        column1.setLabelProvider(new AdvisedCellLabelProvider(columnAdvisor, COLURL));

        fViewer.setContentProvider(new BoardViewContentProvider(true));
        fViewer.setComparator(new BoardTreeColumnComparator(fViewer, columnAdvisor));
        fViewer.setAutoExpandLevel(2);
    }

    @Override
    protected void refresh() {
        if (CpPlugIn.getDefault() == null) {
            return;
        }
        ICpPackManager packManager = CpPlugIn.getPackManager();
        if (packManager != null && packManager.getBoards() != null) {
            ICmsisMapItem<IRteBoardDeviceItem> root = new CmsisMapItem<>();
            IRteBoardItem allBoardRoot = packManager.getRteBoards();
            root.addChild(allBoardRoot);
            if (!fViewer.getControl().isDisposed()) {
                fViewer.setInput(root);
            }
        } else {
            if (!fViewer.getControl().isDisposed()) {
                fViewer.setInput(null);
            }
        }
    }
}
