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

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.generic.ITreeObject;
import com.arm.cmsis.pack.info.ICpComponentInfo;
import com.arm.cmsis.pack.rte.IRteModelController;
import com.arm.cmsis.pack.rte.components.IRteComponent;
import com.arm.cmsis.pack.rte.components.IRteComponentItem;
import com.arm.cmsis.pack.rte.dependencies.IRteDependencyItem;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.CpStringsUI;
import com.arm.cmsis.pack.ui.tree.AdvisedCellLabelProvider;
import com.arm.cmsis.pack.ui.tree.AdvisedEditingSupport;
import com.arm.cmsis.pack.ui.tree.TreeObjectContentProvider;

public class RteValidateWidget extends RteWidget {
	TreeViewer fViewer = null;
	
	
	/** Column label provider for RteComponentTreeWidget 
	 *
	 */
	IRteDependencyItem getDependencyItem(Object element){
		if(element instanceof IRteDependencyItem) {
			return (IRteDependencyItem)element;
		}
		return null;
	}
	
	/**
	 * Set current configuration for this component tree widget
	 * @param configuration A RTE configuration that contains RTE component
	 */
	@Override
	public void setModelController(IRteModelController model) {
		super.setModelController(model);
		if (fViewer != null) {
			fViewer.setInput(model);
			refresh();
		}
	}
	
	public class RteValidateColumnAdvisor extends RteColumnAdvisor {
		/**
		 * Constructs advisor for a viewer
		 * @param columnViewer ColumnViewer on which the advisor is installed
		 */
		public RteValidateColumnAdvisor(ColumnViewer columnViewer) {
			super(columnViewer);
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
			return CmsisConstants.EMPTY_STRING;
		}


		@Override
		public Image getImage(Object obj, int columnIndex) {
			if(columnIndex == 0) {
				IRteDependencyItem item = getDependencyItem(obj);
				if(item != null) {
					if(item.isMaster()) {
						EEvaluationResult res = item.getEvaluationResult();
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
							return CpPlugInUI.getImage(CpPlugInUI.ICON_ERROR); 

						case INACTIVE:
						case INSTALLED:
						case SELECTABLE:
							return CpPlugInUI.getImage(CpPlugInUI.ICON_WARNING);
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
								if(instances > 1) {
									return CpPlugInUI.getImage(CpPlugInUI.ICON_MULTICOMPONENT_ERROR);
								}
								return CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT_ERROR);
							}
							if(instances > 1) {
								return CpPlugInUI.getImage(CpPlugInUI.ICON_MULTICOMPONENT);
							}
							return CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT);							
						}
					} 
				}
			}
			return null;
		}
	}

	/**
	 * 	Content provider for RteValidateWidget tree 
	 */
	public class RteValidateContentProvider extends TreeObjectContentProvider {
		@Override
		public Object[] getElements(Object inputElement) {
			if(inputElement == getModelController()) {
				Collection<? extends IRteDependencyItem> depItems = getModelController().getDependencyItems();
				if(depItems != null) {
					return depItems.toArray();
				}
				return ITreeObject.EMPTY_OBJECT_ARRAY;
			} 
			return getChildren(inputElement);
		}
	}
	
	@Override
    public Composite createControl(Composite parent) {
    	
		Tree tree = new Tree(parent, SWT.FULL_SELECTION | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL|SWT.BORDER);
		tree.setHeaderVisible(true);
		fViewer = new TreeViewer(tree);
		ColumnViewerToolTipSupport.enableFor(fViewer);
		fColumnAdvisor = new RteValidateColumnAdvisor(fViewer);
		
		TreeViewerColumn column0 = new TreeViewerColumn(fViewer, SWT.LEFT);
		tree.setLinesVisible(true);
		column0.getColumn().setText(CpStringsUI.RteValidateWidget_ValidationOutput);
		column0.getColumn().setWidth(400);
		column0.setEditingSupport(new AdvisedEditingSupport(fViewer, fColumnAdvisor, 0));
		
		fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				handleTreeSelectionChanged(event);
			}
		});
		
		AdvisedCellLabelProvider col0LabelProvider = new AdvisedCellLabelProvider(fColumnAdvisor, 0);
		// workaround jface bug: first owner-draw column is not correctly painted when column is resized
		col0LabelProvider.setOwnerDrawEnabled(false);   
		column0.setLabelProvider(col0LabelProvider);
		
		TreeViewerColumn column1 = new TreeViewerColumn(fViewer, SWT.LEFT);
		column1.getColumn().setText(CpStringsUI.RteValidateWidget_Description);
		column1.getColumn().setWidth(500);
		column1.setEditingSupport(new AdvisedEditingSupport(fViewer, fColumnAdvisor, 1));
		column1.setLabelProvider(new AdvisedCellLabelProvider(fColumnAdvisor, 1));

		RteValidateContentProvider validateProvider = new RteValidateContentProvider();
		fViewer.setContentProvider(validateProvider);
    
    	GridData gridData = new GridData();
    	gridData.horizontalAlignment = SWT.FILL;
    	gridData.verticalAlignment = SWT.FILL;
    	gridData.grabExcessHorizontalSpace = true;
    	gridData.grabExcessVerticalSpace = true;
    	tree.setLayoutData(gridData);
    	
    	if (getModelController() != null) {
			fViewer.setInput(getModelController());
		}
    	return tree;
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
		if(getModelController() == null) {
			return;
		}
		
		IRteDependencyItem d = getSelectedDependencyItem();
		if(d == null) {
			return;
		}
		
		IRteComponentItem item = d.getComponentItem();
		if(item != null) {
			getModelController().emitRteEvent(RteEvent.COMPONENT_SHOW, item);
		}
	}

	@Override
	public void handle(RteEvent event) {
		switch(event.getTopic()) {
		case RteEvent.COMPONENT_SELECTION_MODIFIED: 
			update();
			return;
		}
		super.handle(event);
	}
	
	@Override
	public void refresh() {
		if(fViewer != null) {
			fViewer.refresh();
		}
	}

	@Override
	public void update() {
		refresh();
		if(fViewer != null) {
			fViewer.expandAll();
		}
	}

	/**
	 *  Updates widget asynchronously, must run in GUI thread 
	 */
	protected void asyncUpdate() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				update();
			}
		});			
	}	
	
	@Override
	public Composite getFocusWidget() {
		return fViewer.getTree();
	}
}
