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
import java.util.Iterator;

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
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.part.ViewPart;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackInstaller;
import com.arm.cmsis.pack.ICpPackManager;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPack.PackState;
import com.arm.cmsis.pack.data.ICpPackCollection;
import com.arm.cmsis.pack.data.ICpPackFamily;
import com.arm.cmsis.pack.events.IRteEventListener;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.installer.ui.CpInstallerPlugInUI;
import com.arm.cmsis.pack.installer.ui.IHelpContextIds;
import com.arm.cmsis.pack.installer.ui.Messages;
import com.arm.cmsis.pack.installer.utils.PackInstallerUtils;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.tree.AdvisedCellLabelProvider;
import com.arm.cmsis.pack.ui.tree.ColorConstants;
import com.arm.cmsis.pack.ui.tree.ColumnAdvisor;
import com.arm.cmsis.pack.ui.tree.TreeObjectContentProvider;
import com.arm.cmsis.pack.utils.Utils;
import com.arm.cmsis.pack.utils.VersionComparator;


/**
 * The Packs View in the Pack Manager perspective
 */
public class PacksView extends ViewPart implements IRteEventListener {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.arm.cmsis.pack.installer.ui.views.PacksView"; //$NON-NLS-1$

	private final static String ROOT = "Root"; //$NON-NLS-1$

	private static final int COLBUTTON = 1;

	FilteredTree fTree;
	TreeViewer fViewer;

	private Action fExpandAction;
	private Action fExpandItemAction;
	private Action fCollapseAction;
	private Action fCollapseItemAction;
	private Action fHelpAction;
	Action fDoubleClickAction;
	private Action fShowPackProperties;
	private Action fDeletePack;

	private ISelectionListener fViewSelectionListener;

	PacksExamplesViewFilter fPacksExamplesViewFilter;

	private ViewerFilter[] fPacksViewFilters;

	ICpPackInstaller fPackInstaller;

	private PacksViewColumnAdvisor fColumnAdvisor;

	private ColumLabelProviderWithImage fColumnLabelProviderWithImage;


	ICpItem getCpItem(Object obj) {
		if (obj instanceof ICpItem) {
			return (ICpItem)obj;
		}
		return null;
	}

	class PacksViewContentProvider extends TreeObjectContentProvider {

		@Override
		public Object[] getChildren(Object parentElement) {
			ICpItem item = getCpItem(parentElement);
			if(item == null) {
				return null;
			}
			if (item instanceof ICpPackCollection) {
				return item.getChildArray();
			} else if (item instanceof ICpPackFamily) {
				return item.getChildArray();
			} else if(CmsisConstants.PREVIOUS.equals(item.getTag())) {
				return item.getChildArray();
			} else if (ROOT.equals(item.getTag())) {
				return item.getChildArray();
			}
			return null;
		}

		@Override
		public Object getParent(Object element) {
			ICpItem item = getCpItem(element);
			if(item == null) {
				return null;
			}
			if(CmsisConstants.RELEASE_TAG.equals(item.getTag())) {
				ICpPack pack = item.getPack();
				if(pack == null) {
					return null;
				}
				ICpItem parent = pack.getParent();
				if(parent instanceof ICpPackFamily) {
					ICpPackFamily family = (ICpPackFamily)parent;
					return family.getPreviousReleases();
				}
			}
			return item.getParent();
		}

		@Override
		public boolean hasChildren(Object element) {
			return getChildren(element) != null && getChildren(element).length > 0;
		}
	}

	class PacksViewColumnAdvisor extends ColumnAdvisor {

		public PacksViewColumnAdvisor(ColumnViewer columnViewer) {
			super(columnViewer);
		}

		@Override
		public CellControlType getCellControlType(Object obj, int columnIndex) {
			if (columnIndex == COLBUTTON) {
				return CellControlType.BUTTON;
			}
			return CellControlType.TEXT;
		}

