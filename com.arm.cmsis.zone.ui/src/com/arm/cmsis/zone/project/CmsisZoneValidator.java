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

package com.arm.cmsis.zone.project;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.PlatformUI;

import com.arm.cmsis.pack.enums.ESeverity;
import com.arm.cmsis.pack.error.CmsisErrorCollection;
import com.arm.cmsis.pack.error.ICmsisErrorCollection;
import com.arm.cmsis.pack.item.ICmsisVisitor.VisitResult;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.zone.data.ICpMemoryBlock;
import com.arm.cmsis.zone.data.ICpMpuRegion;
import com.arm.cmsis.zone.data.ICpRootZone;
import com.arm.cmsis.zone.data.ICpZone;
import com.arm.cmsis.zone.data.ICpZoneItem;
import com.arm.cmsis.zone.error.CmsisZoneError;
import com.arm.cmsis.zone.ui.CpZonePluginUI;

/**
 * A class to validate CMSIS-Zone files/models
 */
public class CmsisZoneValidator extends CmsisErrorCollection implements ICmsisZoneValidator {

    @Override
    public boolean validate(ICpRootZone rootZone) {
        clearErrors();
        if (rootZone == null)
            return false;

        collectErrors(rootZone);
        updateMarkers(rootZone);

        return getSevereErrorCount() == 0;
    }

    protected void collectErrors(ICpRootZone rootZone) {

        // update startup region warnings
        checkStartupRegions(rootZone);
        // collect already existing errors discovered during parsing and initialization
        rootZone.accept((item) -> {
            if (item instanceof ICpZoneItem) {
                ICpZoneItem zItem = (ICpZoneItem) item;
                addErrors(zItem);
                if (zItem instanceof ICpZone && rootZone.isZoneModeMPU()) {
                    ICpZone zone = (ICpZone) zItem;
                    for (ICpMpuRegion mpuRegion : zone.getMpuSetup().getMpuRegions()) {
                        addErrors(mpuRegion);
                    }
                }
            }
            return VisitResult.CONTINUE;
        });
    }

    @Override
    public void addErrors(ICmsisErrorCollection errors) {
        if (errors != null) {
            addErrors(errors.getErrors());
        }
    }

    protected void checkStartupRegions(ICpRootZone rootZone) {
        // clear existing errors
        rootZone.clearErrors(CmsisZoneError.Z12_MASK);
        Collection<ICpMemoryBlock> allRegions = rootZone.getResources().getStarupMemoryRegions();
        for (ICpMemoryBlock r : allRegions) {
            r.clearErrors(CmsisZoneError.Z12_MASK);
        }
        if (rootZone.isZoneModeMPU()) {
            // check if startup defined more than once
            Collection<ICpMemoryBlock> startupRegions = rootZone.getResources().getStarupMemoryRegions();
            int nStartupRegionCount = startupRegions.size();
            if (nStartupRegionCount <= 0) {
                rootZone.addError(new CmsisZoneError(ESeverity.Warning, CmsisZoneError.Z120));
            } else if (nStartupRegionCount > 1) {
                rootZone.addError(new CmsisZoneError(ESeverity.Warning, CmsisZoneError.Z121));
                for (ICpMemoryBlock r : startupRegions) {
                    r.addError(new CmsisZoneError(ESeverity.Warning, CmsisZoneError.Z121));
                }
            }
        }
    }

    protected void updateMarkers(ICpRootZone rootZone) {
        if (!PlatformUI.isWorkbenchRunning())
            return; // no markers in non-UI
        IFile aFile = CpPlugInUI.getFileForLocation(rootZone.getRootFileName());
        if (aFile == null) {
            return; // outside workspace?
        }
        CpZonePluginUI.removeCmsisZoneMarkers(aFile);
        IFile rFile = CpPlugInUI.getFileForLocation(rootZone.getResourceFileName());
        if (rFile != null) {
            CpZonePluginUI.removeCmsisZoneMarkers(rFile);
        }

        CpZonePluginUI.setCmsisZoneMarkers(this);
    }

}
