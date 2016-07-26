/*******************************************************************************
 * Copyright (c) 2016 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/

package com.arm.cmsis.pack.installer;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 *
 */
public class CopyExampleDialog extends Dialog {

	private final String fExampleName;
	private final String fPackName;
	private final String fWorkspace;
	private final String fProjectName;
	private final boolean fShowAttention;

	Button fCopyButton;

	/**
	 * @param parentShell
	 */
	public CopyExampleDialog(Shell parentShell, String exampleName,
			String packName, String workspace, String projectName,
			boolean showAttention) {
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

		Label lblExample = new Label(composite, SWT.NONE);
		lblExample.setText(Messages.CopyExampleDialog_Example);
		Label lblExampleName = new Label(composite, SWT.NONE);
		lblExampleName.setText(fExampleName);

		Label lblPack = new Label(composite, SWT.NONE);
		lblPack.setText(Messages.CopyExampleDialog_Pack);
		Label lblPackName = new Label(composite, SWT.NONE);
		lblPackName.setText(fPackName);

		Label lblProject = new Label(composite, SWT.NONE);
		lblProject.setText(Messages.CopyExampleDialog_ProjectName);
		Label lblProjectName = new Label(composite, SWT.NONE);
		lblProjectName.setText(fProjectName);

		Label lblWorkspace = new Label(composite, SWT.NONE);
		lblWorkspace.setText(Messages.CopyExampleDialog_ProjectLocation);
		Label lblWorkspaceName = new Label(composite, SWT.NONE);
		lblWorkspaceName.setText(fWorkspace);

		if (fShowAttention) {
			Label dummy = new Label(composite, SWT.NONE);
			dummy.setText("\t\t"); //$NON-NLS-1$

			Label lblAttention = new Label(composite, SWT.NONE);
			lblAttention.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
			lblAttention.setText(Messages.CopyExampleDialog_AttentionMessage);
			GridData gd1 = new GridData();
			gd1.horizontalSpan = 2;
			gd1.grabExcessHorizontalSpace = true;
			lblAttention.setLayoutData(gd1);

			Button btnOverwrite = new Button(composite, SWT.CHECK);
			btnOverwrite.setText(NLS.bind(Messages.CopyExampleDialog_ReplaceExistingProject, fProjectName));
			GridData gd2 = new GridData();
			gd2.horizontalSpan = 2;
			gd2.grabExcessHorizontalSpace = true;
			btnOverwrite.setLayoutData(gd2);
			btnOverwrite.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					Button button = ((Button) event.widget);
					fCopyButton.setEnabled(button.getSelection());
				}
			});
		}

		return super.createDialogArea(parent);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		fCopyButton = createButton(parent, IDialogConstants.OK_ID, "Copy", true); //$NON-NLS-1$
		if (fShowAttention) {
			fCopyButton.setEnabled(false);
		}
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

}
