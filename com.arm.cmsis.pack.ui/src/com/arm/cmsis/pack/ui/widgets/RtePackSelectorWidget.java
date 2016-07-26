/*******************************************************************************
 * Copyright (c) 2015 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/

package com.arm.cmsis.pack.ui.widgets;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;

import com.arm.cmsis.pack.CpStrings;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.enums.EVersionMatchMode;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.rte.IRteModelController;
import com.arm.cmsis.pack.rte.packs.IRtePack;
import com.arm.cmsis.pack.rte.packs.IRtePackFamily;
import com.arm.cmsis.pack.rte.packs.IRtePackItem;
import com.arm.cmsis.pack.rte.packs.RtePack;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.CpStringsUI;
import com.arm.cmsis.pack.ui.tree.AdvisedCellLabelProvider;
import com.arm.cmsis.pack.ui.tree.AdvisedEditingSupport;
import com.arm.cmsis.pack.ui.tree.TreeObjectContentProvider;

/**
 * Tree widget to select pack versions
 */
public class RtePackSelectorWidget extends RteWidget {
	protected final static String[] VERSION_MODES = new String[]{CpStrings.Latest, CpStrings.Fixed, CpStrings.Excluded};

	static final Color GREEN = new Color(Display.getCurrent(), CpPlugInUI.GREEN);
	static final Color YELLOW = new Color(Display.getCurrent(),CpPlugInUI.YELLOW);

	static final String[] PACK_ICONS = new String[]{CpPlugInUI.ICON_PACKAGE,
													CpPlugInUI.ICON_PACKAGE_EMPTY,
													CpPlugInUI.ICON_PACKAGE_GREY,
													CpPlugInUI.ICON_PACKAGE_RED,
													CpPlugInUI.ICON_PACKAGES,
													CpPlugInUI.ICON_PACKAGES_EMPTY,
													CpPlugInUI.ICON_PACKAGES_GREY,
													CpPlugInUI.ICON_PACKAGES_RED};
	public static final int ICON_INDEX_EMPTY = 1;
	public static final int ICON_INDEX_GREY = 2;
	public static final int ICON_INDEX_RED = 3;
	public static final int ICON_INDEX_PACKAGES = 4;


	TreeViewer viewer = null;					// the Tree Viewer
	private static final int COLPACK	= 0;
	private static final int COLSEL 	= 1;
	private static final int COLVERSION = 2;
	private static final int COLDESCR 	= 3;

	public RtePackSelectorWidget() {
	}

	public IRtePackItem getRtePackItem(Object obj){
		if(obj instanceof IRtePackItem) {
			return (IRtePackItem)obj;
		}
		return null;
	}

	public IRtePackFamily getRtePackFamily(Object obj){
		if(obj instanceof IRtePackFamily) {
			return (IRtePackFamily)obj;
		}
		return null;
	}

	public RtePack getRtePack(Object obj){
		if(obj instanceof RtePack) {
			return (RtePack)obj;
		}
		return null;
	}

	public int getIconIndex(IRtePackItem item) {
		int index = 0;
		if(!item.isInstalled()) {
			if(item.isUsed()) {
				index = ICON_INDEX_RED;
			} else {
				index = ICON_INDEX_EMPTY;
			}
		} else if (item.isExcluded()) {
			index = ICON_INDEX_GREY;
		}

		if(item instanceof IRtePackFamily) {
			index += ICON_INDEX_PACKAGES;
		}
		return index;
	};

	/**
	 * 	Content provider for RteValidateWidget tree
	 */
	public class RtePackProvider extends TreeObjectContentProvider {
		@Override
		public Object[] getElements(Object inputElement) {
			if(inputElement == getModelController()) {
				return getModelController().getRtePackCollection().getChildArray();
			}
			return super.getElements(inputElement);
		}
	}

	/**
	 * Column label provider for RtePackSelectorWidget
	 */
	public class RtePackSelectorColumnAdvisor extends RteColumnAdvisor {
		/**
		 * Constructs advisor for a viewer
		 * @param columnViewer ColumnViewer on which the advisor is installed
		 */
		public RtePackSelectorColumnAdvisor(ColumnViewer columnViewer) {
			super(columnViewer);
		}

