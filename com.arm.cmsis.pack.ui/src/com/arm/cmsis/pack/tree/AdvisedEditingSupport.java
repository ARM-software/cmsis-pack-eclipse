/*******************************************************************************
* Copyright (c) 2014 ARM Ltd.
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
package com.arm.cmsis.pack.tree;

import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellEditor.LayoutData;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;


/**
 * This class implements editing possibilities for table cell, e.g. check box, combo box, etc.
 * In case of combo a combo box cell Editor is return, in case of check box, a check box control is returned.
 */
public class AdvisedEditingSupport extends EditingSupport {

	TextCellEditor textCellEditor = null;
	CheckboxCellEditor checkboxCellEditor = null;
	int columnIndex;
	IColumnAdvisor columnAdvisor = null;
	
	
	public AdvisedEditingSupport(TreeViewer treeViewer, IColumnAdvisor columnAdvisor, int columnIndex) {
		
		super(treeViewer);
		this.columnIndex = columnIndex;
		this.columnAdvisor = columnAdvisor;
	}
	
	@Override
	protected CellEditor getCellEditor(final Object element) {
		final TreeViewer treeViewer = (TreeViewer)getViewer();
		
		switch (columnAdvisor.getCellControlType(element, columnIndex)) {
			case CHECK:
				return new CheckboxCellEditor(treeViewer.getTree());
			case COMBO:
				final ComboBoxCellEditor cellEditor = new ComboBoxCellEditor(treeViewer.getTree(), columnAdvisor.getStringArray(element, columnIndex), SWT.READ_ONLY) {

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
					public void widgetDefaultSelected(SelectionEvent e) {
					}
					public void widgetSelected(SelectionEvent e) {
						Integer newVal = new Integer(combo.getSelectionIndex());
						setValue(element, newVal);
					}
				});
				
				return cellEditor;
			default: break;
		}
		
		return null;
	}

	@Override
	protected Object getValue(Object element) {
		
		switch (columnAdvisor.getCellControlType(element, columnIndex)) {
		case CHECK:
			return new Boolean(columnAdvisor.getCheck(element, columnIndex));
		case COMBO:
			return columnAdvisor.getCurrentSelectedIndex(element, columnIndex);
		default:
			break;
		}
		
		return null;
	}

	@Override
	protected void setValue(Object element, Object value) {
		switch (columnAdvisor.getCellControlType(element, columnIndex)) {
		case CHECK:
			if (value instanceof Boolean) { 
				boolean newVal = ((Boolean)value == true);
				boolean oldVal = columnAdvisor.getCheck(element, columnIndex);
				if (oldVal != newVal) {
					columnAdvisor.propertyChange(new PropertyChangeEvent(element, "CheckBox", oldVal, newVal), columnIndex);
				}
			}
			break;
		case COMBO:
			if (value instanceof Integer) {
				int oldVal = columnAdvisor.getCurrentSelectedIndex(element, columnIndex);
				int newVal = ((Integer)value).intValue();
				if (newVal != oldVal) {
					columnAdvisor.propertyChange(new PropertyChangeEvent(element, "ComboSelectionChanged", oldVal, newVal), columnIndex);
				}
			}
			break;
		default:
			break;
		}
	}

	@Override
	protected boolean canEdit(Object element) {
		boolean editable = columnAdvisor.canEdit(element, columnIndex);
		if (!editable) {
			// send propertyChange
			// property	"click"
			// oldVal	columnIndex
			// newVal	columnIndex
			columnAdvisor.propertyChange(new PropertyChangeEvent(element, "click", new Object(), new Object()), columnIndex);
		}
		return editable;
	}
}
