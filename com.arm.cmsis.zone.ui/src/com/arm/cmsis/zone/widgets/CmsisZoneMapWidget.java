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

import java.util.Collection;

import org.eclipse.swt.graphics.Color;

import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.ui.ColorConstants;
import com.arm.cmsis.pack.ui.widgets.CmsisColumnInfo;
import com.arm.cmsis.pack.ui.widgets.CmsisColumnInfo.ColumnType;
import com.arm.cmsis.zone.data.ICpDeviceUnit;
import com.arm.cmsis.zone.data.ICpMemoryBlock;
import com.arm.cmsis.zone.data.ICpProcessorUnit;
import com.arm.cmsis.zone.data.ICpRootZone;
import com.arm.cmsis.zone.data.ICpZone;
import com.arm.cmsis.zone.ui.Messages;
import com.arm.cmsis.zone.ui.editors.CmsisZoneController;


/**
 * CMSIS-Zone map widget.
 *
 */
public class CmsisZoneMapWidget extends CmsisZoneTreeWidget {

	/**
	 * Column label provider for CmsisZoneMapWidget
	 */
	public class CmsisZoneMapColumnAdvisor extends CmsisZoneColumnAdvisor {
		/**
		 * Constructs advisor for a viewer
		 * @param columnViewer ColumnViewer on which the advisor is installed
		 */
		public CmsisZoneMapColumnAdvisor(CmsisZoneTreeWidget treeWidget) {
			super(treeWidget);
		}

		@Override
		protected void createDynamicColumnInfos(){
			addColumnInfo(new CmsisColumnInfo(Messages.CmsisZoneMapWidget_Start, ColumnType.COLSTART, 80, true));
			addColumnInfo(new CmsisColumnInfo(Messages.CmsisZoneMapWidget_End, ColumnType.COLEND, 80, true));
			ICpRootZone system = getModelController().getRootZone();
			Collection<ICpZone> allZones = system.getZones();
			if(allZones == null || allZones.isEmpty())
				return;
			for(ICpZone zone : allZones) {
				addColumnInfo(new CmsisColumnInfo(zone, ColumnType.COLOP, 80, false));
			}
		}


		@Override
		public boolean isPhysicalAddress() {
			return fbPhysicalAddress || isShowList();
		}

		@Override
		public Color getBgColor(Object obj, int columnIndex) {
			ColumnType colType = getColumnType(columnIndex);

			if(colType != ColumnType.COLSTART  && colType != ColumnType.COLSIZE && colType != ColumnType.COLOP && colType != ColumnType.COLEND)
				return null;

			if(obj instanceof ICpDeviceUnit) {
				ICpZone zone = getZone(columnIndex);
				if(zone != null) {
					if(zone.hasWarning())
						return ColorConstants.YELLOW;
					if(zone.hasSevereErrors())
						return ColorConstants.RED;
				}
				return null;
			}

			ICpMemoryBlock block = getMemoryBlock(obj);
			if(block == null)
				return null;
			if( block.isRemoved() || !block.isValid()) {
				return ColorConstants.PALE_RED;
			}

			if(colType != ColumnType.COLOP )
				return null;

			int numAssigned = block.getAssignmentCount();
			if(numAssigned <= 0)
				return null;
			ICpZone zone = getZone(columnIndex);
			if(zone == null)
				return null;
			boolean assigned = block.isAssigned(zone.getName());
			if(assigned) {
				if(numAssigned == 1)
					return ColorConstants.GREEN;
				return ColorConstants.YELLOW;
			}
			return null;
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
			if(colType == ColumnType.COLOP) {
				ICpZone zone = getZone(columnIndex);
				if(zone != null && zone.canAssign(block)) {
					return CellControlType.INPLACE_CHECK;
				}
				return CellControlType.NONE;
			}
			return CellControlType.TEXT;
		}

		@Override
		public boolean canEdit(Object obj, int columnIndex) {
			if(getColumnType(columnIndex) != ColumnType.COLOP)
				return false;
			ICpMemoryBlock block = getMemoryBlock(obj);
			if(block == null || getMemoryBlock(block, columnIndex) == null) {
				return false;
			}
			ICpZone zone = getZone(columnIndex);
			return zone != null && zone.canAssign(block);
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
			ICpZone zone = getZone(columnIndex);
			if(zone == null)
				return;
			if(getSelectionCount() > 1)
				fKeyAdapter.assignBlocks(columnIndex);
			else
				getModelController().assignBlock(block, zone.getName(), newVal);
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
		protected ICpProcessorUnit getProcessor(int columnIndex) {
			ICpZone zone = getZone(columnIndex);
			if(zone == null)
				return null;
			return zone.getTargetProcessor();
		}

		@Override
		protected ICpZone getZone(int columnIndex) {
			CmsisColumnInfo info = getColumnInfo(columnIndex);
			if(info == null)
				return null;
			return info.getDataItemAs(ICpZone.class);
		}

	} /// end of ProjectZoneMemoryMapColumnAdvisor

	@Override
	public CmsisZoneColumnAdvisor createColumnAdvisor() {
		return new CmsisZoneMapColumnAdvisor(this);
	}


	@Override
	public void handle(RteEvent event) {
		if(event.getData() instanceof ICpZone) {
			ICpZone zone = (ICpZone)event.getData();
			switch(event.getTopic()){
			case CmsisZoneController.ZONE_MODIFIED:
				modifyZoneColumn(zone);
				break;
			case CmsisZoneController.ZONE_ADDED:
				addZoneColumn(zone);
				break;
			case CmsisZoneController.ZONE_DELETED:
				removeZoneColumn(zone);
				break;
			}
			return;
		}
		super.handle(event);
	}

	protected void modifyZoneColumn(ICpZone zone) {
		if(fTreeViewer == null)
			return;
		CmsisZoneColumnAdvisor advisor = getCmsisZoneColumnAdvisor();
		CmsisColumnInfo info = advisor.getColumnInfo(zone);
		if(info == null)
			return;
		info.setName(zone.getName());
	}

	protected void removeZoneColumn(ICpZone zone) {
		if(fTreeViewer == null)
			return;
		CmsisZoneColumnAdvisor advisor = getCmsisZoneColumnAdvisor();
		int columnIndex = advisor.getColumnIndex(zone);
		if(columnIndex < 0)
			return;
		advisor.removeColumn(columnIndex);
	}

	protected void addZoneColumn(ICpZone zone) {
		if(fTreeViewer == null)
			return;
		CmsisZoneColumnAdvisor advisor = getCmsisZoneColumnAdvisor();
		CmsisColumnInfo info = advisor.getColumnInfo(zone);

		if(info != null) {
			info.setDataItem(zone);
			info.setName(zone.getName());
			return;
		}
		info = new CmsisColumnInfo(zone.getName(), ColumnType.COLOP, 80, false);
		info.setDataItem(zone);
		int columnIndex = advisor.getInsertIndex();
		advisor.addColumn(info, columnIndex);
	}

}
