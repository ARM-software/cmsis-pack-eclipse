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

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpMemory;
import com.arm.cmsis.pack.enums.ESeverity;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.generic.ITreeObject;
import com.arm.cmsis.pack.ui.ColorConstants;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.widgets.CmsisColumnAdvisor;
import com.arm.cmsis.pack.ui.widgets.CmsisColumnInfo;
import com.arm.cmsis.pack.ui.widgets.CmsisColumnInfo.ColumnType;
import com.arm.cmsis.pack.utils.Utils;
import com.arm.cmsis.zone.data.ICpDeviceUnit;
import com.arm.cmsis.zone.data.ICpMemoryBlock;
import com.arm.cmsis.zone.data.ICpPeripheralGroup;
import com.arm.cmsis.zone.data.ICpProcessorUnit;
import com.arm.cmsis.zone.data.ICpResourceItem;
import com.arm.cmsis.zone.data.ICpZone;
import com.arm.cmsis.zone.data.ICpZoneAssignment;
import com.arm.cmsis.zone.error.CmsisZoneError;
import com.arm.cmsis.zone.ui.Messages;
import com.arm.cmsis.zone.ui.editors.CmsisZoneController;

/**
 * Abstract column adviser for CmsisZone tree views
 */
public abstract class CmsisZoneColumnAdvisor extends CmsisColumnAdvisor<CmsisZoneController> {

    protected CmsisZoneTreeWidget fTreeWidget;
    protected boolean fbPhysicalAddress; // show physical start/stop addresses

    /**
     * Constructs advisor for a CmsisZoneTreeWidget
     *
     * @param treeWidget CmsisZoneTreeWidget on which the advisor is installed
     */
    public CmsisZoneColumnAdvisor(CmsisZoneTreeWidget treeWidget) {
        super(treeWidget.getViewer());
        fTreeWidget = treeWidget;
        fbPhysicalAddress = false;
    }

    /**
     * Constructs advisor for a viewer
     *
     * @param treeViewer TreeViewer on which the advisor is installed
     */
    public CmsisZoneColumnAdvisor(TreeViewer treeViewer) {
        super(treeViewer);
        fTreeWidget = null;
    }

    public CmsisZoneTreeWidget getTreeWidget() {
        return fTreeWidget;
    }

    public boolean isPhysicalAddress() {
        return fbPhysicalAddress;
    }

    public void setPhysicalAddress(boolean bPhysicalAddress) {
        fbPhysicalAddress = bPhysicalAddress;
    }

    @Override
    protected void createPrefixColumnInfos() {
        addColumnInfo(new CmsisColumnInfo(Messages.CmsisZoneColumnAdvisor_Name, ColumnType.COLNAME, 200, false));
        addColumnInfo(
                new CmsisColumnInfo(Messages.CmsisZoneColumnAdvisor_Permissions, ColumnType.COLACCESS, 60, false));
        addColumnInfo(new CmsisColumnInfo(Messages.CmsisZoneColumnAdvisor_Size, ColumnType.COLSIZE, 100, true));
    }

    @Override
    protected void createSuffixColumnInfos() {
        addColumnInfo(new CmsisColumnInfo(Messages.CmsisZoneColumnAdvisor_Info, ColumnType.COLINFO, 200, true));
        addColumnInfo(new CmsisColumnInfo(Messages.CmsisZoneColumnAdvisor_LinkerControl, ColumnType.COLLINKERCONTROL,
                200, true));
    }

    @Override
    protected void createDynamicColumnInfos() {
        addColumnInfo(new CmsisColumnInfo(Messages.CmsisZoneColumnAdvisor_Start, ColumnType.COLSTART, 100, true));
    }

    public CmsisColumnInfo getColumnInfo(ICpItem item) {
        if (item == null)
            return null;
        for (CmsisColumnInfo info : columnInfos) {
            if (info.getDataItem() == item) {
                return info;
            }
        }
        return null;
    }

    @Override
    public CmsisColumnInfo getColumnInfo(String columnName) {
        for (CmsisColumnInfo info : columnInfos) {
            if (info.getName().equals(columnName)) {
                return info;
            }
        }
        return null;
    }

    @Override
    public int getColumnIndex(String name) {
        for (int i = 0; i < columnInfos.size(); i++) {
            CmsisColumnInfo info = columnInfos.get(i);
            if (info.getName().equals(name))
                return i;
        }
        return -1;
    }

    public int getColumnIndex(ICpItem item) {
        for (int i = 0; i < columnInfos.size(); i++) {
            CmsisColumnInfo info = columnInfos.get(i);
            if (info.getDataItem() == item)
                return i;
        }
        return -1;
    }

