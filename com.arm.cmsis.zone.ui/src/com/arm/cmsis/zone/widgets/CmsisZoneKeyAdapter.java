/*******************************************************************************
* Copyright (c) 2021 ARM Ltd. and others
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

package com.arm.cmsis.zone.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.arm.cmsis.pack.ui.tree.IColumnAdvisor.CellControlType;
import com.arm.cmsis.zone.data.ICpMemoryBlock;
import com.arm.cmsis.zone.data.ICpZone;
import com.arm.cmsis.zone.ui.Messages;
import com.arm.cmsis.zone.ui.editors.CmsisZoneController;

/**
 * Key adapter for CmsisZoneTreeWidget TODO: move this class to
 * com.arm.cmsis.pack.ui
 */
public class CmsisZoneKeyAdapter extends KeyAdapter {

    protected CmsisZoneColumnAdvisor fAdvisor = null;
    protected Tree fTree = null;

    public CmsisZoneKeyAdapter(CmsisZoneColumnAdvisor columnAdvisor, Tree tree) {
        fAdvisor = columnAdvisor;
        fTree = tree;
        fTree.addKeyListener(this);
    }

    @Override
    public void keyPressed(KeyEvent event) {
        switch (event.keyCode) {
        case SWT.SPACE:
            processSpacePressed();
            break;
        case SWT.DEL:
            processDeletePressed();
            break;
        default:
            break;
        }
    }

    public void processDeletePressed() {
        if (fTree.getSelectionCount() < 1)
            return;
        List<ICpMemoryBlock> selectedBlocks = getSelectedBlocks(true);
        if (selectedBlocks.isEmpty())
            return;
        String msg = Messages.CmsisZoneKeyAdapter_DeleteSelectedMemoryRegions;
        boolean yes = MessageDialog.openQuestion(fTree.getShell(), Messages.CmsisZoneKeyAdapter_DeleteMemoryRegions,
                msg);
        if (yes) {
            CmsisZoneController controller = fAdvisor.getModelController();
            // run async as we will remove the tree
            Display.getDefault().asyncExec(() -> controller.deleteMemoryBlocks(selectedBlocks));
        }
    }

    public void processSpacePressed() {
        if (fTree.getSelectionCount() < 1)
            return;
        assignBlocks(0);
    }

    public List<ICpMemoryBlock> getSelectedBlocks(boolean bDeletable) {
        List<ICpMemoryBlock> selectedBlocks = new ArrayList<>();
        TreeItem[] selection = fTree.getSelection();
        for (TreeItem item : selection) {
            Object obj = item.getData();
            ICpMemoryBlock b = CmsisZoneColumnAdvisor.getMemoryBlock(obj);
            if (b == null)
                continue;
            if (bDeletable && !b.isDeletable())
                continue;
            selectedBlocks.add(b);
        }
        return selectedBlocks;
    }

    public void assignBlocks(int columntIndex) {
        ICpZone zone = fAdvisor.getZone(columntIndex);
        if (zone == null)
            return;
        List<ICpMemoryBlock> selectedBlocks = new ArrayList<>();
        TreeItem[] selection = fTree.getSelection();
        int nChecked = 0;
        for (TreeItem item : selection) {
            Object obj = item.getData();
            ICpMemoryBlock b = CmsisZoneColumnAdvisor.getMemoryBlock(obj);
            if (b == null)
                continue;
            CellControlType type = fAdvisor.getCellControlType(obj, columntIndex);
            if (type != CellControlType.INPLACE_CHECK)
                continue;
            if (fAdvisor.getCheck(obj, columntIndex))
                nChecked++;
            selectedBlocks.add(b);
        }
        if (selectedBlocks.isEmpty())
            return;
        boolean bAssign = selectedBlocks.size() != nChecked;
        fAdvisor.getModelController().assignBlocks(selectedBlocks, zone.getName(), bAssign);
    }

}
