/*******************************************************************************
 * Copyright (c) 2021 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/
package com.arm.cmsis.zone.widgets;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpMemory;
import com.arm.cmsis.pack.ui.tree.TreeObjectContentProvider;
import com.arm.cmsis.pack.ui.widgets.CmsisColumnInfo;
import com.arm.cmsis.pack.ui.widgets.CmsisColumnInfo.ColumnType;
import com.arm.cmsis.zone.data.ICpMemoryBlock;
import com.arm.cmsis.zone.data.ICpRootZone;
import com.arm.cmsis.zone.ui.Messages;
import com.arm.cmsis.zone.ui.editors.CmsisZoneController;

/**
 * This class displays the component tree for selection.
 *
 */
public class CmsisZoneSystemResourcesWidget extends CmsisZoneTreeWidget {

    // constants for column number
    static final int COLNAME = 0;
    static final int COLOP = 1;
    static final int COLACCESS = 2;
    static final int COLSIZE = 3;
    static final int COLSTART = 4;
    static final int COLOFFSET = 5;
    static final int COLINFO = 6;
    static final int COL_COUNT = 7;

    /**
     * Column label provider for RteComponentTreeWidget
     */
    public class SystemResourcesColumnAdvisor extends CmsisZoneColumnAdvisor {
        /**
         * Constructs advisor for a viewer
         *
         * @param columnViewer ColumnViewer on which the advisor is installed
         */
        public SystemResourcesColumnAdvisor(CmsisZoneTreeWidget treeWidget) {
            super(treeWidget);
        }

        @Override
        protected void createDynamicColumnInfos() {
            addColumnInfo(
                    new CmsisColumnInfo(Messages.CmsisZoneSystemResourcesWidget_Start, ColumnType.COLSTART, 100, true));
            addColumnInfo(new CmsisColumnInfo(Messages.CmsisZoneSystemResourcesWidget_Offset, ColumnType.COLOFFSET, 100,
                    true));
        }

        @Override
        public String getString(Object obj, int index) {
            ICpItem item = ICpItem.cast(obj);
            if (item == null)
                return CmsisConstants.EMPTY_STRING;
            if (index == COLOP) {
                // no operation yet
                return CmsisConstants.EMPTY_STRING;
            }
            if (index == COLNAME) {
                return item.getEffectiveName();
            } else if (index == COLINFO) {
                return item.getDescription();
            }
            if (item instanceof ICpMemory) {
                ICpMemory mem = (ICpMemory) item;
                switch (index) {
                case COLACCESS: {
                    return mem.getAccessString();
                }
                case COLSTART: {
                    String label = mem.getStartString();
                    return label;
                }
                case COLOFFSET: {
                    String label = mem.getOffsetString();
                    return label;
                }
                case COLSIZE: {
                    String label = mem.getSizeString();
                    return label;
                }
                }
            }
            return CmsisConstants.EMPTY_STRING;
        }

        @Override
        protected ICpMemoryBlock getMemoryBlock(ICpMemory block, int columnIndex) {
            if (block instanceof ICpMemoryBlock)
                return (ICpMemoryBlock) block;
            return null;
        }

        @Override
        public String getUrl(Object obj, int columnIndex) {
            if (columnIndex == COLINFO) {
                ICpItem item = ICpItem.cast(obj);
                if (item != null) {
                    return item.getUrl();
                }
            }
            return null;
        }

        @Override
        public CellControlType getCellControlType(Object obj, int columnIndex) {
            ICpItem item = ICpItem.cast(obj);
            if (item == null) {
                return CellControlType.NONE;
            }
            switch (columnIndex) {
            case COLINFO:
                String url = item.getUrl();
                if (url != null && !url.isEmpty()) {
                    return CellControlType.URL;
                }
                break;
            default:
                break;
            }

            return CellControlType.TEXT;
        }

        @Override
        public boolean isUseFixedFont(Object obj, int columnIndex) {
            switch (columnIndex) {
            case COLACCESS:
            case COLSIZE:
            case COLSTART:
            case COLOFFSET:
                return true;
            }
            return false;
        }

    } /// end of RteSystemResourcesColumnAdvisor

    @Override
    protected CmsisZoneColumnAdvisor createColumnAdvisor() {
        return new SystemResourcesColumnAdvisor(this);
    }

    /**
     * Content provider for RTEComponentTreeWidget
     */
    public class RteSystemResourcesContentProvider extends TreeObjectContentProvider {

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            super.inputChanged(viewer, oldInput, newInput);
        }

        @Override
        public Object[] getElements(Object inputElement) {
            return super.getElements(inputElement);
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            return super.getChildren(parentElement);
        }

        @Override
        public Object getParent(Object element) {
            return super.getParent(element);
        }

        @Override
        public boolean hasChildren(Object element) {
            return super.hasChildren(element);
        }

    }

    @Override
    protected ITreeContentProvider createContentProvider() {
        return new RteSystemResourcesContentProvider();
    }

    /**
     * Set current configuration for this component tree widget
     *
     * @param configuration A RTE configuration that contains RTE component
     */
    @Override
    public void setModelController(CmsisZoneController controller) {
        super.setModelController(controller);
        if (fTreeViewer != null && controller != null) {
            ICpRootZone systemInfo = controller.getRootZone();
            fTreeViewer.setInput(systemInfo);
            fTreeViewer.expandToLevel(2);
        }
        update();
    }

}
