/*******************************************************************************
* Copyright (c) 2022 ARM Ltd. and others
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

package com.arm.cmsis.pack.installer.jobs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
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
    private Button fOkButton;

    /**
     * Constructor for License Dialog
     *
     * @param packName     the pack's name
     * @param parentShell  parent shell
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
        Composite container = (Composite) super.createDialogArea(parent);
        createControls(container);
        return container;
    }

    private void createControls(Composite container) {
        createLabels(container);
        createText(container);
        createCheckBoxButton(container);
    }

    private void createLabels(Composite container) {
        Label licenseAgreement = new Label(container, SWT.NONE);
        Font boldFont = JFaceResources.getFontRegistry().getBold(JFaceResources.TEXT_FONT);
        licenseAgreement.setFont(boldFont);
        licenseAgreement
                .setText(Messages.LicenseDialog_LicenseAgreement + System.lineSeparator() + System.lineSeparator());
        Label guidanceText = new Label(container, SWT.BOLD);
        guidanceText.setText(Messages.LicenseDialog_GuidanceText);
    }

    private void createText(Composite container) {
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        layoutData.widthHint = 500;
        layoutData.heightHint = 250;
        Text fText = new Text(container, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        fText.setLayoutData(layoutData);
        fText.setText(fLicencseText);
    }

    private void createCheckBoxButton(Composite container) {
        Button fCheckAgreed = new Button(container, SWT.CHECK);
        fCheckAgreed.setText(Messages.LicenseDialog_AgreeText);
        fCheckAgreed.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                Button button = ((Button) event.widget);
                fOkButton.setEnabled(button.getSelection());
            }
        });
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        fOkButton = getButton(IDialogConstants.OK_ID);
        fOkButton.setEnabled(false);
    }

}
