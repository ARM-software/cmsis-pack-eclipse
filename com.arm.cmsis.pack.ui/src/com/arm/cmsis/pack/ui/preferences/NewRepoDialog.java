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

package com.arm.cmsis.pack.ui.preferences;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.repository.CpRepositoryList;
import com.arm.cmsis.pack.ui.CpStringsUI;

/**
 * The new repository dialog
 */
public class NewRepoDialog extends Dialog {
	private boolean fIsEdit;
	private String[] fData;

	// Directly refer the types defined in Repos.
	private String[] fTypeSelections = CpRepositoryList.TYPES;

	Combo fTypeCombo;
	Text fNameText;
	Text fUrlText;

	String fReturnType;
	String fReturnName;
	String fReturnUrl;

	protected NewRepoDialog(Shell parentShell, String[] data) {

		super(parentShell);

		fIsEdit = (data != null);
		fData = data;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void configureShell(Shell shell) {

		super.configureShell(shell);

		String title;
		if (fIsEdit) {
			title = CpStringsUI.NewRepoDialog_EditPackRepoTitle;
		} else {
			title = CpStringsUI.NewRepoDialog_NewPackRepoTitle;
		}

		if (title != null) {
			shell.setText(title);
		}
	}

	protected String[] getData() {
		return new String[] { fReturnType, fReturnName, fReturnUrl };
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		Composite comp = new Composite(parent, SWT.NULL);
		comp.setFont(parent.getFont());

		GridLayout layout;
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginTop = 10;
		layout.marginBottom = 10;
		layout.marginWidth = 10;
		comp.setLayout(layout);

		GridData layoutData = new GridData();
		layoutData.widthHint = 400;
		layoutData.verticalAlignment = SWT.FILL;
		layoutData.horizontalAlignment = SWT.FILL;
		layoutData.grabExcessHorizontalSpace = true;
		comp.setLayoutData(layoutData);

		{
			Label typeLabel = new Label(comp, SWT.LEFT);
			// typeLabel.setFont(comp.getFont());
			typeLabel.setText(CpStringsUI.NewRepoDialog_TypeLabel);
			layoutData = new GridData();
			typeLabel.setLayoutData(layoutData);

			fTypeCombo = new Combo(comp, SWT.READ_ONLY);
			fTypeCombo.setItems(fTypeSelections);

			int ix = 0;
			if (fIsEdit) {
				for (int i = 0; i < fTypeSelections.length; ++i) {
					if (fData[0].equals(fTypeSelections[i])) {
						ix = i;
						break;
					}
				}
			}
			fTypeCombo.select(ix);

			fReturnType = fTypeSelections[ix];

			layoutData = new GridData();
			fTypeCombo.setLayoutData(layoutData);
		}

		{
			Label nameLabel = new Label(comp, SWT.LEFT);
			// typeLabel.setFont(comp.getFont());
			nameLabel.setText(CpStringsUI.NewRepoDialog_NameLabel);
			layoutData = new GridData();
			nameLabel.setLayoutData(layoutData);

			fNameText = new Text(comp, SWT.SINGLE | SWT.BORDER);
			if (fIsEdit) {
				fNameText.setText(fData[1]);
			} else {
				fNameText.setText(CmsisConstants.EMPTY_STRING);
			}

			fReturnName = fNameText.getText();

			layoutData = new GridData();
			layoutData.horizontalAlignment = SWT.FILL;
			layoutData.grabExcessHorizontalSpace = true;
			fNameText.setLayoutData(layoutData);
		}

		{
			Label urlLabel = new Label(comp, SWT.LEFT);
			// typeLabel.setFont(comp.getFont());
			urlLabel.setText(CpStringsUI.NewRepoDialog_UrlLabel);
			layoutData = new GridData();
			urlLabel.setLayoutData(layoutData);

			fUrlText = new Text(comp, SWT.SINGLE | SWT.BORDER);
			if (fIsEdit) {
				fUrlText.setText(fData[2]);
			} else {
				fUrlText.setText(CmsisConstants.EMPTY_STRING);
			}

			fReturnUrl = fUrlText.getText();

			layoutData = new GridData();
			layoutData.horizontalAlignment = SWT.FILL;
			layoutData.grabExcessHorizontalSpace = true;
			fUrlText.setLayoutData(layoutData);
		}

		fTypeCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fReturnType = fTypeCombo.getText();
			}
		});

		fNameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				fReturnName = fNameText.getText();
			}
		});

		fUrlText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				fReturnUrl = fUrlText.getText();
			}
		});

		return comp;
	}
}
