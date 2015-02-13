/*******************************************************************************
* Copyright (c) 2014 ARM Ltd.
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
package com.arm.cmsis.pack.widgets;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.Collection;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.events.IRteConfigurationProxy;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.info.ICpComponentInfo;
import com.arm.cmsis.pack.rte.components.IRteComponent;
import com.arm.cmsis.pack.rte.components.IRteComponentBundle;
import com.arm.cmsis.pack.rte.components.IRteComponentClass;
import com.arm.cmsis.pack.rte.components.IRteComponentGroup;
import com.arm.cmsis.pack.rte.components.IRteComponentItem;
import com.arm.cmsis.pack.tree.AdvisedCellLabelProvider;
import com.arm.cmsis.pack.tree.AdvisedEditingSupport;
import com.arm.cmsis.pack.tree.IColumnAdvisor;
import com.arm.cmsis.pack.ui.CpPlugInUI;


/**
 * This class displays the component tree for selection. 
 *
 */
public class RteComponentTreeWidget extends RteWidget {

	TreeViewer viewer = null;					// the Tree Viewer
	private static final String SOFTWARECOMPONENT = "Software Components";
	
	private static final Color GREEN = new Color(Display.getCurrent(), 189,249,181);
	private static final Color YELLOW = new Color(Display.getCurrent(), 252,200, 46);

	/**
	 *	Return the effective component
	 */
	public IRteComponentItem getComponentItem(Object obj){
		if(obj instanceof IRteComponentItem) {
			return ((IRteComponentItem)obj).getEffectiveItem();
		}
		return null;
	}
	
	
	/** Column label provider for RteComponentTreeWidget 
	 *
	 */
	public class ColumnAdvisor implements IColumnAdvisor {

