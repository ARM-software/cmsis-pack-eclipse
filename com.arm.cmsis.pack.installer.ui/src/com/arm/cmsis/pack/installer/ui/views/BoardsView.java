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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

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
import com.arm.cmsis.pack.data.ICpBoard;
import com.arm.cmsis.pack.data.ICpPack.PackState;
import com.arm.cmsis.pack.events.IRteEventListener;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.installer.ui.CpInstallerPlugInUI;
import com.arm.cmsis.pack.installer.ui.IHelpContextIds;
import com.arm.cmsis.pack.installer.ui.Messages;
import com.arm.cmsis.pack.item.CmsisMapItem;
import com.arm.cmsis.pack.item.ICmsisMapItem;
import com.arm.cmsis.pack.rte.boards.IRteBoardDeviceItem;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.tree.AdvisedCellLabelProvider;
import com.arm.cmsis.pack.ui.tree.ColumnAdvisor;
import com.arm.cmsis.pack.ui.tree.TreeObjectContentProvider;
import com.arm.cmsis.pack.utils.AlnumComparator;
import com.arm.cmsis.pack.utils.VersionComparator;

public class BoardsView extends ViewPart implements IRteEventListener {

	public static final String ID = "com.arm.cmsis.pack.installer.ui.views.BoardsView"; //$NON-NLS-1$

	private static final int COLURL = 1;

	FilteredTree fTree;
	TreeViewer fViewer;
	private Action fExpandAction;
	private Action fExpandItemAction;
	private Action fCollapseAction;
	private Action fCollapseItemAction;
	private Action fRemoveSelection;
	private Action fHelpAction;
	Action fDoubleClickAction;

	static final String ALL_BOARDS = Messages.BoardsView_AllBoards;
	private static final String MOUNTED_DEVICES = CmsisConstants.MOUNTED_DEVICES;
	private static final String COMPATIBLE_DEVICES = CmsisConstants.COMPATIBLE_DEVICES;

	private BoardsViewColumnAdvisor fColumnAdvisor;

	IRteBoardDeviceItem getBoardDeviceTreeItem(Object obj) {
		if (obj instanceof IRteBoardDeviceItem) {
			return (IRteBoardDeviceItem) obj;
		}
		return null;
	}

	class BoardViewContentProvider extends TreeObjectContentProvider {

		private DevicesView.DeviceViewContentProvider deviceViewContentProvider = new DevicesView.DeviceViewContentProvider();
		private Map<IRteDeviceItem, IRteBoardDeviceItem> deviceToBoardMap = new HashMap<>();

		@Override
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			IRteBoardDeviceItem rteBoardDeviceItem = getBoardDeviceTreeItem(parentElement);
			if (rteBoardDeviceItem != null) {
				if (ALL_BOARDS.equals(rteBoardDeviceItem.getName())) { // All boards
					return rteBoardDeviceItem.getChildArray();
				}
				// Normal board
				Collection<IRteDeviceItem> children = new LinkedList<>();
				IRteDeviceItem mountedDevices = rteBoardDeviceItem.getMountedDevices();
				if (mountedDevices != null) {
					children.add(mountedDevices);
				}
				deviceToBoardMap.put(mountedDevices, rteBoardDeviceItem);

				IRteDeviceItem compatibleDevices = rteBoardDeviceItem.getCompatibleDevices();
				if (compatibleDevices != null) {
					children.add(compatibleDevices);
				}
				deviceToBoardMap.put(compatibleDevices, rteBoardDeviceItem);

				return children.toArray();
			} else if (parentElement instanceof IRteDeviceItem) {
				return deviceViewContentProvider.getChildren(parentElement);
			}

			return super.getChildren(parentElement);

		}

