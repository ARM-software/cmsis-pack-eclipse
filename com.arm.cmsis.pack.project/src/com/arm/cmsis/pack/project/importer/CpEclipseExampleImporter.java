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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpEnvironmentProvider;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.configuration.IRteConfiguration;
import com.arm.cmsis.pack.data.ICpExample;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.project.CpProjectPlugIn;
import com.arm.cmsis.pack.project.IRteProject;
import com.arm.cmsis.pack.project.Messages;
import com.arm.cmsis.pack.project.RteProjectManager;
import com.arm.cmsis.pack.project.utils.ProjectUtils;
import com.arm.cmsis.pack.utils.Utils;

/**
 * Default importer to import Eclipse projects
 *
 */
public class CpEclipseExampleImporter extends CpExampleImporter {

    protected IProject fProject = null;

    @Override
    public IAdaptable importExample(ICpExample example) {
        if (example == null) {
            return null;
        }
        ICpEnvironmentProvider envProvider = CpPlugIn.getEnvironmentProvider();
        if (envProvider == null) {
            return null;
        }
        String loadPath = envProvider.getAbsoluteLoadPath(example);
        if (loadPath == null || loadPath.isEmpty()) {
            return null;
        }

        IPath examplePath = new Path(loadPath);
        // default implementation assumes that the example is an Eclipse project
        IProjectDescription projDesc = ProjectUtils.getProjectDescription(examplePath);
        if (projDesc == null) {
            popupCopyError(
                    NLS.bind(Messages.CpEclipseExampleImporter_ErrorWhileReadingProjectDescriptionFile, examplePath));
            return null;
        }
        String projectName = projDesc.getName();
        final IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IWorkspaceRoot root = workspace.getRoot();
        fProject = root.getProject(projectName);
        File destFile = root.getLocation().append(fProject.getName()).toFile();

        // Open copy dialog to show example's info and set new project name if it is
        // needed
        IProject project = confirmCopyExample(example, destFile, fProject);

        if (project == null) {
            return null;
        } else { // Copy example

            // Check if project name was changed
            boolean isProjectNameChanged = false;
            if (!fProject.equals(project))
                isProjectNameChanged = true;

            CpPlugIn.getDefault().emitRteEvent(RteEvent.PRE_IMPORT, null);
            File importSource = new File(projDesc.getLocationURI());
            FileSystemStructureProvider structureProvider = FileSystemStructureProvider.INSTANCE;
            ImportOperation operation = new ImportOperation(project.getFullPath(), importSource, structureProvider,
                    new OverwriteQuery(null), structureProvider.getChildren(importSource));
            operation.setContext(null);
            operation.setOverwriteResources(true); // need to overwrite
            operation.setCreateContainerStructure(false);
            try {
                // Copy example files into project
                operation.run(new NullProgressMonitor());
                // Clear read-only flag regardless of whether project's name was changed or not
                Utils.clearReadOnly(project.getLocation().toFile(), CmsisConstants.EMPTY_STRING);
                if (isProjectNameChanged) {
                    renameProjectFiles(fProject, project, project);
                    updatePrePostBuildCommands(fProject, project);
                    updateRteProject(project);
                }
            } catch (InvocationTargetException | InterruptedException e) {
                popupCopyError(NLS.bind(Messages.CpEclipseExampleImporter_FailedImportFilesFromFolder,
                        examplePath.removeLastSegments(1)));
                return null;
            }
            CpPlugIn.getDefault().emitRteEvent(RteEvent.POST_IMPORT, project);
        }
        return project;
    }

