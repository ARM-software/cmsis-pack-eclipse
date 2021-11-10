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

package com.arm.cmsis.zone.ui.wizards;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

import com.arm.cmsis.pack.ui.CpPlugInUI;

/**
 *
 */
public class CmsisZoneProjectMainPage extends WizardNewProjectCreationPage {

    /**
     * @param pageName
     */
    public CmsisZoneProjectMainPage(String pageName) {
        super(pageName);
        setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_CMSIS_ZONE_48));
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        createWorkingSetGroup((Composite) getControl(), null, new String[] { "org.eclipse.ui.resourceWorkingSetPage", //$NON-NLS-1$
                "org.eclipse.cdt.ui.CElementWorkingSetPage" }); //$NON-NLS-1$
    }

}
