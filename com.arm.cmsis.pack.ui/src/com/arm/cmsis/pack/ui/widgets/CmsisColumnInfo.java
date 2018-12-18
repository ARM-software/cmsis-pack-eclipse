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

import org.eclipse.jface.viewers.TreeViewerColumn;

import com.arm.cmsis.pack.item.ICmsisItem;

/**
 * Helper class for column information
 */
public class CmsisColumnInfo {

	// constants for column type
	public enum ColumnType {
		COLNAME, COLOP, COLVARIANT, COLVENDOR, COLVERSION, COLACCESS, COLSIZE, COLSTART, COLOFFSET, COLINFO, COLOTHER;
	};

	private TreeViewerColumn fColumn; // column associated with info 
	private String fName; //column name
	private ColumnType fType; // column type 
	private int fWidth; // column width
	private boolean fbNumeric; // flag indicating that column contains numeric data 
	private ICmsisItem fDataItem; // associated data item

	/**
	 * Constructor with name and no associated data  
	 * @param name column name
	 * @param type CoulmnType
	 * @param width initial column width
	 * @param bNumeric flag telling that the column will contain numeric data
	 */
	public CmsisColumnInfo(String name, ColumnType type, int width, boolean bNumeric) {
		fName = name;
		fWidth = width;
		fType = type;
		fbNumeric = bNumeric;
		fColumn = null;
		fDataItem = null;
	}

	/**
	 * Constructor associating column with data item
	 * @param item column data item (object extending ICmsisItem interface) 
	 * @param type CoulmnType
	 * @param width initial column width
	 * @param bNumeric flag telling that the column will contain numeric data
	 */
	public CmsisColumnInfo(ICmsisItem item, ColumnType type, int width, boolean bNumeric) {
		this(item.getName(), type, width, bNumeric);
		fDataItem = item;
	}

	/**
	 * Returns data item associated with column
	 * @return ICmsisItem or null column has no associated item
	 */
	public ICmsisItem getDataItem() {
		return fDataItem;
	}

	/**
	 * Returns data item associated with column casted to supplied type 
	 * @param type class type
	 * @return object of supplied type if can be casted, null otherwise
	 */
	public <C> C getDataItemAs(Class<C> type) {
		if(type.isInstance(fDataItem)) {
			return type.cast(fDataItem);
		}
		return null;
	}
	
	/**
	 * Associates column with a data item   
	 * @param item object extending ICmsisItem interface
	 */
	public void setDataItem(ICmsisItem item) {
		fDataItem = item;
	}
	
	/**
	 * Returns column name
	 * @return column name
	 */
	public String getName() {
		return fName;
	}

	/**
	 * Sets column name
	 * @param name column name  
	 */
	public void setName(String name) {
		if(fName.equals(name))
			return;
		fName = name;
		if(fColumn != null) {
			fColumn.getColumn().setText(name);
			fColumn.getViewer().refresh();
		}
	}
	
	/**
	 * Returns initial column width
	 * @return column width
	 */
	public int getWidth() {
		return fWidth;
	}

	/**
	 * Returns column Type 
	 * @return ColumnType
	 */
	public ColumnType getType() {
		return fType;
	}

	/**
	 * Checks if column represents numeric data 
	 * @return
	 */
	public boolean isNumeric() {
		return fbNumeric;
	}

	/**
	 * Returns TreeViewerColumn associated with column 
	 * @return TreeViewerColumn
	 */
	public TreeViewerColumn getColumn() {
		return fColumn;
	}

	/**
	 * Sets associated column to the info
	 * @param column TreeViewerColumn
	 */
	public void setColumn(TreeViewerColumn column) {
		fColumn = column;
	}

};
