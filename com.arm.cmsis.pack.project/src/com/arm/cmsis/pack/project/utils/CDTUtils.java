/*******************************************************************************
* Copyright (c) 2021 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     Intel Corporation - initial API and implementation
*     IBM Corporation
*     ARM Ltd and ARM Germany GmbH
*
*     Snippet to set description to CDT project is taken from:
*     org.eclipse.cdt.managedbuilder.ui.wizards.MBSWizardHandler
*******************************************************************************/
package com.arm.cmsis.pack.project.utils;

import java.net.URI;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * Class to create a CDT project
 *
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@SuppressWarnings("restriction")
public class CDTUtils {
    // Constants
    public static final String BUILD_ARTEFACT_TYPE = "org.eclipse.cdt.build.core.buildArtefactType"; //$NON-NLS-1$
    public static final String BUILD_ARTEFACT_TYPE_EXE = "org.eclipse.cdt.build.core.buildArtefactType.exe"; //$NON-NLS-1$
    public static final String BUILD_ARTEFACT_TYPE_LIB = "org.eclipse.cdt.build.core.buildArtefactType.staticLib"; //$NON-NLS-1$

    /**
     * @param project
     * @param toolchainID
     * @param projectType C/C++ project type: BUILD_ARTEFACT_TYPE_EXE or
     *                    BUILD_ARTEFACT_TYPE_LIB
     * @param monitor     progress monitor
     * @return IProject
     * @throws OperationCanceledException
     * @throws CoreException
     */
    public static IProject createCDTProject(IProject project, String toolchainID, String projectType,
            IProgressMonitor monitor) throws OperationCanceledException, CoreException {
        // Create CDTproject
        IProjectDescription description = project.getDescription();

        // Create initial CDT project
        project = CCorePlugin.getDefault().createCDTProject(description, project, monitor);

        // Get toolchain
        IToolChain toolChain = getToolchain(toolchainID);

        // Get project configurations
        IConfiguration[] cfgs = getConfigurations(toolChain, projectType);

        // Set project description
        setProjectDescription(project, cfgs, monitor);

        return project;
    }

    /**
     * Creates a project
     *
     * @param projectName project name
     * @param locationURI location URI
     * @param monitor     progress monitor
     * @return IProject
     * @throws CoreException
     * @throws OperationCanceledException
     */
    public static IProject createProject(String projectName, URI locationURI, IProgressMonitor monitor)
            throws OperationCanceledException, CoreException {

        // Get workspace where project will be created
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        // Returns the root resource of the workspace
        IWorkspaceRoot root = workspace.getRoot();
        // Returns a handle to the project resource with the given name
        IProject project = root.getProject(projectName);

        // Creates and returns a new project description for a project with the given
        // name.
        IProjectDescription description = workspace.newProjectDescription(projectName);
        // Sets the location for the described project
        description.setLocationURI(locationURI);

        // create project if does not exist
        if (!project.exists()) {
            project.create(description, monitor);
        }
        // Open created project.
        project.open(monitor);

        return project;
    }

    /**
     * Sets description to CDT project
     *
     * @param project created project in workspace
     * @param monitor progress monitor
     * @param cfgs    array of configurations for CDT project
     * @throws CoreException
     */
    public static void setProjectDescription(IProject project, IConfiguration[] cfgs, IProgressMonitor monitor)
            throws OperationCanceledException, CoreException {

        // Get manager of CDT Project descriptions to create CDT project's description
        ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();

        // Create ICProjectDescription object to hold the CDT project settings
        ICProjectDescription des = mngr.createProjectDescription(project, false, !true);

        // Create build information object and associates it with the project.
        ManagedBuildInfo info = ManagedBuildManager.createBuildInfo(project);

        // Create a configuration object with the first element(Debug) of the cfgs array
        // to get project type
        Configuration cf = (Configuration) cfgs[0];

        // Create a project instance from the cf's project-type
        ManagedProject mProj = new ManagedProject(project, cf.getProjectType());

        // Sets build info with project
        info.setManagedProject(mProj);

        monitor.worked(10);
        int work = 50 / cfgs.length;

        // Create configuration for every configuration item
        for (IConfiguration cfg : cfgs) {
            cf = (Configuration) cfg;
            // Calculate a random number to append it to the configurationID and generates a
            // unique configurationID every time a new project is created
            String id = ManagedBuildManager.calculateChildId(cf.getId(), null);
            // Create a configuration object based on the specification stored in the
            // project file (.cdtbuild)
            Configuration config = new Configuration(mProj, cf, id, false, true);
            // Get data to be associated with configuration description
            CConfigurationData data = config.getConfigurationData();
            // Creates the configuration to the project description based on the build
            // system id "org.eclipse.cdt.managedbuilder.core.configurationDataProvider" and
            // the configuration data
            ICConfigurationDescription cfgDes = des.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, data);
            config.setConfigurationDescription(cfgDes);
            // Add Include, Library path & Library File settings
            config.exportArtifactInfo();

            // Set the builder to "managed" mode
            IBuilder bld = config.getEditableBuilder();
            if (bld != null) {
                bld.setManagedBuildOn(true);
            }

            // Get item configuration name and set it to the configuration
            config.setName(cfg.getName());

            // Get the default build artifact name for the project and set it to the
            // configuration
            config.setArtifactName(mProj.getDefaultArtifactName());

            // Indicate that configuration' settings has been completed.
            monitor.worked(work);
        }
        // Apply project description
        mngr.setProjectDescription(project, des);

        // Notify that setting project description is done
        monitor.done();
    }

    /**
     * Gets configurations for CDT project
     *
     * @param toolChain   Arm toolchain
     * @param projectType C/C++ project type category, BUILD_ARTEFACT_TYPE_EXE or
     *                    BUILD_ARTEFACT_TYPE_LIB
     * @return Array with configurations
     */
    public static IConfiguration[] getConfigurations(IToolChain toolChain, String projectType) {
        // Get configurations from the ManagedBuildManager
        IConfiguration[] configs = ManagedBuildManager.getExtensionConfigurations(toolChain, BUILD_ARTEFACT_TYPE,
                projectType);
        return configs;
    }

    /**
     * Gets toolchain
     *
     * @param toolchainID toolchaindID belonging to selected target by user in
     *                    import wizard
     * @return toolChain of type IToolChain
     */
    public static IToolChain getToolchain(String toolchainID) {
        IToolChain toolChain = null;
        IToolChain[] toolchainList = ManagedBuildManager.getRealToolChains();
        for (int i = 0; i < toolchainList.length; i++) {
            if (toolchainList[i].getId().startsWith(toolchainID)) {
                toolChain = toolchainList[i];
            }
        }
        return toolChain;
    }

}