		@Override
		public boolean isEnabled(Object obj, int columnIndex) {
			String buttonString = getString(obj, columnIndex);
			if (CmsisConstants.BUTTON_UPTODATE.equals(buttonString) ||
					CmsisConstants.BUTTON_OFFLINE.equals(buttonString) ||
					CmsisConstants.BUTTON_DEPRECATED.equals(buttonString)) {
				return false;
			}
			ICpItem cpItem = getCpItem(obj);
			if (cpItem != null && !ROOT.equals(cpItem.getTag())) {
				if (cpItem instanceof ICpPackCollection) {
					return false;
				} else if (!CpInstallerPlugInUI.isOnline() &&
						(CmsisConstants.BUTTON_INSTALL.equals(buttonString) ||
								CmsisConstants.BUTTON_UPDATE.equals(buttonString))){
					return false;
				} else if (cpItem instanceof ICpPackFamily) {
					return !fPackInstaller.isProcessing(cpItem.getPackId());
				} else if (CmsisConstants.PREVIOUS.equals(cpItem.getTag())){
					return false;
				} else {
					return !fPackInstaller.isProcessing(PackInstallerUtils.getFullPackId(cpItem));
				}
			}
			return false;
		}

		@Override
		public Color getBgColor(Object obj, int columnIndex) {
			if (getCellControlType(obj, columnIndex) == CellControlType.BUTTON) {
				ICpItem item = getCpItem(obj);
				if (item != null) {
					if (CmsisConstants.GENERIC.equals(item.getTag()) ||
							CmsisConstants.DEVICE_SPECIFIC.equals(item.getTag())) {
						return null;
					}
					return ColorConstants.COLOR_BUTTON_TOP;
				}
			}
			return null;
		}

