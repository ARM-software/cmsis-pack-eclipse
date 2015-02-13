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

package com.arm.cmsis.pack.tree;

import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

/**
 * This class defines interfaces for a column label provider used in a TreeViewerColumn
 */
public interface IColumnAdvisor {
	
	/**
	 * possible types of cells in a table
	 */
	enum CellControlType { NONE, CHECK, COMBO, URL, TEXT }
	
	/**
	 * Return the control type of a cell
	 * @param obj cell object
	 * @param columnIndex column index of the cell
	 * @return Return the control type of a cell 
	 */
	CellControlType getCellControlType(Object obj, int columnIndex);
	
	/**
	 * Return true if the object is from type CHECK and is checked
	 * @param obj cell object
	 * @param columnIndex column index of the cell
	 * @return Return true if the object is from type CHECK and is checked 
	 */
	boolean getCheck(Object obj, int columnIndex);
	
	/**
	 * Return string representing the object.
	 * Return null if the object is of type CHECK, return current selected string if the object is of type COMBO 
	 * @param obj cell object
	 * @param columnIndex column index of the cell
	 * @return Return string representing the object 
	 */
	String getString(Object obj, int columnIndex);
	
	/**
	 * Return current select index of the object of type COMBO
	 * @param obj cell object
	 * @param columnIndex column index of the cell
	 * @return current select index of the object of type COMBO 
	 */
	int getCurrentSelectedIndex(Object element, int columnIndex);
	
	/**
	 * Return an array of string
	 * @param obj cell object
	 * @param columnIndex column index of the cell
	 * @return Return an array of string 
	 */
	String[] getStringArray(Object obj, int columnIndex);
	
	/**
	 * Return true of the object can be modified
	 * @param obj cell object
	 * @param columnIndex column index of the cell
	 * @return Return true of the object can be modified 
	 */
	boolean canEdit(Object obj, int columnIndex);
	
	/**
	 * Returns the image of the object
	 * @param obj cell object
	 * @param columnIndex column index of the cell
	 * @return Return the image of the object 
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
	 * @return Return the tool tip text of the object 
	 */
	String getTooltipText(Object obj, int columnIndex);
	
	/**
	 * Set the state of the object to checked
	 * @param obj cell object
	 * @param columnIndex column index of the cell
	 * @return Set the state of the object to checked 
	 */
	void setCheck(Object element, int columnIndex, boolean newVal);
	
	/**
	 * Called when a property is changed
	 * @param columnIndex column index of the cell
	 * @param event	object describing the event
	 * event.source the object whose property has been changed
	 * event.property	one of "CheckBox" or "ComboSelectionChanged"
	 * event.oldValue	old value true/false in case of check box. Old selected item index in case of combo 
	 * event.newValue	new value true/false in case of check box. New selected item index in case of combo 
	 */
	void propertyChange(PropertyChangeEvent event, int columnIndex);
}
