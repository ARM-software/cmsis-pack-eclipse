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

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.eclipse.ui.part.ViewPart;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackInstaller;
import com.arm.cmsis.pack.ICpPackManager;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpPack;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.events.IRteEventListener;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.installer.ui.ButtonId;
import com.arm.cmsis.pack.installer.ui.CpInstallerPlugInUI;
import com.arm.cmsis.pack.installer.ui.Messages;
import com.arm.cmsis.pack.installer.ui.PackInstallerViewController;
import com.arm.cmsis.pack.installer.ui.PackInstallerViewFilter;
import com.arm.cmsis.pack.item.ICmsisItem;
import com.arm.cmsis.pack.ui.CpPlugInUI;

/**
 * Base class for all the views in pack manager perspective
 */
public abstract class PackInstallerView extends ViewPart implements IRteEventListener, ISelectionListener {
	protected static final int COLNAME = 0;
	protected static final int COLBUTTON = 1;
	protected static final int COLURL = 1;
	protected static final int COLDESC = 2;

	protected PackInstallerViewController fViewController;

	protected Composite fParentComposite = null;
	protected FilteredTree fTree = null;
	protected TreeViewer fViewer = null;
	protected StackLayout fStackLayout = null;
	protected Link fLink = null;
	protected Listener fLinkListener;
	protected List<String> fSelectionPath = new LinkedList<>();

	protected ViewerFilter[] fViewFilters = null;
	PatternFilter fPatternFilter = null;

	Action fHelpAction = null;
	Action fRemoveSelection = null;
	Action fExpandAction = null;
	Action fExpandItemAction = null;
	Action fCollapseAction = null;
	Action fCollapseItemAction = null;
	Action fDoubleClickAction = null;
	Action fShowPackProperties = null;
	Action fCopyRepository = null;
	Action fCopyTag = null;



	public PackInstallerView() {
		fViewController = CpInstallerPlugInUI.getViewController();
	}

	protected String getButtonString(ButtonId buttonId) {
		switch(buttonId) {
		case BUTTON_UPTODATE: 		return Messages.PackInstallerView_BtUpToDate;
		case BUTTON_OFFLINE: 		return Messages.PackInstallerView_BtOffline;
		case BUTTON_DEPRECATED: 	return Messages.PackInstallerView_BtDeprecated;
		case BUTTON_INSTALL: 		return Messages.PackInstallerView_BtInstall;
		case BUTTON_INSTALL_PLUS: 	return Messages.PackInstallerView_BtInstallPlus;
		case BUTTON_RESOLVE:		return Messages.PackInstallerView_BtResolve;
		case BUTTON_UPDATE:			return Messages.PackInstallerView_BtUpdate;
		case BUTTON_UPDATE_PLUS:	return Messages.PackInstallerView_BtUpdatePlus;
		case BUTTON_UNPACK:			return Messages.PackInstallerView_BtUnpack;
		case BUTTON_UNPACK_PLUS:	return Messages.PackInstallerView_BtUnpackPlus;
		case BUTTON_REMOVE:			return Messages.PackInstallerView_BtRemove;
		case BUTTON_DELETE:			return Messages.PackInstallerView_BtDelete;
		case BUTTON_DELETE_ALL:		return Messages.PackInstallerView_BtDeleteAll;
		case BUTTON_COPY:			return Messages.PackInstallerView_BtCopy;
		case BUTTON_REPOSITORY:		return Messages.PackInstallerView_BtRepository;
		case BUTTON_COPY_REPOSITORY:return Messages.PackInstallerView_BtCopyRepository;
		case BUTTON_COPY_TAG:		return Messages.PackInstallerView_BtCopyTag;
		case BUTTON_1PACK:			return Messages.PackInstallerView_Bt1Pack;
		case BUTTON_PACKS:			return Messages.PackInstallerView_BtPacks;
		case BUTTON_IMPORT:			return Messages.PackInstallerView_BtImport;
		default: 					return Messages.PackInstallerView_BtUndefined;
		}
	}

	@Override
	public void dispose() {
		fViewController.removeListener(this);
		fViewController = null;

		if(getSite() != null && getSite().getPage() != null) {
			getSite().getPage().removeSelectionListener(this);
		}
		super.dispose();
	}

	public ICpPackInstaller getPackInstaller() {
		if(CpPlugIn.getPackManager() == null) {
			return null;
		}
		return CpPlugIn.getPackManager().getPackInstaller();
	}