    public static ICpResourceItem getResourceItem(Object obj) {
        if (obj instanceof ICpResourceItem) {
            return (ICpResourceItem) obj;
        }
        return null;
    }

    public static ICpMemoryBlock getMemoryBlock(Object obj) {
        if (obj instanceof ICpMemoryBlock) {
            return (ICpMemoryBlock) obj;
        }
        return null;
    }

    protected ICpProcessorUnit getProcessor(int columnIndex) {
        if (getTreeWidget() == null)
            return null;
        return getTreeWidget().getTargetProcessor();
    }

    protected ICpZone getZone(int columnIndex) {
        if (getTreeWidget() == null)
            return null;
        return getTreeWidget().getZone();
    }

    protected ICpZoneAssignment getAssignment(ICpMemoryBlock block, int columnIndex) {
        if (block == null)
            return null;
        ICpZone zone = getZone(columnIndex);
        if (zone == null)
            return null;
        return block.getAssignment(zone.getName());
    }

    protected ICpMemoryBlock getMemoryBlock(ICpMemory mem, int columnIndex) {
        if (mem == null)
            return null;

        if (mem instanceof ICpPeripheralGroup) {
            return null;
        }
        if (mem instanceof ICpMemoryBlock) {
            return (ICpMemoryBlock) mem;
        }
        if (mem instanceof ICpZoneAssignment) {
            ICpZoneAssignment zoneItem = (ICpZoneAssignment) mem;
            return zoneItem.getAssignedBlock();
        }
        return null;
    }

    protected String getPermissionsString(ICpMemory block, int columnIndex) {
        if (block == null)
            return CmsisConstants.EMPTY_STRING;
        int index = getSortColumnIndex(); // use sort index , not the access column itself
        ICpMemoryBlock mappedBlock = getMemoryBlock(block, index);
        if (mappedBlock != null) {
            return mappedBlock.getPermissionsString();
        }
        return block.getPermissionsString();
    }

    protected String getStartString(ICpMemory mem, int columnIndex, boolean bPhysical) {
        if (mem instanceof ICpPeripheralGroup) {
            return CmsisConstants.EMPTY_STRING;
        }

        ICpMemoryBlock block = getMemoryBlock(mem, columnIndex);
        if (block != null) {
            long start = bPhysical ? block.getAddress() : block.getStart();
            if (start < 0)
                return Messages.CmsisZoneColumnAdvisor_EightQuestionsSymbol;
            String startString = IAttributes.longToHexString8(start);
            return startString;
        }
        return CmsisConstants.EMPTY_STRING;
    }

    protected String getEndString(ICpMemory mem, boolean bPhysical) {
        if (mem == null) {
            return CmsisConstants.EMPTY_STRING;
        }

        if (mem instanceof ICpPeripheralGroup) {
            return CmsisConstants.EMPTY_STRING;
        }

        long end = bPhysical ? mem.getEndAddress() : mem.getStop();
        if (end < 0)
            return Messages.CmsisZoneColumnAdvisor_EightQuestionsSymbol;
        String endString = IAttributes.longToHexString8(end);
        return endString;
    }

    @Override
    public Font getFont(Object obj, int columnIndex) {
        ColumnType colType = getColumnType(columnIndex);
        if (colType == ColumnType.COLNAME || colType == ColumnType.COLSTART || colType == ColumnType.COLADDRESS) {
            ICpMemoryBlock block = ITreeObject.castTo(obj, ICpMemoryBlock.class);
            if (block != null) {
                if (colType == ColumnType.COLNAME) {
                    if (isStartupBlock(block)) {
                        return getBoldFont();
                    }
                } else if (block.getAttributeAsBoolean(CmsisConstants.FIXED, false)) {
                    return getBoldFont();
                }
            }
        }
        if (colType == ColumnType.COLOP && obj instanceof ICpDeviceUnit) {
            // display error message "MPU Slots!" in bold
            ICpZone zone = getZone(columnIndex);
            if (zone != null && zone.getError(CmsisZoneError.Z110) != null) {
                return getBoldFont();
            }
        }
        return super.getFont(obj, columnIndex);
    }

    protected boolean isStartupBlock(ICpMemoryBlock block) {
        if (block == null)
            return false;
        if (!block.isExecuteAccess() || block.isPeripheralAccess())
            return false;
        return block.isStartup();
    }

    protected Font getBoldFont() {
        return JFaceResources.getFontRegistry().getBold(JFaceResources.TEXT_FONT);
    }

