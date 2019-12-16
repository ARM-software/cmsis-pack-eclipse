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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpComponent;
import com.arm.cmsis.pack.data.ICpGenerator;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.generic.ITreeObject;
import com.arm.cmsis.pack.info.ICpComponentInfo;
import com.arm.cmsis.pack.rte.IRteModel;
import com.arm.cmsis.pack.rte.IRteModelController;
import com.arm.cmsis.pack.rte.components.IRteComponent;
import com.arm.cmsis.pack.rte.components.IRteComponentBundle;
import com.arm.cmsis.pack.rte.components.IRteComponentClass;
import com.arm.cmsis.pack.rte.components.IRteComponentGroup;
import com.arm.cmsis.pack.rte.components.IRteComponentItem;
import com.arm.cmsis.pack.rte.components.RteMoreClass;
import com.arm.cmsis.pack.rte.components.RteSelectedDeviceClass;
import com.arm.cmsis.pack.ui.ColorConstants;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.CpStringsUI;
import com.arm.cmsis.pack.ui.LaunchGenerator;
import com.arm.cmsis.pack.ui.tree.AdvisedCellLabelProvider;
import com.arm.cmsis.pack.ui.tree.AdvisedEditingSupport;
import com.arm.cmsis.pack.ui.tree.TreeObjectContentProvider;
import com.arm.cmsis.pack.utils.Utils;


/**
 * This class displays the component tree for selection.
 *
 */
public class RteComponentSelectorWidget extends RteModelTreeWidget {

	// constants for column number
	static final int COLSWCOMP 	= 0;
	static final int COLSEL 	= 1;
	static final int COLVARIANT = 2;
	static final int COLVENDOR 	= 3;
	static final int COLVERSION = 4;
	static final int COLDESCR 	= 5;

	protected Map<String, LaunchGeneratorAction> laGeneratorActions = new HashMap<>();

	private List<String> selItemKeyPath = null;

	/**
	 *	Return the effective component
	 */
	public IRteComponentItem getComponentItem(Object obj){
		if(obj instanceof IRteComponentItem) {
			return ((IRteComponentItem)obj).getEffectiveItem();
		}
		return null;
	}

	public void launchGenerator(ICpGenerator gen, String launchType) {
		if (gen == null) {
			return;
		}
		LaunchGenerator.launch(gen, getModelController().getConfigurationInfo(), launchType);
	}
	
	/**
	 * Action to launch a generator for selected item  
	 */
	public class LaunchGeneratorAction extends Action {
		protected String fLaunchType;
		public LaunchGeneratorAction(String launchType){
			fLaunchType = launchType;
		}
		public String getLaunchType() {
			return fLaunchType;
		}
		@Override
		public void run() {
			launchGenerator(getSelectedGenerator(), getLaunchType());
		}
	}
	
	/**
	 * Column label provider for RteComponentTreeWidget
	 */
	public class RteComponentColumnAdvisor extends RteColumnAdvisor<IRteModelController> {
		/**
		 * Constructs advisor for a viewer
		 * @param columnViewer ColumnViewer on which the advisor is installed
		 */
		public RteComponentColumnAdvisor(ColumnViewer columnViewer) {
			super(columnViewer);
		}

		/**
		 * Return true if the tree item (RTE component) at column index contains a check box
		 * @param obj
		 * @return true if tree item at columnIndex is check box
		 */
		private CellControlType getSelectionControlType(Object obj) {
			IRteComponentItem item = getComponentItem(obj);
			if(item != null) {
				if (item instanceof IRteComponent) {
					int count = ((IRteComponent) item).getMaxInstanceCount();
					if (count == 1) {
						return CellControlType.INPLACE_CHECK;
					} else if (count > 1) {
						return CellControlType.INPLACE_SPIN;
					}
				}
			}
			return CellControlType.NONE;
		}

