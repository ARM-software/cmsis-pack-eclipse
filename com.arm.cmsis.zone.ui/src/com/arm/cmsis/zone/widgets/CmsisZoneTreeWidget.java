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

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.generic.ITreeObject;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.widgets.RteTreeWidget;
import com.arm.cmsis.pack.ui.wizards.OkWizard;
import com.arm.cmsis.zone.data.ICpDeviceUnit;
import com.arm.cmsis.zone.data.ICpMemoryBlock;
import com.arm.cmsis.zone.data.ICpPeripheral;
import com.arm.cmsis.zone.data.ICpPeripheralGroup;
import com.arm.cmsis.zone.data.ICpProcessorUnit;
import com.arm.cmsis.zone.data.ICpRootZone;
import com.arm.cmsis.zone.data.ICpSlot;
import com.arm.cmsis.zone.data.ICpZone;
import com.arm.cmsis.zone.ui.Messages;
import com.arm.cmsis.zone.ui.editors.CmsisZoneController;
import com.arm.cmsis.zone.ui.wizards.CmsisZoneWizard;
import com.arm.cmsis.zone.ui.wizards.CpPeripheralSlotSetupDlg;
import com.arm.cmsis.zone.ui.wizards.MemoryBlockWizard;

public abstract class CmsisZoneTreeWidget extends RteTreeWidget<CmsisZoneController> {
    protected Action propertiesAction = null;
    protected Action deleteBlockAction = null;
    protected Action addBlockAction = null;
    protected Action arrangeBlocksAction = null;
    protected Action deleteZoneAction = null;
    protected Action configureSlotseAction = null;

    protected CmsisZoneKeyAdapter fKeyAdapter;
    protected ICpItem contextMenuItem = null;

    @Override
    public void destroy() {
        super.destroy();
        propertiesAction = null;
        deleteBlockAction = null;
        addBlockAction = null;
        arrangeBlocksAction = null;
        configureSlotseAction = null;
    }

    public ICpProcessorUnit getTargetProcessor() {
        return null;
    }

    public ICpDeviceUnit getTargetDevice() {
        return null;
    }

    /**
     * Returns ICpZone currently managed by the widget
     *
     * @return ICpZone or null if none
     */
    public ICpZone getZone() {
        if (contextMenuItem instanceof ICpZone)
            return (ICpZone) contextMenuItem;
        return null; // default returns null
    }

    public void setZone(ICpZone zone) {
        // default does nothing
    }

    public boolean isShowList() {
        return getAttributeAsBoolean(CmsisConstants.LIST, false);
    }

    @Override
    protected void fillContextMenu(IMenuManager manager) {
        ICpItem selItem = getSelectedItem();
        contextMenuItem = selItem;
        if (getZone() != null) {
            manager.add(new Separator());
            manager.add(deleteZoneAction);
            manager.add(new Separator());
            manager.add(propertiesAction);
            return;
        }
        if (tContextMenuPoint.y < 0) // on header or outside
            return;

        super.fillContextMenu(manager);
        if (canAddBlock(selItem)) {
            manager.add(new Separator());
            manager.add(addBlockAction);
            // manager.add(arrangeBlocksAction);
        }
        if (canDeleteBlock(selItem)) {
            manager.add(new Separator());
            manager.add(deleteBlockAction);
        }

        if (hasSlots(selItem)) {
            manager.add(new Separator());
            manager.add(configureSlotseAction);
        }
        if (hasProperties(selItem)) {
            manager.add(new Separator());
            manager.add(propertiesAction);
        }
    }

