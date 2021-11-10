/*******************************************************************************
* Copyright (c) 2021 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* IBM Corporation - initial API and implementation
* Anton Leherbauer (Wind River Systems)
* ARM Ltd and ARM Germany GmbH - application-specific implementation
*******************************************************************************/

package com.arm.cmsis.pack.project.wizards;

import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionStatusDialog;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.project.CpProjectPlugIn;
import com.arm.cmsis.pack.project.IRteProject;
import com.arm.cmsis.pack.project.Messages;

/**
 * Dialog for project selection
 */
public class ProjectSelectionDialog extends SelectionStatusDialog {
    private TableViewer fTableViewer;
    Set<IRteProject> fProjectsWithSpecifics;

    // sizing constants
    private static final int SIZING_SELECTION_WIDGET_HEIGHT = 250;
    private static final int SIZING_SELECTION_WIDGET_WIDTH = 300;

    public ProjectSelectionDialog(Shell parentShell, Set<IRteProject> projectsWithSpecifics) {
        super(parentShell);
        setTitle(Messages.ProjectSelectionDialog_RteProjectSelectionDialog);
        fProjectsWithSpecifics = projectsWithSpecifics;
        if (fProjectsWithSpecifics == null || fProjectsWithSpecifics.isEmpty()) {
            setMessage(Messages.ProjectSelectionDialog_NoRteProjectFound);
        } else {
            setMessage(Messages.ProjectSelectionDialog_SelectRteProject);
        }
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        // page group
        Composite composite = (Composite) super.createDialogArea(parent);

        createMessageArea(composite);

        fTableViewer = new TableViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        fTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                doSelectionChanged(((IStructuredSelection) event.getSelection()).toArray());
            }
        });
        fTableViewer.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                okPressed();
            }
        });
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
        data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
        fTableViewer.getTable().setLayoutData(data);

        fTableViewer.setContentProvider(ArrayContentProvider.getInstance());
        fTableViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                IRteProject project = (IRteProject) element;
                if (project != null) {
                    return project.getName();
                }
                return null;
            }

            @Override
            public Image getImage(Object element) {
                IRteProject project = (IRteProject) element;
                if (project != null) {
                    return PlatformUI.getWorkbench().getSharedImages()
                            .getImage(org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT);
                }
                return null;
            }
        });

        fTableViewer.setInput(fProjectsWithSpecifics);
        doSelectionChanged(new Object[0]);
        return composite;
    }

    void doSelectionChanged(Object[] objects) {
        if (objects.length != 1) {
            updateStatus(new Status(IStatus.ERROR, CpProjectPlugIn.PLUGIN_ID, CmsisConstants.EMPTY_STRING));
            setSelectionResult(null);
        } else {
            updateStatus(new Status(IStatus.OK, CpProjectPlugIn.PLUGIN_ID, CmsisConstants.EMPTY_STRING));
            setSelectionResult(objects);
        }
    }

    @Override
    protected void computeResult() {
        // does nothing
    }

}
