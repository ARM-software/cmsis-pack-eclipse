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
package com.arm.cmsis.pack.ui.tree;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellEditor.LayoutData;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * This class implements editing possibilities for table cell, e.g. check box,
 * combo box, etc. In case of combo a combo box cell Editor is return, in case
 * of check box, a check box control is returned.
 */
public class AdvisedEditingSupport extends EditingSupport {

    TextCellEditor textCellEditor = null;
    CheckboxCellEditor checkboxCellEditor = null;
    int columnIndex;
    IColumnAdvisor columnAdvisor = null;

    public AdvisedEditingSupport(ColumnViewer treeViewer, IColumnAdvisor columnAdvisor, int columnIndex) {
        super(treeViewer);
        this.columnIndex = columnIndex;
        this.columnAdvisor = columnAdvisor;
    }

    @Override
    protected CellEditor getCellEditor(final Object element) {
        Composite composite = (Composite) getViewer().getControl();

        switch (columnAdvisor.getCellControlType(element, columnIndex)) {
        case SPIN:
            SpinnerCellEditor sce = new SpinnerCellEditor(composite);
            sce.getSpinner().setMinimum(columnAdvisor.getMinCount(element, columnIndex));
            sce.getSpinner().setMaximum(columnAdvisor.getMaxCount(element, columnIndex));
            sce.getSpinner().setBase(columnAdvisor.getItemBase(element, columnIndex));
            long spinStep = columnAdvisor.getSpinStep(element, columnIndex);
            sce.getSpinner().setIncrement(spinStep);
            // sce.getSpinner().setPageIncrement(spinStep*columnAdvisor.getItemBase(element,
            // columnIndex));
            sce.getSpinner().setSelection(columnAdvisor.getCurrentSelectedIndex(element, columnIndex));
            return sce;
        case MENU:
            MenuCellEditor mce = new MenuCellEditor(composite);
            return mce;
        case CHECK:
            return new CheckboxCellEditor(composite);
        case TEXT:
            return new TextCellEditor(composite);
        case COMBO:
            final ComboBoxCellEditor cellEditor = new ComboBoxCellEditor(composite,
                    columnAdvisor.getStringArray(element, columnIndex), SWT.READ_ONLY) {
                @Override
                public LayoutData getLayoutData() {
                    LayoutData ld = super.getLayoutData();
                    ld.minimumWidth = 20;
                    return ld;
                }
            };
            Control control = cellEditor.getControl();
            final CCombo combo = (CCombo) control;

            LayoutData ld = cellEditor.getLayoutData();
            ld.grabHorizontal = true;
            ld.horizontalAlignment = SWT.RIGHT;
            ld.verticalAlignment = SWT.CENTER;
            combo.setLayoutData(ld);

            combo.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                }

                @Override
                public void widgetSelected(SelectionEvent e) {
                    Integer newVal = Integer.valueOf(combo.getSelectionIndex());
                    setValue(element, newVal);
                }
            });

            return cellEditor;
        default:
            break;
        }

        return null;
    }

    @Override
    protected Object getValue(Object element) {

        switch (columnAdvisor.getCellControlType(element, columnIndex)) {
        case SPIN:
            return columnAdvisor.getCurrentSelectedIndex(element, columnIndex);
        case CHECK:
            return Boolean.valueOf(columnAdvisor.getCheck(element, columnIndex));
        case COMBO:
            return columnAdvisor.getCurrentSelectedIndex(element, columnIndex);
        case MENU:
            return columnAdvisor.getMenu(element, columnIndex);
        case TEXT:
            return columnAdvisor.getString(element, columnIndex);
        default:
            break;
        }

        return null;
    }

    @Override
    protected void setValue(Object element, Object value) {

        switch (columnAdvisor.getCellControlType(element, columnIndex)) {
        case SPIN:
            if (value instanceof String) {
                String valStr = (String) value;
                if (valStr.isEmpty()) {
                    return;
                }
                int base = columnAdvisor.getItemBase(element, columnIndex);
                switch (base) {
                case 16:
                case 2:
                    valStr = valStr.substring(2);
                    break;
                case 8:
                    valStr = valStr.substring(1);
                    break;
                default:
                    break;
                }
                long newVal = columnAdvisor.getMinCount(element, columnIndex);
                try {
                    newVal = Long.parseLong(valStr, base);
                } catch (NumberFormatException e) {
                }
                columnAdvisor.setCurrentSelectedIndex(element, columnIndex, newVal);

            }
            break;
        case CHECK:
            if (value instanceof Boolean) {
                boolean newVal = ((Boolean) value == true);
                columnAdvisor.setCheck(element, columnIndex, newVal);
            }
            break;
        case MENU:
            if (value instanceof String) {
                columnAdvisor.setString(element, columnIndex, (String) value);
            }
            break;
        case COMBO:
            if (value instanceof Integer) {
                int newVal = ((Integer) value).intValue();
                columnAdvisor.setCurrentSelectedIndex(element, columnIndex, newVal);
            }
            break;
        case TEXT:
            if (value instanceof String) {
                columnAdvisor.setString(element, columnIndex, (String) value);
            }
        default:
            break;
        }
    }

    @Override
    protected boolean canEdit(Object element) {
        boolean editable = columnAdvisor.canEdit(element, columnIndex);
        return editable;
    }
}
