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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Base class for CMSIS and RTE project natures
 */
public class CmsisProjectNature implements IProjectNature {

    protected String natureID;

    protected IProject project = null;

    public CmsisProjectNature(String id) {
        natureID = id;
    }

    @Override
    public void configure() throws CoreException {
        // does nothing
    }

    @Override
    public void deconfigure() throws CoreException {
        // does nothing
    }

    @Override
    public IProject getProject() {
        return project;
    }

    @Override
    public void setProject(IProject project) {
        this.project = project;

    }

    public String getNatureId() {
        return natureID;
    }

    /**
     * Checks if supplied project has given nature
     *
     * @param project IProject to test
     * @param id      nature ID
     * @return true if nature is installed for this project
     */
    public static boolean hasNature(IProject project, String id) {
        try {
            if (project != null && project.isOpen() && project.hasNature(id))
                return true;
        } catch (CoreException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Copied from CProjectNature.addNature
    public static void addNature(IProject project, String natureId, IProgressMonitor monitor) {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        try {
            IProjectDescription description = project.getDescription();
            String[] prevNatures = description.getNatureIds();
            for (String prevNature : prevNatures) {
                if (natureId.equals(prevNature)) {
                    return;
                }
            }
            String[] newNatures = new String[prevNatures.length + 1];
            System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
            newNatures[prevNatures.length] = natureId;
            description.setNatureIds(newNatures);
            project.setDescription(description, monitor);
        }

        catch (CoreException e) {
        }

        finally {
            monitor.done();
        }
    }

    public boolean hasNature(IProject project) {
        return hasNature(project, natureID);
    }
}
