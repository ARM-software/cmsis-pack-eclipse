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

package com.arm.cmsis.pack.installer.ui.views;

import java.util.Arrays;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpPackCollection;
import com.arm.cmsis.pack.item.ICmsisItem;
import com.arm.cmsis.pack.ui.tree.ColumnAdvisor;

/**
 * The Tree comparator used to sort the rows of each column
 */
public class TreeColumnComparator extends ViewerComparator {

	protected final TreeViewer treeViewer;
	protected final ColumnAdvisor columnAdvisor;

	public TreeColumnComparator(TreeViewer viewer, ColumnAdvisor advisor) {
		treeViewer = viewer;
		columnAdvisor = advisor;
		addColumnSelectionListeners(treeViewer);
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
		Tree tree = column.getParent();
		if (column.equals(tree.getSortColumn())) {
			tree.setSortDirection(tree.getSortDirection() == SWT.UP ? SWT.DOWN : SWT.UP);
		} else {
			tree.setSortColumn(column);
			tree.setSortDirection(SWT.UP);
		}
		treeViewer.refresh();
	}

	protected int getColumnIndex() {
		Tree tree = treeViewer.getTree();
		int index = Arrays.asList(tree.getColumns()).indexOf(tree.getSortColumn());
		if (index == -1) {
			index = 0;
		}
		return index;
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		ICmsisItem cp1 = (ICmsisItem)e1;
		ICmsisItem cp2 = (ICmsisItem)e2;

		// Error packs should always be shown on top
		// Never switch the Device Specific and Generic row
		if (CmsisConstants.ERRORS.equals(cp1.getTag())) {
			return -1;
		} else if (CmsisConstants.ERRORS.equals(cp2.getTag())) {
			return 1;
		} else if (cp1 instanceof ICpPackCollection && cp2 instanceof ICpPackCollection) {
			return 0;
		}

		Tree tree = treeViewer.getTree();
		int index = Arrays.asList(tree.getColumns()).indexOf(tree.getSortColumn());
		if (index == -1) {
			index = 0;
		}
		int result = 0;
		if (index == 0 || index == 2) {
			ColumnLabelProvider colLabelProvider = (ColumnLabelProvider) treeViewer.getLabelProvider(index);
			String str1 = colLabelProvider.getText(e1);
			String str2 = colLabelProvider.getText(e2);
			result = str1 == null ? (str2 == null ? 0 : 1) : str1.compareToIgnoreCase(str2);
		} else if (index == 1) {
			String str1 = columnAdvisor.getString(e1, index);
			String str2 = columnAdvisor.getString(e2, index);
			result = str1.compareToIgnoreCase(str2);
		}

		return tree.getSortDirection() == SWT.DOWN ? -result : result;

	}

}
