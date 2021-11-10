/*******************************************************************************
* Copyright (c) 2021 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.zone.ui.wizards;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.arm.cmsis.pack.generic.ITreeObject;
import com.arm.cmsis.pack.ui.tree.AdvisedCellLabelProvider;
import com.arm.cmsis.pack.ui.tree.CpItemColumnAdvisor;
import com.arm.cmsis.pack.ui.tree.IColumnAdvisor;
import com.arm.cmsis.pack.ui.tree.TreeObjectContentProvider;
import com.arm.cmsis.pack.ui.wizards.CpItemTreeDialog;
import com.arm.cmsis.zone.data.ICpPeripheral;
import com.arm.cmsis.zone.data.ICpSlot;
import com.arm.cmsis.zone.ui.Messages;

/**
 * Dialog to configure periphela's slots : pins or channels
 */
public class CpPeripheralSlotSetupDlg extends CpItemTreeDialog {

    protected ICpPeripheral fPeripheral = null;
    protected ICpPeripheral fOriginalPeripheral = null;
    protected int fIndexSecurity = -1;
    protected int fIndexPrivilege = -1;

    class CpPeripheralSlotContentProvider extends TreeObjectContentProvider {

        @Override
        public Object[] getChildren(Object parentElement) {
            if (parentElement != null && parentElement == fPeripheral) {
                Collection<ICpSlot> slots = fPeripheral.getSlots();
                if (slots != null && !slots.isEmpty())
                    return slots.toArray();
            }
            return ITreeObject.EMPTY_OBJECT_ARRAY;
        }

        @Override
        public boolean hasChildren(Object element) {
            if (element != null && element == fPeripheral) {
                Collection<ICpSlot> slots = fPeripheral.getSlots();
                return slots != null && !slots.isEmpty();
            }
            return false;
        }
    }

    class CpPeripheralSlotColumnAdvisor extends CpItemColumnAdvisor {

        public CpPeripheralSlotColumnAdvisor(ColumnViewer columnViewer) {
            super(columnViewer);
        }

        @Override
        public CellControlType getCellControlType(Object obj, int columnIndex) {
            if (columnIndex > 0)
                return CellControlType.INPLACE_CHECK;
            return CellControlType.NONE;
        }

        @Override
        public boolean canEdit(Object obj, int columnIndex) {
            return getCellControlType(obj, columnIndex) == CellControlType.INPLACE_CHECK;
        }

        @Override
        public boolean getCheck(Object obj, int columnIndex) {
            ICpSlot slot = ITreeObject.castTo(obj, ICpSlot.class);
            if (slot == null)
                return false;
            if (columnIndex == fIndexSecurity)
                return slot.isSecure();
            else if (columnIndex == fIndexPrivilege)
                return slot.isPrivileged();
            return false;
        }

        @Override
        public void setCheck(Object element, int columnIndex, boolean newVal) {
            ICpSlot slot = ITreeObject.castTo(element, ICpSlot.class);
            if (slot == null)
                return;
            boolean bModified = false;
            if (columnIndex == fIndexSecurity)
                bModified = slot.setSecure(newVal);
            else if (columnIndex == fIndexPrivilege) {
                bModified = slot.setPrivileged(newVal);
            }
            if (bModified)
                getViewer().refresh();
        }
    }

    public CpPeripheralSlotSetupDlg(Shell parentShell, ICpPeripheral peripheral) {
        super(parentShell, peripheral);
        fOriginalPeripheral = peripheral;
        fPeripheral = (ICpPeripheral) fItem; // cloned
        // assign column indexes
        String slotType = peripheral.getSlotType();
        int col = 1;
        if (slotType.indexOf('s') >= 0) {
            fIndexSecurity = col++;
        }
        if (slotType.indexOf('p') >= 0) {
            fIndexPrivilege = col;
        }
    }

    @Override
    protected ITreeContentProvider createContentProvider() {
        return new CpPeripheralSlotContentProvider();
    }

    @Override
    protected IColumnAdvisor createColumnAdvisor(TreeViewer viewer) {
        return new CpPeripheralSlotColumnAdvisor(viewer);
    }

    @Override
    protected void createColumns() {
        if (fPeripheral == null)
            return;
        super.createColumns(); // creates name column
        if (fIndexSecurity > 0) {
            TreeViewerColumn columnSecurity = new TreeViewerColumn(fTreeViewer, SWT.LEFT);
            columnSecurity.getColumn().setText(Messages.CpPeripheralSlotSetupDlg_Securiry);
            columnSecurity.getColumn().setWidth(100);
            columnSecurity.setLabelProvider(new AdvisedCellLabelProvider(fColumnAdvisor, fIndexSecurity));
        }

        if (fIndexPrivilege > 0) {
            TreeViewerColumn columnPrivilege = new TreeViewerColumn(fTreeViewer, SWT.LEFT);
            columnPrivilege.getColumn().setText(Messages.CpPeripheralSlotSetupDlg_Privilege);
            columnPrivilege.getColumn().setWidth(100);
            columnPrivilege.setLabelProvider(new AdvisedCellLabelProvider(fColumnAdvisor, fIndexPrivilege));
        }
    }

    @Override
    protected void init() {
        String slotName = fPeripheral.getSlotName();
        setTitle(fPeripheral.getName() + Messages.CpPeripheralSlotSetupDlg_SemicolonWithSpaces
                + Messages.CpPeripheralSlotSetupDlg_Configure + fPeripheral.getSlotName());
        lblTreeLabel.setText(slotName);
        super.init();
    }

    @Override
    public boolean apply() {
        boolean bModified = false;
        Collection<ICpSlot> slots = fPeripheral.getSlots();
        Collection<ICpSlot> originalSlots = fOriginalPeripheral.getSlots();
        Iterator<ICpSlot> it = slots.iterator();
        Iterator<ICpSlot> ito = originalSlots.iterator();

        while (it.hasNext() && ito.hasNext()) {
            ICpSlot s = it.next();
            ICpSlot so = ito.next();
            if (so.updateAttributes(s))
                bModified = true;
        }
        return bModified;
    }

}