    /**
     * Updates pre-build and post-build step commands with new project's name
     * 
     * @param oldProject existing project
     * @param newProject project to be imported with different name than oldProject
     */
    public void updatePrePostBuildCommands(IProject oldProject, IProject newProject) {
        if (oldProject == null || newProject == null)
            return;

        // Get project's configurations
        List<IConfiguration> cfgsOldProject = getProjectConfiguration(oldProject);
        List<IConfiguration> cfgsNewProject = getProjectConfiguration(newProject);

        if (cfgsOldProject == null || cfgsNewProject == null)
            return;
        if (cfgsOldProject.size() != cfgsNewProject.size())
            return;

        for (int i = 0; i < cfgsOldProject.size(); i++) {
            IConfiguration oldProjectCfg = cfgsOldProject.get(i);
            IConfiguration newProjectCfg = cfgsNewProject.get(i);
            if (oldProjectCfg.getName().equals(newProjectCfg.getName())) {

                // Get old project's pre-build step command
                String oldProjectPreBuildStep = oldProjectCfg.getPrebuildStep();
                if (!oldProjectPreBuildStep.isEmpty()) {
                    // Rename pre-build step command with new project's name
                    String newProjectPreBuildStep = oldProjectPreBuildStep.replace(oldProject.getName(),
                            newProject.getName());
                    // Set updated pre-build step command to new project
                    newProjectCfg.setPrebuildStep(newProjectPreBuildStep);
                }

                // Get old project's post-build step command
                String oldProjectPostBuildStep = oldProjectCfg.getPostbuildStep();
                if (!oldProjectPostBuildStep.isEmpty()) {
                    // Rename post-build step command with new project's name
                    String newProjectPostBuildStep = oldProjectPostBuildStep.replace(oldProject.getName(),
                            newProject.getName());
                    // Set updated post-build step command to new project
                    newProjectCfg.setPostbuildStep(newProjectPostBuildStep);
                }
            }
        }
        ManagedBuildManager.saveBuildInfo(newProject, true);
    }

    /**
     * Gets project's configuration
     * 
     * @param project selected project to get configuration
     * @return config debug configuration
     */
    public List<IConfiguration> getProjectConfiguration(IProject project) {
        List<IConfiguration> cfgs = new ArrayList<>();
        IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
        String[] configNames = buildInfo.getConfigurationNames();
        for (String name : configNames) {
            IConfiguration config = ProjectUtils.getConfiguration(project, name);
            cfgs.add(config);
        }
        return cfgs;
    }

