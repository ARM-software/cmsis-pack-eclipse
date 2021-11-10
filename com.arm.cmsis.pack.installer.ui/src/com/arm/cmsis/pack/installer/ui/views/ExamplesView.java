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

package com.arm.cmsis.pack.installer.ui.views;

import java.util.Collection;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackInstaller;
import com.arm.cmsis.pack.ICpPackManager;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpBoard;
import com.arm.cmsis.pack.data.ICpExample;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.installer.ui.ButtonId;
import com.arm.cmsis.pack.installer.ui.IHelpContextIds;
import com.arm.cmsis.pack.installer.ui.Messages;
import com.arm.cmsis.pack.repository.RtePackJobResult;
import com.arm.cmsis.pack.rte.examples.IRteExampleItem;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.tree.AdvisedCellLabelProvider;
import com.arm.cmsis.pack.ui.tree.ColumnAdvisor;
import com.arm.cmsis.pack.ui.tree.IColumnAdvisor;
import com.arm.cmsis.pack.ui.tree.TreeObjectContentProvider;
import com.arm.cmsis.pack.utils.DeviceVendor;

/**
 * Default implementation of the examples view in pack manager
 */
public class ExamplesView extends PackInstallerView {

    public static final String ID = "com.arm.cmsis.pack.installer.ui.views.ExamplesView"; //$NON-NLS-1$

    Action fShowInstOnlyAction;
    ICpExample fExampleToInstall = null;

    IRteExampleItem getRteExampleItem(Object obj) {
        if (obj instanceof IRteExampleItem) {
            return (IRteExampleItem) obj;
        }
        return null;
    }

    class ExamplesViewColumnAdvisor extends ColumnAdvisor {

        public ExamplesViewColumnAdvisor(ColumnViewer columnViewer) {
            super(columnViewer);
        }

        @Override
        public CellControlType getCellControlType(Object obj, int columnIndex) {
            if (columnIndex == COLBUTTON) {
                return CellControlType.BUTTON;
            }
            return CellControlType.TEXT;
        }

        @Override
        public boolean isEnabled(Object obj, int columnIndex) {
            if (fExampleToInstall != null) {
                return false; // do not allow several examples at once
            }
            switch (columnIndex) {
            case COLNAME:
                return true;
            case COLBUTTON:
                if (getCellControlType(obj, columnIndex) == CellControlType.BUTTON) {
                    ICpPackInstaller packInstaller = getPackInstaller();
                    if (packInstaller == null) {
                        return false;
                    }
                    IRteExampleItem example = getRteExampleItem(obj);
                    if (example != null) {
                        ICpExample e = example.getExample();
                        if (e == null || packInstaller.isProcessing(e.getPackId())) {
                            return false;
                        }
                        return true;
                    }
                }
            }
            return true;
        }

        @Override
        public Image getImage(Object obj, int columnIndex) {
            IRteExampleItem item = null;
            switch (columnIndex) {
            case COLNAME:
                item = getRteExampleItem(obj);
                if (item != null) {
                    if (CmsisConstants.UV.equals(item.getEnvironment()))
                        return CpPlugInUI.getImage(CpPlugInUI.ICON_UV5);
                    else if (item.isSupported() && !item.isToImport())
                        return Window.getDefaultImage();
                }
                break;
            default:
                if (getCellControlType(obj, columnIndex) == CellControlType.BUTTON) {
                    switch (getButtonId(obj, columnIndex)) {
                    case BUTTON_COPY:
                    case BUTTON_IMPORT:
                        item = getRteExampleItem(obj);
                        if (item != null && item.getExample().isDeprecated()) {
                            return CpPlugInUI.getImage(CpPlugInUI.ICON_RTE_WARNING);
                        }
                        return CpPlugInUI.getImage(CpPlugInUI.ICON_RTE);
                    case BUTTON_INSTALL:
                        return CpPlugInUI.getImage(CpPlugInUI.ICON_RTE_INSTALL);
                    default:
                        break;
                    }
                }
                break;
            }
            return null;
        }

