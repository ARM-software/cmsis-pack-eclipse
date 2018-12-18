/*******************************************************************************
* Copyright (c) 2015 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* Eclipse Project - generation from template   
* ARM Ltd and ARM Germany GmbH - application-specific implementation
*******************************************************************************/
package com.arm.cmsis.pack.refclient.ui;


import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackManager;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpDeviceItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.enums.EDeviceHierarchyLevel;
import com.arm.cmsis.pack.events.IRteEventListener;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.generic.Attributes;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.CpStringsUI;
import com.arm.cmsis.pack.ui.tree.TreeObjectContentProvider;

public class DeviceTreeView extends ViewPart implements IRteEventListener{

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.arm.cmsis.pack.refclient.ui.DeviceTreeView"; //$NON-NLS-1$

	TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	private Action expandAction;
	private Action collapseAction;
	Action doubleClickAction;
	
	IRteDeviceItem getDeviceTreeItem(Object obj) {
		if (obj instanceof IRteDeviceItem) {
			return (IRteDeviceItem)obj;
		}
		return null;
	}

	class DeviceViewContentProvider extends TreeObjectContentProvider {

		public Object getParent(Object child) {
			IRteDeviceItem rteDeviceItem = getDeviceTreeItem(child); 
			if(rteDeviceItem != null) {
				return rteDeviceItem.getParent();
			}
			return null;
		}
		public Object [] getChildren(Object parent) {
			IRteDeviceItem rteDeviceItem = getDeviceTreeItem(parent); 
			if(rteDeviceItem != null) {
				if(!rteDeviceItem.hasChildren()) {
					ICpDeviceItem device = rteDeviceItem.getDevice();
					if(device != null) {
						ICpItem props = device.getEffectiveProperties(rteDeviceItem.getProcessorName());
						return props.getChildArray();
					}
				}
			}  
			ICpItem item = ICpItem.cast(parent);
			if(item != null && item.providesEffectiveContent()){
				ICpItem effective = item.getEffectiveContent();
				if(effective != null)
					return effective.getChildArray();
			}
			
			return super.getChildren(parent);
		}

		public boolean hasChildren(Object parent) {
			IRteDeviceItem rteDeviceItem = getDeviceTreeItem(parent); 
			if(rteDeviceItem != null) {
				if(rteDeviceItem.hasChildren())
					return true;
				ICpDeviceItem device = rteDeviceItem.getDevice();
				if(device != null) {
					ICpItem props = device.getEffectiveProperties(rteDeviceItem.getProcessorName());
					return props.hasChildren();
				}
				return false;
			} 
			ICpItem item = ICpItem.cast(parent);
			if(item != null && item.providesEffectiveContent()){
				ICpItem effective = item.getEffectiveContent();
				if(effective != null)
					return true;
			}
			return super.hasChildren(parent);
		}
	}
	
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider{
	
		@Override
		public String getColumnText(Object obj, int index){
			IRteDeviceItem rteDeviceItem = getDeviceTreeItem(obj); 
			if(rteDeviceItem != null) {
				switch(index) {
				case 0:
					return rteDeviceItem.getName();
				case 1:
					return EDeviceHierarchyLevel.toString(rteDeviceItem.getLevel());
				case 2:
					if(!rteDeviceItem.hasChildren()) {
						ICpDeviceItem device = rteDeviceItem.getDevice();
						if(device != null) {
							ICpItem props = device.getEffectiveProperties(rteDeviceItem.getProcessorName());
							if(props != null)
								return props.attributes().toString();
						}
					}
				default:
					break;
				}
				return CmsisConstants.EMPTY_STRING;
			}
			ICpItem prop = ICpItem.cast(obj);
			if(prop != null) {
				switch(index) {
				case 0:
					return prop.getTag();
				case 1:
					return CpStringsUI.DeviceTreeView_Property;
				case 2:
					String text = prop.getText();
					if(text != null && !text.isEmpty())
						return text;
					return prop.attributes().toString();
				default:
					break;
				}
			}
			return CmsisConstants.EMPTY_STRING;
		}

		@Override
		public Image getColumnImage(Object obj, int index){
			if(index == 0)
				return getImage(obj);
			return null;
		}

		@Override
		public Image getImage(Object obj){
			IRteDeviceItem rteDeviceItem = getDeviceTreeItem(obj); 
			if(rteDeviceItem != null) {
				if(rteDeviceItem.hasChildren())
					return CpPlugInUI.getImage(CpPlugInUI.ICON_FOLDER);
				return CpPlugInUI.getImage(CpPlugInUI.ICON_DEVICE); 
			}
			ICpItem item = ICpItem.cast(obj);
			if(item != null) {
				switch(item.getTag()) {
				case CmsisConstants.ALGORITHM_TAG:
					return CpPlugInUI.getImage(CpPlugInUI.ICON_FILE);
				case CmsisConstants.BOOK_TAG:
					return CpPlugInUI.getImage(CpPlugInUI.ICON_BOOK);
				default:
					break;
				}
			}
			
			return  CpPlugInUI.getImage(CpPlugInUI.ICON_ITEM);
		}
	}

