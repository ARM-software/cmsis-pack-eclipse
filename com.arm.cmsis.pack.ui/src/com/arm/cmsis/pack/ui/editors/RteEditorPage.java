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

package com.arm.cmsis.pack.ui.editors;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import com.arm.cmsis.pack.events.IRteController;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.ui.widgets.RteWidget;

/**
 * Base abstract class for RTE configuration editor pages
 */
public abstract class RteEditorPage<TController extends IRteController> extends RteWidget<TController> {

    protected RteEditorPageHeader headerWidget = null;
    protected IAction saveAction = null;
    protected RteWidget<TController> contentWidget = null;

    /**
     * Creates page content
     *
     * @param parent parent composite for content
     */
    public void createPageContent(Composite parent) {
        getContentWidget().createControl(parent);
    };

    @Override
    public void destroy() {
        if (contentWidget != null) {
            contentWidget.destroy();
            contentWidget = null;
        }
        super.destroy();
    }

    /**
     * Creates content widget
     */
    abstract protected RteWidget<TController> createContentWidget();

    /**
     * Returns page help ID
     *
     * @return help ID
     */
    abstract protected String getHelpID();

    /**
     * Returns page image
     *
     * @return page Image
     */
    abstract protected Image getImage();

    /**
     * Returns page label
     *
     * @return page label string
     */
    abstract protected String getLabel();

    /**
     * Check if the page has been modified from last saved state
     *
     * @return true if modified
     */
    abstract public boolean isModified();

    /**
     * Creates page content
     *
     * @param parent parent composite for content
     */
    protected RteWidget<TController> getContentWidget() {
        if (contentWidget == null) {
            contentWidget = createContentWidget();
        }
        return contentWidget;
    }

    @Override
    public Composite createControl(Composite parent) {
        Composite pageComposite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        gridLayout.marginHeight = 0;
        gridLayout.marginTop = 0;
        pageComposite.setLayout(gridLayout);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        pageComposite.setLayoutData(gd);

        headerWidget = new RteEditorPageHeader(pageComposite, SWT.NONE);
        headerWidget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        createPageContent(pageComposite);
        setupHeader();
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getFocusWidget(), getHelpID());
        update();
        return pageComposite;
    }

    @Override
    public void handle(RteEvent event) {
        switch (event.getTopic()) {
        case RteEvent.CONFIGURATION_COMMITED:
        case RteEvent.CONFIGURATION_MODIFIED:
            asyncUpdate();
            return;
        default:
            break;
        }
        updateSaveAction(); // update on every event
    }

    /**
     * Creates action elements in the header and sets the header label
     */
    protected void setupHeader() {
        headerWidget.setLabel(getLabel(), getImage());
        createActions();
        headerWidget.addHelpAction();
        headerWidget.setFocusWidget(getFocusWidget());
        updateActions();
    }

    protected void createActions() {
        saveAction = headerWidget.addSaveAction();
    }

    public void updateActions() {
        updateSaveAction();
    }

    protected void updateSaveAction() {
        if (saveAction != null && getModelController() != null) {
            saveAction.setEnabled(!getModelController().isReadOnly() && getModelController().isModified());
        }
    }

    @Override
    public void update() {
        if (headerWidget != null && getModelController() != null) {
            headerWidget.setModified(isModified());
            updateActions();
        }
        // refresh(); // TODO : ensure it is redundant
    }

    @Override
    public void refresh() {
        RteWidget<TController> w = getContentWidget();
        if (w != null)
            getContentWidget().refresh();
    }

    @Override
    public void setModelController(TController modelController) {
        super.setModelController(modelController);
        getContentWidget().setModelController(modelController);
        update();
    }

    @Override
    public Composite getFocusWidget() {
        return getContentWidget().getFocusWidget();
    }

}