    /**
     * Renames project files
     * 
     * @param old        IProject project
     * @param new        IProject project
     * @param IContainer container(parent container e.g. project)
     */
    public void renameProjectFiles(IProject oldProject, IProject newProject, IContainer newContainer) {
        IResource[] members;
        try {
            members = newContainer.members();
            for (IResource member : members) {
                if (member instanceof IContainer)
                    // Read content
                    renameProjectFiles(oldProject, newProject, (IContainer) member);
                if (member instanceof IFile) {
                    String memberName = Utils.extractBaseFileName(member.getName());
                    if (memberName.equals(oldProject.getName())) {
                        IPath memberPath = member.getProjectRelativePath();
                        String newMemberName = member.getName().replace(oldProject.getName(), newProject.getName());
                        IPath newMemberPath = new Path(CmsisConstants.SLASH + newProject.getName()
                                + CmsisConstants.SLASH + memberPath.removeLastSegments(1).append(newMemberName));

                        String fileExtension = CmsisConstants.DOT + Utils.extractFileExtension(member.getName());
                        if (fileExtension.equals(CmsisConstants.DOT_LAUNCH))
                            updateLaunchFile(oldProject, newProject);
                        else
                            member.move(newMemberPath, IResource.FORCE, null);
                    }
                }
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates rteProject with new project's name
     * 
     * @param new IProject project
     */
    public void updateRteProject(IProject newProject) {
        String rteNewConfigName = newProject.getName() + CmsisConstants.DOT_RTECONFIG;
        RteProjectManager rteProjectManager = CpProjectPlugIn.getRteProjectManager();
        IRteProject rteProject = rteProjectManager.getRteProject(newProject);
        rteProject.setRteConfigurationName(rteNewConfigName);
        IRteConfiguration rteConfig = rteProject.getRteConfiguration();
        rteProject.setRteConfiguration(rteNewConfigName, rteConfig);
    }

    /**
     * Updates launch file
     * 
     * @param old IProject project
     * @param new IProject project
     */
    public void updateLaunchFile(IProject oldProject, IProject newProject) {
        // Build new .launch file's name
        String newProjectName = newProject.getName();

        // Build old .launch file's name
        String oldProjectName = oldProject.getName();
        String oldLaunchFileName = oldProjectName + CmsisConstants.DOT_LAUNCH;

        // Create a handle for the old .launchfile. Note: new project has old .launch
        // file
        IFile iFileOldLaunchFile = newProject.getFile(oldLaunchFileName);
        if (iFileOldLaunchFile == null)
            return;

        try {
            ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
            ILaunchConfiguration conf = launchManager.getLaunchConfiguration(iFileOldLaunchFile);
            ILaunchConfigurationWorkingCopy workingCopy = conf.getWorkingCopy();

            if (!workingCopy.isReadOnly()) {
                String oldCmsisExeFile = workingCopy.getAttribute(CmsisConstants.CMSIS_EXE_FILE,
                        CmsisConstants.EMPTY_STRING);
                String newCmsisExeFile = oldCmsisExeFile.replace(oldProjectName, newProjectName);
                workingCopy.setAttribute(CmsisConstants.CMSIS_EXE_FILE, newCmsisExeFile);

                String oldCmsisPreviousPrimaryFile = workingCopy
                        .getAttribute(CmsisConstants.CMSIS_PREVIOUS_PRIMARY_FILE, CmsisConstants.EMPTY_STRING);
                String newCmsisPreviousPrimaryFile = oldCmsisPreviousPrimaryFile.replace(oldProjectName,
                        newProjectName);
                workingCopy.setAttribute(CmsisConstants.CMSIS_PREVIOUS_PRIMARY_FILE, newCmsisPreviousPrimaryFile);

                String oldCmsisPrimaryFile = workingCopy.getAttribute(CmsisConstants.CMSIS_PRIMARY_FILE,
                        CmsisConstants.EMPTY_STRING);
                String newCmsisPrimaryFile = oldCmsisPrimaryFile.replace(oldProjectName, newProjectName);
                workingCopy.setAttribute(CmsisConstants.CMSIS_PRIMARY_FILE, newCmsisPrimaryFile);

                String oldCmsisProj = workingCopy.getAttribute(CmsisConstants.CMSIS_PROJ, CmsisConstants.EMPTY_STRING);
                String newCmsisProj = oldCmsisProj.replace(oldProjectName, newProjectName);
                workingCopy.setAttribute(CmsisConstants.CMSIS_PROJ, newCmsisProj);

                String oldCmsisProjectExec = workingCopy.getAttribute(CmsisConstants.CMSIS_PROJECT_EXECUTABLE,
                        CmsisConstants.EMPTY_STRING);
                String newCmsisProjectExec = oldCmsisProjectExec.replace(oldProjectName, newProjectName);
                workingCopy.setAttribute(CmsisConstants.CMSIS_PROJECT_EXECUTABLE, newCmsisProjectExec);

                String oldFilesIceDebugRscValue = workingCopy
                        .getAttribute(CmsisConstants.FILES_ICE_DEBUG_RESOURCES_0_VALUE, CmsisConstants.EMPTY_STRING);
                String newFilesIceDebugRscValue = oldFilesIceDebugRscValue.replace(oldProjectName, newProjectName);
                workingCopy.setAttribute(CmsisConstants.FILES_ICE_DEBUG_RESOURCES_0_VALUE, newFilesIceDebugRscValue);

                String oldKeyCommand = workingCopy.getAttribute(CmsisConstants.KEY_COMMANDS_AS_CONNECT_TEXT,
                        CmsisConstants.EMPTY_STRING);
                String newKeyCommand = oldKeyCommand.replace(oldProjectName, newProjectName);
                workingCopy.setAttribute(CmsisConstants.KEY_COMMANDS_AS_CONNECT_TEXT, newKeyCommand);

                workingCopy.rename(newProjectName);
                workingCopy.doSave();
            }

        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Collection<String> getCreatedProjectNames() {
        if (fProject != null) {
            return Arrays.asList(fProject.getName());
        }
        return Arrays.asList(CmsisConstants.EMPTY_STRING);
    }

}
