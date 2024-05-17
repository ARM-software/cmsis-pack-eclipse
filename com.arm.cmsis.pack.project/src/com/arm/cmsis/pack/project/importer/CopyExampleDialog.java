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

package com.arm.cmsis.pack.project.importer;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.project.Messages;
import com.arm.cmsis.pack.ui.ColorConstants;

/**
 * The dialog to show before copying an example from Pack Manager
 */
public class CopyExampleDialog extends Dialog {

    private final String fExampleName;
    private final String fPackName;
    private final String fWorkspace;
    private String fProjectName;
    private final boolean fShowAttention;
    Button fCopyButton;
    Text fTxtProjectName;
    Label fLblAttention;
    Label fLblWorkspaceName;

    /**
     * Default constructor
     * 
     * @param parentShell   the parent shell for the dialog
     * @param exampleName   example's name
     * @param packName      name of the pack that the example belongs to
     * @param workspace     workspace name
     * @param projectName   project name
     * @param showAttention true if the example already exists in the workspace
     */
    public CopyExampleDialog(Shell parentShell, String exampleName, String packName, String workspace,
            String projectName, boolean showAttention) {
        super(parentShell);
        fExampleName = exampleName;
        fPackName = packName;
        fWorkspace = workspace;
        fProjectName = projectName;
        fShowAttention = showAttention;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.CopyExampleDialog_CopyExampleTitle);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Example's name
        Label lblExample = new Label(composite, SWT.NONE);
        lblExample.setText(Messages.CopyExampleDialog_Example);
        Label lblExampleName = new Label(composite, SWT.NONE);
        lblExampleName.setText(fExampleName);

        // Pack's name
        Label lblPack = new Label(composite, SWT.NONE);
        lblPack.setText(Messages.CopyExampleDialog_Pack);
        Label lblPackName = new Label(composite, SWT.NONE);
        lblPackName.setText(fPackName);

        // Project's name
        Label lblProject = new Label(composite, SWT.NONE);
        lblProject.setText(Messages.CopyExampleDialog_ProjectName);
        fTxtProjectName = new Text(composite, SWT.BORDER | SWT.SINGLE);
        fTxtProjectName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fTxtProjectName.setText(fProjectName);
        fTxtProjectName.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                projectNameChanged();
            }
        });

        // Workspace name
        Label lblWorkspace = new Label(composite, SWT.NONE);
        lblWorkspace.setText(Messages.CopyExampleDialog_ProjectLocation);

        GridData gridDataLblWorkspacesName = new GridData();
        gridDataLblWorkspacesName.horizontalAlignment = GridData.FILL;
        gridDataLblWorkspacesName.grabExcessHorizontalSpace = true;

        fLblWorkspaceName = new Label(composite, SWT.NONE);
        fLblWorkspaceName.setText(fWorkspace);
        fLblWorkspaceName.setLayoutData(gridDataLblWorkspacesName);

        Label dummy = new Label(composite, SWT.NONE);
        dummy.setText("\t\t"); //$NON-NLS-1$

        // Validation message
        GridData gridDataLblAttention = new GridData();
        gridDataLblAttention.horizontalAlignment = GridData.FILL;
        gridDataLblAttention.grabExcessHorizontalSpace = true;

        fLblAttention = new Label(composite, SWT.NONE);
        fLblAttention.setForeground(ColorConstants.RED);
        fLblAttention.setLayoutData(gridDataLblAttention);

        if (fShowAttention)
            fLblAttention.setText(Messages.CopyExampleDialog_AttentionMessage);

        return super.createDialogArea(parent);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        fCopyButton = createButton(parent, IDialogConstants.OK_ID, "Copy", true); //$NON-NLS-1$
        if (fShowAttention) {
            fCopyButton.setEnabled(false);
        }
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected void okPressed() {
        fProjectName = fTxtProjectName.getText();
        super.okPressed();
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    /**
     * Handles 'fTxtProjectName' Text events
     */
    void projectNameChanged() {
        String validateProjectName = null;

        // Update workspace path
        String wsPath = getWorkspacePath(fTxtProjectName.getText());
        if (wsPath == null)
            fLblWorkspaceName.setText(CmsisConstants.EMPTY_STRING);
        else
            fLblWorkspaceName.setText(wsPath);

        // Validate project name
        validateProjectName = validate();
        if (validateProjectName.isEmpty()) {
            fLblAttention.setText(CmsisConstants.EMPTY_STRING);
            fCopyButton.setEnabled(true);

        } else {
            fLblAttention.setText(validateProjectName);
            fCopyButton.setEnabled(false);
        }

    }

    /**
     * Gets workspace's path
     * 
     * @param projectName project name
     */
    public String getWorkspacePath(String projectName) {
        String wsPath = null;
        File destFile = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(projectName).toFile();

        if (destFile != null)
            wsPath = destFile.toString();
        return wsPath;
    }

    /**
     * Calls validation's project name methods
     */
    public String validate() {
        String result = CmsisConstants.EMPTY_STRING;

        // Validate project's name
        String validateProjectName = validateProjectName(fTxtProjectName.getText());
        if (!validateProjectName.isEmpty())
            return validateProjectName;

        // Validate project existence in workspace
        String validateProjectExistence = validateProjectExistence();
        if (!validateProjectExistence.isEmpty())
            return validateProjectExistence;

        return result;
    }

    /**
     * Validates project's name
     * 
     * @param projectName project name
     */
    private String validateProjectName(String projectName) {
        IStatus status = ResourcesPlugin.getWorkspace().validateName(projectName, IResource.PROJECT);
        if (!status.isOK())
            return status.getMessage();

        return CmsisConstants.EMPTY_STRING;
    }

    /**
     * Validates project's existence in workspace
     */
    private String validateProjectExistence() {
        // Validate project's existence in workspace
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(fTxtProjectName.getText());
        if (project.exists()) {
            return Messages.CopyExampleDialog_ValidateProjectExistence;
        }
        return CmsisConstants.EMPTY_STRING;
    }

    public String getProjectName() {
        return fProjectName;
    }

    public void setProjectName(String projectName) {
        this.fProjectName = projectName;
    }

}
