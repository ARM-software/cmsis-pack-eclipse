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
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackInstaller;
import com.arm.cmsis.pack.ICpPackManager;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpItem;
import com.arm.cmsis.pack.data.CpPack;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPack.PackState;
import com.arm.cmsis.pack.data.ICpPackCollection;
import com.arm.cmsis.pack.data.ICpPackFamily;
import com.arm.cmsis.pack.installer.ui.IHelpContextIds;
import com.arm.cmsis.pack.installer.ui.Messages;
import com.arm.cmsis.pack.item.ICmsisItem;
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
public class PacksView extends PackInstallerView {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.arm.cmsis.pack.installer.ui.views.PacksView"; //$NON-NLS-1$

	private final static String ROOT = "Root"; //$NON-NLS-1$

	private Action fRemovePack;
	private Action fDeletePack;
	private Action fInstallSinglePack;
	private Action fInstallRequiredPacks;

	private ColumLabelProviderWithImage fColumnLabelProviderWithImage;


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
			ICpPackInstaller packInstaller = getPackInstaller();
			if(packInstaller == null) {
				return false;
			}

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
				} else if (cpItem instanceof ICpPackFamily) {
					return !packInstaller.isProcessing(cpItem.getPackId());
				} else if (CmsisConstants.PREVIOUS.equals(cpItem.getTag())){
					return false;
				} else {
					return !packInstaller.isProcessing(CpPack.getFullPackId(cpItem));
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
				case CmsisConstants.BUTTON_UPTODATE: // the latest pack is installed
					ICpPackFamily pf = (ICpPackFamily) obj;
					if (pf != null && !pf.getPack().isRequiredPacksInstalled()) {
						return CpPlugInUI.getImage(CpPlugInUI.ICON_RTE_SUB_WARNING);
					}
					return CpPlugInUI.getImage(CpPlugInUI.ICON_RTE);

				case CmsisConstants.BUTTON_UPDATE:
				case CmsisConstants.BUTTON_UPDATE_PLUS:
				case CmsisConstants.BUTTON_OFFLINE:
				case CmsisConstants.BUTTON_DEPRECATED:
				case CmsisConstants.BUTTON_RESOLVE:
					return CpPlugInUI.getImage(CpPlugInUI.ICON_RTE_WARNING);
				case CmsisConstants.BUTTON_INSTALL:
				case CmsisConstants.BUTTON_INSTALL_PLUS:
					return CpPlugInUI.getImage(CpPlugInUI.ICON_RTE_INSTALL);
				case CmsisConstants.BUTTON_UNPACK:
				case CmsisConstants.BUTTON_UNPACK_PLUS:
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
				ICpPack latestPack = packFamily.getPack(); 
				if ( latestPack == null) {
					return text;
				}
				Collection<? extends ICpItem> releases = latestPack.getReleases();
				if (releases == null) {
					return CmsisConstants.BUTTON_DELETE;
				}
				if (latestPack.isDeprecated()) {
					return CmsisConstants.BUTTON_DEPRECATED;
				}
				
				if (latestPack.getPackState() == PackState.INSTALLED) {
					return CmsisConstants.BUTTON_UPTODATE;
				}
				boolean bPackInstalled = false, bPackOffline = false;
				for (ICpPack pack : packFamily.getPacks()) {
					if (pack.getPackState() == PackState.INSTALLED) {
						bPackInstalled = true;
						ICpItem urlItem = pack.getFirstChild(CmsisConstants.URL);
						if (urlItem == null || !Utils.isValidURL(urlItem.getText())) {
							bPackOffline = true;
						}
						break;
					}
				}
				if (!bPackInstalled) {
					if (latestPack.isRequiredPacksInstalled()) {
						return CmsisConstants.BUTTON_INSTALL;
					}
					return CmsisConstants.BUTTON_INSTALL_PLUS;
				} else if (bPackOffline) {
					return CmsisConstants.BUTTON_OFFLINE;
				} else {
					if (latestPack.isRequiredPacksInstalled()) {
						return CmsisConstants.BUTTON_UPDATE;
					}
					return CmsisConstants.BUTTON_UPDATE_PLUS;
				}
			} else if (element instanceof ICpPack) {
				ICpPack pack = (ICpPack) element;
				PackState state = pack.getPackState();
				boolean requiredPacksInstalled = pack.isRequiredPacksInstalled();
				if (state == PackState.INSTALLED) {
					if (requiredPacksInstalled) {
						return CmsisConstants.BUTTON_REMOVE;
					}
					return CmsisConstants.BUTTON_RESOLVE;
				} else if (state == PackState.DOWNLOADED) {
					if (requiredPacksInstalled) {
						return CmsisConstants.BUTTON_UNPACK;
					}
					return CmsisConstants.BUTTON_UNPACK_PLUS;
				} else if (state == PackState.ERROR) {
					return CmsisConstants.BUTTON_DELETE;
				} else if (state == PackState.AVAILABLE ) {
					if (requiredPacksInstalled) {
						return CmsisConstants.BUTTON_INSTALL;
					}
					return CmsisConstants.BUTTON_INSTALL_PLUS;
				}
			} else if (element instanceof ICpItem) {
				ICpItem item = getCpItem(element);
				if (CmsisConstants.GENERIC.equals(item.getTag())) {
					int count = item.getChildCount();
					text = getPackCountString(count);
				} else if(CmsisConstants.DEVICE_SPECIFIC.equals(item.getTag())) {
					int count = fViewController.getFilter().getFilteredDevicePackFamilies().size();
					text = getPackCountString(count);
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

		private String getPackCountString(int count) {
			if (count == 1 ) {
				return Messages.PacksView_1Pack;
			} else if (count > 1 ) {
				return count + Messages.PacksView_Packs;
			}
			return CmsisConstants.EMPTY_STRING;
		}

		@Override
		public String getTooltipText(Object obj, int columnIndex) {
			ICpItem item = getCpItem(obj);
			if (item != null) {
				return getActionTooltip(item) == null ? getNameTooltip(item) : getActionTooltip(item);
			}
			return null;
		}

		@Override
		protected void handleMouseUp(MouseEvent e) {
			if (e.button == 1) {
				ICpPackInstaller packInstaller = getPackInstaller();
				if(packInstaller == null) {
					return;
				}

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

				String packId = CpPack.getFullPackId(cpItem);
				switch (getString(element, colIndex)) {
				case CmsisConstants.BUTTON_INSTALL:
				case CmsisConstants.BUTTON_INSTALL_PLUS:
				case CmsisConstants.BUTTON_UPDATE:
				case CmsisConstants.BUTTON_UPDATE_PLUS:
				case CmsisConstants.BUTTON_UNPACK:
				case CmsisConstants.BUTTON_UNPACK_PLUS:
					packInstaller.installPack(packId);
					break;
				case CmsisConstants.BUTTON_REMOVE:
					packInstaller.removePack((ICpPack) cpItem, false);
					break;
				case CmsisConstants.BUTTON_DELETE:
					packInstaller.removePack((ICpPack) cpItem, true);
					break;
				case CmsisConstants.BUTTON_RESOLVE:
					packInstaller.installRequiredPacks((ICpPack) cpItem);
					break;
				case CmsisConstants.BUTTON_DELETE_ALL:
					for (Iterator<? extends ICpItem> iter = cpItem.getChildren().iterator(); iter.hasNext();) {
						ICpPack pack = (ICpPack) iter.next();
						packInstaller.removePack(pack, true);
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
				String date = CpPack.getCpItemDate(item);
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
				if (pack.getPackState() == PackState.INSTALLED) {
					return CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGE);
				}
				return CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGE_GREY);
			} else if ((obj instanceof ICpPackFamily) && !(obj instanceof ICpPackCollection)) {
				ICpPackFamily packFamily = (ICpPackFamily) obj;
				if (CmsisConstants.ERRORS.equals(packFamily.getTag())) {
					return CpPlugInUI.getImage(CpPlugInUI.ICON_WARNING);
				}

				for (ICpPack pack : packFamily.getPacks()) {
					if (pack.getPackState() == PackState.INSTALLED) {
						return CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGES);
					}
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
				return getNameTooltip(item);
			}
			return null;
		}
	}

	/**
	 * Get the tooltip for the Pack column
	 * @param item the tree item
	 * @return the tooltip or null if no tooltip needed
	 */
	String getNameTooltip(ICpItem item) {
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
			return getNameTooltip(item.getPack());
		} else if (item instanceof ICpPack) {
			ICpPack pack = item.getPack();
			if (pack.getPackState() == PackState.ERROR) {
				return Messages.PacksView_Delete_ + pack.getFileName();
			}
			if (pack.getReleases() == null) {
				return null;
			}
			for (ICpItem release : pack.getReleases()) {
				if (release.getVersion().equals(pack.getVersion())) {
					String releaseTooltip = getNameTooltip(release);
					String requiredPacks = CmsisConstants.EMPTY_STRING;
					if (pack.getRequiredPacks() != null) {
						requiredPacks += Messages.PacksView_RequiredPacks;
						for (ICpItem reqPack: pack.getRequiredPacks()) {
							String packId = reqPack.getVendor() + '.'
									+ reqPack.getName() + '.'
									+ '[' + reqPack.getVersion() + ']';
							requiredPacks += "    " + packId + '\n'; //$NON-NLS-1$
						}
					}
					return requiredPacks + releaseTooltip;
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
	 * Get the tooltip for the Action column
	 * @param item the tree item
	 * @return the tooltip or null if no tooltip needed
	 */
	String getActionTooltip(ICpItem item) {
		if (!(item instanceof ICpPack)) {
			return null;
		}
		ICpPack pack = (ICpPack) item;
		if (pack.getPackState() != PackState.INSTALLED || pack.isRequiredPacksInstalled()) {
			return null;
		}

		Collection<? extends ICpItem> reqPacks = pack.getRequiredPacks();
		if (reqPacks == null) {
			return null;
		}
		String tooltip = Messages.PacksView_ResolveRequiredPacks;
		for (ICpItem reqPack : reqPacks) {
			ICpPackCollection installedPacks = CpPlugIn.getPackManager().getInstalledPacks();
			if (installedPacks != null && installedPacks.getPack(reqPack.attributes()) != null) {
				continue;
			}
			String packId = reqPack.getVendor() + '.' + reqPack.getName() + '.'
					+ '[' + reqPack.getVersion() + ']';
			tooltip += "    " + packId + '\n'; //$NON-NLS-1$
		}
		return tooltip;
	}

	/**
	 * The constructor.
	 */
	public PacksView() {
		fColumnLabelProviderWithImage = new ColumLabelProviderWithImage();
	}


	@Override
	protected boolean hasManagerCommands() {
		return true;
	}

	@Override
	public void createTreeColumns() {
		fTree.setInitialText(Messages.PacksView_SearchPack);

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
		PacksViewColumnAdvisor columnAdvisor = new PacksViewColumnAdvisor(fViewer);
		column1.setLabelProvider(new AdvisedCellLabelProvider(columnAdvisor, COLBUTTON));

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
						String filterString = fViewController.getFilter().getFilterString();
						return filterString == null ? null	: filterString + Messages.PacksView_Selected;
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
		fViewer.setComparator(new PackTreeColumnComparator(fViewer, columnAdvisor));
		fViewer.setAutoExpandLevel(2);
	}

	@Override
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

	@Override
	protected void handleFilterChanged() {
		super.handleFilterChanged();
		ICpItem root = (ICpItem) fViewer.getInput();
		if (root != null && fViewer.isExpandable(root.getFirstChild())) {
			fViewer.expandToLevel(root.getFirstChild(), 1);
		}
	}

	@Override
	protected void makeActions() {

		fRemovePack = new Action() {
			@Override
			public void run() {
				ICpPackInstaller packInstaller = getPackInstaller();
				if(packInstaller != null) {
					packInstaller.removePack(getPackItem(), false);
				}
			}
		};
		fRemovePack.setText(Messages.PacksView_Remove);
		fRemovePack.setToolTipText(Messages.PacksView_RemoveSelectedPack);
		fRemovePack.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));

		fDeletePack = new Action() {
			@Override
			public void run() {
				ICpPackInstaller packInstaller = getPackInstaller();
				if(packInstaller != null) {
					packInstaller.removePack(getPackItem(), true);
				}
			}
		};
		fDeletePack.setToolTipText(Messages.PacksView_DeleteSelectedPack);
		fDeletePack.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));

		fInstallSinglePack = new Action() {
			@Override
			public void run() {
				ICpPackInstaller packInstaller = getPackInstaller();
				ICpPack pack = getPackItem();
				if(packInstaller != null && pack != null) {
					packInstaller.installPack(pack.getId(), false);
				}
			};
		};
		fInstallSinglePack.setToolTipText(Messages.PacksView_InstallSinglePackTooltip);

		fInstallRequiredPacks = new Action() {
			@Override
			public void run() {
				ICpPackInstaller packInstaller = getPackInstaller();
				if(packInstaller != null) {
					packInstaller.installRequiredPacks(getPackItem());
				}
			}
		};
		fInstallRequiredPacks.setText(Messages.PackInstallerView_InstallRequiredPacks);
		fInstallRequiredPacks.setToolTipText(Messages.PackInstallerView_InstallRequiredPacksToolTip);
		fInstallRequiredPacks.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_RTE_WARNING));

		super.makeActions();

	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		super.fillContextMenu(manager);
		ICpPackInstaller packInstaller = getPackInstaller();
		if(packInstaller == null) {
			return;
		}
		manager.add(new Separator());

		ICpPack pack = getPackItem();
		if (pack != null) {
			switch (pack.getPackState()) {
			case INSTALLED:
				manager.add(fRemovePack);
				manager.add(fDeletePack);
				fDeletePack.setText(Messages.PacksView_RemovePlusDelete);
				if (!pack.isRequiredPacksInstalled()) {
					manager.add(new Separator());
					manager.add(fInstallRequiredPacks);
				}
				break;
			case DOWNLOADED:
				manager.add(fDeletePack);
				fDeletePack.setText(Messages.PacksView_Delete);
				if (!pack.isRequiredPacksInstalled()) {
					manager.add(new Separator());
					manager.add(fInstallSinglePack);
					fInstallSinglePack.setText(Messages.PacksView_UnpackSinglePack);
					fInstallSinglePack.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_RTE_UNPACK));
				}
				break;
			case AVAILABLE:
				if (!pack.isRequiredPacksInstalled()) {
					manager.add(new Separator());
					manager.add(fInstallSinglePack);
					fInstallSinglePack.setText(Messages.PacksView_InstallSinglePack);
					fInstallSinglePack.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_RTE_INSTALL));
				}
				break;
			default:
				break;
			}

			if (packInstaller.isProcessing(pack.getId())) {
				fRemovePack.setEnabled(false);
				fDeletePack.setEnabled(false);
				fInstallSinglePack.setEnabled(false);
				fInstallRequiredPacks.setEnabled(false);
			}
		}
	}

	/**
	 * Returns selected item if it represents an ICpPack
	 * @return ICpPack
	 */
	ICpPack getPackItem() {
		ICmsisItem item = getSelectedItem();
		if(item != null && item instanceof ICpPack) {
			return (ICpPack) item;
		}
		return null;
	}


	@Override
	protected String getHelpContextId() {
		return IHelpContextIds.PACKS_VIEW;
	}

}
