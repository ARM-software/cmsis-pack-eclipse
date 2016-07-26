/*******************************************************************************
 * Copyright (c) 2016 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/

package com.arm.cmsis.pack.installer.ui.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.part.ViewPart;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackManager;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack.PackState;
import com.arm.cmsis.pack.enums.EDeviceHierarchyLevel;
import com.arm.cmsis.pack.events.IRteEventListener;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.info.CpDeviceInfo;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.installer.ui.CpInstallerPlugInUI;
import com.arm.cmsis.pack.installer.ui.IHelpContextIds;
import com.arm.cmsis.pack.installer.ui.Messages;
import com.arm.cmsis.pack.item.CmsisMapItem;
import com.arm.cmsis.pack.item.ICmsisMapItem;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.tree.AdvisedCellLabelProvider;
import com.arm.cmsis.pack.ui.tree.ColumnAdvisor;
import com.arm.cmsis.pack.ui.tree.TreeObjectContentProvider;
import com.arm.cmsis.pack.utils.AlnumComparator;

public class DevicesView extends ViewPart implements IRteEventListener {

	public static final String ID = "com.arm.cmsis.pack.installer.ui.views.DevicesView"; //$NON-NLS-1$

	private static final int COLURL = 1;

	FilteredTree fTree;
	TreeViewer fViewer;
	//private DrillDownAdapter fDrillDownAdapter;
	private Action fExpandAction;
	private Action fExpandItemAction;
	private Action fCollapseAction;
	private Action fCollapseItemAction;
	private Action fRemoveSelection;
	private Action fHelpAction;
	Action fDoubleClickAction;

	private DevicesViewColumnAdvisor fColumnAdvisor;

	static IRteDeviceItem getDeviceTreeItem(Object obj) {
		if (obj instanceof IRteDeviceItem) {
			return (IRteDeviceItem)obj;
		}
		return null;
	}

	static ICpItem getCpItem(Object obj) {
		if (obj instanceof ICpItem) {
			return (ICpItem)obj;
		}
		return null;
	}

	static boolean stopAtCurrentLevel(IRteDeviceItem rteDeviceItem) {
		IRteDeviceItem firstChild = rteDeviceItem.getFirstChild();
		if (firstChild == null || firstChild.getLevel() == EDeviceHierarchyLevel.PROCESSOR.ordinal()) {
			return true;
		}
		return false;
	}

	static class DeviceViewContentProvider extends TreeObjectContentProvider {

		@Override
		public Object getParent(Object child) {
			IRteDeviceItem rteDeviceItem = getDeviceTreeItem(child);
			if(rteDeviceItem != null) {
				return rteDeviceItem.getParent();
			}
			return null;
		}

		@Override
		public Object [] getChildren(Object parent) {
			IRteDeviceItem rteDeviceItem = getDeviceTreeItem(parent);
			if(rteDeviceItem != null) {
				if(!rteDeviceItem.hasChildren() || stopAtCurrentLevel(rteDeviceItem)) {
					return null;
				}
				return rteDeviceItem.getChildArray();
			}
			return super.getChildren(parent);
		}

		@Override
		public boolean hasChildren(Object parent) {
			IRteDeviceItem rteDeviceItem = getDeviceTreeItem(parent);
			if(rteDeviceItem != null) {
				if (stopAtCurrentLevel(rteDeviceItem)) {
					return false;
				}
				return rteDeviceItem.hasChildren();
			}
			return super.hasChildren(parent);
		}
	}

	static class DevicesViewLabelProvider extends ColumnLabelProvider {

		@Override
		public String getText(Object obj) {
			IRteDeviceItem rteDeviceItem = getDeviceTreeItem(obj);
			if (rteDeviceItem != null) {
				// added spaces at last of text as a workaround to show the complete text in the views
				return removeColon(rteDeviceItem.getName()) + ' ';
			}
			return CmsisConstants.EMPTY_STRING;
		}

		private String removeColon(String string) {
			if (string.indexOf(':') != -1) {
				return string.substring(0, string.indexOf(':'));
			}
			return string;
		}

		@Override
		public Image getImage(Object obj){
			IRteDeviceItem rteDeviceItem = getDeviceTreeItem(obj);
			if(rteDeviceItem != null) {
				if (rteDeviceItem.getLevel() == EDeviceHierarchyLevel.VENDOR.ordinal()) {
					return CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT);
				} else if (rteDeviceItem.hasChildren() && !stopAtCurrentLevel(rteDeviceItem)) {
					return CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT_CLASS);
				} else if (packInstalledAndContainsDevice(rteDeviceItem)) {
					return CpPlugInUI.getImage(CpPlugInUI.ICON_DEVICE);
				} else {
					return CpPlugInUI.getImage(CpPlugInUI.ICON_DEPRDEVICE);
				}
			}

			return  null;
		}

		private boolean packInstalledAndContainsDevice(IRteDeviceItem rteDeviceItem) {
			IRteDeviceItem deviceItem;
			if (rteDeviceItem.getDevice() == null) {
				IRteDeviceItem parent = getClosestParentRteDeviceItem(rteDeviceItem);
				deviceItem = parent.findItem(rteDeviceItem.getName(), rteDeviceItem.getVendorName(), false);
				if (deviceItem == null) {
					return false;
				}
			} else {
				deviceItem = rteDeviceItem;
			}
			return deviceItem.getDevice().getPack().getPackState() == PackState.INSTALLED ||
					deviceItem.getDevice().getPack().getPackState() == PackState.GENERATED;
		}

		@Override
		public String getToolTipText(Object obj) {
			IRteDeviceItem item = getDeviceTreeItem(obj);
			IRteDeviceItem parent = getClosestParentRteDeviceItem(item);
			IRteDeviceItem rteDeviceItem;
			if (parent == item) {
				rteDeviceItem = item;
			} else {
				rteDeviceItem = parent.findItem(item.getName(), item.getVendorName(), false);
			}
			if(rteDeviceItem != null && rteDeviceItem.getDevice() != null) {
				return NLS.bind(Messages.DevicesView_AvailableInPack, rteDeviceItem.getDevice().getPackId());
			}
			return null;
		}

		private IRteDeviceItem getClosestParentRteDeviceItem(IRteDeviceItem item) {
			IRteDeviceItem parent = item;
			while (parent != null && parent.getAllDeviceNames().isEmpty()) {
				parent = parent.getParent();
			}
			return parent;
		}
	}

	static class DevicesViewColumnAdvisor extends ColumnAdvisor {

		public DevicesViewColumnAdvisor(ColumnViewer columnViewer) {
			super(columnViewer);
		}

		@Override
		public CellControlType getCellControlType(Object obj, int columnIndex) {
			if (columnIndex == COLURL) {
				IRteDeviceItem item = getDeviceTreeItem(obj);
				if (item != null) {
					if (item.getLevel() == EDeviceHierarchyLevel.VARIANT.ordinal()
							|| (item.getLevel() == EDeviceHierarchyLevel.DEVICE.ordinal()
							&& stopAtCurrentLevel(item))) {
						return CellControlType.URL;
					}
				}
			}
			return CellControlType.TEXT;
		}

		@Override
		public String getString(Object obj, int columnIndex) {
			if (getCellControlType(obj, columnIndex) == CellControlType.URL) {
				IRteDeviceItem item = getDeviceTreeItem(obj);
				ICpDeviceInfo deviceInfo = new CpDeviceInfo(null, item);
				return deviceInfo.getSummary();
			} else if (columnIndex == COLURL) {
				IRteDeviceItem item = getDeviceTreeItem(obj);
				int nrofDevices = item.getAllDeviceNames().size();
				if (nrofDevices == 1) {
					return Messages.DevicesView_1Device;
				} else if (nrofDevices == 0) {
					return Messages.DevicesView_Processor;
				} else {
					return nrofDevices + Messages.DevicesView_Devices;
				}
			}
			return null;
		}

		@Override
		public String getUrl(Object obj, int columnIndex) {
			if (getCellControlType(obj, columnIndex) == CellControlType.URL) {
				IRteDeviceItem item = getDeviceTreeItem(obj);
				return item.getUrl();
			}
			return null;
		}

		@Override
		public String getTooltipText(Object obj, int columnIndex) {
			if (getCellControlType(obj, columnIndex) == CellControlType.URL) {
				IRteDeviceItem item = getDeviceTreeItem(obj);
				return item.getUrl();
			}
			return null;
		}

	}

	class DeviceTreeColumnComparator extends TreeColumnComparator {

		private final AlnumComparator alnumComparator;

		public DeviceTreeColumnComparator(TreeViewer viewer, ColumnAdvisor advisor) {
			super(viewer, advisor);
			alnumComparator = new AlnumComparator(false, false);
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {

			Tree tree = fViewer.getTree();
			int index = getColumnIndex();
			if (index != 0) {
				return super.compare(viewer, e1, e2);
			}

			int result = 0;
			ColumnLabelProvider colLabelProvider = (ColumnLabelProvider) treeViewer.getLabelProvider(index);
			String str1 = colLabelProvider.getText(e1);
			String str2 = colLabelProvider.getText(e2);
			result = alnumComparator.compare(str1, str2);

			return tree.getSortDirection() == SWT.DOWN ? -result : result;
		}
	}

	public DevicesView() {
	}

	@Override
	public void createPartControl(Composite parent) {

		PatternFilter filter = new PatternFilter() {
			@Override
			protected boolean isLeafMatch(final Viewer viewer, final Object element) {
				TreeViewer treeViewer = (TreeViewer)viewer;
				boolean isMatch = false;
				ColumnLabelProvider labelProvider = (ColumnLabelProvider)treeViewer.getLabelProvider(0);
				String labelText = labelProvider.getText(element);
				isMatch |= wordMatches(labelText);
				return isMatch;
			}
		};
		filter.setIncludeLeadingWildcard(true);
		fTree = new FilteredTree(parent,
				SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, filter, true);
		fTree.setInitialText(Messages.DevicesView_SearchDevice);
		fViewer = fTree.getViewer();
		fViewer.getTree().setLinesVisible(true);
		fViewer.getTree().setHeaderVisible(true);

		TreeViewerColumn column0 = new TreeViewerColumn(fViewer, SWT.LEFT);
		column0.getColumn().setText(CmsisConstants.DEVICE_TITLE);
		column0.getColumn().setWidth(200);
		column0.setLabelProvider(new DevicesViewLabelProvider());

		TreeViewerColumn column1 = new TreeViewerColumn(fViewer, SWT.LEFT);
		column1.getColumn().setText(CmsisConstants.SUMMARY_TITLE);
		column1.getColumn().setWidth(300);
		fColumnAdvisor = new DevicesViewColumnAdvisor(fViewer);
		column1.setLabelProvider(new AdvisedCellLabelProvider(fColumnAdvisor, COLURL));

		fViewer.setContentProvider(new DeviceViewContentProvider());
		fViewer.setComparator(new DeviceTreeColumnComparator(fViewer, fColumnAdvisor));
		fViewer.setAutoExpandLevel(2);
		refresh();

		ColumnViewerToolTipSupport.enableFor(fViewer);

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(fViewer.getControl(), IHelpContextIds.DEVICES_VIEW);

		getSite().setSelectionProvider(fViewer);

		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();

		CpInstallerPlugInUI.registerViewPart(this);

		CpPlugIn.addRteListener(this);
	}

	protected void refresh() {
		if(CpPlugIn.getDefault() == null) {
			return;
		}
		ICpPackManager packManager = CpPlugIn.getPackManager();
		if(packManager != null) {
			ICmsisMapItem<IRteDeviceItem> root = new CmsisMapItem<>();
			IRteDeviceItem allDevices = packManager.getDevices();
			root.addChild(allDevices);
			if (!fViewer.getControl().isDisposed()) {
				fViewer.setInput(root);
			}
		} else {
			if (!fViewer.getControl().isDisposed()) {
				fViewer.setInput(null);
			}
		}
	}


	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				DevicesView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(fViewer.getControl());
		fViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, fViewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(fExpandAction);
		manager.add(fCollapseAction);
		manager.add(fHelpAction);
		manager.add(new Separator());
		manager.add(fRemoveSelection);
	}

	void fillContextMenu(IMenuManager manager) {
		if (fViewer.getSelection() == null ||
				fViewer.getSelection().isEmpty()) {
			manager.add(fExpandAction);
			manager.add(fCollapseAction);
		} else {
			manager.add(fExpandItemAction);
			manager.add(fCollapseItemAction);
		}
		manager.add(new Separator());
		manager.add(fRemoveSelection);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(fExpandAction);
		manager.add(fCollapseAction);
		manager.add(fHelpAction);
		manager.add(new Separator());
		manager.add(fRemoveSelection);
	}

	private void makeActions() {

		fRemoveSelection = new Action() {

			@Override
			public void run() {
				// Empty search text and selection
				fViewer.setSelection(null);
				fTree.getFilterControl().setText(CmsisConstants.EMPTY_STRING);
			}
		};

		fRemoveSelection.setText(Messages.DevicesView_RemoveSelection);
		fRemoveSelection.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_REMOVE_ALL));
		fRemoveSelection.setToolTipText(Messages.DevicesView_RemoveSelection);

		fExpandAction = new Action() {
			@Override
			public void run() {
				if(fViewer == null) {
					return;
				}
				fViewer.expandAll();
			}
		};
		fExpandAction.setText(Messages.ExpandAll);
		fExpandAction.setToolTipText(Messages.ExpandAllNodes);
		fExpandAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_EXPAND_ALL));

		fExpandItemAction = new Action() {
			@Override
			public void run() {
				if(fViewer == null) {
					return;
				}
				ISelection selection = fViewer.getSelection();
				if (selection == null) {
					return;
				}
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				fViewer.expandToLevel(obj, AbstractTreeViewer.ALL_LEVELS);
			}
		};
		fExpandItemAction.setText(Messages.ExpandSelected);
		fExpandItemAction.setToolTipText(Messages.ExpandSelectedNode);
		fExpandItemAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_EXPAND_ALL));

		fCollapseAction = new Action() {
			@Override
			public void run() {
				if(fViewer == null) {
					return;
				}
				fViewer.collapseAll();
			}
		};
		fCollapseAction.setText(Messages.CollapseAll);
		fCollapseAction.setToolTipText(Messages.CollapseAllNodes);
		fCollapseAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_COLLAPSE_ALL));

		fCollapseItemAction = new Action() {
			@Override
			public void run() {
				if(fViewer == null) {
					return;
				}
				ISelection selection = fViewer.getSelection();
				if (selection == null) {
					return;
				}
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				fViewer.collapseToLevel(obj, AbstractTreeViewer.ALL_LEVELS);
			}
		};
		fCollapseItemAction.setText(Messages.CollapseSelected);
		fCollapseItemAction.setToolTipText(Messages.CollapseSelectedNode);
		fCollapseItemAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_COLLAPSE_ALL));

		fHelpAction = new Action(Messages.Help, IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				fViewer.getControl().notifyListeners(SWT.Help, new Event());
			}
		};
		fHelpAction.setToolTipText(Messages.DevicesView_HelpForDevicesView);
		fHelpAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_HELP));

		fDoubleClickAction = new Action() {
			@Override
			public void run() {
				ISelection selection = fViewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				if (fViewer.getExpandedState(obj)) {
					fViewer.collapseToLevel(obj, AbstractTreeViewer.ALL_LEVELS);
				} else if (fViewer.isExpandable(obj)) {
					fViewer.expandToLevel(obj, 1);
				}
			}
		};
	}

	private void hookDoubleClickAction() {
		fViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				fDoubleClickAction.run();
			}
		});
	}

	public Composite getComposite() {
		return fTree;
	}

	@Override
	public void setFocus() {
		fViewer.getControl().setFocus();
	}

	@Override
	public void handle(RteEvent event) {
		switch(event.getTopic()) {
		case RteEvent.PACKS_RELOADED:
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					refresh();
				}
			});
			break;
		case RteEvent.PACK_INSTALL_JOB_FINISHED:
		case RteEvent.PACK_UNPACK_JOB_FINISHED:
		case RteEvent.PACK_REMOVE_JOB_FINISHED:
		case RteEvent.PACK_DELETE_JOB_FINISHED:
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					fViewer.refresh();
				}
			});
			break;
		default:
			return;
		}
	}

	@Override
	public void dispose() {
		if(CpPlugIn.getDefault() != null) {
			CpPlugIn.removeRteListener(this);
		}
		super.dispose();
	}

}
