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
package com.arm.cmsis.pack.project.importer;

import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import com.arm.cmsis.pack.ICpExampleImporter;
import com.arm.cmsis.pack.data.ICpExample;

/**
 * Base interface to import projects
 */
public interface IRteProjectImporter extends ICpExampleImporter {

    /**
     * Performs import of selected project with selected options
     *
     * @return any successfully created IProject, null if import failed entirely
     */
    boolean importProject();

    /**
     * Returns IProject being currently created
     *
     * @return IProject
     */
    IProject getProject();

    /**
     * Sets example to import
     *
     * @param example ICpExample to import
     */
    void setExample(ICpExample example);

    /**
     * Returns example to import
     *
     * @return ICpExample to import, null if not set.
     */
    ICpExample getExample();

    /**
     * Validates the supplied information for completeness and correctness
     *
     * @return validation result : empty string if import is possible, otherwise
     *         reason why not
     */
    String validate();

    /**
     * Sets absolute project filename to be imported
     *
     * @param file absolute filename of the project to be imported
     */
    void setSourceProjectFile(String fileName);

    /**
     * Returns absolute filename of the project to be imported
     *
     * @return project filename or null if not set
     * @see #setSourceProjectFile(String)
     */
    String getSourceProjectFile();

    /**
     * Returns absolute path to projects file's directory
     *
     * @return project directory path
     */
    String getSourceProjectPath();

    /**
     * Sets destination project path: parent for created project(s) directories
     *
     * @param destinationPath destination project path
     */
    void setDestinationPath(String destinationPath);

    /**
     * Returns destination project path
     *
     * @return destination project path
     * @see #setDestinationPath(String)
     */
    String getDestinationPath();

    /**
     * Installs required packs
     *
     * @throws OperationCanceledException
     * @throws InterruptedException
     */
    void installRequiredPacks(IProgressMonitor monitor) throws OperationCanceledException, InterruptedException;

    /**
     * Returns collection of required pack IDs
     *
     * @return collection of required pack IDs, null or empty if no packs are
     *         required
     */
    default Collection<String> getRequiredPackIDs() {
        return null;
    }

}