    @Override
    protected void makeActions() {
        super.makeActions();

        propertiesAction = new Action() {
            @Override
            public void run() {
                properties();
            }
        };
        propertiesAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_PROPERTIES));
        propertiesAction.setText(Messages.CmsisZoneTreeWidget_Properties);

        configureSlotseAction = new Action() {
            @Override
            public void run() {
                configureSlots();
            }
        };
        configureSlotseAction.setText(Messages.CmsisZoneTreeWidget_Configure);

        deleteBlockAction = new Action() {
            @Override
            public void run() {
                deleteSelectedBlocks();
            }
        };
        deleteBlockAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_DELETE));
        deleteBlockAction.setText(Messages.CmsisZoneTreeWidget_DeleteMemoryRegion);

        addBlockAction = new Action() {
            @Override
            public void run() {
                addBlock();
            }
        };
        addBlockAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_BLOCK_NEW));
        addBlockAction.setText(Messages.CmsisZoneTreeWidget_AddMemoryRegion);

        arrangeBlocksAction = new Action() {
            @Override
            public void run() {
                arrangeBlocks();
            }
        };
        arrangeBlocksAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_ARRANGE));
        arrangeBlocksAction.setText(Messages.CmsisZoneTreeWidget_ArrangeMemoryRegions);
        arrangeBlocksAction
                .setToolTipText(Messages.CmsisZoneTreeWidget_ArrangeMemoryRegionsAccordingToSizesAndPermissions);

        String text = Messages.CmsisZoneTreeWidget_DeleteZone;
        deleteZoneAction = new Action(text, IAction.AS_PUSH_BUTTON) {
            @Override
            public void run() {
                ICpZone zone = getZone();
                if (zone == null)
                    return;
                String msg = text + Messages.CmsisZoneTreeWidget_SimpleQuotationMark + zone.getName()
                        + Messages.CmsisZoneTreeWidget_SimpleQuotationMarkWithQuestionSymbol;
                boolean yes = MessageDialog.openQuestion(getFocusWidget().getShell(), msg, msg);
                if (yes) {
                    // run async as we will remove this page /column
                    Display.getDefault().asyncExec(() -> getModelController().deleteZone(zone));
                }
            }
        };
        deleteZoneAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_DELETE));
    }

    protected void arrangeBlocks() {
        getModelController().arrangeBlocks();

    }

    protected void deleteSelectedBlocks() {
        fKeyAdapter.processDeletePressed();
    }

    public static boolean canDeleteBlock(ICpItem selItem) {
        if (selItem == null)
            return false;
        if (selItem instanceof ICpMemoryBlock) {
            ICpMemoryBlock block = (ICpMemoryBlock) selItem;
            return block.isDeletable();
        }
        return false;
    }

    protected void addBlock() {
        ICpItem selItem = getSelectedItem();
        if (selItem == null)
            return;
        if (!canAddBlock(selItem))
            return;
        if (!(selItem instanceof ICpMemoryBlock))
            return;
        ICpMemoryBlock parentBlock = (ICpMemoryBlock) selItem;
        MemoryBlockWizard newWizard = new MemoryBlockWizard(getModelController(), parentBlock);
        newWizard.execute(getFocusWidget().getShell());
    }

    protected boolean canAddBlock(ICpItem selItem) {
        if (selItem == null)
            return false;
        if (selItem instanceof ICpPeripheral)
            return false;
        if (selItem instanceof ICpPeripheralGroup)
            return false;
        if (selItem instanceof ICpMemoryBlock) {
            ICpMemoryBlock r = (ICpMemoryBlock) selItem;
            return r.getParentBlock() == null;
        }
        return false;
    }

    protected boolean hasProperties(ICpItem selItem) {
        if (selItem == null)
            return false;
        if (selItem instanceof ICpZone)
            return true;
        if (selItem instanceof ICpPeripheralGroup) {
            return false;
        }
        if (selItem instanceof ICpMemoryBlock) {
            return true;
        }
        return false;
    }

    protected void properties() {
        OkWizard wizard = getPropertiesWizard();
        if (wizard != null)
            wizard.execute(getFocusWidget().getShell());
    }

    protected OkWizard getPropertiesWizard() {
        ICpItem selItem = contextMenuItem != null ? contextMenuItem : getSelectedItem();
        contextMenuItem = null;
        if (!hasProperties(selItem))
            return null;
        if (selItem instanceof ICpZone) {
            return new CmsisZoneWizard(getModelController(), (ICpZone) selItem);
        }
        if (selItem instanceof ICpMemoryBlock) {
            ICpMemoryBlock block = (ICpMemoryBlock) selItem;
            ICpMemoryBlock parentBlock = block.getParentBlock();
            return new MemoryBlockWizard(getModelController(), parentBlock, block);
        }
        return null;
    }

    protected boolean hasSlots(ICpItem selItem) {
        if (selItem instanceof ICpPeripheral) {
            ICpPeripheral p = (ICpPeripheral) selItem;
            Collection<ICpSlot> slots = p.getSlots();
            if (slots != null && !slots.isEmpty()) {
                if (configureSlotseAction != null) {
                    configureSlotseAction
                            .setText(Messages.CmsisZoneTreeWidget_Configure + CmsisConstants.SPACE + p.getSlotName());
                }
                return true;
            }
        }
        return false;
    }

    protected void configureSlots() {
        ICpItem selItem = contextMenuItem != null ? contextMenuItem : getSelectedItem();
        contextMenuItem = null;
        if (!hasSlots(selItem)) {
            return;
        }
        ICpPeripheral p = (ICpPeripheral) selItem;

        CpPeripheralSlotSetupDlg dlg = new CpPeripheralSlotSetupDlg(getFocusWidget().getShell(), p);
        if (dlg.open() == Window.OK) {
            if (dlg.apply()) {
                getModelController().setModified(true);
            }
        }
    }

    protected ICpItem getSelectedItem() {
        if (getViewer() != null) {
            if (tContextMenuColumn > 0) {
                CmsisZoneColumnAdvisor advisor = (CmsisZoneColumnAdvisor) getColumnAdvisor();
                ICpZone zone = advisor.getZone(tContextMenuColumn);
                if (zone != null)
                    return zone;
            }

            IStructuredSelection sel = (IStructuredSelection) getViewer().getSelection();
            if (sel != null) {
                Object element = sel.getFirstElement();
                return ICpItem.cast(element);
            }
        }
        return null;
    }

    public Collection<ICpItem> getSelectedItems() {
        List<ICpItem> selectedItems = new ArrayList<>();
        if (getViewer() != null) {
            IStructuredSelection sel = getViewer().getStructuredSelection();
            if (sel != null) {
                for (Object element : sel.toList()) {
                    if (element instanceof ICpItem)
                        selectedItems.add((ICpItem) element);
                }
            }
        }
        return selectedItems;
    }

    public <T> Collection<T> getSelectedItemsOfType(Class<T> type) {
        List<T> selectedItems = new ArrayList<>();
        if (getViewer() != null) {
            IStructuredSelection sel = (IStructuredSelection) getViewer().getSelection();
            if (sel != null) {
                for (Object element : sel.toList()) {
                    if (type.isInstance(element))
                        selectedItems.add(type.cast(element));
                }
            }
        }
        return selectedItems;
    }

    @Override
    public void handle(RteEvent event) {
        switch (event.getTopic()) {
        case CmsisZoneController.ZONE_MODIFIED:
            asyncUpdate();
            return;
        case CmsisZoneController.ZONE_ITEM_SHOW:
            showItem(ITreeObject.castTo(event.getData(), ICpItem.class));
        }
        super.handle(event);
    }

    /**
     * Highlights given item expanding parent nodes if needed
     *
     * @param item Component item to select
     */
    public void showItem(ICpItem item) {
        if (fTreeViewer == null) {
            return;
        }
        if (item == null) {
            return;
        }

        if (item == getSelectedItem()) {
            return;
        }

        Object[] path = item.getEffectiveHierachyPath();
        if (path.length == 0) {
            return;
        }
        TreePath tp = new TreePath(path);
        TreeSelection ts = new TreeSelection(tp);
        fTreeViewer.setSelection(ts, true);
    }

    protected abstract CmsisZoneColumnAdvisor createColumnAdvisor();

    protected CmsisZoneColumnAdvisor getCmsisZoneColumnAdvisor() {
        return (CmsisZoneColumnAdvisor) fColumnAdvisor;
    }

    protected ITreeContentProvider createContentProvider() {
        return new CmsisZoneContentProvider(this);
    }

    @Override
    public void setModelController(CmsisZoneController controller) {
        super.setModelController(controller);
        if (fTreeViewer != null && controller != null) {
            ICpRootZone rootZone = getModelController().getRootZone();
            createColumns();
            fTreeViewer.setInput(rootZone);
            fTreeViewer.expandToLevel(2);
            if (isExpandAllSelectedSupported())
                expandAllSelected();

        }
        update();
    }

    @Override
    public Composite createControl(Composite parent) {

        Tree tree = new Tree(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);
        fTreeViewer = new TreeViewer(tree);
        ColumnViewerToolTipSupport.enableFor(fTreeViewer);
        CmsisZoneColumnAdvisor advisor = createColumnAdvisor();
        fColumnAdvisor = advisor;

        fKeyAdapter = new CmsisZoneKeyAdapter(advisor, tree);

        ITreeContentProvider contentProvider = createContentProvider();
        fTreeViewer.setContentProvider(contentProvider);

        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.verticalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalSpan = 2;
        tree.setLayoutData(gridData);

        hookContextMenu();
        return tree;
    }

    protected void createColumns() {
        CmsisZoneColumnAdvisor advisor = (CmsisZoneColumnAdvisor) getColumnAdvisor();
        advisor.createColumns();
    }

    @Override
    public void refresh() {
        if (fTreeViewer == null || fTreeViewer.getControl() == null || fTreeViewer.getControl().isDisposed())
            return;
        // restore selection if possible
        fTreeViewer.refresh();
    }

    /**
     * Refresh completely the tree viewer.
     */
    @Override
    public void update() {
        if (fTreeViewer == null || fTreeViewer.getControl() == null || fTreeViewer.getControl().isDisposed())
            return;
        CmsisZoneController controller = getModelController();
        if (controller != null) {
            ICpRootZone systemInfo = controller.getRootZone();
            ICpZone zone = getZone();
            if (zone != null) {
                zone = systemInfo.getZone(zone.getName());
                setZone(zone);
            }
            if (fTreeViewer.getInput() != systemInfo) {
                fTreeViewer.setInput(systemInfo);
                fTreeViewer.expandToLevel(2);
            }
        }
        refresh();
    }

    @Override
    protected void expandAllSelected() {
        if (fTreeViewer == null) {
            return;
        }
        if (getModelController() == null) {
            return;
        }
        if (getModelController().getRootZone() == null) {
            return;
        }

        fTreeViewer.getTree().setRedraw(false);
        ISelection prevSel = fTreeViewer.getSelection();
        Collection<ICpMemoryBlock> assignedBlocks = getModelController().getAssignedBlocks(getZone());
        for (ICpMemoryBlock block : assignedBlocks) {
            fTreeViewer.expandToLevel(block, AbstractTreeViewer.ALL_LEVELS);
        }
        fTreeViewer.setSelection(prevSel, true);
        fTreeViewer.getTree().setRedraw(true);
    }

    @Override
    public boolean isExpandAllSelectedSupported() {
        return !isShowList();
    }

}
