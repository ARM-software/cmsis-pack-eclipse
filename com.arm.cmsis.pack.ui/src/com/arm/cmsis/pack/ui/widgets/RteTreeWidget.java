/*******************************************************************************
* Copyright (c) 2017 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/
package com.arm.cmsis.pack.ui.widgets;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;

import com.arm.cmsis.pack.events.IRteController;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.CpStringsUI;

public abstract class RteTreeWidget<TController extends IRteController> extends RteWidget<TController> {

	protected TreeViewer fTreeViewer = null;	 
	protected IRteColumnAdvisor<TController> fColumnAdvisor;

	protected Action expandAll;
	protected Action collapseAll;

	/**
	 * Return the tree viewer embedded in this widget
	 * @return
	 */
	public TreeViewer getViewer() {
		return fTreeViewer;
	}
	

	/**
	 * Sets an RTE model controller to be used by the widget 
	 * @param modelController IRteController controller to use
	 */
	protected void setModelController(TController modelController) {
		super.setModelController(modelController);
		if(fColumnAdvisor != null)
			fColumnAdvisor.setModelController(modelController);
	}
	
	/**
	 * Returns Column adviser 
	 * @return IColumnAdvisor
	 */
	public IRteColumnAdvisor<TController> getColumnAdvisor() {
		return fColumnAdvisor;
	}

	/**
	 * Sets column adviser
	 * @param columnAdvisor IColumnAdvisor
	 */
	public void setColumnAdvisor(IRteColumnAdvisor<TController> columnAdvisor) {
		fColumnAdvisor = columnAdvisor;
	}

	/**
	 * Returns Composite that should be used as focus widget
	 * @return widget to set focus to 
	 */
	public Composite getFocusWidget() {
		if(fTreeViewer != null) {
			return fTreeViewer.getTree();
		}
		return null;
	}
	
	protected void hookContextMenu() {
		if(fTreeViewer == null)
			return;
		final MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		makeActions();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(manager -> fillContextMenu(menuMgr));
		Menu menu = menuMgr.createContextMenu(fTreeViewer.getControl());
		fTreeViewer.getControl().setMenu(menu);
	}
	
	protected void fillContextMenu(IMenuManager manager) {
		manager.add(expandAll);
		manager.add(collapseAll);
	}
	
	protected void makeActions() {
		expandAll = new Action() {
			@Override
			public void run() {
				if(fTreeViewer == null) {
					return;
				}
				fTreeViewer.expandAll();
			}
		};

		expandAll.setText(CpStringsUI.ExpandAll);
		expandAll.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_EXPAND_ALL));

		collapseAll = new Action() {
			@Override
			public void run() {
				if(fTreeViewer == null) {
					return;
				}
				fTreeViewer.collapseAll();
			}
		};
		collapseAll.setText(CpStringsUI.CollapseAll);
		collapseAll.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_COLLAPSE_ALL));
	}
}
