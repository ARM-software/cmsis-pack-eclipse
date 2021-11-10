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

package com.arm.cmsis.pack.project;

import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.arm.cmsis.pack.common.CmsisConstants;

/**
 * Class to manage Rte nature
 */
public class RteProjectNature extends CmsisProjectNature {

    public static final String RTE_NATURE_ID = "com.arm.cmsis.pack.project.RteNature"; //$NON-NLS-1$

    public RteProjectNature() {
        super(RTE_NATURE_ID);
    }

    /**
     * Checks if supplied project has RteNature
     *
     * @param project IProject to test
     * @return true if RteNature is installed for this project
     */
    public static boolean hasRteNature(IProject project) {
        return hasNature(project, RTE_NATURE_ID);
    }

    /**
     * Adds RTE nature
     *
     * @param project IProject which will contain RTE nature
     * @param monitor monitors progress
     * @throws CoreException a checked exception representing a failure
     */
    public static String addRteNature(IProject project, IProgressMonitor monitor) throws CoreException {
        String msg = CmsisConstants.EMPTY_STRING;
        String natureId = RTE_NATURE_ID;
        // Add RTE nature to the project
        try {
            CProjectNature.addNature(project, natureId, monitor);
        } catch (CoreException e) {
            msg = Messages.RteProjectNature_AddRteNature + ": " + e.getMessage(); //$NON-NLS-1$
        }
        return msg;
    }

}
