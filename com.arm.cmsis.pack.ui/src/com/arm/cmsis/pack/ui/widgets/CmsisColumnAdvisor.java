/*******************************************************************************
 * Copyright (c) 2017 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/
package com.arm.cmsis.pack.ui.widgets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.events.IRteController;
import com.arm.cmsis.pack.item.ICmsisItem;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.tree.AdvisedCellLabelProvider;
import com.arm.cmsis.pack.ui.tree.AdvisedEditingSupport;
import com.arm.cmsis.pack.ui.tree.TreeColumnComparator;
import com.arm.cmsis.pack.ui.widgets.CmsisColumnInfo.ColumnType;


/**
 * Abstract column adviser with array of CmsisColumnInfo objects and column comparator 
 *
 */
public abstract class CmsisColumnAdvisor<TController extends IRteController> extends RteColumnAdvisor<TController> {


	protected TreeColumnComparator fComparator = null;
	protected ArrayList<CmsisColumnInfo> columnInfos = new ArrayList<>();

	/**
	 * Constructs advisor for a viewer
	 * @param treeViewer TreeViewer on which the advisor is installed
	 */
	public CmsisColumnAdvisor(TreeViewer treeViewer) {
		super(treeViewer);
	}

	public CmsisColumnAdvisor(ColumnViewer columnViewer, TController modelController) {
		super(columnViewer, modelController);
	}
	
	protected void createColumnInfos() {
		createPrefixColumnInfos();
		createDynamicColumnInfos();
		createSuffixColumnInfos();
	}

	/**
	 * Adds column infos before dynamic columns
	 */
	protected void createPrefixColumnInfos() {
		// default does nothing
	}

	/**
	 * Adds dynamic column infos
	 */
	protected void createDynamicColumnInfos(){
		// default does nothing
	}

	/**
	 * Adds column infos after dynamic columns
	 */
	protected void createSuffixColumnInfos() {
		// default does nothing
	}
	
	/**
	 * Returns column info array 
	 * @return ArrayList of CmsisColumnInfo objects
	 */
	public ArrayList<CmsisColumnInfo> getColumnInfos() {
		return columnInfos;
	}
	
	/**
	 * Returns CmsisColumnInfo for a column
	 * @param columnIndex column index
	 * @return CmsisColumnInfo or null if out of range
	 */
	public CmsisColumnInfo getColumnInfo(int columnIndex) {
		if(columnIndex >=0 && columnIndex < columnInfos.size())
			return columnInfos.get(columnIndex);
		return null;
	}

	/**
	 * Returns CmsisColumnInfo for a column containing supplied data item 
	 * @param item ICmsisItem
	 * @return CmsisColumnInfo or null if not found
	 */
	public CmsisColumnInfo getColumnInfo(ICmsisItem item) {
		if(item == null)
			return null;
		for(CmsisColumnInfo info : columnInfos) {
			if(info.getDataItem() == item) {
				return info;
			}
		}
		return null;
	}

	/**
	 * Returns CmsisColumnInfo for a column with given name 
	 * @param columnName column name 
	 * @return  CmsisColumnInfo or null if not found
	 */
	public CmsisColumnInfo getColumnInfo(String columnName) {
		for(CmsisColumnInfo info : columnInfos) {
			if(info.getName().equals(columnName)) {
				return info;
			}
		}
		return null;
	}

	/**
	 * Returns an index of a column with given name 
	 * @param name column name
	 * @return column index or -1 if not found
	 */
	public int getColumnIndex(String name) {
		for(int i = 0 ; i < columnInfos.size(); i++) {
			CmsisColumnInfo info  = columnInfos.get(i);
			if(info.getName().equals(name))
				return i;
		}
		return -1;
	}

