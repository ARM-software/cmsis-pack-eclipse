/*******************************************************************************
* Copyright (c) 2016 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* Ivor Williams - Initial API and implementation: https://dzone.com/articles/javaswt-click-table-column
* ARM Ltd and ARM Germany GmbH - application-specific implementation
*******************************************************************************/

package com.arm.cmsis.pack.ui.tree;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.utils.AlnumComparator;

/**
 * The Tree comparator to sort the rows of each column alpha-numerically
 */
public class TreeColumnComparator extends ViewerComparator {

	protected final TreeViewer treeViewer;
	protected final IColumnAdvisor columnAdvisor;
	protected final AlnumComparator alnumComparator;
	protected int columnIndex;
	protected int maxColumnIndex;
	protected boolean bDescending;
	protected boolean bNumeric;
	protected Set<Integer> numericColumns;

	public TreeColumnComparator(TreeViewer viewer, IColumnAdvisor advisor) {
		this(viewer, advisor, -1); // -1 == no sort
	}

	public TreeColumnComparator(TreeViewer viewer, IColumnAdvisor advisor, int index) {
		treeViewer = viewer;
		columnAdvisor = advisor;
		columnIndex = index; // no sort
		bDescending = false;
		bNumeric = false;
		numericColumns = null;
		alnumComparator = new AlnumComparator(false, false);
		addColumnSelectionListeners(treeViewer);
	}

	public void setNumericColumns(Set<Integer> columns) {
		numericColumns = columns;
		bNumeric = isNumericColumn(columnIndex);
	}
	
	/**
	 * Checks if column is numeric
	 * @param index column index
	 * @return true if column is numeric
	 */
	public boolean isNumericColumn(int index) {
		if(numericColumns == null || numericColumns.size() == 0)
			return false;
		return numericColumns.contains(index);
	}

	/**
	 * Checks if current column is numeric
	 * @return true if current column is numeric
	 */
	public boolean isNumeric() {
		return bNumeric;
	}

	private void addColumnSelectionListeners(TreeViewer treeViewer) {
		TreeColumn[] columns = treeViewer.getTree().getColumns();
		for (int i = 0; i < columns.length; i++) {
			addColumnSelectionListener(columns[i]);
		}
	}

	private void addColumnSelectionListener(TreeColumn column) {
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				treeColumnClicked((TreeColumn) e.widget);
			}
		});
	}

	void treeColumnClicked(TreeColumn column) {
		Tree tree = treeViewer.getTree();
		if(column == null) {
			columnIndex = -1;
			bNumeric = false;
			tree.setSortColumn(column);
		} else {
			if (column.equals(tree.getSortColumn())) {
				tree.setSortDirection(tree.getSortDirection() == SWT.UP ? SWT.DOWN : SWT.UP);
			} else {
				tree.setSortColumn(column);
				tree.setSortDirection(SWT.UP);
			}
			updateColumnIndex();
		}
		treeViewer.refresh();
	}

	/**
	 * Returns current column index for sorting
	 * @return current column index 
	 */
	public int getColumnIndex() {
		return columnIndex;
	}
	
	/**
	 * Sets column index to sort, disables sorting when index is negative or exceeds column count
	 * @param index column index to use
	 */
	public void setColumnIndex(int index) { // actually to disable/enable sorting
		columnIndex = index;
		Tree tree = treeViewer.getTree();
		TreeColumn[] columns = tree.getColumns();
		if(columns == null || columnIndex >= columns.length) {
			columnIndex = -1;
		}
		bNumeric = isNumericColumn(columnIndex);
		if(columns == null ||columnIndex < 0) {
			treeColumnClicked(null);
		} else {
			treeColumnClicked(columns[columnIndex]);
		}
	}

	protected void updateColumnIndex() {
		Tree tree = treeViewer.getTree();
		List<TreeColumn> columns = Arrays.asList(tree.getColumns());
		int columnCount = columns.size();
		columnIndex = columns.indexOf(tree.getSortColumn());
		if(columnIndex >= columnCount)
			columnIndex = -1;
		bNumeric = isNumericColumn(columnIndex);
		bDescending = tree.getSortDirection() == SWT.DOWN;
	}

	
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		int index = getColumnIndex();
		if (index < 0)
			return 0;

		String str1;
		String str2;
		CellLabelProvider cellLabelProvider = treeViewer.getLabelProvider(index);
		if(cellLabelProvider instanceof ILabelProvider) {
			ILabelProvider labelProvider = (ILabelProvider) cellLabelProvider;
			str1 = labelProvider.getText(e1);
			str2 = labelProvider.getText(e2);
		} else if(columnAdvisor != null) {
			str1 = columnAdvisor.getString(e1, index);
			str2 = columnAdvisor.getString(e2, index);
		} else {
			return 0;
		}
		int result = 0;
		if(bNumeric) {
			result = numericalCompare(str1, str2);
		} else {
			result = alnumComparator.compare(str1, str2);
		}
		return bDescending ? -result : result;

	}
	
	public static int numericalCompare(final String str1, final String str2) {
		// allow comparison of null and empty strings 
		if (str1 == null || str1.isEmpty()) {
			if (str2 == null || str2.isEmpty()) {
				return 0;
			}
			return -1;
		} else if (str2 == null || str2.isEmpty()) {
			return 1;
		}
		long l1 = IAttributes.stringToLong(str1, 0L);
		long l2 = IAttributes.stringToLong(str2, 0L);
		return Long.compare(l1, l2);
	}

}