		@Override
		public CellControlType getCellControlType(Object obj, int columnIndex) {
			IRtePackItem item = getRtePackItem(obj);
			if(item == null) {
				return CellControlType.NONE;
			}
			IRtePackFamily packFamily = getRtePackFamily(obj);
			switch (columnIndex) {
			case COLSEL:
				if(packFamily != null) {
					return CellControlType.MENU;
				}
				return CellControlType.CHECK;
			case COLDESCR:
				if(packFamily != null) {
					String url = item.getUrl();
					if(url != null && ! url.isEmpty()) {
						return CellControlType.URL;
					}
				}
				break;
			case COLPACK:
			case COLVERSION:
			default:
				break;
			}

			return CellControlType.TEXT;
		}

		@Override
		public boolean getCheck(Object obj, int columnIndex) {
			if (getCellControlType(obj, columnIndex) == CellControlType.CHECK) {
				IRtePack pack = getRtePack(obj);
				boolean check = pack.isSelected();
				return check;
			}
			return false;
		}

		@Override
		public String getString(Object obj, int index) {
			IRtePackItem item = getRtePackItem(obj);
			if(item != null) {
				String id = item.getId();
				IRtePackFamily packFamily =  getRtePackFamily(obj);
				switch(index) {
				case COLPACK: {
					if(packFamily != null) {
						return id;
					}
					return item.getVersion();
				}
				case COLSEL:
					if(packFamily != null) {
						int i = (int) getCurrentSelectedIndex(obj, index);
						return VERSION_MODES[i];
					}
					break;
				case COLVERSION:
					if(packFamily != null) {
						return packFamily.getVersion();
					}
					break;
				case COLDESCR:
					if(packFamily != null || !item.isInstalled()) {
						return item.getDescription();
					}
				default:
					break;
				}
			}
			return CmsisConstants.EMPTY_STRING;
		}

		@Override
		public long getCurrentSelectedIndex(Object element, int columnIndex) {
			if(columnIndex == COLSEL) {
				IRtePackFamily packFamily = getRtePackFamily(element);
				if(packFamily != null)  {
					return packFamily.getVersionMatchMode().ordinal();
				}
			}
			return -1;
		}

		@Override
		public String[] getStringArray(Object obj, int columnIndex) {
			if(columnIndex == COLSEL) {
				IRtePackFamily packFamily = getRtePackFamily(obj);
				if(packFamily != null) {
					return VERSION_MODES;
				}
			}
			return null;
		}

		@Override
		public boolean canEdit(Object obj, int columnIndex) {
			if(columnIndex == COLSEL) {
				return isEnabled(obj, columnIndex);
			}
			return false;
		}


		@Override
		public boolean isEnabled(Object obj, int columnIndex) {
			if(columnIndex == COLSEL ) {
				if(getModelController().getRtePackCollection().isUseAllLatestPacks()) {
					return false;
				}
				IRtePack pack = getRtePack(obj);
				if(pack != null) {
					return pack.getVersionMatchMode() != EVersionMatchMode.LATEST;
				}
				return getRtePackFamily(obj) != null;
			}
			return true;
		}


		@Override
		public Image getImage(Object obj, int columnIndex) {
			IRtePackItem item = getRtePackItem(obj);
			if (item != null) {
				if (columnIndex == 0) {
					int iconIndex = getIconIndex(item);
					Image baseImage = CpPlugInUI.getImage(PACK_ICONS[iconIndex]);
					return getOverlayImage(baseImage, obj, columnIndex);
				}
				switch (columnIndex) {
				case COLVERSION:
				case COLSEL:
					break;
				default:
					break;
				}
			}
			return null;
		}

		@Override
		public String getUrl(Object obj, int columnIndex) {
			if(columnIndex == COLDESCR) {
				IRtePackFamily item = getRtePackFamily(obj);
				if(item != null) {
					return item.getUrl();
				}
			}
			return null;
		}

		private Image getOverlayImage(Image baseImage, Object obj, int columnIndex) {
			return baseImage;
		}

		@Override
		public String getTooltipText(Object obj, int columnIndex) {
			IRtePackItem item = getRtePackItem(obj);
			if(item == null) {
				return null;
			}

			switch(columnIndex) {
			case COLPACK:
				return item.getDescription();
			case COLVERSION:
				return null; // TODO
			case COLDESCR:
				String url = item.getUrl();
				if(url != null && !url.isEmpty()) {
					return url;
				}
				break;
			default:
				break;
			}
			return null;
		}

