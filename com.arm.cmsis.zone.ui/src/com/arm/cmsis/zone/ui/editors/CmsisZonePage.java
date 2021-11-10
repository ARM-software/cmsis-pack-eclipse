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

package com.arm.cmsis.zone.ui.editors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.editors.RteEditorPage;
import com.arm.cmsis.zone.data.ICpZone;
import com.arm.cmsis.zone.ui.IZoneHelpContextIds;
import com.arm.cmsis.zone.ui.Messages;
import com.arm.cmsis.zone.ui.handlers.CmsisZoneGenerateAction;
import com.arm.cmsis.zone.ui.wizards.CmsisZoneWizard;

/**
 * Base editor page for CMSIS-Zone
 *
 */
public abstract class CmsisZonePage extends RteEditorPage<CmsisZoneController> {

    protected IAction showListAction = null;
    protected IAction showTreeAction = null;
    protected IAction arrangeBlocksAction = null;
    protected IAction addZoneAction = null;
    protected IAction editZoneAction = null;
    protected IAction deleteZoneAction = null;
    protected IAction generateAction = null;
    protected IAction checkAction = null;

    protected String fPageName;

    public CmsisZonePage(String pageName) {
        fPageName = pageName;
    }

    public String getZoneLabel() {
        return fPageName;
    }

    public String getPageName() {
        return fPageName;
    }

    public void setPageName(String pageName) {
        fPageName = pageName;
        if (headerWidget != null) {
            headerWidget.setLabelText(getLabel());
        }
    }

    public ICpZone getZone() {
        return null;
    }

    public boolean isSingleZonePage() {
        return false;
    }

    @Override
    public boolean isModified() {
        CmsisZoneController controller = getModelController();
        if (controller == null)
            return false;
        return controller.isModified();
    }

    @Override
    protected String getHelpID() {
        return IZoneHelpContextIds.ZONE_PAGE;
    }

    @Override
    protected Image getImage() {
        return CpPlugInUI.getImage(CpPlugInUI.ICON_MEMORY_MAP);
    }

    @Override
    protected String getLabel() {
        return fPageName;
    }

    @Override
    protected void createActions() {
        createShowActions();
//		createMemoryBlockActions();
        createZoneActions();
        super.createActions();
    }

    protected void createZoneActions() {
        addZoneAction = new Action(null, IAction.AS_PUSH_BUTTON) {
            @Override
            public void run() {
                CmsisZoneWizard wizard = new CmsisZoneWizard(getModelController(), null);
                wizard.execute(getFocusWidget().getShell());
            }
        };
        ImageDescriptor icon = CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_ADD);
        addZoneAction.setToolTipText(Messages.CmsisZonePage_AddNewZone);
        addZoneAction.setImageDescriptor(icon);
        addZoneAction.setDisabledImageDescriptor(icon);
        headerWidget.addAction(addZoneAction, SWT.LEFT, false);

        if (isSingleZonePage()) {
            createSingleZoneActions();
        }

