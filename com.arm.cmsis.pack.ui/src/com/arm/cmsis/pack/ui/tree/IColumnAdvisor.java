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

package com.arm.cmsis.pack.ui.tree;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;

/**
 * This class defines interfaces for a column label provider used in a TreeViewerColumn
 */
public interface IColumnAdvisor {
	
	/**
	 * possible types of cells in a table
	 */
	enum CellControlType { NONE, CHECK, COMBO, SPIN, MENU, URL, TEXT }
	
	/**
	 * Returns the viewer on which this advisor is installed on
	 * @return the viewer on which this advisor is installed installed on or <code>null</code>
	 */
	ColumnViewer getViewer();
	
	/**
	 * Return the control type of a cell
	 * @param obj cell object
	 * @param columnIndex column index of the cell
	 * @return the control type of a cell 
	 */
	CellControlType getCellControlType(Object obj, int columnIndex);
	
	/**
	 * Return true if the object is from type CHECK and is checked
	 * @param obj cell object
	 * @param columnIndex column index of the cell
	 * @return true if the object is from type CHECK and is checked 
	 */
	boolean getCheck(Object obj, int columnIndex);
	
	/**
	 * Changes object checked state 
	 * @param obj cell object
	 * @param columnIndex column index of the cell
	 * @param newVal new value
	 */
	void setCheck(Object obj, int columnIndex, boolean newVal);

	/**
	 * Returns string representing the object.<br>
	 * Return null if the object is of type CHECK, return current selected string if the object is of type COMBO 
	 * @param obj cell object
	 * @param columnIndex column index of the cell
	 * @return string representing the object 
	 */
	String getString(Object obj, int columnIndex);

	/**
	 * Sets new String value for object   
	 * @param obj cell object
	 * @param columnIndex column index of the cell
	 * @param newVal new String value
	 */
	void setString(Object obj, int columnIndex, String newVal);
	
	/**
	 * Returns URL associated with object for current column if the cell if any  
	 * @param obj cell object
	 * @param columnIndex column index of the cell
	 * @return URL string 
	 */
	String getUrl(Object obj, int columnIndex);

	
	/**
	 * Return default object string for given column if any 
	 * @param obj cell object
	 * @param columnIndex column index of the cell
	 * @return string representing the object 
	 */
	String getDefaultString(Object obj, int columnIndex);
	
	/**
	 * Checks if the object is in default state for given column
	 * @param obj cell object
	 * @param columnIndex column index of the cell
	 * @return true if the object can have a default state and is in the default state 
	 * @see #getDefaultString(Object, int) 
	 */
	boolean isDefault(Object obj, int columnIndex);
	
	/**
	 * Return current select index of the object of type COMBO or SPIN
	 * @param obj cell object
	 * @param columnIndex column index of the cell
	 * @return current select index of the object of type COMBO 
	 */
	int getCurrentSelectedIndex(Object obj, int columnIndex);
	
	/**
	 * Sets current selected index for COMBO or SPIN editor types 
	 * @param obj cell object
	 * @param columnIndex column index of the cell
	 * @param newVal current select index to set  
	 */
	void setCurrentSelectedIndex(Object obj, int columnIndex, int newVal);

	/**
	 * Return max count for SPIN editor
	 * @param obj cell object
	 * @param columnIndex column index of the cell
	 * @return current select index of the object of type COMBO 
	 */
	int getMaxCount(Object obj, int columnIndex);

	
	/**
	 * Return an array of string
	 * @param obj cell object
	 * @param columnIndex column index of the cell
	 * @return an array of string 
	 */
	String[] getStringArray(Object obj, int columnIndex);
	
	/**
	 * Return an instance of Menu which describes a popup menu
	 * @param obj cell object
	 * @param columnIndex column index of the cell
	 * @return an instance of Menu which describes a popup menu 
	 */
	Menu getMenu(Object obj, int columnIndex);
	
	/**
	 * Checks if object is enabled 
	 * @param obj cell object
	 * @param columnIndex column index of the cell
	 * @return true if the object is enabled, false otherwise 
	 */
	boolean isEnabled(Object obj, int columnIndex);
	
	/**
	 * Return true of the object can be modified
	 * @param obj cell object
	 * @param columnIndex column index of the cell
	 * @return true of the object can be modified 
	 */
	boolean canEdit(Object obj, int columnIndex);
	
	/**
	 * Returns the final image for the object
	 * @param obj cell object
	 * @param columnIndex column index of the cell
	 * @return the final image for the object 
	 */
	Image getImage(Object obj, int columnIndex);
	
	
	/**
	 * Returns cell background color
	 * @param obj cell object
	 * @param columnIndex column index of the cell
	 * @return background color or null to use default color 
	 */
	Color getBgColor(Object obj, int columnIndex);
	
	/**
	 * Returns the tool tip text of the object
	 * @param obj cell object
	 * @param columnIndex column index of the cell
	 * @return  tool tip text for the object if any 
	 */
	String getTooltipText(Object obj, int columnIndex);
	
	
}
