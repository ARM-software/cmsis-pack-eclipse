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

package com.arm.cmsis.pack.ui.wizards;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * A class that represent a one-page wizard dialog with OK button instead of
 * Finish
 */
public class OkWizardDialog extends WizardDialog {

    /**
     * Constructs wizard dialog
     *
     * @param parentShell parent Shell
     * @param wizard      one-page wizard
     */
    public OkWizardDialog(Shell parentShell, IWizard wizard) {
        super(parentShell, wizard);
    }

    @Override
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        Button finishButton = getButton(IDialogConstants.FINISH_ID);
        finishButton.setText(IDialogConstants.OK_LABEL);
        return contents;
    }

}