    @Override
    protected boolean isUseFixedFont(Object obj, int columnIndex) {
        ColumnType colType = getColumnType(columnIndex);
        switch (colType) {
        case COLACCESS:
        case COLADDRESS:
        case COLSTART:
        case COLSIZE:
        case COLEND:
        case COLOFFSET:
            return true;
        case COLINFO:
            break;
        case COLLINKERCONTROL:
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
        ICpItem item = ICpItem.cast(obj);
        if (item == null)
            return null;

        if (columnIndex == 0) {
            return getItemImage(item);
        }
        return null;
    }

    protected Image getItemImage(ICpItem item) {
        Image baseImage = null;
        if (item == null)
            return baseImage;
        String tag = item.getTag();
        switch (tag) {
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
        case CmsisConstants.MEMORIES:
            baseImage = CpPlugInUI.getImage(CpPlugInUI.ICON_MEMORY);
            break;
        case CmsisConstants.MEMORY_TAG:
        case CmsisConstants.BLOCK_TAG: {
            ESeverity severity = item.getSeverity();
            if (severity.isSevere())
                baseImage = CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT_ERROR);
            else if (severity.isWarning())
                baseImage = CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT_WARNING);
            else
                baseImage = CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT);
        }
            break;
        case CmsisConstants.PERIPHERAL: {
            ESeverity severity = item.getSeverity();
            if (severity.isSevere())
                baseImage = CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT_ERROR);
            else if (severity.isWarning())
                baseImage = CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT_WARNING);
            else
                baseImage = CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT_GROUP);
        }
            break;
        default:
            break;
        }
        return baseImage;
    }

    protected boolean hasStrings(Object obj, int columnIndex) {
        return false;
    }

    @Override
    public String getString(Object obj, int columnIndex) {
        ICpItem item = ICpItem.cast(obj);
        if (item == null)
            return CmsisConstants.EMPTY_STRING;

        ColumnType colType = getColumnType(columnIndex);
        if (colType == ColumnType.COLINFO) {
            if (item instanceof ICpMemoryBlock && item.isRemoved()) {
                return Messages.CmsisZoneColumnAdvisor_MemoryBlockRemovedOrRenamed;
            }
            return item.getDescription();
        }

        if (colType == ColumnType.COLOP) {
            if (obj instanceof ICpDeviceUnit) {
                // display error message "MPU Slots!" in device row
                ICpZone zone = getZone(columnIndex);
                if (zone != null && zone.getError(CmsisZoneError.Z110) != null) {
                    return Messages.CmsisZoneColumnAdvisor_CmsisZoneErrorZ110;
                }
            }
            return CmsisConstants.EMPTY_STRING;
        }

        if (colType == ColumnType.COLNAME) {
            if (item instanceof ICpDeviceUnit && getZone(columnIndex) != null) {
                return getZone(columnIndex).getFullDeviceName();
            }
            return item.getEffectiveName();
        }

        if (item instanceof ICpPeripheralGroup)
            return CmsisConstants.EMPTY_STRING;

        if (item instanceof ICpMemory) {
            ICpMemory mem = (ICpMemory) item;
            switch (colType) {
            case COLACCESS: {
                return getPermissionsString(mem, columnIndex);
            }
            case COLSIZE: {
                return Utils.getFormattedMemorySizeString(mem.getSize());
            }
            case COLADDRESS:
                return getStartString(mem, columnIndex, true);
            case COLSTART:
                return getStartString(mem, columnIndex, isPhysicalAddress());
            case COLEND:
                return getEndString(mem, isPhysicalAddress());
            case COLOFFSET:
                return mem.getOffsetString();
            case COLLINKERCONTROL:
                return mem.getLinkerControl();
            default:
                break;
            }
        }
        return CmsisConstants.EMPTY_STRING;
    }

    @Override
    public Color getBgColor(Object obj, int columnIndex) {
        ColumnType colType = getColumnType(columnIndex);
        if (colType == ColumnType.COLSTART || colType == ColumnType.COLADDRESS) {
            ICpMemoryBlock block = getMemoryBlock(obj);
            if (block != null && (block.isRemoved() || !block.isValid())) {
                return ColorConstants.PALE_RED;
            }
        }
        return super.getBgColor(obj, columnIndex);
    }

    /**
     * Returns next index to insert a column, default returns index after last
     * column with data item.
     *
     * @return index for the new column
     */
    @Override
    public int getInsertIndex() {
        if (columnInfos.isEmpty())
            return 0;
        return columnInfos.size() - 2; // 'Info' and 'Linker Control' are always the last ones
    }

} /// end of CmsisZoneColumnAdvisor