	protected abstract String getHelpContextId();
	protected abstract void createTreeColumns();
	protected abstract void refresh();

	protected boolean isExpandable() {
		return true;
	}

	protected boolean hasManagerCommands() {
		return false;
	}

	public boolean isFilterSource() {
		return false;
	}


	public boolean isFilterClient() {
		return !isFilterSource();
	}

	protected ICmsisItem getSelectedItem() {
		ITreeSelection sel = fViewer.getStructuredSelection();
		if(sel.size() == 1) {
			return ICmsisItem.cast(sel.getFirstElement());
		}
		return null;
	}

	public static List<String> createSelectionPath(ISelection selection) {
		List<String> selectionPath = new LinkedList<>();
		if(!(selection instanceof ITreeSelection))
			return selectionPath;
		ITreeSelection treeSelection = (ITreeSelection)selection;
		TreePath[] treePaths = treeSelection.getPaths();
		if(treePaths == null || treePaths.length == 0) {
			return selectionPath;
		}
		TreePath path = treePaths[0];
		for(int i = 0; i < path.getSegmentCount(); i++) {
			Object o = path.getSegment(i);
			if(o instanceof ICmsisItem) {
				ICmsisItem item = (ICmsisItem)o;
				String name = item.getName();
				selectionPath.add(name);
			}
		}

		return selectionPath;
	}


	protected void hookViewSelection() {
		IWorkbenchPartSite site = getSite();
		if(site != null) {
			if(isFilterClient() || isFilterSource()) {
				site.setSelectionProvider(fViewer);
			}
			if(site.getPage() != null) {
				site.getPage().addSelectionListener(this);
			}
		}
	}


