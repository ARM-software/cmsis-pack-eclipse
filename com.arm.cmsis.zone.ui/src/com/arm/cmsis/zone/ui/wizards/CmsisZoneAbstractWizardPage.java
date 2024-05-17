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
package com.arm.cmsis.zone.ui.wizards;

import org.eclipse.jface.wizard.WizardPage;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.enums.ECoreArchitecture;
import com.arm.cmsis.zone.data.ICpRootZone;
import com.arm.cmsis.zone.ui.editors.CmsisZoneController;

public abstract class CmsisZoneAbstractWizardPage<T extends ICpItem> extends WizardPage {
    protected CmsisZoneController fController = null;
    protected ICpRootZone fRootZone = null;
    protected T fExistingItem = null;

    protected ECoreArchitecture fArchitecture = ECoreArchitecture.UNKNOWN;
    protected boolean fMpuMode = false;

    public CmsisZoneAbstractWizardPage(String pageName) {
        super(pageName);
    }

    public CmsisZoneAbstractWizardPage(String pageName, CmsisZoneController controller) {
        this(pageName, controller, null);
    }

    /**
     * Create the wizard page
     */
    public CmsisZoneAbstractWizardPage(String pageName, CmsisZoneController controller, T existingItem) {
        super(pageName);
        fController = controller;
        fRootZone = fController.getRootZone();
        fExistingItem = existingItem;
        if (fRootZone != null) {
            fMpuMode = CmsisConstants.MPU.equals(fRootZone.getZoneMode());
            fArchitecture = fRootZone.getArchitecture();
        }

    }

    public CmsisZoneController getController() {
        return fController;
    }

    public void setfController(CmsisZoneController controller) {
        fController = controller;
        if (fController != null) {
            fRootZone = fController.getRootZone();
            fMpuMode = fRootZone.getZoneMode() == CmsisConstants.MPU;
            fArchitecture = fRootZone.getArchitecture();
        } else {
            fRootZone = null;
        }
    }

    public ICpRootZone getRootZone() {
        return fRootZone;
    }

    public T getExistingItem() {
        return fExistingItem;
    }

    public void setfExistingItem(T existingItem) {
        fExistingItem = existingItem;
    }

    public abstract boolean apply();
}