		@Override
		public CellControlType getCellControlType(Object obj, int columnIndex) {
			IRteComponentItem item = getComponentItem(obj);
			int minItems = 0;
			if(item == null) {
				return CellControlType.NONE;
			}
			Collection<String> strings = null;
			switch (columnIndex) {
			case COLSWCOMP:
				break;
			case COLSEL:
				return getSelectionControlType(obj);
			case COLVARIANT:
				strings = item.getVariantStrings();
				minItems = 1;
				break;
			case COLVENDOR:
				strings = item.getVendorStrings();
				minItems = 1;
				break;
			case COLVERSION:
				strings = item.getVersionStrings();
				break;
			case COLDESCR:
				String url = item.getUrl();
				if(url != null && ! url.isEmpty()) {
					return CellControlType.URL;
				}
				break;
			default: break;
			}

			if (strings != null && strings.size() > minItems) {
				return CellControlType.MENU;
			}

			return CellControlType.TEXT;
		}

		@Override
		public boolean getCheck(Object obj, int columnIndex) {
			if (columnIndex != COLSEL) {
				return false;
			}
			if (getSelectionControlType(obj) == CellControlType.CHECK
					|| getSelectionControlType(obj) == CellControlType.INPLACE_CHECK) {
				IRteComponentItem item = getComponentItem(obj);
				if(item instanceof IRteComponent) {
					boolean check = item.isSelected();
					return check;
				}
			}
			return false;
		}

		@Override
		public boolean hasSuffixButton(Object obj, int columnIndex) {
			if (columnIndex == COLSEL && getSelectionControlType(obj) == CellControlType.INPLACE_CHECK) {
				IRteComponentItem item = getComponentItem(obj);
				if(getGenerator(item) != null) {
					return true;
				}
			}
			return false;
		}


		@Override
		public boolean isEnabled(Object obj, int columnIndex) {
			IRteComponentItem item = getComponentItem(obj);
			if(columnIndex ==  COLSEL  || columnIndex ==  COLSWCOMP  || columnIndex == COLDESCR) {
				return true;
			}
			if(item instanceof IRteComponent) {
				IRteComponent rteComponent = (IRteComponent) item;
				if(rteComponent.isBootStrap()) {
					return true;
				} else if(rteComponent.isGenerated()) {
					return false;
				}
			}
			return true;
		}


		@Override
		public boolean isSuffixButtonEnabled(Object obj, int columnIndex) {
			if(columnIndex == COLSEL && hasSuffixButton(obj, columnIndex)) {
				return getCheck(obj, columnIndex);
			}
			return false;
		}

		@Override
		public String getString(Object obj, int index) {
			IRteComponentItem item = getComponentItem(obj);
			if(item != null) {
				switch(index) {
				case COLSWCOMP: {
					String label = item.getEffectiveName();
					return label;
				}
				case COLSEL:
					if (getSelectionControlType(obj) == CellControlType.INPLACE_SPIN) {
						return Integer.toString(((IRteComponent)item).getSelectedCount());
					}
					break;
				case COLVARIANT:
					return item.getActiveVariant();
				case COLVENDOR:
					return item.getActiveVendor();
				case COLVERSION:
					return item.getActiveVersion();		// active variant
				case COLDESCR:
					return item.getDescription();
				default:
					break;
				}
			}
			return CmsisConstants.EMPTY_STRING;
		}

		@Override
		public long getCurrentSelectedIndex(Object element, int columnIndex) {
			int index = -1;
			IRteComponentItem item = getComponentItem(element);

			if(item != null) {
				switch(columnIndex) {
				case COLSEL:
					if (getSelectionControlType(element) == CellControlType.INPLACE_SPIN) {
						index = ((IRteComponent)item).getSelectedCount();
					}
					break;
				case COLVARIANT:
					index = Utils.indexOf(item.getVariantStrings(), item.getActiveVariant());
					break;
				case COLVENDOR:
					index = Utils.indexOf(item.getVendorStrings(), item.getActiveVendor());
					break;
				case COLVERSION:
					index = Utils.indexOf(item.getVersionStrings(), item.getActiveVersion());
					break;
				default:
					break;
				}
			}

			return index;
		}