		@Override
		public void setCheck(Object element, int columnIndex, boolean newVal) {
			if (getCellControlType(element, columnIndex) == CellControlType.CHECK) {
				IRtePack pack = getRtePack(element);
				if(pack != null) {
					fModelController.selectPack(pack, newVal);
				}
			}
		}

		@Override
		public void setString(Object obj, int columnIndex, String newVal) {
			if(columnIndex != COLSEL) {
				return;
			}
			IRtePackFamily packFamily = getRtePackFamily(obj);
			if(packFamily == null) {
				return;
			}
			EVersionMatchMode mode = EVersionMatchMode.fromString(newVal);
			packFamily.setVersionMatchMode(mode);
			fModelController.setVesrionMatchMode(packFamily, mode);
		}

		@Override
		public Color getBgColor(Object obj, int columnIndex) {
			if(columnIndex != COLSEL) {
				return null;
			}
			IRtePackItem item = getRtePackItem(obj);
			if(item != null && item.isUsed()) {
				Device device = Display.getCurrent();
				if(!item.isInstalled()) {
					return device.getSystemColor(SWT.COLOR_RED);
				} else if (!item.isSelected()) {
					return YELLOW;
				}
				return GREEN;
			}
			return null;
		}

	} /// end of ColumnAdviser


	@Override
	public Composite createControl(Composite parent) {
		Tree tree = new Tree(parent, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL|SWT.BORDER);
		tree.setHeaderVisible(true);
		viewer = new TreeViewer(tree);
		ColumnViewerToolTipSupport.enableFor(viewer);

		// Tree item name
		TreeViewerColumn column0 = new TreeViewerColumn(viewer, SWT.LEFT);
		tree.setLinesVisible(true);
		column0.getColumn().setText(CpStrings.Pack);
		column0.getColumn().setWidth(180);
		fColumnAdvisor = new RtePackSelectorColumnAdvisor(viewer);
		column0.setEditingSupport(new AdvisedEditingSupport(viewer, fColumnAdvisor, COLPACK));
		AdvisedCellLabelProvider col0LabelProvider = new AdvisedCellLabelProvider(fColumnAdvisor, COLPACK);
		// workaround jface bug: first owner-draw column is not correctly painted when column is resized
		col0LabelProvider.setOwnerDrawEnabled(false);
		column0.setLabelProvider(col0LabelProvider);

		// Check/menu box for selection
		TreeViewerColumn column1 = new TreeViewerColumn(viewer, SWT.LEFT);
		tree.setLinesVisible(true);
		column1.getColumn().setText(CpStrings.Selection);
		column1.getColumn().setWidth(100);
		column1.setEditingSupport(new AdvisedEditingSupport(viewer, fColumnAdvisor, COLSEL));
		column1.setLabelProvider(new AdvisedCellLabelProvider(fColumnAdvisor, COLSEL));

		// Version
		TreeViewerColumn column2 = new TreeViewerColumn(viewer, SWT.LEFT);
		column2.getColumn().setText(CpStringsUI.RteComponentTreeWidget_Version);
		column2.getColumn().setWidth(70);
		column2.setEditingSupport(new AdvisedEditingSupport(viewer, fColumnAdvisor, COLVERSION));
		column2.setLabelProvider(new AdvisedCellLabelProvider(fColumnAdvisor, COLVERSION));

		// Description/URL
		TreeViewerColumn column3= new TreeViewerColumn(viewer, SWT.LEFT);
		column3.getColumn().setText(CpStringsUI.RteComponentTreeWidget_Description);
		column3.getColumn().setWidth(400);
		column3.setEditingSupport(new AdvisedEditingSupport(viewer, fColumnAdvisor, COLDESCR));
		column3.setLabelProvider(new AdvisedCellLabelProvider(fColumnAdvisor, COLDESCR));

		viewer.setContentProvider(new RtePackProvider());

		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 2;
		tree.setLayoutData(gridData);

//		hookContextMenu();
		return tree;
	}


	@Override
	public void setModelController(IRteModelController model) {
		super.setModelController(model);
		if(model != null) {
			viewer.setInput(fModelController);
		} else {
			viewer.setInput(null);
		}
		update();
	}


	@Override
	public void handle(RteEvent event) {
	}

	@Override
	public void refresh() {
		viewer.refresh();
	}

	@Override
	public void update() {
		refresh();
	}

	@Override
	public Composite getFocusWidget() {
		return viewer.getTree();
	}

}
