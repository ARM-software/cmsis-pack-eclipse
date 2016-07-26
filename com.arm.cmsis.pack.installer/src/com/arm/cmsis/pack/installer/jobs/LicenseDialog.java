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

package com.arm.cmsis.pack.installer.jobs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.arm.cmsis.pack.installer.Messages;

/**
 * License Dialog
 */
public class LicenseDialog extends Dialog {

	private String fPackName;
	private String fLicencseText;
	private Text fText;
	private Button fCheckAgreed;
	Button okButton;

	/**
	 * Constructor for License Dialog
	 * @param packName the pack's name
	 * @param parentShell parent shell
	 * @param licencseText license text
	 */
	public LicenseDialog(Shell parentShell, String packName, String licencseText) {
		super(parentShell);
		fPackName = packName;
		fLicencseText = licencseText;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(NLS.bind(Messages.LicenseDialog_LicenseDialogTitle, fPackName));
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		GridData layoutData = new GridData();
		layoutData.widthHint = 510;
		layoutData.verticalAlignment = SWT.FILL;
		layoutData.horizontalAlignment = SWT.FILL;
		layoutData.grabExcessHorizontalSpace = true;
		composite.setLayoutData(layoutData);

		Label licenseAgreement = new Label(composite, SWT.NONE);
		FontDescriptor boldDescriptor = FontDescriptor.createFrom(licenseAgreement.getFont()).setStyle(SWT.BOLD);
		Font boldFont = boldDescriptor.createFont(licenseAgreement.getDisplay());
		licenseAgreement.setFont(boldFont);
		licenseAgreement.setText(Messages.LicenseDialog_LicenseAgreement + System.lineSeparator() + System.lineSeparator());

		Label guidanceText = new Label(composite, SWT.BOLD);
		guidanceText.setText(Messages.LicenseDialog_GuidanceText);

		fText = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		layoutData = new GridData();
		layoutData.widthHint = 500;
		layoutData.heightHint = 250;
		layoutData.verticalAlignment = SWT.FILL;
		layoutData.horizontalAlignment = SWT.FILL;
		layoutData.grabExcessHorizontalSpace = true;
		fText.setLayoutData(layoutData);
		fText.setText(fLicencseText);

		fCheckAgreed = new Button(composite, SWT.CHECK);
		fCheckAgreed.setText(Messages.LicenseDialog_AgreeText);
		fCheckAgreed.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				Button button = ((Button) event.widget);
				okButton.setEnabled(button.getSelection());
			}
		});

		return composite;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		okButton = getButton(IDialogConstants.OK_ID);
		okButton.setEnabled(false);
	}

}
