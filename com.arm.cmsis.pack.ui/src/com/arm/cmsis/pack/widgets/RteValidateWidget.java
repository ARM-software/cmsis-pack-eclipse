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

import java.util.Collection;

import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;

import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.events.IRteConfigurationProxy;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.generic.ITreeObject;
import com.arm.cmsis.pack.info.ICpComponentInfo;
import com.arm.cmsis.pack.rte.IRteDependency;
import com.arm.cmsis.pack.rte.IRteDependencyItem;
import com.arm.cmsis.pack.rte.components.IRteComponent;
import com.arm.cmsis.pack.rte.components.IRteComponentItem;
import com.arm.cmsis.pack.tree.AdvisedCellLabelProvider;
import com.arm.cmsis.pack.tree.AdvisedEditingSupport;
import com.arm.cmsis.pack.tree.IColumnAdvisor;
import com.arm.cmsis.pack.ui.CpPlugInUI;

public class RteValidateWidget extends RteWidget {
	TreeViewer fViewer = null;
	
	
	/** Column label provider for RteComponentTreeWidget 
	 *
	 */
	IRteDependencyItem getDependencyItem(Object element){
		if(element instanceof IRteDependencyItem)
			return (IRteDependencyItem)element;
		return null;
	}
	
	/**
	 * Set current configuration for this component tree widget
	 * @param configuration A RTE configuration that contains RTE component
	 */
	@Override
	public void setConfiguration(IRteConfigurationProxy configuration) {
		super.setConfiguration(configuration);
		if (fViewer != null) {
			fViewer.setInput(configuration);
			refresh();
		}
	}
	
	public class ColumnAdvisor implements IColumnAdvisor {

		
		@Override
		public CellControlType getCellControlType(Object obj, int columnIndex) {
			return CellControlType.TEXT;
		}

		@Override
		public boolean getCheck(Object obj, int columnIndex) {
			return false;
		}

		@Override
		public String getString(Object obj, int index) {
			IRteDependencyItem item = getDependencyItem(obj);
			if(item != null) {
				switch(index) {
				case 0 : return item.getName();
				case 1 : return item.getDescription();
				default:
					break;
				}
			}
			
			return IAttributes.EMPTY_STRING;
		}

		@Override
		public int getCurrentSelectedIndex(Object element, int columnIndex) {
			return -1;
		}

		@Override
		public String[] getStringArray(Object obj, int columnIndex) {
			return null;
		}

		@Override
		public boolean canEdit(Object obj, int columnIndex) {
			return false;
		}

		@Override
		public Image getImage(Object obj, int columnIndex) {
			if(columnIndex == 0) {
				IRteDependencyItem item = getDependencyItem(obj);
				if(item != null) {
					if(item instanceof IRteDependency || item.hasChildren()) {
						EEvaluationResult res = item.getEvaluationResult();
						if(res == EEvaluationResult.FULFILLED && item.isDeny())
							res = EEvaluationResult.INCOMPATIBLE;
						switch(res) {
						case IGNORED:
						case UNDEFINED:
						case FULFILLED:
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
							return CpPlugInUI.getImage(CpPlugInUI.ICON_RTEERROR); 

						case INACTIVE:
						case INSTALLED:
						case SELECTABLE:
							return CpPlugInUI.getImage(CpPlugInUI.ICON_RTEWARNING);
						default:
							break;
						}
						return null;
					} else if( item.getComponentItem() != null) {
						IRteComponent component = item.getComponentItem().getParentComponent();
						if(component != null) {
							ICpComponentInfo ci = component.getActiveCpComponentInfo();
							int instances = component.getMaxInstanceCount();
							if(ci != null && ci.getComponent() == null) {
								if(instances > 1)
									return CpPlugInUI.getImage(CpPlugInUI.ICON_MULTICOMPONENT_ERROR);
								else
									return CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT_ERROR);

							} else {
								if(instances > 1)
									return CpPlugInUI.getImage(CpPlugInUI.ICON_MULTICOMPONENT);
								else
									return CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT);
							}							
						}
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
		}

		@Override
		public void propertyChange(PropertyChangeEvent event, int columnIndex) {
		}

		@Override
		public Color getBgColor(Object obj, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}
	}

	/**	Content provider for RTEComponentTreeWidget
	 *
	 */
	public class ValidateProvider implements ITreeContentProvider {
		
		@Override
		public Object[] getElements(Object parent) {
			return getChildren(parent);
		}
		