		@Override
		public long getMaxCount(Object obj, int columnIndex) {
			if(columnIndex == COLSEL && obj instanceof IRteComponent) {
				IRteComponent c = (IRteComponent)(obj);
				return c.getMaxInstanceCount();
			}
			return 0;
		}

		@Override
		public String[] getStringArray(Object obj, int columnIndex) {
			String [] strings = null;
			IRteComponentItem item = getComponentItem(obj);
			if(item != null) {
				switch(columnIndex) {
				case COLSWCOMP:
					break;
				case COLSEL:
					if (getSelectionControlType(obj) == CellControlType.INPLACE_SPIN) {
						strings = getSelectionStrings(obj);
					}
					break;
				case COLVARIANT:
					strings = item.getVariantStrings().toArray(new String[0]);
					break;
				case COLVENDOR:
					strings = item.getVendorStrings().toArray(new String[0]);
					break;
				case COLVERSION:
					strings = item.getVersionStrings().toArray(new String[0]);
					break;
				case COLDESCR:
				default:
					break;
				}
			}
			return strings;
		}


		@Override
		public boolean canEdit(Object obj, int columnIndex) {
			Collection<String> strings = null;
			IRteComponentItem item = getComponentItem(obj);
			int minValues = 0;
			if(!isEnabled(obj, columnIndex)) {
				return false;
			}
			if(item != null) {
				switch(columnIndex) {
				case COLSWCOMP:
					break;
				case COLSEL:
					CellControlType ct = getSelectionControlType(obj);
					return ct == CellControlType.CHECK || ct == CellControlType.INPLACE_SPIN
							|| ct == CellControlType.INPLACE_CHECK;
				case COLVARIANT:
					strings = item.getVariantStrings();
					minValues = 1;
					break;
				case COLVENDOR:
					strings = item.getVendorStrings();
					minValues = 1;
					break;
				case COLVERSION:
					strings = item.getVersionStrings();
					break;
				case COLDESCR:
				default:
					break;
				}
			}

			if (strings != null) {
				return strings.size() > minValues;
			}
			return false;
		}