	/**
	 * Returns an index of a column associated with supplied data item  
	 * @param item ICmsisItem
	 * @return column index or -1 if not found
	 */
	public int getColumnIndex(ICmsisItem item) {
		for(int i = 0 ; i < columnInfos.size(); i++) {
			CmsisColumnInfo info  = columnInfos.get(i);
			if(info.getDataItem() == item)
				return i;
		}
		return -1;
	}
	
	
	/**
	 * Adds column info to the end of columns
	 * @param info CmsisColumnInfo to add
	 */
	public void addColumnInfo(CmsisColumnInfo info) {
		columnInfos.add(info);
	}

	/**
	 * Adds column info to the specified index 
	 * @param info CmsisColumnInfo to add
	 * @param index index at which to add the info
	 */
	public void addColumnInfo(CmsisColumnInfo info, int index) {
		columnInfos.add(index, info);
	}

	
	/**
	 * Returns index of currently active sort column
	 * @return index of sort column or -1 if no sorting is active
	 */
	public int getSortColumnIndex(){
		if(fComparator == null)
			return -1;
		return fComparator.getColumnIndex();
	}
	
	
	/**
	 * Creates columns 
	 */
	public void createColumns(){
		createColumnInfos();
		int index = 0;
		Set<Integer> numericColumns = new HashSet<>(); 
		for(CmsisColumnInfo colInfo : getColumnInfos()) {
			createColumn(colInfo, index);
			if(colInfo.isNumeric()){
				numericColumns.add(index);
			}
			index++;
		}
		fComparator = new TreeColumnComparator(getTreeViewer(), this);
		fComparator.setNumericColumns(numericColumns);
		getViewer().setComparator(fComparator);
	}


	/**
	 * Adds a new column
	 * @param colInfo CmsisColumnInfo to add
	 * @param columIndex index at which to add column
	 */
	public void addColumn(CmsisColumnInfo colInfo, int columIndex) {
		addColumnInfo(colInfo, columIndex);
		createColumn(colInfo, columIndex);
		// refresh label providers for the columns to the right
		for(int i = columIndex + 1 ; i < columnInfos.size(); i++) {
			createColumnLabelProvider(columnInfos.get(i), i);
		}
		getViewer().refresh();
	}

	/**
	 * Removes column at specified index
	 * @param columIndex index to remove
	 */
	public void removeColumn(int columIndex) {
		if(columIndex < 0 || columIndex >= columnInfos.size())
			return;
		CmsisColumnInfo infoToRemove = getColumnInfo(columIndex);
		if(infoToRemove == null)
			return;
		columnInfos.remove(columIndex);
		infoToRemove.getColumn().getColumn().dispose();
		// refresh label providers for the columns to the right
		for(int i = columIndex ; i < columnInfos.size(); i++) {
			createColumnLabelProvider(columnInfos.get(i), i);
		}
		getViewer().refresh();
	}

	/**
	 * Creates a column
	 * @param colInfo CmsisColumnInfo
	 * @param columnIndex column index
	 * @return TreeViewerColumn
	 */
	protected TreeViewerColumn createColumn(CmsisColumnInfo colInfo, int columnIndex) {
		TreeViewer treeViewer =  (TreeViewer) getViewer();
		TreeViewerColumn column = new TreeViewerColumn(treeViewer, SWT.LEFT, columnIndex);
		colInfo.setColumn(column);
		column.getColumn().setText(colInfo.getName());
		column.getColumn().setWidth(colInfo.getWidth());
		createColumnLabelProvider(colInfo, columnIndex);
		return column;
	}

	/**
	 * Creates new AdvisedCellLabelProvider and AdvisedEditingSupport cobject for the column  
	 * @param colInfo CmsisColumnInfo
	 * @param columnIndex column index
	 */
	protected void createColumnLabelProvider(CmsisColumnInfo colInfo, int columnIndex) {
		TreeViewer treeViewer =  (TreeViewer) getViewer();
		colInfo.getColumn().setEditingSupport(new AdvisedEditingSupport(treeViewer, this, columnIndex));
		AdvisedCellLabelProvider colLabelProvider = new AdvisedCellLabelProvider(this, columnIndex);
		if (columnIndex == 0) {
			// workaround jface bug: first owner-draw column is not correctly painted when column is resized
			colLabelProvider.setOwnerDrawEnabled(false);
		}
		colInfo.getColumn().setLabelProvider(colLabelProvider);
	}
	