		@Override
		public boolean isEmpty(Object obj, int columnIndex) {
			if (getCellControlType(obj, columnIndex) == CellControlType.BUTTON) {
				ICpItem item = getCpItem(obj);
				if (item != null) {
					String tag = item.getTag();
					if (CmsisConstants.PREVIOUS.equals(tag) ||
							CmsisConstants.GENERIC.equals(tag) ||
							CmsisConstants.DEVICE_SPECIFIC.equals(tag)) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public Image getImage(Object obj, int columnIndex) {
			if (getCellControlType(obj, columnIndex) == CellControlType.BUTTON) {
				switch (getString(obj, columnIndex)) {
					case CmsisConstants.BUTTON_UPTODATE:
						return CpPlugInUI.getImage(CpPlugInUI.ICON_RTE);
					case CmsisConstants.BUTTON_UPDATE:
					case CmsisConstants.BUTTON_OFFLINE:
					case CmsisConstants.BUTTON_DEPRECATED:
						return CpPlugInUI.getImage(CpPlugInUI.ICON_RTE_WARNING);
					case CmsisConstants.BUTTON_INSTALL:
						return CpPlugInUI.getImage(CpPlugInUI.ICON_RTE_INSTALL);
					case CmsisConstants.BUTTON_UNPACK:
						return CpPlugInUI.getImage(CpPlugInUI.ICON_RTE_UNPACK);
					case CmsisConstants.BUTTON_REMOVE:
					case CmsisConstants.BUTTON_DELETE:
					case CmsisConstants.BUTTON_DELETE_ALL:
						return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_DELETE);
					default:
						break;
				}
			}
			return null;
		}

		@Override
		public String getString(Object element, int index) {
			String text = CmsisConstants.EMPTY_STRING;
			if (getCellControlType(element, index) != CellControlType.BUTTON) {
				return text;
			}
			if ((element instanceof ICpPackFamily) && !(element instanceof ICpPackCollection)) {
				ICpPackFamily packFamily = ((ICpPackFamily) element);
				if (CmsisConstants.ERRORS.equals(packFamily.getTag())) {
					return CmsisConstants.BUTTON_DELETE_ALL;
				}
				if (packFamily.getPack() == null) {
					return text;
				}
				Collection<? extends ICpItem> releases = packFamily.getPack().getGrandChildren(CmsisConstants.RELEASES_TAG);
				if (releases == null || releases.isEmpty()) {
					return CmsisConstants.BUTTON_DELETE;
				}
				ICpItem latestRelease = releases.iterator().next();
				if (latestRelease.hasAttribute(CmsisConstants.DEPRECATED)) {
					return CmsisConstants.BUTTON_DEPRECATED;
				}
				if (packFamily.getPack().getPackState() == ICpPack.PackState.INSTALLED){
					text = CmsisConstants.BUTTON_UPTODATE;
				} else {
					boolean hasPackInstalled = false, hasPackGenerated = false;
					for (ICpItem cpItem : packFamily.getChildren()) {
						if (cpItem instanceof ICpPack) {
							ICpPack pack = (ICpPack) cpItem;
							ICpItem urlItem = pack.getFirstChild(CmsisConstants.URL);
							if (pack.getPackState() == PackState.INSTALLED) {
								hasPackInstalled = true;
								break;
							} else if (pack.getPackState() == PackState.GENERATED
									|| urlItem == null || !Utils.isValidURL(urlItem.getText())) {
								hasPackGenerated = true;
								break;
							}
						}
					}
					if (hasPackInstalled) {
						text = CmsisConstants.BUTTON_UPDATE;
					} else if (hasPackGenerated) {
						text = CmsisConstants.BUTTON_OFFLINE;
					} else {
						text = CmsisConstants.BUTTON_INSTALL;
					}
				}
			} else if (element instanceof ICpPack) {
				ICpPack pack = (ICpPack) element;
				if (pack.getPackState() == PackState.INSTALLED ||
						pack.getPackState() == PackState.GENERATED) {
					text = CmsisConstants.BUTTON_REMOVE;
				} else if (pack.getPackState() == PackState.DOWNLOADED) {
					text = CmsisConstants.BUTTON_UNPACK;
				} else if (pack.getPackState() == PackState.ERROR) {
					text = CmsisConstants.BUTTON_DELETE;
				} else {
					text = CmsisConstants.BUTTON_INSTALL;
				}
			} else if (element instanceof ICpItem) {
				ICpItem item = getCpItem(element);
				if (CmsisConstants.GENERIC.equals(item.getTag()) ||
						CmsisConstants.DEVICE_SPECIFIC.equals(item.getTag())) {
					int count = 0;
					if (item.getChildren() != null) {
						for (ICpItem child : item.getChildren()) {
							if (fPacksExamplesViewFilter.select(fViewer, item, child)) {
								count++;
							}
						}
						if (count == 1) {
							return Messages.PacksView_1Pack;
						}
						return count + Messages.PacksView_Packs;
					}
				} else if (!CmsisConstants.PREVIOUS.equals(item.getTag())) {
					if (item.hasAttribute(CmsisConstants.DEPRECATED)) {
						text = CmsisConstants.BUTTON_DEPRECATED;
					} else {
						text = CmsisConstants.BUTTON_INSTALL;
					}
				}
			}

			return text;
		}

		@Override
		public String getTooltipText(Object obj, int columnIndex) {
			ICpItem item = getCpItem(obj);
			if (item != null) {
				return getToolTipDescription(item);
			}
			return null;
		}

		@Override
		protected void handleMouseUp(MouseEvent e) {
			if (e.button == 1) {
				Point pt = new Point(e.x, e.y);
				ViewerCell cell = getViewer().getCell(pt);
				if (cell == null) {
					return;
				}

				int colIndex = cell.getColumnIndex();
				Object element = cell.getElement();
				if (getCellControlType(element, colIndex) != CellControlType.BUTTON ||
						!isEnabled(element, colIndex) ||
						!isButtonPressed(element, colIndex)) {
					return;
				}

				ICpItem cpItem = getCpItem(element);
				if (cpItem == null || ROOT.equals(cpItem.getTag())) {
					return;
				}

				String packId = PackInstallerUtils.getFullPackId(cpItem);
				switch (getString(element, colIndex)) {
					case CmsisConstants.BUTTON_INSTALL:
					case CmsisConstants.BUTTON_UPDATE:
					case CmsisConstants.BUTTON_UNPACK:
						fPackInstaller.installPack(packId);
						break;
					case CmsisConstants.BUTTON_REMOVE:
						fPackInstaller.removePack((ICpPack) cpItem, false);
						break;
					case CmsisConstants.BUTTON_DELETE:
						fPackInstaller.removePack((ICpPack) cpItem, true);
						break;
					case CmsisConstants.BUTTON_DELETE_ALL:
						for (Iterator<? extends ICpItem> iter = cpItem.getChildren().iterator(); iter.hasNext();) {
							ICpPack pack = (ICpPack) iter.next();
							fPackInstaller.removePack(pack, true);
						}
						break;
					default:
						break;
				}

				setButtonPressed(null, COLBUTTON, null);
				this.control.redraw();
			}
		}

	}

	class PackTreeColumnComparator extends TreeColumnComparator {

		private VersionComparator versionComparator;

		public PackTreeColumnComparator(TreeViewer viewer, ColumnAdvisor advisor) {
			super(viewer, advisor);
			versionComparator = new VersionComparator();
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			ICpItem cp1 = (ICpItem)e1;
			ICpItem cp2 = (ICpItem)e2;

			// Error packs should always be shown on top
			if (CmsisConstants.ERRORS.equals(cp1.getTag())) {
				return -1;
			} else if (CmsisConstants.ERRORS.equals(cp2.getTag())) {
				return 1;
			}

			Tree tree = treeViewer.getTree();

			// For this view we only sort ICpPackFamily
			if (!(e1 instanceof ICpPackFamily || e1 instanceof ICpPack)
					|| !(e2 instanceof ICpPackFamily || e2 instanceof ICpPack)) {
				return 0;
			}

			// if this is not the 1st column, use default sorting
			if (getColumnIndex() != 0) {
				return super.compare(viewer, e1, e2);
			}

			// regular comparison
			int result = cp1.getPackFamilyId().compareToIgnoreCase(cp2.getPackFamilyId());
			if (result == 0) {
				if (cp1.hasAttribute(CmsisConstants.VERSION) && cp2.hasAttribute(CmsisConstants.VERSION)) {
					result = versionComparator.compare(cp1.getAttribute(CmsisConstants.VERSION),
							cp2.getAttribute(CmsisConstants.VERSION));
				} else {
					result = 0;
				}
			}
			return tree.getSortDirection() == SWT.DOWN ? -result : result;
		}
	}

	class ColumLabelProviderWithImage extends ColumnLabelProvider {
		@Override
		public String getText(Object obj) {
			ICpItem item = getCpItem(obj);
			if(item != null && !ROOT.equals(item.getTag())) {
				String date = PackInstallerUtils.getCpItemDate(item);
				String appendDate = CmsisConstants.EMPTY_STRING;
				if (!date.isEmpty()) {
					appendDate = " (" + date + ')'; //$NON-NLS-1$
				}
				// added spaces at last of text as a workaround to show the complete text in the views
				if (item.hasAttribute(CmsisConstants.VERSION)) {
					return item.getAttribute(CmsisConstants.VERSION) + appendDate + ' ';
				} else if (item instanceof ICpPack) {
					ICpPack pack = (ICpPack) item;
					if (pack.getPackState() != PackState.ERROR) {
						return item.getVersion() + appendDate + ' ';
					}
					return item.getTag() + ' ';
				} else {
					return item.getTag() + ' ';
				}
			}
			return null;
		}

		@Override
		public Image getImage(Object obj) {
			ICpItem item = getCpItem(obj);
			if(item == null || ROOT.equals(item.getTag())) {
				return null;
			}
			if (obj instanceof ICpPack) {
				ICpPack pack = (ICpPack) obj;
				if (pack.getPackState() == PackState.INSTALLED ||
						pack.getPackState() == PackState.GENERATED) {
					return CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGE);
				}
				return CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGE_GREY);
			} else if ((obj instanceof ICpPackFamily) && !(obj instanceof ICpPackCollection)) {
				ICpPackFamily packFamily = (ICpPackFamily) obj;
				if (CmsisConstants.ERRORS.equals(packFamily.getTag())) {
					return CpPlugInUI.getImage(CpPlugInUI.ICON_WARNING);
				}
				boolean hasPackInstalled = false;
				for (ICpItem pack : packFamily.getChildren()) {
					if (((ICpPack) pack).getPackState() == PackState.INSTALLED) {
						hasPackInstalled = true;
						break;
					}
				}
				if (hasPackInstalled) {
					return CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGES);
				}
				return CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGES_GREY);
			} else if (CmsisConstants.RELEASE_TAG.equals(item.getTag())) {
				return CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGE_GREY);
			}

			return CpPlugInUI.getImage(CpPlugInUI.ICON_ITEM);
		}

		@Override
		public String getToolTipText(Object element) {
			ICpItem item = getCpItem(element);
			if (item != null) {
				return getToolTipDescription(item);
			}
			return null;
		}
	}

	String getToolTipDescription(ICpItem item) {
		if (item == null) {
			return null;
		}
		if (item instanceof ICpPackFamily) {
			if (CmsisConstants.ERRORS.equals(item.getTag())) {
				StringBuilder sb = new StringBuilder(Messages.PacksView_DeleteAllTooltip);
				for (ICpItem child : item.getChildren()) {
					ICpPack pack = (ICpPack) child;
					sb.append("\n  " + pack.getFileName()); //$NON-NLS-1$
				}
				return sb.toString();
			}
			return getToolTipDescription(item.getPack());
		} else if (item instanceof ICpPack) {
			ICpPack pack = item.getPack();
			if (pack.getPackState() == PackState.ERROR) {
				return Messages.PacksView_Delete_ + pack.getFileName();
			}
			for (ICpItem release : pack.getGrandChildren(CmsisConstants.RELEASES_TAG)) {
				if (release.getAttribute(CmsisConstants.VERSION).equals(pack.getVersion())) {
					return getToolTipDescription(release);
				}
			}
			return null;
		} else if (CmsisConstants.RELEASE_TAG.equals(item.getTag())) {
			String tooltip = CmsisConstants.EMPTY_STRING;
			if (item.hasAttribute(CmsisConstants.DEPRECATED)) {
				tooltip += NLS.bind(Messages.PacksView_DeprecatedOn, item.getAttribute(CmsisConstants.DEPRECATED));
			}
			if (item.hasAttribute(CmsisConstants.REPLACEMENT)) {
				tooltip += NLS.bind(Messages.PacksView_ReplacedBy, item.getAttribute(CmsisConstants.REPLACEMENT));
			}
			tooltip += NLS.bind(Messages.PacksView_Version, fColumnLabelProviderWithImage.getText(item)) + item.getText();
			return tooltip;
		}

		return null;
	}

	/**
	 * The constructor.
	 */
	public PacksView() {
		fColumnLabelProviderWithImage = new ColumLabelProviderWithImage();
		fPackInstaller = CpPlugIn.getPackManager().getPackInstaller();
	}

	@Override
	public void createPartControl(Composite parent) {
		fPacksExamplesViewFilter = PacksExamplesViewFilter.getInstance();
		PatternFilter patternFilter = new PatternFilter() {
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
		patternFilter.setIncludeLeadingWildcard(true);
		fPacksViewFilters = new ViewerFilter[] { patternFilter, fPacksExamplesViewFilter };

		fTree = new FilteredTree(parent,
				SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, patternFilter, true);
		fTree.setInitialText(Messages.PacksView_SearchPack);
		fViewer = fTree.getViewer();
		Tree tree = fViewer.getTree();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		//------ Start Setting ALL Columns for the Packs View
		//------ First Column
		TreeViewerColumn column0 = new TreeViewerColumn(fViewer, SWT.LEFT);
		column0.getColumn().setText(CmsisConstants.PACK_TITLE);
		column0.getColumn().setWidth(250);
		column0.setLabelProvider(fColumnLabelProviderWithImage);

		//------ Second Column
		TreeViewerColumn column1 = new TreeViewerColumn(fViewer, SWT.LEFT);
		column1.getColumn().setText(CmsisConstants.ACTION_TITLE);
		column1.getColumn().setWidth(90);
		fColumnAdvisor = new PacksViewColumnAdvisor(fViewer);
		column1.setLabelProvider(new AdvisedCellLabelProvider(fColumnAdvisor, COLBUTTON));

		//------ Third Column
		TreeViewerColumn column2 = new TreeViewerColumn(fViewer, SWT.LEFT);
		column2.getColumn().setText(CmsisConstants.DESCRIPTION_TITLE);
		column2.getColumn().setWidth(400);
		column2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object obj) {
				ICpItem item = getCpItem(obj);
				if (item == null) {
					return null;
				}
				// Add selection string on the first line
				if (item instanceof ICpPackCollection) {
					if (CmsisConstants.DEVICE_SPECIFIC.equals(item.getTag())) {
						String filterString = fPacksExamplesViewFilter
								.getFilterString();
						return filterString == null
								? null
										: filterString + Messages.PacksView_Selected;
					} else if (CmsisConstants.GENERIC.equals(item.getTag())){
						return Messages.PacksView_GenericPacksDescription;
					}
				}

				if (item instanceof ICpPackFamily) {
					ICpPackFamily packFamily = (ICpPackFamily) item;
					if (CmsisConstants.ERRORS.equals(packFamily.getTag())) {
						return Messages.PacksView_CannotLoadPdscFiles;
					}
					return formatDescription(item.getDescription());
				}

				if (CmsisConstants.RELEASE_TAG.equals(item.getTag())) {
					return formatDescription(item.getText());
				} else if (CmsisConstants.PREVIOUS.equals(item.getTag())) {
					return item.getPackFamilyId() + Messages.PacksView_PreviousPackVersions;
				}

				ICpPack pack = item.getPack();
				if (pack.getPackState() != PackState.ERROR) {
					for (ICpItem cpItem : pack.getGrandChildren(CmsisConstants.RELEASES_TAG)) {
						if (cpItem.getAttribute(CmsisConstants.VERSION).equals(pack.getVersion())) {
							return formatDescription(cpItem.getText());
						}
					}
				} else {
					return Messages.PacksView_Location + pack.getFileName();
				}
				return null;
			}

			// Due to the multi-line table cell in Linux, we
			// only use the first line when OS is Linux
			private String formatDescription(String description) {
				boolean isWinOS = System.getProperty("os.name").startsWith("Windows"); //$NON-NLS-1$ //$NON-NLS-2$
				return isWinOS ? description : description.split("\\r?\\n")[0]; //$NON-NLS-1$
			}
		});
		//------ End Setting ALL Columns for the Packs View

		fViewer.setContentProvider(new PacksViewContentProvider());
		fViewer.setComparator(new PackTreeColumnComparator(fViewer, fColumnAdvisor));
		fViewer.setAutoExpandLevel(2);
		refresh();

		ColumnViewerToolTipSupport.enableFor(fViewer);

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(fViewer.getControl(), IHelpContextIds.PACKS_VIEW);

		getSite().setSelectionProvider(fViewer);

		makeActions();
		hookViewSelection();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();

		CpInstallerPlugInUI.registerViewPart(this);

		fViewer.setFilters(fPacksViewFilters);

		CpPlugIn.addRteListener(this);
	}

	protected void refresh() {
		if(CpPlugIn.getDefault() == null) {
			return;
		}
		ICpPackManager packManager = CpPlugIn.getPackManager();
		if(packManager != null) {
			ICpItem root = new CpItem(null, ROOT);
			ICpPackFamily errorPacks = packManager.getErrorPacks();
			if (errorPacks != null && errorPacks.getChildCount() > 0) {
				root.addChild(errorPacks);
			}
			root.addChild(packManager.getDevicePacks());
			root.addChild(packManager.getGenericPacks());
			fViewer.setInput(root);
		} else {
			fViewer.setInput(null);
		}
	}

	/**
	 * hook the selection listener of device/board view
	 */
	private void hookViewSelection() {

		fViewSelectionListener = new ISelectionListener() {

			@Override
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {

				if ((part instanceof DevicesView)
						|| (part instanceof BoardsView)) {
					if (!fTree.getFilterControl().isDisposed()) {
						fTree.getFilterControl().setText(CmsisConstants.EMPTY_STRING);
					}
					fireSelectionChanged(part, selection);
				}
			}
		};
		getSite().getPage().addSelectionListener(fViewSelectionListener);
	}

	/**
	 * @param part
	 * @param selection
	 */
	protected void fireSelectionChanged(IWorkbenchPart part, ISelection selection) {
		fPacksExamplesViewFilter.setSelection(part, (IStructuredSelection)selection);
		if (!fViewer.getControl().isDisposed()) {
			fViewer.setFilters(fPacksViewFilters);
			fViewer.setSelection(null);
		}
		ICpItem root = (ICpItem) fViewer.getInput();
		if (fViewer.isExpandable(root.getFirstChild())) {
			fViewer.expandToLevel(root.getFirstChild(), 1);
		}
	}

	private void makeActions() {

		//Show Pack Outline Action
		fShowPackProperties = new Action() {
			@Override
			public void run() {
				try {
					PackPropertyView outlineView = (PackPropertyView) PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage().showView(PackPropertyView.ID);
					ICpItem proot = new CpItem(null);
					proot.addChild(getPackItem());
					outlineView.fViewer.setAutoExpandLevel(2);
					outlineView.fViewer.setInput(proot);
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
		};
		fShowPackProperties.setText(Messages.PacksView_ShowPacksOutline);
		fShowPackProperties.setToolTipText(Messages.PacksView_ShowPacksOutline);
		fShowPackProperties.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_DETAILS));

		fDeletePack = new Action() {
			@Override
			public void run() {
				fPackInstaller.removePack(getPackItem(), true);
			};
		};
		fDeletePack.setText(Messages.PacksView_RemovePlusDelete);
		fDeletePack.setToolTipText(Messages.PacksView_DeleteSelectedPack);
		fDeletePack.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));

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
		fHelpAction.setToolTipText(Messages.PacksView_HelpForPacksView);
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

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				PacksView.this.fillContextMenu(manager);
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
		PackInstallerViewUtils.addManagementCommandsToLocalToolBar(this, manager);
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
		ICpPack pack = getPackItem();
		if (pack != null) {
			manager.add(new Separator());
			manager.add(fShowPackProperties);
			if (pack.getPackState() == PackState.INSTALLED || pack.getPackState() == PackState.GENERATED) {
				manager.add(new Separator());
				manager.add(fDeletePack);
				fDeletePack.setText(Messages.PacksView_RemovePlusDelete);
				if (fPackInstaller.isProcessing(pack.getId())) {
					fDeletePack.setEnabled(false);
				}
			} else if (pack.getPackState() == PackState.DOWNLOADED) {
				manager.add(new Separator());
				manager.add(fDeletePack);
				fDeletePack.setText(Messages.PacksView_Delete);
				if (fPackInstaller.isProcessing(pack.getId())) {
					fDeletePack.setEnabled(false);
				}
			}
		}
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(fExpandAction);
		manager.add(fCollapseAction);
		manager.add(fHelpAction);
		manager.add(new Separator());
		PackInstallerViewUtils.addManagementCommandsToLocalToolBar(this, manager);
	}

	ICpPack getPackItem() {
		IStructuredSelection selection = (IStructuredSelection) fViewer.getSelection();
		if (selection == null || selection.isEmpty()) {
			return null;
		}
		Object obj = selection.getFirstElement();
		if (obj instanceof ICpPack) {
			return (ICpPack)obj;
		}
		return null;
	}

	public Composite getComposite() {
		return fTree;
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		fViewer.getControl().setFocus();
	}

	@Override
	public void handle(RteEvent event) {
		if (RteEvent.PACKS_RELOADED.equals(event.getTopic())) {
			fPackInstaller.reset();
			Display.getDefault().asyncExec(() -> refresh());
		} else {
			Display.getDefault().asyncExec(() -> fViewer.refresh());
		}
	}

	@Override
	public void dispose() {
		CpPlugIn.removeRteListener(this);
		if (fViewSelectionListener != null) {
			getSite().getPage().removeSelectionListener(
					fViewSelectionListener);
		}
		super.dispose();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == Viewer.class) {
			return adapter.cast(fViewer);
		}
		return super.getAdapter(adapter);
	}

}