        @Override
        public String getString(Object obj, int index) {
            switch (index) {
            case COLNAME:
                IRteExampleItem ei = getRteExampleItem(obj);
                if (ei == null) {
                    return null;
                }
                ICpExample e = ei.getExample();
                if (e != null) {
                    return e.getId();
                }
            case COLBUTTON:
                if (getCellControlType(obj, index) == CellControlType.BUTTON) {
                    IRteExampleItem item = getRteExampleItem(obj);
                    if (item != null) {
                        if (item.getExample().getPack().getPackState().isInstalledOrLocal()) {
                            if (item.isToImport()) {
                                return getButtonString(ButtonId.BUTTON_IMPORT);
                            }
                            return getButtonString(ButtonId.BUTTON_COPY);
                        }
                        return getButtonString(ButtonId.BUTTON_INSTALL);
                    }
                }
            }
            return CmsisConstants.EMPTY_STRING;
        }

        @Override
        public String getTooltipText(Object obj, int columnIndex) {
            switch (columnIndex) {
            case COLNAME:
                ICpExample e = ((IRteExampleItem) obj).getExample();
                return constructExampleTooltipText(e, obj);
            case COLBUTTON:
                if (getCellControlType(obj, columnIndex) == CellControlType.BUTTON) {
                    IRteExampleItem item = getRteExampleItem(obj);
                    if (item != null) {
                        if (item.getPack() != null && item.getPack().getPackState().isInstalledOrLocal() == false) {
                            StringBuilder str = new StringBuilder(Messages.ExamplesView_CopyExampleInstallPack)
                                    .append(item.getExample().getPackId());
                            return str.toString() + "\n" + Messages.ExamplesView_Format + ": " + item.getEnvironment(); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                        return constructExampleTooltipText(item.getExample(), obj);
                    }
                }
            }
            return null;
        }

        @Override
        protected void handleMouseUp(MouseEvent e) {
            Point pt = new Point(e.x, e.y);
            ViewerCell cell = getViewer().getCell(pt);

            if (cell == null) {
                return;
            }

            int colIndex = cell.getColumnIndex();
            Object element = cell.getElement();
            if (getCellControlType(element, colIndex) != CellControlType.BUTTON || !isEnabled(element, colIndex)
                    || !isButtonPressed(element, colIndex)) {
                return;
            }

            IRteExampleItem example = getRteExampleItem(element);
            if (example != null) {
                switch (getButtonId(element, colIndex)) {
                case BUTTON_COPY:
                case BUTTON_IMPORT:
                    ICpExample cpExample = example.getExample();
                    copyExample(cpExample);
                    break;
                case BUTTON_INSTALL:
                    ICpPackInstaller packInstaller = getPackInstaller();
                    if (packInstaller != null && fExampleToInstall == null) {
                        fExampleToInstall = example.getExample();
                        packInstaller.installPack(example.getExample().getPackId());
                    }
                default:
                    break;
                }
            }

            setButtonPressed(null, COLBUTTON, null);
            this.control.redraw();
        }

        protected ButtonId getButtonId(Object obj, int index) {
            if (getCellControlType(obj, index) == CellControlType.BUTTON) {
                IRteExampleItem item = getRteExampleItem(obj);
                if (item != null) {
                    if (item.getExample().getPack().getPackState().isInstalledOrLocal()) {
                        if (item.isToImport())
                            return ButtonId.BUTTON_IMPORT;
                        return ButtonId.BUTTON_COPY;
                    }
                    return ButtonId.BUTTON_INSTALL;
                }
            }
            return ButtonId.BUTTON_UNDEFINED;
        }

    } /// end of ColumnAdviser

    void copyExample(ICpExample cpExample) {
        if (cpExample == null || fViewController == null) {
            return;
        }
        fViewController.copyExample(cpExample);
    }