	@Override
	public void setFocus() {
		fViewer.getControl().setFocus();
	}


	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if(part == this && fViewController !=null ) {
			fSelectionPath = createSelectionPath(selection);
			fViewController.selectionChanged(this, getSelectedItem(), fSelectionPath);
		}
	}

	protected void handleFilterChanged() {
		if(!isFilterClient()) {
			return;
		}
		if (!fTree.getFilterControl().isDisposed()) {
			fTree.getFilterControl().setText(CmsisConstants.EMPTY_STRING);
		}

		if (!fViewer.getControl().isDisposed()) {
			fViewer.setFilters(fViewFilters);
			fViewer.setSelection(null);
		}
	}


	@Override
	public void createPartControl(Composite parent) {
		createViewFilters();

		fParentComposite = new Composite(parent, SWT.NONE);
		fStackLayout = new StackLayout();
		fParentComposite.setLayout(fStackLayout);

		fTree = new FilteredTree(fParentComposite,
				SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL,
				fPatternFilter, true);

		fViewer = fTree.getViewer();
		Tree tree = fViewer.getTree();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		createTreeColumns();
		refresh();

		ColumnViewerToolTipSupport.enableFor(fViewer);
		setHelpContext();
		hookViewSelection();
		makeActions();
		hookContextMenu();
		contributeToActionBars();
		if(fViewFilters != null) {
			fViewer.setFilters(fViewFilters);
		}
		fViewController.addListener(this);
		updateOnlineState();

		fLink = new Link(fParentComposite, SWT.NONE);

		showRelevantPage();
	}

	protected void createViewFilters() {
		fPatternFilter = new PatternFilter() {
			@Override
			protected boolean isLeafMatch(final Viewer viewer, final Object element) {
				TreeViewer treeViewer = (TreeViewer) viewer;
				ILabelProvider labelProvider =  (ILabelProvider) treeViewer.getLabelProvider(0);
				String labelText = labelProvider.getText(element);
				if(wordMatches(labelText)) {
					return true;
				}
				IContentProvider contentProvider = treeViewer.getContentProvider();
				if(contentProvider instanceof ITreeContentProvider) {
					ITreeContentProvider treeContentProvider = (ITreeContentProvider)contentProvider;
					Object parent;
					for(parent = treeContentProvider.getParent(element); parent != null; parent = treeContentProvider.getParent(parent)) {
						labelText = labelProvider.getText(parent);
						if(wordMatches(labelText)) {
							return true;
						}
					}
				}
				return false;
			}
		};
		fPatternFilter.setIncludeLeadingWildcard(true);
		if(isFilterClient()) {
			fViewFilters = new ViewerFilter[]{fPatternFilter, fViewController.getFilter()};
		}

	}


	protected void setHelpContext() {
		// Create the help context id for the viewer's control
		IWorkbench wb = PlatformUI.getWorkbench();
		if(wb == null) {
			return;
		}
		IWorkbenchHelpSystem hs = wb.getHelpSystem();
		if(hs == null) {
			return;
		}
		hs.setHelp(fViewer.getControl(), getHelpContextId());
	}


	protected void makeActions() {
		fHelpAction = new Action(Messages.Help, IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				fViewer.getControl().notifyListeners(SWT.Help, new Event());
			}
		};
		fHelpAction.setToolTipText(Messages.PackInstallerView_Help);
		fHelpAction.setImageDescriptor(	CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_HELP));

		fShowPackProperties = new Action() {
			@Override
			public void run() {
				fViewController.showPackProperties(getSelectedItem()); // the item should be already selected
			}
		};
		fShowPackProperties.setText(Messages.PacksView_ShowPacksOutline);
		fShowPackProperties.setToolTipText(Messages.PacksView_ShowPacksOutline);
		fShowPackProperties.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_DETAILS));

		if(isFilterSource()) {
			fRemoveSelection = new Action() {

				@Override
				public void run() {
					// Empty search text and fSelection
					fViewer.setSelection(null);
					fTree.getFilterControl().setText(CmsisConstants.EMPTY_STRING);
				}
			};

			fRemoveSelection.setText(Messages.DevicesView_RemoveSelection);
			fRemoveSelection.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_REMOVE_ALL));
			fRemoveSelection.setToolTipText(Messages.DevicesView_RemoveSelection);
		}


		if(!isExpandable()) {
			return;
		}
		// expandAction
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
				ICmsisItem cmsisItem = getSelectedItem();
				if (cmsisItem == null) {
					return;
				}
				fViewer.expandToLevel(cmsisItem, AbstractTreeViewer.ALL_LEVELS);
			}
		};
		fExpandItemAction.setText(Messages.ExpandSelected);
		fExpandItemAction.setToolTipText(Messages.ExpandSelectedNode);
		fExpandItemAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_EXPAND_ALL));

		// collapseAction
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
				ICmsisItem cmsisItem = getSelectedItem();
				if (cmsisItem == null) {
					return;
				}

				fViewer.collapseToLevel(cmsisItem, AbstractTreeViewer.ALL_LEVELS);
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
		fHelpAction.setToolTipText(Messages.PackInstallerView_Help);
		fHelpAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_HELP));

		fDoubleClickAction = new Action() {
			@Override
			public void run() {
				ISelection selection = fViewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				if (obj != null) {
					if (fViewer.getExpandedState(obj)) {
						fViewer.collapseToLevel(obj, AbstractTreeViewer.ALL_LEVELS);
					} else if (fViewer.isExpandable(obj)) {
						fViewer.expandToLevel(obj, 1);
					}
				}
			}
		};

		fCopyRepository = new Action() {

			@Override
			public void run() {
				Toolkit toolkit =  Toolkit.getDefaultToolkit();
				Clipboard cb = toolkit.getSystemClipboard();
				ICmsisItem cmsisItem = getSelectedItem();
				if(cmsisItem instanceof ICpPack) {
					ICpPack pack = (CpPack)cmsisItem;
					if (pack.getReleases() != null) {
						for (ICpItem release : pack.getReleases()) {
							if (release.getVersion().equals(pack.getVersion())) {
								StringSelection sel = new StringSelection(release.getAttribute(CmsisConstants.URL));
								cb.setContents(sel, null);
								break;
							}
						}
					}

				}
				super.run();
			}

		};
		fCopyRepository.setText(Messages.PackInstallerView_BtCopyRepository);
		fCopyRepository.setToolTipText(Messages.PackInstallerView_BtCopyRepository);

		fCopyTag = new Action() {

			@Override
			public void run() {
				Toolkit toolkit =  Toolkit.getDefaultToolkit();
				Clipboard cb = toolkit.getSystemClipboard();
				ISelection selection = fViewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				if(obj instanceof ICpPack) {
					ICpPack pack = (CpPack)obj;
					if (pack.getReleases() != null) {
						for (ICpItem release : pack.getReleases()) {
							if (release.getVersion().equals(pack.getVersion())) {
								StringSelection sel = new StringSelection(release.getAttribute(CmsisConstants.TAG));
								cb.setContents(sel, null);
								break;
							}
						}
					}

				}
				super.run();
			}

		};
		fCopyTag.setText(Messages.PackInstallerView_BtCopyTag);
		fCopyTag.setToolTipText(Messages.PackInstallerView_BtCopyTag);

		fViewer.addDoubleClickListener(event -> fDoubleClickAction.run());
	}


	protected void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	protected void fillLocalPullDown(IMenuManager manager) {
		if(isExpandable()) {
			manager.add(fExpandAction);
			manager.add(fCollapseAction);
		}
		manager.add(fHelpAction);
		manager.add(new Separator());

		if(isFilterSource()) {
			manager.add(fRemoveSelection);
		}

		if(hasManagerCommands()) {
			PackInstallerViewUtils.addManagementCommandsToLocalToolBar(this, manager);
		}
	}

	protected void fillLocalToolBar(IToolBarManager manager) {
		if(isExpandable()) {
			manager.add(fExpandAction);
			manager.add(fCollapseAction);
		}

		if(isFilterSource()) {
			manager.add(new Separator());
			manager.add(fRemoveSelection);
		}
		if(hasManagerCommands())  {
			manager.add(new Separator());
			PackInstallerViewUtils.addManagementCommandsToLocalToolBar(this, manager);
		}
		manager.add(new Separator());
		manager.add(fHelpAction);
	}

	protected void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(manager -> fillContextMenu(manager));
		Menu menu = menuMgr.createContextMenu(fViewer.getControl());
		fViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, fViewer);
	}


	protected void fillContextMenu(IMenuManager manager) {
		if(isExpandable()) {
			ICmsisItem item = getSelectedItem();
			if (item == null ) {
				manager.add(fExpandAction);
				manager.add(fCollapseAction);
			} else {
				if(fViewer.isExpandable(item)) {
					boolean isExpanded = fViewer.getExpandedState(item);
					if(!isExpanded) {
						manager.add(fExpandItemAction);
					} else {
						manager.add(fCollapseItemAction);
					}
				}
			}
		}

		if(isFilterSource()) {
			manager.add(new Separator());
			manager.add(fRemoveSelection);
		}

		ICpPack pack = PackInstallerViewController.getPackFromSelection(fViewer.getSelection());
		if(pack == null) {
			return;
		}
		if(isFilterSource() || isFilterClient()) {
			manager.add(new Separator());
			manager.add(fShowPackProperties);
		}
		if (pack.getReleases() == null)
			return;
		ICmsisItem item = getSelectedItem();
		if (item instanceof ICpItem) {
			String selectedVersion = ((ICpItem) item).getAttribute(CmsisConstants.VERSION);
			for (ICpItem release : pack.getReleases()) {
				String releaseVersion = release.getVersion();
				if (releaseVersion.equals(selectedVersion)) {
					String repo = release.getAttribute(CmsisConstants.URL);
					String tag = release.getAttribute(CmsisConstants.TAG);
					boolean sep = false;
					if (!repo.isEmpty()) {
						manager.add(new Separator());
						manager.add(fCopyRepository);
						sep = true;
					}
					if (!tag.isEmpty()) {
						if (!sep) {
							manager.add(new Separator());
						}
						manager.add(fCopyTag);
					}
					break;
				}
			}
		}
	}


	@Override
	public void handle(RteEvent event) {
		String topic = event.getTopic();
		switch(topic) {
		case PackInstallerViewController.INSTALLER_UI_FILTER_CHANGED:
			if(isFilterClient()) {
				Display.getDefault().asyncExec(() -> handleFilterChanged());
			}
			return;
		case RteEvent.PACK_OLNLINE_STATE_CHANGED:
			updateOnlineState(); // already in UI thread
			return;
		default:
			break;
		}

		Display.getDefault().asyncExec(() -> handleRteEvent(event));
	}

	/**
	 *
	 */
	protected void restoreSelection() {
		if(fViewController == null) {
			return;
		}
		if(fSelectionPath == null || fSelectionPath.isEmpty()) {
			return;
		}
		ICmsisItem selectedItem = selectItem(fSelectionPath);

		PackInstallerViewFilter filter = fViewController.getFilter();
		if(filter == null || this != filter.getSelectionView())
			return;

		fViewController.selectionChanged(this, selectedItem, fSelectionPath);
	}

	/**
	 * @param selectionPath
	 */
	protected ICmsisItem selectItem(List<String> selectionPath) {
		if(fViewer == null || !(fViewer.getContentProvider() instanceof ITreeContentProvider))
			return null;
		ITreeContentProvider cp = (ITreeContentProvider)fViewer.getContentProvider();
		Object[] elements = cp.getElements(fViewer.getInput());
		List<ICmsisItem> path = new LinkedList<>();
		ICmsisItem selectedItem = null;
		for(String name : selectionPath) {
			ICmsisItem cmsisItem = getCmsisItem(elements, name);
			if(cmsisItem == null) {
				break;
			}
			path.add(cmsisItem);
			selectedItem = cmsisItem;
			elements = cp.getChildren(cmsisItem);
		}

		if(path.isEmpty())
			return null;
		TreePath tp = new TreePath(path.toArray());
		TreeSelection ts = new TreeSelection(tp);
		fViewer.setSelection(ts, true);
		return selectedItem;
	}

	protected ICmsisItem getCmsisItem(Object[] elements, String name) {
		if(elements == null || name == null || name.isEmpty())
			return null;
		for(Object o: elements) {
			ICmsisItem cmsisItem = ICmsisItem.cast(o);
			if(cmsisItem != null && name.equals(cmsisItem.getName())) {
				return cmsisItem;
			}
		}
		return null;
	}

	protected void enableActions(boolean en) {
		if (fHelpAction != null)
			fHelpAction.setEnabled(en);
		if (fRemoveSelection != null)
			fRemoveSelection.setEnabled(en);
		if (fExpandAction != null)
			fExpandAction.setEnabled(en);
		if (fExpandItemAction != null)
			fExpandItemAction.setEnabled(en);
		if (fCollapseAction != null)
			fCollapseAction.setEnabled(en);
		if (fCollapseItemAction != null)
			fCollapseItemAction.setEnabled(en);
		if (fDoubleClickAction != null)
			fDoubleClickAction.setEnabled(en);
		if (fShowPackProperties != null)
			fShowPackProperties.setEnabled(en);
		if (fCopyRepository != null)
			fCopyRepository.setEnabled(en);
		if (fCopyTag != null)
			fCopyTag.setEnabled(en);

		IActionBars bars = getViewSite().getActionBars();
		bars.updateActionBars();

		fViewer.refresh();
	}

	protected void updateOnlineState() {
		if(getViewSite() == null || getViewSite().getActionBars() == null) {
			return;
		}
		IStatusLineManager mgr = getViewSite().getActionBars().getStatusLineManager();
		if(mgr == null) {
			return;
		}
		if (isFilterClient() && fViewer != null && !fViewer.getControl().isDisposed()) {
			fViewer.refresh();
		}
	}


	protected void handleRteEvent(RteEvent event) {
		switch (event.getTopic()) {
		case RteEvent.PACKS_RELOADED:
			refresh();
			showRelevantPage();
			restoreSelection();
			break;
		case RteEvent.PACK_INSTALL_JOB_FINISHED:
		case RteEvent.PACK_REMOVE_JOB_FINISHED:
		case RteEvent.PACK_DELETE_JOB_FINISHED:
			fViewer.refresh();
			break;

		case RteEvent.PACK_UPDATE_JOB_STARTED:
			enableActions(false);
			break;
		case RteEvent.PACK_UPDATE_JOB_FINISHED:
			enableActions(true);
			break;
		default:
			return;
		}
	}

	protected void showRelevantPage() {
		Control topControl = fTree;
		final ICpPackManager pm = CpPlugIn.getPackManager();
		if (fLinkListener != null) {
			fLink.removeListener(SWT.Selection, fLinkListener);
		}

		String rootDir = pm.getCmsisPackRootDirectory();
		if (!pm.arePacksLoaded()) {
			fLink.setText(Messages.PackInstallerView_OpenPreferenceLink);
			fLinkListener = (event) -> {
				PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(fParentComposite.getShell(),
						"com.arm.cmsis.pack.ui.CpPreferencePage", null, null); //$NON-NLS-1$
				dialog.open();
			};
			fLink.addListener(SWT.Selection, fLinkListener);
			topControl = fLink;
		} else if (pm.getPacks() == null || !pm.getPacks().hasChildren()) {
			fLink.setText(NLS.bind(Messages.PackInstallerView_CheckForUpdatesLink, rootDir));
			fLinkListener = (event) -> {
				CpPlugInUI.startCheckForUpdates();
			};
			fLink.addListener(SWT.Selection, fLinkListener);
			topControl = fLink;
		}
		fStackLayout.topControl = topControl;
		fParentComposite.layout();
	}

}
