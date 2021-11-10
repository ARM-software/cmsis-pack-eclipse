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

package com.arm.cmsis.pack.project.template;

import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpEnvironmentProvider;
import com.arm.cmsis.pack.build.settings.RteToolChainAdapterInfo;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.configuration.IRteConfiguration;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.project.IRteProject;
import com.arm.cmsis.pack.project.Messages;
import com.arm.cmsis.pack.project.utils.ProjectUtils;

/**
 * Process runner that creates new RTE Project with default RTE configuration
 */
public class CreateRteProject extends ProcessRunner {

    @Override
    public void process(TemplateCore template, ProcessArgument[] args, String processId, IProgressMonitor monitor)
            throws ProcessFailureException {
        String projectName = args[0].getSimpleValue();
        String compiler = args[1].getSimpleValue();
        String output = args[2].getSimpleValue();
        String adapterId = args[3].getSimpleValue();
        String lastStep = args[4].getSimpleValue();

        // Create project in workspace
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        if (project == null) {
            String msg = Messages.CreateRteProject_ErrorCreatingRteProject;
            msg += Messages.CreateRteProject_EclipseProjectNotExists;
            msg += projectName;
            throw new ProcessFailureException(getProcessMessage(processId, IStatus.ERROR, msg));
        }
        // Get toolchain adapter info to create Rte project
        RteToolChainAdapterInfo adapterInfo = ProjectUtils.createToolChainAdapter(adapterId);
        if (adapterInfo == null) {
            String msg = Messages.CreateRteProject_ErrorCreatingRteProject;
            msg += Messages.CreateRteProject_ToolchainAdapterNotFound;
            msg += adapterId;
            throw new ProcessFailureException(getProcessMessage(processId, IStatus.ERROR, msg));
        }
        // Get device info to create Rte configuration
        ICpDeviceInfo deviceInfo = RteProjectTemplate.getSelectedDeviceInfo();
        // Create Rte configuration
        IRteConfiguration rteConf = ProjectUtils.createRteConfiguration(compiler, output, deviceInfo);
        // Set Rte project's name
        String rteConfigName = projectName + CmsisConstants.DOT_RTECONFIG;

        // Create Rte file
        IFile rteFile = ProjectUtils.createRteFile(project, rteConfigName, rteConf, monitor);
        if (rteFile == null) {
            String msg = Messages.CreateRteProject_ErrorCreatingRteProject;
            msg += Messages.CreateRteProject_ErrorCreatingConfigFile;
            msg += rteConfigName;
            throw new ProcessFailureException(getProcessMessage(processId, IStatus.ERROR, msg));
        }
        // Create Rte project
        IRteProject rteProject = ProjectUtils.createRteProject(project, adapterInfo); // never fails
        // Set Rte configuration
        rteProject.setRteConfiguration(rteConfigName, rteConf);
        // Initializes new project
        if ("1".equals(lastStep)) { //$NON-NLS-1$
            // in some customized process, we should prevent the indexer from indexing at
            // this point
            // because it may still has some work to do (e.g. copy resources)
            rteProject.init();
        }
        ICpEnvironmentProvider envProvider = CpPlugIn.getEnvironmentProvider();
        if (envProvider != null) {
            envProvider.contibuteToNewProject(projectName);
        }
        // Open Rte configuration file if this is the last step
        if ("1".equals(lastStep) && rteFile != null) { //$NON-NLS-1$
            ProjectUtils.openEditorAsync(rteFile);
        }
    }
}
