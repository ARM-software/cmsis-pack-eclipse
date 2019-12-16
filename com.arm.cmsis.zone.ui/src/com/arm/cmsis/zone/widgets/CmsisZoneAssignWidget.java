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
package com.arm.cmsis.zone.widgets;

import org.eclipse.swt.graphics.Color;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.ui.ColorConstants;
import com.arm.cmsis.pack.ui.widgets.CmsisColumnInfo;
import com.arm.cmsis.pack.ui.widgets.CmsisColumnInfo.ColumnType;
import com.arm.cmsis.zone.data.ICpDeviceUnit;
import com.arm.cmsis.zone.data.ICpMemoryBlock;
import com.arm.cmsis.zone.data.ICpProcessorUnit;
import com.arm.cmsis.zone.data.ICpZone;
import com.arm.cmsis.zone.ui.Messages;


/**
 * Widget to edit zone assignments
 *
 */
public class CmsisZoneAssignWidget extends CmsisZoneTreeWidget {
	protected ICpZone fZone = null;
	private ICpProcessorUnit fProcessor = null;
	private ICpDeviceUnit fDevice = null;

	
	public CmsisZoneAssignWidget(ICpZone zone) {
		fZone = zone;
	}
	
	@Override
	public void destroy(){
		fZone = null;
		fProcessor = null;
		fDevice = null;
		super.destroy();
	}
	
	@Override
	public ICpProcessorUnit getTargetProcessor() {
		if(getZone() == null)
			return null;
		if(fProcessor == null)
			fProcessor = getZone().getTargetProcessor();
		return fProcessor;
	}
	@Override
	public ICpDeviceUnit getTargetDevice() {
		if(getZone() == null)
			return null;
		if(fDevice == null)
			fDevice = getZone().getTargetDevice();
		return fDevice;
	}

	
	@Override
	public ICpZone getZone() {
		return fZone;
	}

	@Override
	public void setZone(ICpZone zone) {
		if(fZone != zone) {
			fZone = zone;
			refresh();
		}
	}
	
	/**
	 * Column label provider for RteComponentTreeWidget
	 */
	public class CmsisZoneAssignmentColumnAdvisor extends CmsisZoneColumnAdvisor {
		/**
		 * Constructs advisor for a viewer
		 * @param columnViewer ColumnViewer on which the advisor is installed
		 */
		public CmsisZoneAssignmentColumnAdvisor(CmsisZoneTreeWidget treeWidget) {
			super(treeWidget);
		}

		
		@Override
		protected void createPrefixColumnInfos() {
			addColumnInfo(new CmsisColumnInfo(Messages.CmsisZoneAssignWidget_Name, ColumnType.COLNAME, 200, false));
			addColumnInfo(new CmsisColumnInfo(CmsisConstants.EMPTY_STRING, ColumnType.COLOP, 40, false));
			addColumnInfo(new CmsisColumnInfo(Messages.CmsisZoneAssignWidget_Permissions, ColumnType.COLACCESS, 60, false));
			addColumnInfo(new CmsisColumnInfo(Messages.CmsisZoneAssignWidget_Size, ColumnType.COLSIZE, 100, true));
		}

		
		@Override
		public Color getBgColor(Object obj, int columnIndex) {
			if(getColumnType(columnIndex) != ColumnType.COLOP)
				return null;
			ICpMemoryBlock block = getMemoryBlock(obj);
			if(block == null) 
				return null;
			
			if( block.isRemoved() || !block.isValid()) {
				return ColorConstants.PALE_RED;
			}
			
			int numAssigned = block.getAssignmentCount();
			if(numAssigned <= 0)
				return null;
			boolean assigned = block.isAssigned(fZone.getName());
			if(assigned) {
				if(numAssigned == 1)
					return ColorConstants.GREEN;
				return ColorConstants.YELLOW;
			}
			return ColorConstants.GRAY;
		}

		@Override
		public CellControlType getCellControlType(Object obj, int columnIndex) {
			ICpItem item = ICpItem.cast(obj);
			if(item == null) {
				return CellControlType.NONE;
			}
			
			ICpMemoryBlock block = getMemoryBlock(obj);
			if(block == null) {
				return CellControlType.TEXT;
			}

			ColumnType colType = getColumnType(columnIndex);
			if(colType == ColumnType.COLSTART && hasStrings(obj, columnIndex)) {
				return CellControlType.MENU;
			}
			if(colType == ColumnType.COLOP) {
				ICpMemoryBlock mappedBlock = getMemoryBlock(block, columnIndex);
				if(fZone != null && fZone.canAssign(mappedBlock)) {
					return CellControlType.INPLACE_CHECK;
				}
				return CellControlType.NONE;
			}
			return CellControlType.TEXT;
		}
		
		@Override
		public boolean getCheck(Object obj, int columnIndex) {
			if(getColumnType(columnIndex) != ColumnType.COLOP)
				return false;
			ICpMemoryBlock block = getMemoryBlock(obj);
			if(block == null || getMemoryBlock(block, columnIndex) == null) {
				return false;
			}
			ICpZone zone = getZone(columnIndex);
			if(zone != null && zone.canAssign(block)) {
				return block.isAssigned(zone.getName());
			}
			return false;
		}

		@Override
		public void setCheck(Object element, int columnIndex, boolean newVal) {
			if(getColumnType(columnIndex) != ColumnType.COLOP) {
				return;
			}
			ICpMemoryBlock block = getMemoryBlock(element);
			if(block == null || getMemoryBlock(block, columnIndex) == null) {
				return;
			}
			if(getSelectionCount() > 1)
				fKeyAdapter.assignBlocks(columnIndex);
			else
				getModelController().assignBlock(block, fZone.getName(), newVal);
		}
		
		@Override
		public boolean canEdit(Object obj, int columnIndex) {
			ColumnType colType = getColumnType(columnIndex);
			if(colType != ColumnType.COLOP) {
				return false;
			}
			ICpMemoryBlock block = getMemoryBlock(obj);
			if(block == null || getMemoryBlock(block, columnIndex) == null) {
				return false;
			}	
			ICpZone zone = getZone(columnIndex);
			
			return zone != null && zone.canAssign(block);
		}
		
		@Override
		public ICpZone getZone(int columnUndex) {
			return fZone;
			
		}
	} /// end of CmsisZoneAssignmentColumnAdvisor

	@Override
	protected CmsisZoneColumnAdvisor createColumnAdvisor() {
		return new CmsisZoneAssignmentColumnAdvisor(this);
	}
	

}