		// constants for column number
		private static final int COLSWCOMP 	= 0;
		private static final int COLSEL 	= 1;
		private static final int COLVARIANT = 2;
		private static final int COLVENDOR 	= 3;
		private static final int COLVERSION = 4;
		private static final int COLDESCR 	= 5;
		
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
						return CellControlType.COMBO;
					}
				}
			}
			return CellControlType.NONE;
		}
		
		@Override
		public CellControlType getCellControlType(Object obj, int columnIndex) {
			IRteComponentItem item = getComponentItem(obj);
			if(item == null)
				return CellControlType.NONE;
			Collection<String> strings = null;
			switch (columnIndex) {
				case COLSWCOMP: break;
				case COLSEL:
					return getSelectionControlType(obj);
				case COLVARIANT: 
					strings = item.getVariantStrings(); 	break;
				case COLVENDOR:  
					strings = item.getVendorStrings();		break;
				case COLVERSION: 
					strings = item.getVersionStrings();	break;
				case COLDESCR:
					String url = item.getUrl();
					if(url != null && ! url.isEmpty()) {
						return CellControlType.URL;
					}
					break;
				default: break;
			}
			
			if (strings != null && strings.size() > 0) {
				return CellControlType.COMBO;
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
					if (getSelectionControlType(obj) == CellControlType.COMBO) {
						return Integer.toString(((IRteComponent)item).getSelectedCount());
					}
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
			return IAttributes.EMPTY_STRING;
		}

		@Override
		public int getCurrentSelectedIndex(Object element, int columnIndex) {
			int index = -1;
			IRteComponentItem item = getComponentItem(element);
			
			if(item != null) {
				switch(columnIndex) {
				case COLSEL:
					if (getSelectionControlType(element) == CellControlType.COMBO) {
						index = ((IRteComponent)item).getSelectedCount();
					}
					break;
				case COLVARIANT:
					index = lookForIndex(item.getVariantStrings(), item.getActiveVariant()); 
					break;
				case COLVENDOR:
					index = lookForIndex(item.getVendorStrings(), item.getActiveVendor());
					break;
				case COLVERSION:
					index = lookForIndex(item.getVersionStrings(), item.getActiveVersion());
					break;
				default:
					break;
				}
			}
			
			return index;
		}

		/**
		 * Look for the combo index based on the selected string 
		 * @param stringCollection
		 * @param str
		 * @return Combo box index if found, otherwise -1 
		 */
		private int lookForIndex(Collection<String> stringCollection, String str) {
			if (str != null && stringCollection != null) {
				int i = 0;
				for(String name : stringCollection) {
					if (str.equals(name)) {
						return i;
					}
					i++;
				}
			}
			return -1;
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
					if (getSelectionControlType(obj) == CellControlType.COMBO) {
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
			if(item != null) {
				
				switch(columnIndex) {
				case COLSWCOMP:
					break;
				case COLSEL:
					CellControlType ct = getSelectionControlType(obj);
					return ct == CellControlType.CHECK || ct == CellControlType.COMBO;
				case COLVARIANT:
					strings = item.getVariantStrings();
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
				return strings.size() > 1;
			}
			return false;
		}

		@Override
		public Image getImage(Object obj, int columnIndex) {
			IRteComponentItem item = getComponentItem(obj);
			if (item != null && columnIndex == 0) {
				if (item instanceof IRteComponentClass) {
					return CpPlugInUI.getImage(CpPlugInUI.ICON_GROUP);
				}
				else if (item instanceof IRteComponentGroup) {
					return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
				}
				else if (item instanceof IRteComponent) {
					IRteComponent c = (IRteComponent)item;
					ICpComponentInfo ci = c.getActiveCpComponentInfo();
					if(ci != null && ci.getComponent() == null) {
						if(c.getMaxInstanceCount() > 1)
							return CpPlugInUI.getImage(CpPlugInUI.ICON_MULTICOMPONENT_ERROR);
						else
							return CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT_ERROR);

					} else {
						if(c.getMaxInstanceCount() > 1)
							return CpPlugInUI.getImage(CpPlugInUI.ICON_MULTICOMPONENT);
						else
							return CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT);
					}
				}
			}
			return null;
		}

		@Override
		public String getTooltipText(Object obj, int columnIndex) {
			return null;
		}

		@Override
		public void setCheck(Object element, int columnIndex, boolean newVal) {
			if (getSelectionControlType(element) == CellControlType.CHECK) {
				IRteComponentItem item = getComponentItem(element);
				((IRteComponent)item).setSelected(newVal ? 1 : 0);
			}
		}

		/**
		 * @param obj
		 * @return a string array containing indexes starting from 0 to max instance count of the RTE component
		 */
		private String[] getSelectionStrings(Object obj) {
			String[] strings = null;
			if (getSelectionControlType(obj) == CellControlType.COMBO) {
				int count = ((IRteComponent)obj).getMaxInstanceCount();
				strings = new String[count+1];
				for (int i = 0; i <= count; ++i) {
					strings[i] = Integer.toString(i);
				}
			}
			return strings;
		}
		
		/**
		 * handle selection changes in combo box 
		 */
		private void handleComboSelectionChange(PropertyChangeEvent event, int columnIndex) {
			
			IRteComponentItem item = getComponentItem(event.getSource());
			
			if(item == null || getConfiguration() == null) 
				return;
			
			Integer tmp = (Integer)event.getNewValue();
			int selIndex = (tmp == null) ? 0 : tmp.intValue();
			Collection<String> strings = null;
			
			switch(columnIndex) {
			case COLSEL:
				if (getSelectionControlType(item) == CellControlType.COMBO) {
					getConfiguration().selectComponent((IRteComponent)item, selIndex);
				}
				return;
			case COLVARIANT:
				strings = item.getVariantStrings();
				break;
			case COLVENDOR:
				strings = item.getVendorStrings();
				break;
			case COLVERSION:
				strings = item.getVersionStrings();
				break;
			case COLDESCR:
			default:
				return;
			}

			// currently item is valid only in case Cvariant has been changed
			if (strings != null) {
				String[] stringArr = strings.toArray(new String[0]);
				String name = stringArr[selIndex];
				switch(columnIndex) {
				case COLVARIANT:
					getConfiguration().selectActiveVariant(item, name);
					break;
				case COLVENDOR:
					getConfiguration().selectActiveVendor(item, name);
					break;
				case COLVERSION:
					getConfiguration().selectActiveVersion(item, name);
					break;
				default:
					return;
				}
				viewer.update(item,  null);
			}
		}

		@Override
		public void propertyChange(PropertyChangeEvent event, int columnIndex) {
			IRteComponentItem item = getComponentItem( event.getSource());
			if(item == null)
				return;
			
			switch(event.getProperty()) {
				case "ComboSelectionChanged":
					handleComboSelectionChange(event, columnIndex);
				break;
				case "CheckBox":
					if (getConfiguration() != null) {
						// select or unselect an component.
						boolean selected = ((Boolean)event.getNewValue()).booleanValue();
						setCheck(item, columnIndex, selected);
						viewer.update(item,  null);
						getConfiguration().selectComponent((IRteComponent)item, selected ? 1 : 0);
					}
				break;
				case "click":
					if (getCellControlType(item, columnIndex) == CellControlType.URL) {
					    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
						if (desktop != null) {
							String url = item.getUrl();
							if (url == null || url.isEmpty()) {
								return;
							}
							
							String tmp = url.toLowerCase();
							boolean isUrl = tmp.startsWith("http:") || tmp.startsWith("www.") || tmp.startsWith("https:");
							try {
								if (isUrl && desktop.isSupported(Desktop.Action.BROWSE)) {
						        	URI uri = new URI(url);
						            desktop.browse(uri);
								} else {
									File file = new File(url);
									desktop.open(file);
								}
							} catch (Exception e) {
								MessageDialog.openError(viewer.getControl().getShell(), SOFTWARECOMPONENT, "Cannot Open '" + url + "'");
					            e.printStackTrace();
							}
					    }
					}
					break;
			}
		}

		@Override
		public Color getBgColor(Object obj, int columnIndex) {
			if(columnIndex != COLSEL)
				return null;
			IRteComponentItem item = getComponentItem(obj);
			if(item != null) {
				Device device = Display.getCurrent(); 
				EEvaluationResult res = getConfiguration().getEvaluationResult(item);			
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
		
	} /// end of ColumnAdviser
	
	/**	Content provider for RTEComponentTreeWidget
	 *
	 */
	public class RteContentProvider implements ITreeContentProvider {
		IRteComponentItem components = null;
		
		@Override
		public Object[] getElements(Object parent) {
			return getChildren(parent);
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
			return EMPTY_OBJECT_ARRAY;
		}
		
		@Override
		public boolean hasChildren(Object parent) {
			IRteComponentItem item = getComponentItem(parent);
			if (item != null) 
				return item.hasEffectiveChildren();
			return false;
		}
		
		@Override
		public void dispose() {
			components = null;
		}

		@Override
		public void inputChanged(Viewer viewerRef, Object oldInput, Object newInput) {
			if(newInput != null && newInput instanceof IRteComponentItem)
				components = (IRteComponentItem) newInput; 
			else
				components = null;
		}
	}
	
	/**
	 * Set current configuration for this component tree widget
	 * @param configuration A RTE configuration that contains RTE component
	 */
	@Override
	public void setConfiguration(IRteConfigurationProxy configuration) {
		super.setConfiguration(configuration);
		if (viewer != null) {
			viewer.setInput(configuration.getComponents());
			//refresh();
		}
	}
	
    /**
     * Create a widget containing the RTE component
     * @param parent a parent composite containing this RTE component tree widget
     */
    public void createControl(Composite parent) {
    	
		Tree tree = new Tree(parent, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL|SWT.BORDER);
		tree.setHeaderVisible(true);
		viewer = new TreeViewer(tree);
		ColumnViewerToolTipSupport.enableFor(viewer);

		// Tree item name
		TreeViewerColumn column0 = new TreeViewerColumn(viewer, SWT.LEFT);
		tree.setLinesVisible(true);
		column0.getColumn().setAlignment(SWT.LEFT);
		column0.getColumn().setText(SOFTWARECOMPONENT);
		column0.getColumn().setWidth(180);
		IColumnAdvisor columnAdvisor = new ColumnAdvisor();
		column0.setEditingSupport(new AdvisedEditingSupport(viewer, columnAdvisor, 0));
		AdvisedCellLabelProvider col0LabelProvider = new AdvisedCellLabelProvider(columnAdvisor, 0);
		// workaround jface bug: first owner-draw column is not correctly painted when column is resized
		col0LabelProvider.setOwnerDrawEnabled(false);   
		column0.setLabelProvider(col0LabelProvider);
		
		// Check box for selection
		TreeViewerColumn column1 = new TreeViewerColumn(viewer, SWT.CENTER);
		tree.setLinesVisible(true);
		column1.getColumn().setAlignment(SWT.CENTER);
		column1.getColumn().setText("Sel.");
		column1.getColumn().setWidth(35);
		column1.setEditingSupport(new AdvisedEditingSupport(viewer, columnAdvisor, 1));
		column1.setLabelProvider(new AdvisedCellLabelProvider(columnAdvisor, 1));

		// Variant
		TreeViewerColumn column2 = new TreeViewerColumn(viewer, SWT.RIGHT);
		column2.getColumn().setAlignment(SWT.LEFT);
		column2.getColumn().setText("Variant");
		column2.getColumn().setWidth(110);
		column2.setEditingSupport(new AdvisedEditingSupport(viewer, columnAdvisor, 2));
		column2.setLabelProvider(new AdvisedCellLabelProvider(columnAdvisor, 2));

		// Vendor
		TreeViewerColumn column3 = new TreeViewerColumn(viewer, SWT.RIGHT);
		column3.getColumn().setAlignment(SWT.LEFT);
		column3.getColumn().setText("Vendor");
		column3.getColumn().setWidth(110);
		column3.setEditingSupport(new AdvisedEditingSupport(viewer, columnAdvisor, 3));
		column3.setLabelProvider(new AdvisedCellLabelProvider(columnAdvisor, 3));
		
		// Version
		TreeViewerColumn column4 = new TreeViewerColumn(viewer, SWT.RIGHT);
		column4.getColumn().setAlignment(SWT.LEFT);
		column4.getColumn().setText("Version");
		column4.getColumn().setWidth(70);
		column4.setEditingSupport(new AdvisedEditingSupport(viewer, columnAdvisor, 4));
		column4.setLabelProvider(new AdvisedCellLabelProvider(columnAdvisor, 4));

		// Description/URL
		TreeViewerColumn column5 = new TreeViewerColumn(viewer, SWT.RIGHT);
		column5.getColumn().setAlignment(SWT.LEFT);
		column5.getColumn().setText("Description");
		column5.getColumn().setWidth(400);
		column5.setEditingSupport(new AdvisedEditingSupport(viewer, columnAdvisor, 5));
		column5.setLabelProvider(new AdvisedCellLabelProvider(columnAdvisor, 5));

		RteContentProvider rteContentProvider = new RteContentProvider();
		viewer.setContentProvider(rteContentProvider);
    
    	GridData gridData = new GridData();
    	gridData.horizontalAlignment = SWT.FILL;
    	gridData.verticalAlignment = SWT.FILL;
    	gridData.grabExcessHorizontalSpace = true;
    	gridData.grabExcessVerticalSpace = true;
    	gridData.horizontalSpan = 2;
    	tree.setLayoutData(gridData);

		if (getConfiguration() != null) {
			viewer.setInput(getConfiguration().getComponents());
		}
    }

	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		if (viewer != null) {
			viewer.refresh();
		}
	}

	/**
	 * Refresh completely the tree viewer. 
	 */
	@Override
	public void update() {
		viewer.refresh();
	}

	@Override
	public void handleRteEvent(RteEvent event) {
		if(event.getTopic().equals(RteEvent.COMPONENT_SHOW)) {
			showComponentItem((IRteComponentItem)event.getData());
			return;
		} else if(event.getTopic().equals(RteEvent.CONFIGURATION_MODIFIED)) {		
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
	
}
