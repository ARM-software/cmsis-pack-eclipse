/*******************************************************************************
 * Copyright (c) 2021 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/
package com.arm.cmsis.zone.widgets;

import java.util.Map;

import org.eclipse.swt.graphics.Color;

import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.ui.ColorConstants;
import com.arm.cmsis.pack.ui.widgets.CmsisColumnInfo;
import com.arm.cmsis.pack.ui.widgets.CmsisColumnInfo.ColumnType;
import com.arm.cmsis.zone.data.ICpMemoryBlock;
import com.arm.cmsis.zone.data.ICpProcessorUnit;
import com.arm.cmsis.zone.data.ICpRootZone;
import com.arm.cmsis.zone.ui.Messages;

/**
 * This class displays the component tree for selection.
 *
 */
public class CmsisZoneResourceMapWidget extends CmsisZoneTreeWidget {
    /**
     * Column label provider for RteComponentTreeWidget
     */
    public class ResourceMapColumnAdvisor extends CmsisZoneColumnAdvisor {
        /**
         * Constructs advisor for a viewer
         *
         * @param columnViewer ColumnViewer on which the advisor is installed
         */
        public ResourceMapColumnAdvisor(CmsisZoneTreeWidget treeWidget) {
            super(treeWidget);
        }

        @Override
        protected void createDynamicColumnInfos() {
            ICpRootZone rootZone = getModelController().getRootZone();
            if (rootZone == null)
                return;
            addColumnInfo(
                    new CmsisColumnInfo(Messages.CmsisZoneResourceMapWidget_Physical, ColumnType.COLADDRESS, 80, true));

            Map<String, ICpProcessorUnit> processors = rootZone.getProcessorUnits();
            if (processors == null || processors.isEmpty())
                return;
            for (ICpProcessorUnit p : rootZone.getProcessorUnits().values()) {
                addColumnInfo(new CmsisColumnInfo(p, ColumnType.COLSTART, 80, true));
            }
        }

        @Override
        public Color getBgColor(Object obj, int columnIndex) {
            ColumnType colType = getColumnType(columnIndex);
            ICpMemoryBlock block = getMemoryBlock(obj);
            if (block == null)
                return null;

            if (colType != ColumnType.COLSTART && colType != ColumnType.COLSIZE)
                return null;
            if (block.isRemoved() || !block.isValid()) {
                return ColorConstants.PALE_RED;
            }

            if (colType != ColumnType.COLSTART)
                return null;
            int numAssigned = block.getAssignmentCount();
            if (numAssigned <= 0)
                return null;
            ICpProcessorUnit processor = getProcessor(columnIndex);
            boolean assigned = block.isAssigned(processor);
            if (assigned) {
                if (numAssigned == 1)
                    return ColorConstants.GREEN;
                return ColorConstants.YELLOW;
            } else {
                assigned = block.isAssigned(processor);
            }
            return null;
        }

        @Override
        public CellControlType getCellControlType(Object obj, int columnIndex) {
            ICpItem item = ICpItem.cast(obj);
            if (item == null) {
                return CellControlType.NONE;
            }
            if (hasStrings(obj, columnIndex)) {
                return CellControlType.MENU;
            }
            return CellControlType.TEXT;
        }

        @Override
        public boolean canEdit(Object obj, int columnIndex) {
            if (getColumnType(columnIndex) != ColumnType.COLSTART)
                return false;
            return hasStrings(obj, columnIndex);
        }

        @Override
        protected ICpProcessorUnit getProcessor(int columnIndex) {
            CmsisColumnInfo info = getColumnInfo(columnIndex);
            if (info != null)
                return info.getDataItemAs(ICpProcessorUnit.class);
            return null;
        }

    } /// end of ResourceMapColumnAdvisor

    @Override
    public CmsisZoneColumnAdvisor createColumnAdvisor() {
        return new ResourceMapColumnAdvisor(this);
    }

}