		@Override
		public Object getParent(Object child) {
			return null;
		}
		
		@Override
		public Object [] getChildren(Object parent) {
			if(parent == getConfiguration()) {
				Collection<? extends IRteDependencyItem> depItems = getConfiguration().getDependencyItems();
				if(depItems != null)
					return depItems.toArray();
			} else {
				IRteDependencyItem item = getDependencyItem(parent);
				if(item != null)
					return item.getChildArray();
			}
			return ITreeObject.EMPTY_OBJECT_ARRAY;
		}
		
		@Override
		public boolean hasChildren(Object parent) {
			if(parent == getConfiguration()) {
				Collection<? extends IRteDependencyItem> depItems = getConfiguration().getDependencyItems();
				if(depItems != null)
					return !depItems.isEmpty();
			} else {
				IRteDependencyItem item = getDependencyItem(parent);
				if(item != null)
					return item.hasChildren();
			}
			return false;
		}
		
		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public Object getInput() {
			return null;
		}
		
	}
	
    public void createControl(Composite parent) {
    	
		Tree tree = new Tree(parent, SWT.FULL_SELECTION | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL|SWT.BORDER);
		tree.setHeaderVisible(true);
		fViewer = new TreeViewer(tree);
		ColumnViewerToolTipSupport.enableFor(fViewer);
		
		TreeViewerColumn column0 = new TreeViewerColumn(fViewer, SWT.LEFT);
		tree.setLinesVisible(true);
		column0.getColumn().setAlignment(SWT.LEFT);
		column0.getColumn().setText("Validation Output");
		column0.getColumn().setWidth(500);
		IColumnAdvisor columnAdvisor = new ColumnAdvisor();
		column0.setEditingSupport(new AdvisedEditingSupport(fViewer, columnAdvisor, 0));

		
		fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleTreeSelectionChanged(event);
			}
		});
		
		AdvisedCellLabelProvider col0LabelProvider = new AdvisedCellLabelProvider(columnAdvisor, 0);
		// workaround jface bug: first owner-draw column is not correctly painted when column is resized
		col0LabelProvider.setOwnerDrawEnabled(false);   
		column0.setLabelProvider(col0LabelProvider);
		
		TreeViewerColumn column1 = new TreeViewerColumn(fViewer, SWT.RIGHT);
		column1.getColumn().setAlignment(SWT.LEFT);
		column1.getColumn().setText("Description");
		column1.getColumn().setWidth(500);
		column1.setEditingSupport(new AdvisedEditingSupport(fViewer, columnAdvisor, 1));
		column1.setLabelProvider(new AdvisedCellLabelProvider(columnAdvisor, 1));

		ValidateProvider validateProvider = new ValidateProvider();
		fViewer.setContentProvider(validateProvider);
    
    	GridData gridData = new GridData();
    	gridData.horizontalAlignment = SWT.FILL;
    	gridData.verticalAlignment = SWT.FILL;
    	gridData.grabExcessHorizontalSpace = true;
    	gridData.grabExcessVerticalSpace = true;
    	tree.setLayoutData(gridData);
    	
    	if (getConfiguration() != null) {
			fViewer.setInput(getConfiguration());
		}
    	
    }

    private IRteDependencyItem getSelectedDependencyItem() {
		IStructuredSelection sel= (IStructuredSelection)fViewer.getSelection();
		if(sel.size() == 1) {
			Object o = sel.getFirstElement();
			if(o instanceof IRteDependencyItem ){
				return (IRteDependencyItem)o;
			}
		}
		return null;
	}
    
	/**
	 * @param event
	 */
	protected void handleTreeSelectionChanged(SelectionChangedEvent event) {
		if(fConfiguration == null)
			return;
		
		IRteDependencyItem d = getSelectedDependencyItem();
		if(d == null)
			return;
		
		IRteComponentItem item = d.getComponentItem();
		if(item != null) {
			fConfiguration.fireRteEvent(new RteEvent(RteEvent.COMPONENT_SHOW, item));
		}
	}

	@Override
	public void handleRteEvent(RteEvent event) {
		if(event.getTopic().equals(RteEvent.CONFIGURATION_MODIFIED)) {
			update();
		}
	}
	
	@Override
	public void refresh() {
		if(fViewer != null)
			fViewer.refresh();
	}

	@Override
	public void update() {
		refresh();
		if(fViewer != null)
			fViewer.expandAll();
		
	}
}