        // add check to all pages
        checkAction = new Action(Messages.CmsisZonePage_Check, IAction.AS_PUSH_BUTTON) {
            @Override
            public void run() {
                check();
            }

        };
        icon = CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_RESOLVE_CHECK);
        checkAction.setToolTipText(Messages.CmsisZonePage_CheckResourcesPartitionArrangements);
        checkAction.setImageDescriptor(icon);
        checkAction.setDisabledImageDescriptor(icon);
        headerWidget.addAction(checkAction, SWT.LEFT, true);

        // add generate to all pages
        generateAction = new CmsisZoneGenerateAction();
        headerWidget.addAction(generateAction, SWT.LEFT, true);
    }

    protected void createSingleZoneActions() {
        editZoneAction = new Action(null, IAction.AS_PUSH_BUTTON) {
            @Override
            public void run() {
                ICpZone zone = getZone();
                CmsisZoneWizard wizard = new CmsisZoneWizard(getModelController(), zone);
                wizard.execute(getFocusWidget().getShell());
            }
        };
        ImageDescriptor icon = CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_PROPERTIES);

        String toolTip = Messages.CmsisZonePage_Edit + getZoneLabel() + Messages.CmsisZonePage_ZoneProperties;
        editZoneAction.setToolTipText(toolTip);
        editZoneAction.setImageDescriptor(icon);
        editZoneAction.setDisabledImageDescriptor(icon);

        headerWidget.addAction(editZoneAction, SWT.LEFT, false);

        String text = Messages.CmsisZonePage_Delete + getZoneLabel() + Messages.CmsisZonePage_Zone;
        deleteZoneAction = new Action(text, IAction.AS_PUSH_BUTTON) {
            @Override
            public void run() {
                ICpZone zone = getZone();
                if (zone == null)
                    return;
                String msg = text + Messages.CmsisZonePage_QuestionSymbol;
                boolean yes = MessageDialog.openQuestion(getFocusWidget().getShell(), msg, msg);
                if (yes) {
                    // run async as we will remove this page
                    Display.getDefault().asyncExec(() -> getModelController().deleteZone(zone));
                }
            }
        };
        icon = CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_DELETE);
        deleteZoneAction.setToolTipText(text);
        deleteZoneAction.setImageDescriptor(icon);
        deleteZoneAction.setDisabledImageDescriptor(icon);
        headerWidget.addAction(deleteZoneAction, SWT.RIGHT, false);
    }

    protected void createShowActions() {
        showTreeAction = createShowAction(false);
        showListAction = createShowAction(true);
    }

    protected void createMemoryBlockActions() {
        arrangeBlocksAction = new Action() {
            @Override
            public void run() {
                arrangeBlocks();
            }
        };
        arrangeBlocksAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_ARRANGE));
        arrangeBlocksAction.setText(Messages.CmsisZonePage_ArrangeMemory);
        arrangeBlocksAction.setToolTipText(Messages.CmsisZonePage_ArrangeMemoryRegions);
        headerWidget.addAction(arrangeBlocksAction, SWT.LEFT, false);
    }

    protected void check() {
        getModelController().check(true);
    }

    protected void arrangeBlocks() {
        getModelController().arrangeBlocks();
    }

    protected IAction createShowAction(final boolean bList) {
        IAction action = new Action(null, IAction.AS_CHECK_BOX) {
            @Override
            public void run() {
                boolean checked = isChecked();
                setShowList(bList ? checked : !checked);
            }
        };
        String toolTip;
        ImageDescriptor icon;
        if (bList) {
            toolTip = Messages.CmsisZonePage_ShowResourcesAsFlatList;
            icon = CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_LIST);
        } else {
            toolTip = Messages.CmsisZonePage_ShowResourcesAsTree;
            icon = CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_TREE);
        }

        action.setToolTipText(toolTip);
        action.setImageDescriptor(icon);
        action.setDisabledImageDescriptor(icon);
        headerWidget.addAction(action, SWT.LEFT, false);
        return action;
    }

    protected void setShowList(boolean bShowList) {
        if (isShowList() != bShowList) {
            getContentWidget().setAttribute(CmsisConstants.LIST, bShowList);
            update();
            refresh();
        } else {
            updateShowActions();
        }
    }

    @Override
    public void updateActions() {
        updateShowActions();
        super.updateActions();
    }

    protected void updateShowActions() {
        boolean bShowList = isShowList();
        if (showListAction != null) {
            showListAction.setEnabled(!bShowList);
            showListAction.setChecked(bShowList);
        }
        if (showTreeAction != null) {
            showTreeAction.setEnabled(bShowList);
            showTreeAction.setChecked(!bShowList);
        }
    }

    protected boolean isShowList() {
        return getContentWidget().getAttributeAsBoolean(CmsisConstants.LIST, false);
    }

}
