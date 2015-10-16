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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.arm.cmsis.pack.common.CmsisConstants;
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
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.CpStringsUI;
import com.arm.cmsis.pack.ui.tree.AdvisedCellLabelProvider;
import com.arm.cmsis.pack.ui.tree.AdvisedEditingSupport;
import com.arm.cmsis.pack.ui.tree.ColumnAdvisor;
import com.arm.cmsis.pack.ui.tree.IColumnAdvisor;
import com.arm.cmsis.pack.ui.tree.OverlayImage;
import com.arm.cmsis.pack.ui.tree.OverlayImage.OverlayPos;
import com.arm.cmsis.pack.ui.tree.TreeObjectContentProvider;
import com.arm.cmsis.pack.utils.Utils;


/**
 * This class displays the component tree for selection. 
 *
 */
public class RteComponentSelectorWidget extends RteWidget {

	// constants for column number
	static final int COLSWCOMP 	= 0;
	static final int COLSEL 	= 1;
	static final int COLVARIANT = 2;
	static final int COLVENDOR 	= 3;
	static final int COLVERSION = 4;
	static final int COLDESCR 	= 5;
	
	private Action expandAll;
	private Action collapseAll;
	private Action expandAllSelected;
	
	TreeViewer viewer = null;					// the Tree Viewer
	
	static final Color GREEN = new Color(Display.getCurrent(), CpPlugInUI.GREEN);
	static final Color YELLOW = new Color(Display.getCurrent(),CpPlugInUI.YELLOW);

	/**
	 *	Return the effective component
	 */
	public IRteComponentItem getComponentItem(Object obj){
		if(obj instanceof IRteComponentItem) {
			return ((IRteComponentItem)obj).getEffectiveItem();
		}
		return null;
	}
	