	/**
	 * Returns data item associated with the a column
	 * @param columnIndex column index 
	 * @return ICmsisItem object if associated, null otherwise
	 */
	public ICmsisItem getColumnDataItem(int columnIndex) {
		CmsisColumnInfo info = getColumnInfo(columnIndex);
		if(info != null) {
			return info.getDataItem();
		}
		return null;
	}
	
	/**
	 * Returns data item associated with the a column casted for the given type
	 * @param type class type 
	 * @param columnIndex column index 
	 * @return casted associated data item, null if column has no data item of item cannot be casted  
	 */
	public <C> C getColumnDataItemAs(Class<C> type, int columnIndex) {
		CmsisColumnInfo info = getColumnInfo(columnIndex);
		if(info != null) {
			return info.getDataItemAs(type);
		}
		return null;
	}

	/**
	 * Returns column type 
	 * @param columnIndex column index
	 * @return ColumnType
	 */
	public ColumnType getColumnType(int columnIndex){
		CmsisColumnInfo colInfo = getColumnInfo(columnIndex);
		if(colInfo != null)
			return colInfo.getType();
		return ColumnType.COLOTHER;
	}
	
	@Override
	protected boolean isUseFixedFont(Object obj, int columnIndex) {
		ColumnType colType = getColumnType(columnIndex);
		switch(colType) {
		case COLACCESS:
		case COLSTART:
		case COLSIZE:
		case COLOFFSET:
			return true;
		case COLINFO:
			break;
		case COLNAME:
			break;
		case COLOP:
			break;
		case COLOTHER:
			break;
		default:
			break;
		
		}
		return false;
	}

	@Override
	public Image getImage(Object obj, int columnIndex) {
		ICmsisItem item = ICmsisItem.cast(obj);
		if (item == null) 
			return null;

		if (columnIndex == 0) {
			return getItemImage(item);
		}
		return null;
	}

	/**
	 * Returns an image associated with the supplied item 
	 * @param item ICmsisItem
	 * @return Image or null 
	 */
	protected Image getItemImage(ICmsisItem item) {
		Image baseImage = null;
		String tag = item.getTag();
		switch(tag) {
		case CmsisConstants.PACKAGE_TAG:
			baseImage = CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGE);
			break;
		case CmsisConstants.DEVICE_TAG:
			baseImage = CpPlugInUI.getImage(CpPlugInUI.ICON_DEVICE);
			break;
		case CmsisConstants.PROCESSOR_TAG:
			baseImage = CpPlugInUI.getImage(CpPlugInUI.ICON_PROCESSOR);
			break;
		case CmsisConstants.RESOURCES:
		case CmsisConstants.PERIPHERALS:
			baseImage = CpPlugInUI.getImage(CpPlugInUI.ICON_PERIPHERALS);
			break;
		case CmsisConstants.GROUP:
			baseImage = CpPlugInUI.getImage(CpPlugInUI.ICON_MULTICOMPONENT);
			break;
		case CmsisConstants.MEMORY_TAG:
			baseImage = CpPlugInUI.getImage(CpPlugInUI.ICON_MEMORY);
			break;
		case CmsisConstants.BLOCK_TAG:
			baseImage = CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT);
			break;
		case CmsisConstants.PERIPHERAL:
			baseImage = CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT_GROUP);
			break;
		default:
			break;
		}
		return baseImage;
	}

	/**
	 * Returns next index to insert a column, default returns index after last column with data item.  
	 * @return index for the new column
	 */
	public int getInsertIndex() {
		if(columnInfos.isEmpty())
			return 0;
		// find last column with data item
		for(int i = columnInfos.size(); i > 0 ; i--) {
			CmsisColumnInfo info = columnInfos.get(i - 1); 
			if(info.getDataItem() != null) 
				return i;
		}
		return columnInfos.size();
	}
	
} /// end of CmsisColumnAdvisor
