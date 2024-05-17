/*******************************************************************************
* Copyright (c) 2023 ARM Ltd. and others
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

package com.arm.cmsis.pack.project.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.project.Messages;
import com.arm.cmsis.pack.project.csolution.CsolutionProjectImporter;

/**
 *
 */
public class CsolutionImportWizard extends Wizard implements IImportWizard {

    private CsolutionProjectImporter projectImporter;
    private CsolutionImportWizardPage csolutionImporterMainPage;
    private String pageTitle;

    public CsolutionImportWizard() {
        this(null, Messages.CsolutionProjectImporter_ProjectTitle);
    }

    /**
     * Create Csolution Import Wizard
     *
     * @param projectImporter
     * @param pageTitle
     */
    public CsolutionImportWizard(CsolutionProjectImporter projectImporter, String pageTitle) {
        super();
        this.pageTitle = pageTitle;
        setNeedsProgressMonitor(true);
        this.projectImporter = projectImporter;
        if (this.projectImporter == null) {
            this.projectImporter = new CsolutionProjectImporter();
        }
    }

    @Override
    public String getWindowTitle() {
        return Messages.CsolutionProjectImporter_Window;
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        //
    }

    @Override
    public boolean performFinish() {
        return projectImporter.importProject();
    }

    /**
     * Adding main page to the wizard.
     */
    @Override
    public void addPages() {
        csolutionImporterMainPage = new CsolutionImportWizardPage(projectImporter, CmsisConstants.IMPORT_TITLE,
                pageTitle);
        addPage(csolutionImporterMainPage);
        pageTitle = Messages.CsolutionProjectImporter_ProjectTitle;

    }
}