    @Override
    protected void handleRteEvent(RteEvent event) {
        super.handleRteEvent(event);
        if (RteEvent.PACK_INSTALL_JOB_FINISHED.equals(event.getTopic())) {
            RtePackJobResult result = (RtePackJobResult) event.getData();
            if (fExampleToInstall != null && result.getPackId().equals(fExampleToInstall.getPackId())) {
                if (result.isSuccess()) {
                    final ICpExample example = getExample(fExampleToInstall.getId(), result.getPack());
                    if (example != null) {
                        // execute not immediately: still running progress dialog will kill import
                        // wizard immediately!
                        Display.getDefault().asyncExec(() -> {
                            copyExample(example);
                        });
                    }
                }
            }
            fExampleToInstall = null;
        }
    }

    String constructExampleTooltipText(ICpExample example, Object obj) {
        String tooltip = CmsisConstants.EMPTY_STRING;

        String boardId = example.getBoardId();
        ICpBoard b = CpPlugIn.getPackManager().getBoard(boardId);

        if (b != null) {
            String line1 = NLS.bind(Messages.ExamplesView_Board, b.getName(), b.getVendor());
            StringBuilder lb2 = new StringBuilder(Messages.ExamplesView_Device);
            for (ICpItem device : b.getMountedDevices()) {
                String vendorName = DeviceVendor.getOfficialVendorName(device.getVendor());
                String deviceName = CmsisConstants.EMPTY_STRING;
                if (device.hasAttribute(CmsisConstants.DFAMILY)) {
                    deviceName = device.getAttribute(CmsisConstants.DFAMILY);
                } else if (device.hasAttribute(CmsisConstants.DSUBFAMILY)) {
                    deviceName = device.getAttribute(CmsisConstants.DSUBFAMILY);
                } else if (device.hasAttribute(CmsisConstants.DNAME)) {
                    deviceName = device.getAttribute(CmsisConstants.DNAME);
                } else if (device.hasAttribute(CmsisConstants.DVARIANT)) {
                    deviceName = device.getAttribute(CmsisConstants.DVARIANT);
                }
                if (!deviceName.isEmpty()) {
                    lb2.append(deviceName).append(" (").append(vendorName) //$NON-NLS-1$
                            .append("), "); //$NON-NLS-1$
                }
            }
            if (lb2.lastIndexOf(",") >= 0) { //$NON-NLS-1$
                lb2.deleteCharAt(lb2.lastIndexOf(",")); //$NON-NLS-1$
            }
            String line2 = lb2.append(System.lineSeparator()).toString();
            tooltip = line1 + line2;
        }
        String line3 = NLS.bind(Messages.ExamplesView_Pack, example.getPackId());
        String line4 = example.getDescription();
        if (line4.length() > 128) {
            // truncate long description
            line4 = line4.substring(0, 128) + "..."; //$NON-NLS-1$
        }
        String line5 = CmsisConstants.EMPTY_STRING;
        IRteExampleItem item = getRteExampleItem(obj);
        if (item != null)
            line5 = "\n" + Messages.ExamplesView_Format + ": " + item.getEnvironment(); //$NON-NLS-1$ //$NON-NLS-2$
        return tooltip + line3 + line4 + line5;
    }

    class ExampleTreeColumnComparator extends PackInstallerTreeColumnComparator {

        public ExampleTreeColumnComparator(TreeViewer viewer, IColumnAdvisor advisor) {
            super(viewer, advisor);
        }

        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {

            if (getColumnIndex() != 0) {
                return super.compare(viewer, e1, e2);
            }

            ICpExample cp1 = ((IRteExampleItem) e1).getExample();
            ICpExample cp2 = ((IRteExampleItem) e2).getExample();

            int result = alnumComparator.compare(cp1.getId(), cp2.getId());
            return bDescending ? -result : result;
        }
    }

    public ExamplesView() {
    }