		@Override
		public Image getImage(Object obj, int columnIndex) {
			IRteComponentItem item = getComponentItem(obj);
			if (item != null) {
				if (columnIndex == 0) {
					EEvaluationResult res = getModelController().getEvaluationResult(item);
					Image baseImage = null;
					if (item instanceof RteSelectedDeviceClass) {
						if (res.isFulfilled()) {
							baseImage = CpPlugInUI.getImage(CpPlugInUI.ICON_DEVICE);
						} else {
							baseImage = CpPlugInUI.getImage(CpPlugInUI.ICON_DEVICE_GREY);
						}
					} else if (item instanceof RteMoreClass) {
						baseImage = CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGES_FILTER);
					} else if (item instanceof IRteComponentClass) {
						baseImage = CpPlugInUI.getImage(CpPlugInUI.ICON_RTE);
					} else if (item instanceof IRteComponentGroup) {
						baseImage = CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT_GROUP);
					} else if (item instanceof IRteComponent) {
						IRteComponent c = (IRteComponent) item;
						ICpComponentInfo ci = c.getActiveCpComponentInfo();
						if (ci != null && ci.getComponent() == null) {
							if (c.getMaxInstanceCount() > 1) {
								baseImage = CpPlugInUI.getImage(CpPlugInUI.ICON_MULTICOMPONENT_ERROR);
							} else {
								baseImage = CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT_ERROR);
							}

						} else {
							if (c.getMaxInstanceCount() > 1) {
								baseImage = CpPlugInUI.getImage(CpPlugInUI.ICON_MULTICOMPONENT);
							} else {
								baseImage = CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT);
							}
						}
					}
					return baseImage;
				}
				switch (columnIndex) {
				case COLSEL:
				case COLSWCOMP:
				case COLVARIANT:
					break;
				case COLVENDOR:
					break;
				case COLVERSION: 
				{
					String version = item.getActiveVersion(); 
					if (version != null && !version.isEmpty() && !item.isUseLatestVersion() && !item.hasBundle()) {
						return CpPlugInUI.getImage(CpPlugInUI.ICON_PIN);
					}
				}
				break;
				case COLDESCR:
				default:
					break;
				}
			}
			return null;
		}
		
		@Override
		public Image getSuffixButtonImage(Object obj, int columnIndex) {
			if(columnIndex == COLSEL) {
				if(isSuffixButtonEnabled(obj, columnIndex))
					return CpPlugInUI.getImage(CpPlugInUI.ICON_RUN);
				return CpPlugInUI.getImage(CpPlugInUI.ICON_RUN_GREY);
			}
			return null;
		}

		@Override
		public String getTooltipText(Object obj, int columnIndex) {
			IRteComponentItem item = getComponentItem(obj);
			if(item == null) {
				return null;
			}

			switch(columnIndex) {
			case COLSWCOMP: {
				String tooltip = item.getDescription();
				ICpItem cpItem = item.getActiveCpItem();
				if(cpItem != null) {
					tooltip += '\n' + CpStringsUI.RteDeviceSelectorWidget_lblPack_text + ' ' + cpItem.getPackId();
				}
				return tooltip;
			}
			case COLVERSION:
				String ver = item.getActiveVersion();
				if(ver == null || ver.isEmpty()) {
					return null;
				}
				String tt;
				if(item.isUseLatestVersion()) {
					tt = CpStringsUI.RteComponentTreeWidget_UseLatestVersion;
				} else {
					tt = CpStringsUI.RteComponentTreeWidget_StickToFixedVersion;
				}

				tt += ": ";  //$NON-NLS-1$

				return tt + ver;
			case COLDESCR:
				String url = item.getUrl();
				if(url != null && !url.isEmpty()) {
					return url;
				}
				break;
			case COLSEL:
			case COLVARIANT:
			case COLVENDOR:
			default:
				break;
			}
			return null;
		}

		@Override
		public String getUrl(Object obj, int columnIndex) {
			if(columnIndex == COLDESCR) {
				IRteComponentItem item = getComponentItem(obj);
				if(item != null) {
					return item.getUrl();
				}
			}
			return null;
		}

		@Override
		public void setCheck(Object element, int columnIndex, boolean newVal) {
			if (columnIndex != COLSEL) {
				return;
			}
			if (getSelectionControlType(element) == CellControlType.CHECK
					|| getSelectionControlType(element) == CellControlType.INPLACE_CHECK) {
				IRteComponentItem item = getComponentItem(element);
				getModelController().selectComponent((IRteComponent)item, newVal ? 1 : 0);
			}
		}

		/**
		 * @param obj
		 * @return a string array containing indexes starting from 0 to max instance count of the RTE component
		 */
		private String[] getSelectionStrings(Object obj) {
			String[] strings = null;
			if (getSelectionControlType(obj) == CellControlType.INPLACE_SPIN) {
				int count = ((IRteComponent)obj).getMaxInstanceCount();
				strings = new String[count+1];
				for (int i = 0; i <= count; ++i) {
					strings[i] = Integer.toString(i);
				}
			}
			return strings;
		}



		@Override
		public void setString(Object obj, int columnIndex, String newVal) {
			IRteComponentItem item = getComponentItem(obj);

			if(item == null || getModelController() == null || newVal == null) {
				return;
			}
			switch(columnIndex) {
			case COLVARIANT:
				getModelController().selectActiveVariant(item, newVal);
				break;
			case COLVENDOR:
				getModelController().selectActiveVendor(item, newVal);
				break;
			case COLVERSION:
				getModelController().selectActiveVersion(item, newVal);
				break;
			default:
				return;
			}
			fTreeViewer.update(item,  null);
		}


		@Override
		public void setCurrentSelectedIndex(Object obj, int columnIndex, long newVal) {
			IRteComponentItem item = getComponentItem(obj);
			if(item == null || getModelController() == null) {
				return;
			}
			if(columnIndex == COLSEL &&  getSelectionControlType(item) == CellControlType.INPLACE_SPIN){
				getModelController().selectComponent((IRteComponent)item, (int) newVal);
			}

		}

		@Override
		public Color getBgColor(Object obj, int columnIndex) {
			if(columnIndex != COLSEL) {
				return null;
			}
			IRteComponentItem item = getComponentItem(obj);
			if(item != null) {
				EEvaluationResult res = getModelController().getEvaluationResult(item);
				switch(res){
				case UNDEFINED:
					break;
				case CONFLICT:
				case ERROR:
				case FAILED:
				case INCOMPATIBLE:
				case INCOMPATIBLE_API:
				case INCOMPATIBLE_BUNDLE:
				case INCOMPATIBLE_VARIANT:
				case INCOMPATIBLE_VENDOR:
				case INCOMPATIBLE_VERSION:
				case MISSING:
				case MISSING_GPDSC:
				case MISSING_API:
				case MISSING_BUNDLE:
				case MISSING_VARIANT:
				case MISSING_VENDOR:
				case MISSING_VERSION:
				case UNAVAILABLE:
				case UNAVAILABLE_PACK:
					return ColorConstants.RED;
				case IGNORED:
					if(!item.isSelected()) {
						break;
					}
				case FULFILLED:
					return ColorConstants.GREEN;

				case INACTIVE:
				case INSTALLED:
				case SELECTABLE:
					return ColorConstants.YELLOW;
				default:
					break;
				}
			}
			return null;
		}

		@Override
		public String getDefaultString(Object obj, int columnIndex) {
			if(columnIndex != COLVERSION) {
				return null;
			}
			IRteComponentItem item = getComponentItem(obj);
			if(item == null) {
				return null;
			}
			return item.getDefaultVersion();
		}

		@Override
		public boolean isDefault(Object obj, int columnIndex) {
			if(columnIndex != COLVERSION) {
				return false;
			}
			IRteComponentItem item = getComponentItem(obj);
			if(item == null) {
				return false;
			}
			return item.isUseLatestVersion();
		}

		@Override
		protected void executeSuffixButtonAction(Object element, int colIndex, Point pt) {
			IRteComponentItem item = getComponentItem(element);
			if (item == null) {
				return;
			}
			launchGenerator(getGenerator(item), null);
		}

	} /// end of RteColumnAdvisor

	/**
	 * Content provider for RTEComponentTreeWidget
	 */
	public class RteComponentContentProvider extends TreeObjectContentProvider {
		@Override
		public Object[] getElements(Object inputElement) {
			if(inputElement == getModelController()) {
				return getModelController().getComponents().getChildArray();
			}
			return getChildren(inputElement);
		}

		@Override
		public Object getParent(Object child) {
			IRteComponentItem item = getComponentItem(child);
			if (item != null) {
				return item.getEffectiveParent();
			}
			return null;
		}

		@Override
		public Object [] getChildren(Object parent) {
			IRteComponentItem item = getComponentItem(parent);
			if (item != null) {
				return item.getEffectiveChildArray();
			}
			return ITreeObject.EMPTY_OBJECT_ARRAY;
		}

		@Override
		public boolean hasChildren(Object parent) {
			IRteComponentItem item = getComponentItem(parent);
			if (item != null) {
				return item.hasEffectiveChildren();
			}
			return false;
		}
	}

	/**
	 * Set current configuration for this component tree widget
	 * @param configuration A RTE configuration that contains RTE component
	 */
	@Override
	public void setModelController(IRteModelController model) {
		super.setModelController(model);
		if (fTreeViewer != null) {
			fTreeViewer.setInput(model);
		}
	}

	@Override
	public Composite createControl(Composite parent) {

		Tree tree = new Tree(parent, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL|SWT.BORDER);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		fTreeViewer = new TreeViewer(tree);
		ColumnViewerToolTipSupport.enableFor(fTreeViewer);
		fColumnAdvisor = new RteComponentColumnAdvisor(fTreeViewer);

		// Tree item name
		TreeViewerColumn column0 = new TreeViewerColumn(fTreeViewer, SWT.LEFT);
		column0.getColumn().setText(CpStringsUI.RteComponentTreeWidget_SoftwareComponents);
		column0.getColumn().setWidth(180);
		column0.setEditingSupport(new AdvisedEditingSupport(fTreeViewer, fColumnAdvisor, 0));
		AdvisedCellLabelProvider col0LabelProvider = new AdvisedCellLabelProvider(fColumnAdvisor, 0);
		// workaround jface bug: first owner-draw column is not correctly painted when column is resized
		col0LabelProvider.setOwnerDrawEnabled(false);
		column0.setLabelProvider(col0LabelProvider);

		// Check box for selection
		TreeViewerColumn column1 = new TreeViewerColumn(fTreeViewer, SWT.LEFT);
		column1.getColumn().setText(CpStringsUI.RteComponentTreeWidget_Sel);
		column1.getColumn().setWidth(35);
		column1.setEditingSupport(new AdvisedEditingSupport(fTreeViewer, fColumnAdvisor, 1));
		column1.setLabelProvider(new AdvisedCellLabelProvider(fColumnAdvisor, 1));

		// Variant
		TreeViewerColumn column2 = new TreeViewerColumn(fTreeViewer, SWT.LEFT);
		column2.getColumn().setText(CpStringsUI.RteComponentTreeWidget_Variant);
		column2.getColumn().setWidth(110);
		column2.setEditingSupport(new AdvisedEditingSupport(fTreeViewer, fColumnAdvisor, 2));
		column2.setLabelProvider(new AdvisedCellLabelProvider(fColumnAdvisor, 2));

		// Vendor
		TreeViewerColumn column3 = new TreeViewerColumn(fTreeViewer, SWT.LEFT);
		column3.getColumn().setText(CpStringsUI.RteComponentTreeWidget_Vendor);
		column3.getColumn().setWidth(110);
		column3.setEditingSupport(new AdvisedEditingSupport(fTreeViewer, fColumnAdvisor, 3));
		column3.setLabelProvider(new AdvisedCellLabelProvider(fColumnAdvisor, 3));

		// Version
		TreeViewerColumn column4 = new TreeViewerColumn(fTreeViewer, SWT.LEFT);
		column4.getColumn().setText(CpStringsUI.RteComponentTreeWidget_Version);
		column4.getColumn().setWidth(70);
		column4.setEditingSupport(new AdvisedEditingSupport(fTreeViewer, fColumnAdvisor, 4));
		column4.setLabelProvider(new AdvisedCellLabelProvider(fColumnAdvisor, 4));

		// Description/URL
		TreeViewerColumn column5 = new TreeViewerColumn(fTreeViewer, SWT.LEFT);
		column5.getColumn().setText(CpStringsUI.RteComponentTreeWidget_Description);
		column5.getColumn().setWidth(400);
		column5.setEditingSupport(new AdvisedEditingSupport(fTreeViewer, fColumnAdvisor, 5));
		column5.setLabelProvider(new AdvisedCellLabelProvider(fColumnAdvisor, 5));

		RteComponentContentProvider rteContentProvider = new RteComponentContentProvider();
		fTreeViewer.setContentProvider(rteContentProvider);
		fTreeViewer.addSelectionChangedListener(event -> handleTreeSelectionChanged(event));

		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 2;
		tree.setLayoutData(gridData);

		if (getModelController() != null) {
			fTreeViewer.setInput(getModelController());
		}
		hookContextMenu();
		return tree;
	}

	protected IRteComponentItem getSelectedItem() {
		if (fTreeViewer != null) {
			IStructuredSelection sel = (IStructuredSelection)fTreeViewer.getSelection();
			if(sel != null) {
				return getComponentItem(sel.getFirstElement());
			}
		}
		return null;
	}

	ICpGenerator getSelectedGenerator() {
		return getGenerator(getSelectedItem());
	}

	ICpGenerator getGenerator(IRteComponentItem item) {
		if (item == null) {
			return null;
		}
		ICpComponent cp = item.getActiveCpComponent();
		if (cp == null) {
			return null;
		}
		return cp.getGenerator();
	}

	
	/**
	 * @param event
	 */
	protected void handleTreeSelectionChanged(SelectionChangedEvent event) {
		if(getModelController() == null) {
			return;
		}
		IRteComponentItem c = getSelectedItem();
		if(c != null) {
			selItemKeyPath = c.getKeyPath();
		}
	}


	/**
	 * Refresh completely the tree viewer.
	 */
	@Override
	public void update() {
		refresh();

		if(fTreeViewer == null || getModelController() == null) {
			return;
		}
		if( selItemKeyPath == null || selItemKeyPath.isEmpty()) {
			return;
		}

		IRteComponentItem item = getModelController().getComponents().findChild(selItemKeyPath, false);
		if(item != null) {
			showComponentItem(item);
		}
	}

	@Override
	public void handle(RteEvent event) {
		if(event.getTopic().equals(RteEvent.COMPONENT_SHOW)) {
			showComponentItem((IRteComponentItem)event.getData());
			return;
		} else if(event.getTopic().equals(RteEvent.COMPONENT_SELECTION_MODIFIED)) {
			refresh();
		} else {
			super.handle(event);
		}
	}


	/**
	 * Highlights given item expanding parent nodes if needed
	 * @param item Component item to select
	 */
	public void showComponentItem(IRteComponentItem item) {
		if(fTreeViewer == null) {
			return;
		}
		if(item == null) {
			return;
		}

		IRteComponent c  = item.getParentComponent();
		if(c != null) {
			// if supplied item has parent component, highlight it
			item = c;
		}
		IRteComponentBundle b = item.getParentBundle();
		if(b != null && !b.isActive()) {
			// if bundle is not active, highlight bundle's parent (component class)
			item = b.getParent();
		}

		if(item == null) {
			return;
		}

		if(item == getSelectedItem()) {
			return;
		}

		Object[] path = item.getEffectiveHierachyPath();
		if(path.length == 0) {
			return;
		}
		TreePath tp = new TreePath(path);
		TreeSelection ts = new TreeSelection(tp);

		fTreeViewer.setSelection(ts, true);

	}


	private void addGeneratorActions(IMenuManager manager) {
		ICpGenerator gen = getSelectedGenerator();
		if(gen == null)
			return;
		Collection<String> types = gen.getAvailableTypes();
		if(types == null || types.isEmpty())
			return;
		String genName = gen.getId(); 
		int n = 0;
		for(String type : types) {
			Action a = laGeneratorActions.get(type);
			if(a == null) 
				continue;
			String text = CpStringsUI.Launch + ' ' + genName + ' ' + '(' + type + ')';
			a.setText(text);
			manager.add(a);
			n++;
		}
	
		if(n > 0)
			manager.add(new Separator());
	}
	
	@Override
	protected void fillContextMenu(IMenuManager manager) {
		addGeneratorActions(manager);
		manager.add(expandAllSelected);
		manager.add(expandAll);
		manager.add(collapseAll);
	}
	
	@Override
	public boolean isExpandAllSelectedSupported() {
		return true; 
	}

	
	@Override
	protected void expandAllSelected() {
		if(fTreeViewer == null) {
			return;
		}
		IRteModel model = getModelController();
		if (model != null) {
			fTreeViewer.getTree().setRedraw(false);
			ISelection prevSel = fTreeViewer.getSelection();
			Collection<IRteComponent> selectedComponents = model.getSelectedComponents();
			for (IRteComponent comp: selectedComponents) {
				Object[] path = comp.getEffectiveHierachyPath();
				TreePath tp = new TreePath(path);
				TreeSelection ts = new TreeSelection(tp);
				fTreeViewer.setSelection(ts, false);
			}
			fTreeViewer.setSelection(prevSel, true);
			fTreeViewer.getTree().setRedraw(true);
		}
	}

	@Override
	protected void makeActions() {
		super.makeActions();
		
		for(String type : CmsisConstants.LAUNCH_TYPES) {
			LaunchGeneratorAction lga = new LaunchGeneratorAction(type);
			laGeneratorActions.put(type, lga); 
		}
	}

}