	/**  
	 * Column label provider for RteComponentTreeWidget
	 */
	public class RteComponentColumnAdvisor extends ColumnAdvisor {
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
						return CellControlType.CHECK;
					} else if (count > 1) {
						return CellControlType.SPIN;
					}
				}
			}
			return CellControlType.NONE;
		}
		
		@Override
		public CellControlType getCellControlType(Object obj, int columnIndex) {
			IRteComponentItem item = getComponentItem(obj);
			int minItems = 0;
			if(item == null)
				return CellControlType.NONE;
			Collection<String> strings = null;
			switch (columnIndex) {
				case COLSWCOMP: break;
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
			if (getSelectionControlType(obj) == CellControlType.CHECK) {
				IRteComponentItem item = getComponentItem(obj);
				if(item instanceof IRteComponent) {
					boolean check = item.isSelected();
					return check;
				}
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
					if (getSelectionControlType(obj) == CellControlType.SPIN) {
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
		public int getCurrentSelectedIndex(Object element, int columnIndex) {
			int index = -1;
			IRteComponentItem item = getComponentItem(element);
			
			if(item != null) {
				switch(columnIndex) {
				case COLSEL:
					if (getSelectionControlType(element) == CellControlType.SPIN) {
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
		public int getMaxCount(Object obj, int columnIndex) {
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
					if (getSelectionControlType(obj) == CellControlType.SPIN) {
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
			if(item != null) {
				
				switch(columnIndex) {
				case COLSWCOMP:
					break;
				case COLSEL:
					CellControlType ct = getSelectionControlType(obj);
					return ct == CellControlType.CHECK || ct == CellControlType.SPIN;
				case COLVARIANT:
					strings = item.getVariantStrings();
					minValues = 1;
					break;
				case COLVENDOR:
					strings = item.getVendorStrings();
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
					if(item instanceof RteSelectedDeviceClass) {
						if(res.isFulfilled())
							baseImage = CpPlugInUI.getImage(CpPlugInUI.ICON_DEVICE);
						else
							baseImage = CpPlugInUI.getImage(CpPlugInUI.ICON_DEPRDEVICE);
					}else if(item instanceof RteMoreClass) {
						baseImage = CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGES_FILTER);
					} else if (item instanceof IRteComponentClass) {
						baseImage = CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT_CLASS);
					}else if (item instanceof IRteComponentGroup) {
						baseImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
					}else if (item instanceof IRteComponent) {
						IRteComponent c = (IRteComponent)item;
						ICpComponentInfo ci = c.getActiveCpComponentInfo();
						if(ci != null && ci.getComponent() == null) {
							if(c.getMaxInstanceCount() > 1)
								baseImage = CpPlugInUI.getImage(CpPlugInUI.ICON_MULTICOMPONENT_ERROR);
							else
								baseImage = CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT_ERROR);
	
						} else {
							if(c.getMaxInstanceCount() > 1)
								baseImage = CpPlugInUI.getImage(CpPlugInUI.ICON_MULTICOMPONENT);
							else
								baseImage = CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT);
						}
					}
					return baseImage;
				} else {
					switch (columnIndex) {
					case COLSWCOMP:
					case COLSEL:
					case COLVARIANT:
						break;
					case COLVENDOR:
						break;
					case COLVERSION:
						if(item.getActiveVersion() != null && !item.getActiveVersion().isEmpty() && !item.isUseLatestVersion()) {
							return CpPlugInUI.getImage(CpPlugInUI.ICON_PIN);
						}
						break;
					case COLDESCR:
					default:
						break;
					}
				}
			}
			return null;
		}

		@Override
		public String getTooltipText(Object obj, int columnIndex) {
			IRteComponentItem item = getComponentItem(obj);
			if(item == null)
				return null;
			
			switch(columnIndex) {
			case COLSWCOMP:
				return item.getDescription();
			case COLVERSION:
				String ver = item.getActiveVersion();
				if(ver == null || ver.isEmpty())
					return null;
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
				if(url != null && !url.isEmpty())
					return url;
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
				if(item != null)
					return item.getUrl();
			}
			return null;
		}

		@Override
		public void setCheck(Object element, int columnIndex, boolean newVal) {
			if (getSelectionControlType(element) == CellControlType.CHECK) {
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
			if (getSelectionControlType(obj) == CellControlType.SPIN) {
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
			
			if(item == null || getModelController() == null || newVal == null) 
				return;
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
			viewer.update(item,  null);
		}


		@Override
		public void setCurrentSelectedIndex(Object obj, int columnIndex, int newVal) {
			IRteComponentItem item = getComponentItem(obj);
			if(item == null || getModelController() == null) 
				return;
			if(columnIndex == COLSEL &&  getSelectionControlType(item) == CellControlType.SPIN){
				getModelController().selectComponent((IRteComponent)item, newVal);
			}
			
		}
		
		@Override
		public Color getBgColor(Object obj, int columnIndex) {
			if(columnIndex != COLSEL)
				return null;
			IRteComponentItem item = getComponentItem(obj);
			if(item != null) {
				Device device = Display.getCurrent(); 
				EEvaluationResult res = getModelController().getEvaluationResult(item);			
				switch(res){
				case IGNORED:
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
				case MISSING_API:
				case MISSING_BUNDLE:
				case MISSING_VARIANT:
				case MISSING_VENDOR:
				case MISSING_VERSION:
				case UNAVAILABLE:
				case UNAVAILABLE_PACK:
					return device.getSystemColor(SWT.COLOR_RED); 
				case FULFILLED:
					return GREEN;

				case INACTIVE:
				case INSTALLED:
				case SELECTABLE:
					return YELLOW;
				default:
					break;
				}
			}
			return null;
		}

		@Override
		public String getDefaultString(Object obj, int columnIndex) {
			if(columnIndex != COLVERSION)
				return null;
			IRteComponentItem item = getComponentItem(obj);
			if(item == null)
				return null;
			return item.getDefaultVersion();
		}

		@Override
		public boolean isDefault(Object obj, int columnIndex) {
			if(columnIndex != COLVERSION)
				return false;
			IRteComponentItem item = getComponentItem(obj);
			if(item == null)
				return false;
			return item.isUseLatestVersion();
		}

		
		
	} /// end of ColumnAdviser
	
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
			if (item != null) 
				return item.getEffectiveChildArray();
			return ITreeObject.EMPTY_OBJECT_ARRAY;
		}
		
		@Override
		public boolean hasChildren(Object parent) {
			IRteComponentItem item = getComponentItem(parent);
			if (item != null) 
				return item.hasEffectiveChildren();
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
		if (viewer != null) {
			viewer.setInput(model);
		}
	}
	
    @Override
    public Composite createControl(Composite parent) {
    	
		Tree tree = new Tree(parent, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL|SWT.BORDER);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		viewer = new TreeViewer(tree);
		ColumnViewerToolTipSupport.enableFor(viewer);
		IColumnAdvisor columnAdvisor = new RteComponentColumnAdvisor(viewer);

		// Tree item name
		TreeViewerColumn column0 = new TreeViewerColumn(viewer, SWT.LEFT);
		column0.getColumn().setAlignment(SWT.LEFT);
		column0.getColumn().setText(CpStringsUI.RteComponentTreeWidget_SoftwareComponents);
		column0.getColumn().setWidth(180);
		column0.setEditingSupport(new AdvisedEditingSupport(viewer, columnAdvisor, 0));
		AdvisedCellLabelProvider col0LabelProvider = new AdvisedCellLabelProvider(columnAdvisor, 0);
		// workaround jface bug: first owner-draw column is not correctly painted when column is resized
		col0LabelProvider.setOwnerDrawEnabled(false);   
		column0.setLabelProvider(col0LabelProvider);
		
		// Check box for selection
		TreeViewerColumn column1 = new TreeViewerColumn(viewer, SWT.CENTER);
		column1.getColumn().setAlignment(SWT.CENTER);
		column1.getColumn().setText(CpStringsUI.RteComponentTreeWidget_Sel);
		column1.getColumn().setWidth(35);
		column1.setEditingSupport(new AdvisedEditingSupport(viewer, columnAdvisor, 1));
		column1.setLabelProvider(new AdvisedCellLabelProvider(columnAdvisor, 1));

		// Variant
		TreeViewerColumn column2 = new TreeViewerColumn(viewer, SWT.RIGHT);
		column2.getColumn().setAlignment(SWT.LEFT);
		column2.getColumn().setText(CpStringsUI.RteComponentTreeWidget_Variant);
		column2.getColumn().setWidth(110);
		column2.setEditingSupport(new AdvisedEditingSupport(viewer, columnAdvisor, 2));
		column2.setLabelProvider(new AdvisedCellLabelProvider(columnAdvisor, 2));

		// Vendor
		TreeViewerColumn column3 = new TreeViewerColumn(viewer, SWT.RIGHT);
		column3.getColumn().setAlignment(SWT.LEFT);
		column3.getColumn().setText(CpStringsUI.RteComponentTreeWidget_Vendor);
		column3.getColumn().setWidth(110);
		column3.setEditingSupport(new AdvisedEditingSupport(viewer, columnAdvisor, 3));
		column3.setLabelProvider(new AdvisedCellLabelProvider(columnAdvisor, 3));
		
		// Version
		TreeViewerColumn column4 = new TreeViewerColumn(viewer, SWT.RIGHT);
		column4.getColumn().setAlignment(SWT.LEFT);
		column4.getColumn().setText(CpStringsUI.RteComponentTreeWidget_Version);
		column4.getColumn().setWidth(70);
		column4.setEditingSupport(new AdvisedEditingSupport(viewer, columnAdvisor, 4));
		column4.setLabelProvider(new AdvisedCellLabelProvider(columnAdvisor, 4));

		// Description/URL
		TreeViewerColumn column5 = new TreeViewerColumn(viewer, SWT.RIGHT);
		column5.getColumn().setAlignment(SWT.LEFT);
		column5.getColumn().setText(CpStringsUI.RteComponentTreeWidget_Description);
		column5.getColumn().setWidth(400);
		column5.setEditingSupport(new AdvisedEditingSupport(viewer, columnAdvisor, 5));
		column5.setLabelProvider(new AdvisedCellLabelProvider(columnAdvisor, 5));

		RteComponentContentProvider rteContentProvider = new RteComponentContentProvider();
		viewer.setContentProvider(rteContentProvider);
    
    	GridData gridData = new GridData();
    	gridData.horizontalAlignment = SWT.FILL;
    	gridData.verticalAlignment = SWT.FILL;
    	gridData.grabExcessHorizontalSpace = true;
    	gridData.grabExcessVerticalSpace = true;
    	gridData.horizontalSpan = 2;
    	tree.setLayoutData(gridData);

		if (getModelController() != null) {
			viewer.setInput(getModelController());
		}
		hookContextMenu();
		return tree;
    }

	@Override
	public void refresh() {
		if (viewer != null) {
			viewer.refresh();
		}
	}

	/**
	 * Refresh completely the tree viewer. 
	 */
	@Override
	public void update() {
		refresh();
	}

	@Override
	public void handle(RteEvent event) {
		if(event.getTopic().equals(RteEvent.COMPONENT_SHOW)) {
			showComponentItem((IRteComponentItem)event.getData());
			return;
		} else if(event.getTopic().equals(RteEvent.COMPONENT_SELECTION_MODIFIED) ||
				  event.getTopic().equals(RteEvent.CONFIGURATION_COMMITED) || 
				   event.getTopic().equals(RteEvent.CONFIGURATION_MODIFIED)) {
			update();
		}
	}
	
	
	/**
	 * Highlights given item expanding parent nodes if needed 
	 * @param item Component item to select
	 */
	public void showComponentItem(IRteComponentItem item) {
		if(viewer == null)
			return;
		if(item == null)
			return;
		
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
		
		if(item == null)
			return;
		Object[] path = item.getEffectiveHierachyPath();
		TreePath tp = new TreePath(path);
		TreeSelection ts = new TreeSelection(tp);
		
		viewer.setSelection(ts, true);
		
	}
	
	/**
	 * Return the tree viewer embedded in this widget
	 * @return
	 */
	public TreeViewer getViewer() {
		return viewer;
	}

	private void hookContextMenu() {
		
		final MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		makeActions();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(menuMgr);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
	}

	void fillContextMenu(IMenuManager manager) {
		manager.add(expandAll);
		manager.add(collapseAll);
		manager.add(expandAllSelected);
	}

	private void makeActions() {
		expandAll = new Action() {
			public void run() {
				if(viewer == null)
					return;
				viewer.expandAll();
			}
		};

		expandAll.setText(CpStringsUI.ExpandAll);
		expandAll.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_EXPAND_ALL));
		
		collapseAll = new Action() {
			public void run() {
				if(viewer == null)
					return;
				viewer.collapseAll();
			}
		};
		collapseAll.setText(CpStringsUI.CollapseAll);
		collapseAll.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
					getImageDescriptor(ISharedImages.IMG_ELCL_COLLAPSEALL));
		
		expandAllSelected = new Action() {
			public void run() {
				if(viewer == null)
					return;
				IRteModel model = getModelController();
				if (model != null) {
					viewer.getTree().setRedraw(false);
					ISelection prevSel = viewer.getSelection();
					Collection<IRteComponent> selectedComponents = model.getSelectedComponents();
					for (IRteComponent comp: selectedComponents) {
						Object[] path = comp.getEffectiveHierachyPath();
						TreePath tp = new TreePath(path);
						TreeSelection ts = new TreeSelection(tp);
						viewer.setSelection(ts, false);
					}
					viewer.setSelection(prevSel, true);
					viewer.getTree().setRedraw(true);
				}
			}
		};
		expandAllSelected.setText(CpStringsUI.RteManagerWidget_ExpandAllSelected);
		
		OverlayImage overlayImage = new OverlayImage(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_EXPAND_ALL).createImage(), 
				CpPlugInUI.getImageDescriptor(CpPlugInUI.CHECKEDOUT_OVR).createImage(), OverlayPos.TOP_RIGHT);
		expandAllSelected.setImageDescriptor(overlayImage);
	}

	@Override
	public Composite getFocusWidget() {
		return viewer.getTree();
	}
	
}