	/**
	 * The constructor.
	 */
	public DeviceTreeView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {

		Tree tree = new Tree(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		viewer = new TreeViewer(tree);

		TreeColumn column0 = new TreeColumn(tree, SWT.LEFT);
		column0.setText(CpStringsUI.DeviceTreeView_Name);
		column0.setWidth(400);
		
		TreeColumn column1 = new TreeColumn(tree, SWT.LEFT);
		column1.setText(CpStringsUI.DeviceTreeView_Type);
		column1.setWidth(80);

		TreeColumn column2 = new TreeColumn(tree, SWT.LEFT);
		column2.setText(CpStringsUI.DeviceTreeView_Attributes);
		column2.setWidth(400);
		
		
		drillDownAdapter = new DrillDownAdapter(viewer);

		viewer.setContentProvider(new DeviceViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		refresh();
		
		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), ID); 
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		
		CpPlugIn.addRteListener(this);
	}

	protected void refresh() {
		if(CpPlugIn.getDefault() == null)
			return;
		ICpPackManager packManager = CpPlugIn.getPackManager();
		if(packManager != null)
			viewer.setInput(packManager.getInstalledDevices());
		else 
			viewer.setInput(null);
	}

	
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				DeviceTreeView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(expandAction);
		manager.add(collapseAction);
	}

	void fillContextMenu(IMenuManager manager) {
		manager.add(expandAction);
		manager.add(collapseAction);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(expandAction);
		manager.add(collapseAction);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private void makeActions() {
		expandAction = new Action() {
			public void run() {
				if(viewer == null)
					return;
				viewer.expandAll();
			}
		};

		expandAction.setText(CpStringsUI.ExpandAll);
		expandAction.setToolTipText(CpStringsUI.ExpandAllNodes);
		expandAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_EXPAND_ALL));
		
		collapseAction = new Action() {
			public void run() {
				if(viewer == null)
					return;
				viewer.collapseAll();
			}
		};
		collapseAction.setText(CpStringsUI.CollapseAll);
		collapseAction.setToolTipText(CpStringsUI.CollapseAllNodes);
		collapseAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_COLLAPSE_ALL));
		
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				if(obj instanceof ICpItem) {
					ICpItem item = (ICpItem)obj;
					String s = item.getName();
					s += "\n"; //$NON-NLS-1$
					Attributes a = new Attributes();
					Map<String, String> m = item.getEffectiveAttributes(null); 
					a.setAttributes(m);
					s += a.toString(); 
					showMessage(s);
				}
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"DeviceView", //$NON-NLS-1$
			message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
	@Override
	public void handle(RteEvent event) {
		if(event.getTopic().equals(RteEvent.PACKS_RELOADED)) 
			refresh();
	}

	@Override
	public void dispose() {
		if(CpPlugIn.getDefault() != null)
				CpPlugIn.removeRteListener(this);
		super.dispose();
	}
}
