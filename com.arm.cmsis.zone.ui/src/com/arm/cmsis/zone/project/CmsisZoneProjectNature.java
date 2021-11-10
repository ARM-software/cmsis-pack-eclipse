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

package com.arm.cmsis.zone.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import com.arm.cmsis.pack.project.CmsisProjectNature;

/**
 *
 */
public class CmsisZoneProjectNature extends CmsisProjectNature {

    public static final String CMSIS_ZONE_MASTER_NATURE_ID = "com.arm.cmsis.zone.ui.CmsisZoneProjectNature"; //$NON-NLS-1$

    public CmsisZoneProjectNature() {
        super(CMSIS_ZONE_MASTER_NATURE_ID);
    }

    /**
     * Checks if supplied project has CmsisZoneProjectNature
     *
     * @param project IProject to test
     * @return true if CmsisZoneMasterProjectNature is installed for this project
     */
    public static boolean hasCmsisZoneNature(IResource resource) {
        if (resource == null)
            return false;
        IProject project = resource.getProject();

        return hasNature(project, CMSIS_ZONE_MASTER_NATURE_ID);
    }

}