		@Override
		public Object getParent(Object element) {
			if (element instanceof IRteBoardDeviceItem) { // Board Node
				return ((IRteBoardDeviceItem) element).getParent();
			} else if (element instanceof IRteDeviceItem) {
				IRteDeviceItem item = (IRteDeviceItem) element;
				if (MOUNTED_DEVICES.equals(item.getName()) || COMPATIBLE_DEVICES.equals(item.getName())) {
					return deviceToBoardMap.get(item);
				}
				return item.getParent();
			}
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof IRteDeviceItem) {
				return deviceViewContentProvider.hasChildren(element);
			}
			return getChildren(element).length > 0;
		}

	}

	class BoardsViewLabelProvider extends ColumnLabelProvider {

		private DevicesView.DevicesViewLabelProvider devicesViewLabelProvider = new DevicesView.DevicesViewLabelProvider();

		@Override
		public String getText(Object element) {
			if (element instanceof IRteBoardDeviceItem) {
				IRteBoardDeviceItem bdItem = (IRteBoardDeviceItem) element;
				// added spaces at last of text as a workaround to show the complete text in the views
				if (ALL_BOARDS.equals(bdItem.getName()) || MOUNTED_DEVICES.equals(bdItem.getName())
						|| COMPATIBLE_DEVICES.equals(bdItem.getName())) {
					return bdItem.getName() + ' ';
				}
				return getBoardTitle(bdItem.getBoard()) + ' ';
			} else if (element instanceof IRteDeviceItem) {
				return devicesViewLabelProvider.getText(element);
			}
			return CmsisConstants.EMPTY_STRING;
		}

		@Override
		public Image getImage(Object element) {
			if (element instanceof IRteBoardDeviceItem) {

				IRteBoardDeviceItem bdItem = (IRteBoardDeviceItem) element;
				if (ALL_BOARDS.equals(bdItem.getName()) || MOUNTED_DEVICES.equals(bdItem.getName())
						|| COMPATIBLE_DEVICES.equals(bdItem.getName())) {
					return CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT_CLASS);
				}

				if (packInstalledAndContainsBoard(((IRteBoardDeviceItem) element).getBoard())) {
					return CpPlugInUI.getImage(CpPlugInUI.ICON_BOARD);
				}
				return CpPlugInUI.getImage(CpPlugInUI.ICON_BOARD_GREY);
			} else if (element instanceof IRteDeviceItem) {
				return devicesViewLabelProvider.getImage(element);
			} else {
				return CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT_CLASS);
			}
		}

		private boolean packInstalledAndContainsBoard(ICpBoard board) {
			if (board == null) {
				return false;
			}
			return board.getPack().getPackState() == PackState.INSTALLED
					|| board.getPack().getPackState() == PackState.GENERATED;
		}

		@Override
		public String getToolTipText(Object obj) {
			if (obj instanceof IRteBoardDeviceItem) {
				IRteBoardDeviceItem board = (IRteBoardDeviceItem) obj;
				if (board.getBoard() != null) {
					return NLS.bind(Messages.BoardsView_AvailableInPack, board.getBoard().getPackId());
				}
			} else if (obj instanceof IRteDeviceItem) {
				return devicesViewLabelProvider.getToolTipText(obj);
			}
			return null;
		}
	}

	class BoardsViewColumnAdvisor extends ColumnAdvisor {

		private DevicesView.DevicesViewColumnAdvisor devicesViewColumnAdvisor = new DevicesView.DevicesViewColumnAdvisor(null);

		public BoardsViewColumnAdvisor(ColumnViewer columnViewer) {
			super(columnViewer);
		}

		@Override
		public CellControlType getCellControlType(Object obj, int columnIndex) {
			if (columnIndex == COLURL) {
				IRteBoardDeviceItem item = getBoardDeviceTreeItem(obj);
				if (item != null && item.getBoard() != null) {
					return CellControlType.URL;
				} else if (obj instanceof IRteDeviceItem) {
					return devicesViewColumnAdvisor.getCellControlType(obj, columnIndex);
				}
			}
			return CellControlType.TEXT;
		}

		@Override
		public String getString(Object obj, int columnIndex) {
			if (getCellControlType(obj, columnIndex) == CellControlType.URL) {
				IRteBoardDeviceItem item = getBoardDeviceTreeItem(obj);
				if (item != null) {
					if (item.getMountedDevices() != null) {
						return item.getMountedDevices().getFirstChildKey();
					} else if (item.getCompatibleDevices() != null) {
						return item.getCompatibleDevices().getFirstChildKey();
					} else if (item.getBoard() != null) {
						return item.getBoard().getName();
					}
				} else if (obj instanceof IRteDeviceItem) {
					return devicesViewColumnAdvisor.getString(obj, columnIndex);
				}
			} else if (columnIndex == COLURL) {
				IRteBoardDeviceItem item = getBoardDeviceTreeItem(obj);
				if (item != null) {
					if (ALL_BOARDS.equals(item.getName())) {
						int nrofBoards = item.getChildCount();
						return nrofBoards + Messages.BoardsView_Boards;
					}
				} else if (obj instanceof IRteDeviceItem) {
					return devicesViewColumnAdvisor.getString(obj, columnIndex);
				}
			}
			return null;
		}

		@Override
		public String getUrl(Object obj, int columnIndex) {
			if (getCellControlType(obj, columnIndex) == CellControlType.URL) {
				IRteBoardDeviceItem item = getBoardDeviceTreeItem(obj);
				if (item != null) {
					return item.getUrl();
				} else if (obj instanceof IRteDeviceItem) {
					return devicesViewColumnAdvisor.getTooltipText(obj, columnIndex);
				}
			}
			return null;
		}

		@Override
		public String getTooltipText(Object obj, int columnIndex) {
			if (getCellControlType(obj, columnIndex) == CellControlType.URL) {
				IRteBoardDeviceItem item = getBoardDeviceTreeItem(obj);
				if (item != null) {
					return item.getUrl();
				} else if (obj instanceof IRteDeviceItem) {
					return devicesViewColumnAdvisor.getTooltipText(obj, columnIndex);
				}
			}
			return null;
		}

	}

	class BoardTreeColumnComparator extends TreeColumnComparator {

		private final AlnumComparator alnumComparator;
		private VersionComparator versionComparator;

		public BoardTreeColumnComparator(TreeViewer viewer, ColumnAdvisor advisor) {
			super(viewer, advisor);
			alnumComparator = new AlnumComparator(false);
			versionComparator = new VersionComparator(false);
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {

			Tree tree = treeViewer.getTree();
			if (getColumnIndex() != 0) {
				return super.compare(viewer, e1, e2);
			}

			int result = 0;
			if ((e1 instanceof IRteBoardDeviceItem) && (e2 instanceof IRteBoardDeviceItem)) {

				IRteBoardDeviceItem cp1 = (IRteBoardDeviceItem) e1;
				IRteBoardDeviceItem cp2 = (IRteBoardDeviceItem) e2;

				String title1 = getBoardTitle(cp1.getBoard());
				String title2 = getBoardTitle(cp2.getBoard());

				result = versionComparator.compare(title1, title2);

			} else if ((e1 instanceof IRteDeviceItem) && (e2 instanceof IRteDeviceItem)) {
				IRteDeviceItem d1 = (IRteDeviceItem) e1;
				IRteDeviceItem d2 = (IRteDeviceItem) e2;
				if (MOUNTED_DEVICES.equals(d1.getName()) || COMPATIBLE_DEVICES.equals(d1.getName())) {
					return 0;
				}
				result = alnumComparator.compare(d1.getName(), d2.getName());
			}
			return tree.getSortDirection() == SWT.DOWN ? -result : result;
		}
	}

	String getBoardTitle(ICpBoard cpBoard) {
		if (cpBoard == null) {
			return CmsisConstants.EMPTY_STRING;
		}
		String boardTitle = cpBoard.getAttribute(CmsisConstants.NAME);
		if (!cpBoard.getAttribute(CmsisConstants.REVISION).isEmpty()) {
			boardTitle += " (" + cpBoard.getAttribute(CmsisConstants.REVISION) + ')'; //$NON-NLS-1$
		}
		return boardTitle;
	}

	public BoardsView() {
	}

	@Override
	public void createPartControl(Composite parent) {

		PatternFilter filter = new PatternFilter() {
			@Override
			protected boolean isLeafMatch(final Viewer viewer, final Object element) {
				TreeViewer treeViewer = (TreeViewer) viewer;
				boolean isMatch = false;
				ColumnLabelProvider labelProvider = (ColumnLabelProvider) treeViewer.getLabelProvider(0);
				String labelText = labelProvider.getText(element);
				isMatch |= wordMatches(labelText);
				return isMatch;
			}
		};
		filter.setIncludeLeadingWildcard(true);
		fTree = new FilteredTree(parent, SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, filter, true);
		fTree.setInitialText(Messages.BoardsView_SearchBoard);
		fViewer = fTree.getViewer();
		fViewer.getTree().setLinesVisible(true);
		fViewer.getTree().setHeaderVisible(true);

		TreeViewerColumn column0 = new TreeViewerColumn(fViewer, SWT.LEFT);
		column0.getColumn().setText(CmsisConstants.BOARD_TITLE);
		column0.getColumn().setWidth(200);
		column0.setLabelProvider(new BoardsViewLabelProvider());

		TreeViewerColumn column1 = new TreeViewerColumn(fViewer, SWT.LEFT);
		column1.getColumn().setText(CmsisConstants.SUMMARY_TITLE);
		column1.getColumn().setWidth(300);
		fColumnAdvisor = new BoardsViewColumnAdvisor(fViewer);
		column1.setLabelProvider(new AdvisedCellLabelProvider(fColumnAdvisor, COLURL));

		fViewer.setContentProvider(new BoardViewContentProvider());
		fViewer.setComparator(new BoardTreeColumnComparator(fViewer, fColumnAdvisor));
		fViewer.setAutoExpandLevel(2);
		refresh();

		ColumnViewerToolTipSupport.enableFor(fViewer);

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(fViewer.getControl(), IHelpContextIds.BOARDS_VIEW);

		getSite().setSelectionProvider(fViewer);

		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();

		CpInstallerPlugInUI.registerViewPart(this);

		CpPlugIn.addRteListener(this);
	}

	/**
	 * Set the viewer's input
	 */
	void refresh() {
		if (CpPlugIn.getDefault() == null) {
			return;
		}
		ICpPackManager packManager = CpPlugIn.getPackManager();
		if (packManager != null && packManager.getBoards() != null) {
			ICmsisMapItem<IRteBoardDeviceItem> root = new CmsisMapItem<>();
			IRteBoardDeviceItem allBoardRoot = packManager.getRteBoardDevices();
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

	/**
	 * make actions
	 */
	private void makeActions() {
		fRemoveSelection = new Action() {
			@Override
			public void run() {
				fViewer.setSelection(null);
				fTree.getFilterControl().setText(CmsisConstants.EMPTY_STRING);
			}
		};

		fRemoveSelection.setText(Messages.BoardsView_RemoveSelection);
		fRemoveSelection.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_REMOVE_ALL));
		fRemoveSelection.setToolTipText(Messages.BoardsView_RemoveSelection);

		fExpandAction = new Action() {
			@Override
			public void run() {
				if (fViewer == null) {
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
				if (fViewer == null) {
					return;
				}
				ISelection selection = fViewer.getSelection();
				if (selection == null) {
					return;
				}
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				fViewer.expandToLevel(obj, AbstractTreeViewer.ALL_LEVELS);
			}
		};
		fExpandItemAction.setText(Messages.ExpandSelected);
		fExpandItemAction.setToolTipText(Messages.ExpandSelectedNode);
		fExpandItemAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_EXPAND_ALL));

		fCollapseAction = new Action() {
			@Override
			public void run() {
				if (fViewer == null) {
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
				if (fViewer == null) {
					return;
				}
				ISelection selection = fViewer.getSelection();
				if (selection == null) {
					return;
				}
				Object obj = ((IStructuredSelection) selection).getFirstElement();
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
		fHelpAction.setToolTipText(Messages.BoardsView_HelpForBoardsView);
		fHelpAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_HELP));

		fDoubleClickAction = new Action() {
			@Override
			public void run() {
				ISelection selection = fViewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();
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

	/**
	 * hook context menu
	 */
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				BoardsView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(fViewer.getControl());
		fViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, fViewer);
	}

	/**
	 * fill context menu
	 *
	 * @param manager
	 */
	void fillContextMenu(IMenuManager manager) {
		if (fViewer.getSelection() == null || fViewer.getSelection().isEmpty()) {
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

	/**
	 * contribute to action bars
	 */
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	/**
	 * @param manager
	 */
	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(fExpandAction);
		manager.add(fCollapseAction);
		manager.add(fHelpAction);
		manager.add(new Separator());
		manager.add(fRemoveSelection);
	}

	/**
	 * @param manager
	 */
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(fExpandAction);
		manager.add(fCollapseAction);
		manager.add(fHelpAction);
		manager.add(new Separator());
		manager.add(fRemoveSelection);
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
		switch (event.getTopic()) {
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

}