    @Override
    public void createTreeColumns() {
        fTree.setInitialText(Messages.ExamplesView_SearchExample);

        // ------ Start Setting ALL Columns for the Examples View
        // ------ First Column
        TreeViewerColumn column0 = new TreeViewerColumn(fViewer, SWT.LEFT);
        column0.getColumn().setText(CmsisConstants.EXAMPLE_TITLE);
        column0.getColumn().setWidth(300);
        ExamplesViewColumnAdvisor columnAdvisor = new ExamplesViewColumnAdvisor(fViewer);
        column0.setLabelProvider(new AdvisedCellLabelProvider(columnAdvisor, 0));

        // ------ Second Column
        TreeViewerColumn column1 = new TreeViewerColumn(fViewer, SWT.LEFT);
        column1.getColumn().setText(CmsisConstants.ACTION_TITLE);
        column1.getColumn().setWidth(90);
        column1.setLabelProvider(new AdvisedCellLabelProvider(columnAdvisor, COLBUTTON));

        // ------ Third Column
        TreeViewerColumn column2 = new TreeViewerColumn(fViewer, SWT.LEFT);
        column2.getColumn().setText(CmsisConstants.DESCRIPTION_TITLE);
        column2.getColumn().setWidth(400);
        column2.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object obj) {
                IRteExampleItem example = getRteExampleItem(obj);
                if (example != null) {
                    return example.getExample().getDescription();
                }
                return null;
            }
        });
        // ------ End Setting ALL Columns for the Examples View

        fViewer.setContentProvider(new TreeObjectContentProvider());
        fViewer.setComparator(new ExampleTreeColumnComparator(fViewer, columnAdvisor));
    }

    @Override
    protected String getHelpContextId() {
        return IHelpContextIds.EXAMPLES_VIEW;
    }

    @Override
    protected boolean isExpandable() {
        return false;
    }

    @Override
    protected boolean hasManagerCommands() {
        return true;
    }

    @Override
    protected void refresh() {
        if (CpPlugIn.getDefault() == null) {
            return;
        }
        ICpPackManager packManager = CpPlugIn.getPackManager();
        if (packManager != null) {
            fViewer.setInput(packManager.getExamples());
        } else {
            fViewer.setInput(null);
        }
    }

    @Override
    protected void makeActions() {
        fShowInstOnlyAction = new Action(Messages.ExamplesView_OnlyShowInstalledPack, IAction.AS_CHECK_BOX) {
            @Override
            public void run() {
                boolean bChecked = fShowInstOnlyAction.isChecked();
                fViewController.getFilter().setShowExamplesInstalledOnly(bChecked);
                fViewer.setFilters(fViewFilters);
                fViewer.setSelection(null);
                if (fShowInstOnlyAction.isChecked()) {
                    fShowInstOnlyAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_CHECKED));
                } else {
                    fShowInstOnlyAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_UNCHECKED));
                }
            }
        };
        fShowInstOnlyAction.setToolTipText(Messages.ExamplesView_OnlyShowInstalledPack);
        fShowInstOnlyAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_CHECKED));
        fShowInstOnlyAction.setEnabled(true);
        fShowInstOnlyAction.setChecked(true);

        super.makeActions();
    }

    @Override
    protected void fillLocalPullDown(IMenuManager manager) {
        manager.add(fShowInstOnlyAction);
        manager.add(new Separator());
        super.fillLocalPullDown(manager);
    }

    @Override
    protected void fillLocalToolBar(IToolBarManager manager) {
        ActionContributionItem aci = new ActionContributionItem(fShowInstOnlyAction);
        aci.setMode(ActionContributionItem.MODE_FORCE_TEXT);
        manager.add(aci);
        manager.add(new Separator());
        super.fillLocalToolBar(manager);
    }

    protected ICpExample getExample(String id, ICpPack pack) {
        if (pack == null)
            return null;
        Collection<? extends ICpItem> examples = pack.getGrandChildren(CmsisConstants.EXAMPLES_TAG);
        if (examples == null || examples.isEmpty())
            return null;
        for (ICpItem item : examples) {
            if (!(item instanceof ICpExample)) {
                continue;
            }
            if (item.getId().equals(id))
                return (ICpExample) item;
        }
        return null;
    }

}
