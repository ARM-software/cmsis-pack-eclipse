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
package com.arm.cmsis.pack.project.importer;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackInstaller;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpExample;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.project.CpProjectPlugIn;
import com.arm.cmsis.pack.project.Messages;
import com.arm.cmsis.pack.project.RteProjectNature;
import com.arm.cmsis.pack.project.utils.CDTUtils;
import com.arm.cmsis.pack.ui.console.RteConsole;
import com.arm.cmsis.pack.ui.console.RteConsoleStrategy;
import com.arm.cmsis.pack.utils.Utils;

/**
 * Base class for project importers
 *
 */
public abstract class RteProjectImporter extends RteConsoleStrategy implements IRteProjectImporter {

    private ICpExample fExample = null; // example to import
    private String fSourceProjectFileName = CmsisConstants.EMPTY_STRING; // Absolute project filename to import
    private String fSourceProjectPath = CmsisConstants.EMPTY_STRING; // project source path
    private String fDestinationPath; // Destination path (parent for all projects) within workspace or outside
    protected IProject fProject = null; // project being created, a temporary variable if several are created
    protected List<String> fCreatedProjects = new ArrayList<>();
    private boolean fisHeadlessImport = false; // Flag to know headless import mode

    /**
     * Default constructor
     */
    public RteProjectImporter() {
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        return Adapters.adapt(this, adapter);
    }

    /**
     * Clears supplied and temporary information
     */
    protected void clear() {
        fProject = null;
    }

    public String getImportTaskName() {
        String taskName = CmsisConstants.EMPTY_STRING;

        ICpExample example = getExample();
        if (example != null) {
            taskName += NLS.bind(Messages.RteProjectImporter_Importing_Example, example.getId());
        } else {
            taskName += NLS.bind(Messages.RteProjectImporter_Importing_File, fSourceProjectFileName);
        }
        return taskName;
    }

    @Override
    public boolean importProject() {

        fCreatedProjects.clear();
        fProject = null;

        WorkspaceJob worker = new WorkspaceJob(Messages.RteProjectImporter_JobMessage + getSourceProjectFile()) {
            @Override
            public IStatus runInWorkspace(IProgressMonitor monitor) {
                IStatus status = null;
                if (monitor == null)
                    monitor = new NullProgressMonitor();
                String taskName = getImportTaskName();
                monitor.setTaskName(taskName);
                getCmsisConsole().outputInfo(taskName);

                // Creates Rte event and notifies listeners before creating a Rte project
                CpPlugIn.getDefault().emitRteEvent(RteEvent.PRE_IMPORT, null);
                try {
                    installRequiredPacks(monitor);
                    doImportProject(monitor);
                } catch (OperationCanceledException | InterruptedException e) {
                    status = handlePackInstallerException(e);
                } catch (CoreException e) {
                    e.printStackTrace();
                    status = new Status(e.getStatus().getSeverity(), CpProjectPlugIn.PLUGIN_ID, e.getMessage(), e);
                    getCmsisConsole().outputError(e.getMessage());
                }
                if (status == null) {
                    status = new Status(IStatus.OK, CpProjectPlugIn.PLUGIN_ID, null);
                    getCmsisConsole().outputInfo(Messages.RteProjectImporter_Import_Completed);
                    getCmsisConsole().outputInfo(CmsisConstants.EMPTY_STRING);
                }
                monitor.done();
                notifyImportCompleted();
                return status;
            }
        };
        worker.schedule();

        if (!PlatformUI.isWorkbenchRunning()) {
            // Set flag to know if it is headless import mode
            fisHeadlessImport = true;

            // in headless mode we need to wait until the job is completed/terminated
            try {
                worker.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public void installRequiredPacks(IProgressMonitor monitor) throws OperationCanceledException, InterruptedException {

        ICpPackInstaller packInstaller = CpPlugIn.getPackManager().getPackInstaller();
        if (packInstaller == null) {
            return;
        }
        Collection<String> packIds = getRequiredPackIDs();
        if (packIds == null || packIds.isEmpty())
            return;
        boolean supressMessages = packInstaller.isSuppressMessages();
        packInstaller.setSuppressMessages(true);
        try {
            // initialize pack installation
            packInstaller.installPacks(packIds);
            // wait for install jobs to complete
            while (packInstaller.isBusy()) {
                // We have no reference to the progress monitor to notify us when the cancel
                // button is pressed
                // so best we can do is poll the cancel status flag regularly
                Thread.sleep(2000);
                progress(1, monitor);
                if (monitor.isCanceled())
                    break;
            }
        } finally {
            packInstaller.setSuppressMessages(supressMessages);
        }
    }

    /**
     * Send RteEvent to notify that the import is completed
     */
    protected void notifyImportCompleted() {
        CpPlugIn.getDefault().emitRteEvent(RteEvent.POST_IMPORT, this);
    }

    /**
     * Performs actual project import
     *
     * @param monitor monitor monitors the progress of import process
     * @throws OperationCanceledException exception thrown when the user cancels the
     *                                    operation
     * @throws CoreException              exception representing a failure
     */
    abstract protected void doImportProject(IProgressMonitor monitor) throws OperationCanceledException, CoreException;

    /**
     * Creates a project with RTE nature
     *
     * @param destinationProjectName
     * @param destinationURI
     * @param toolchainID
     * @param projectType
     * @param monitor
     * @param createCDT              TODO
     * @return created project
     * @throws OperationCanceledException
     * @throws CoreException
     */
    protected IProject createProject(String destinationProjectName, URI destinationURI, String toolchainID,
            String projectType, IProgressMonitor monitor, boolean createCDT)
            throws OperationCanceledException, CoreException {
        // Create project in workspace
        IProject project = CDTUtils.createProject(destinationProjectName, destinationURI, monitor);
        if (project == null) {
            throw createErrorException(Messages.RteProjectImporter_ErrorCDTProjectCreation);
        }

        // Add Rte nature to the project before creating CDT project to avoid
        // unnecessary indexing
        String msgAddRteNature = RteProjectNature.addRteNature(project, monitor);
        if (!msgAddRteNature.isEmpty()) {
            throw createErrorException(msgAddRteNature);
        }

        progress(1, monitor);
        if (createCDT) {
            // Create CDT project
            project = CDTUtils.createCDTProject(project, toolchainID, projectType, monitor);
            if (project == null) {
                throw createErrorException(Messages.RteProjectImporter_ErrorCDTProjectCreation);
            }
        }
        return project;
    }

    @Override
    public void setExample(ICpExample example) {
        fExample = example;
        if (fExample == null)
            return;

        String loadPath = fExample.getAbsoluteLoadPath(getEnvironmentString());
        if (loadPath == null) {
            fExample = null;
            return;
        }
        setSourceProjectFile(loadPath);
    }

    /**
     * Returns environment string required to obtain example path
     *
     * @return environment string
     */
    protected String getEnvironmentString() {
        return null;
    }

    @Override
    public ICpExample getExample() {
        return fExample;
    }

    @Override
    public String validate() {
        // default does nothing
        return null;
    }

    @Override
    public void setSourceProjectFile(String fileName) {
        if (fSourceProjectPath != null && fSourceProjectPath.equals(fileName))
            return; // nothing to change
        fSourceProjectFileName = fileName;
        if (fSourceProjectFileName == null || fSourceProjectFileName.isEmpty()) {
            fSourceProjectPath = CmsisConstants.EMPTY_STRING;
            return;
        }
        fSourceProjectPath = Utils.extractPath(fileName, false);
        parseProject();
    }

    /**
     * Parsers supplied project if needed
     */
    protected void parseProject() {
        // default does nothing
    }

    @Override
    public void setDestinationPath(String destinationPath) {
        fDestinationPath = destinationPath;
    }

    @Override
    public String getDestinationPath() {
        if (fDestinationPath == null || fDestinationPath.isEmpty()) {
            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            if (root == null)
                return null;
            return root.getLocation().toString();
        }
        return fDestinationPath;
    }

    /**
     * Creates a new exception
     *
     * @param message error message
     * @return exception
     */
    protected CoreException createErrorException(String message) {
        return new CoreException(new Status(IStatus.ERROR, getPlugInId(), message));
    }

    /**
     * Creates a new exception
     *
     * @param message error message
     * @param cause   root cause
     * @return exception
     */
    protected CoreException createErrorException(String message, Exception cause) {
        return new CoreException(new Status(IStatus.ERROR, getPlugInId(), message, cause));
    }

    /**
     * Returns plug-in ID implementing the importer
     *
     * @return plug-in ID
     */
    protected String getPlugInId() {
        return CpProjectPlugIn.PLUGIN_ID;
    }

    /**
     * Notifies that a given number of work unit of the main task has been
     * completed. Check if this operation should be cancelled
     *
     * @param work    a non-negative number of work units just completed
     * @param monitor IProgressMonitor
     * @throws OperationCanceledException in case user cancels import
     */
    protected void progress(int work, IProgressMonitor monitor) throws OperationCanceledException {
        if (monitor == null)
            return;
        // Cancel import
        if (monitor.isCanceled()) {
            throw new OperationCanceledException(Messages.RteProjectImporter_CancelImport);
        }
        monitor.worked(work);
    }

    /**
     * Deletes project if user cancels import at some point
     */
    protected void deleteProject() {
        // Delete project from workspace if exists
        if (fProject != null) {
            // Check if destination is different from source!
            if (fSourceProjectPath != null) {
                IPath sourcePath = new Path(fSourceProjectPath);
                IPath destinationPath = fProject.getLocation();

                boolean bDeleteContent = !sourcePath.equals(destinationPath);
                try {
                    fProject.delete(bDeleteContent, true, new NullProgressMonitor());
                    fProject = null;
                } catch (CoreException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Deletes project if it already exists in the workspace or directory with the
     * project name exists under destination folder
     */
    protected void deleteExistingProject(IProject project) {
        // Delete project from workspace if exists
        if (project != null) {
            // Check if destination is different from source!
            if (fSourceProjectPath != null) { // Source path is the same for every target from same project
                IPath sourcePath = new Path(fSourceProjectPath);
                IPath destinationPath = project.getLocation();

                boolean bDeleteContent = !sourcePath.equals(destinationPath);
                try {
                    project.delete(bDeleteContent, true, new NullProgressMonitor());
                    project = null;
                } catch (CoreException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Handle the exception thrown by the pack installer
     *
     * @param exception
     */
    protected IStatus handlePackInstallerException(Exception exception) {
        ICpPackInstaller packInstaller = CpPlugIn.getPackManager().getPackInstaller();
        if (packInstaller != null) {
            packInstaller.reset(); // cancel all pending install jobs
        }

        String msgCancel = exception.getMessage();
        getCmsisConsole().outputInfo(msgCancel);
        return new Status(IStatus.CANCEL, CpProjectPlugIn.PLUGIN_ID, msgCancel, exception);
    }

    @Override
    public IAdaptable importExample(ICpExample example) {
        setCmsisConsole(RteConsole.openGlobalConsole());
        setExample(example);
        importProject();
        return this;
    }

    /*** getters ***/

    @Override
    public Collection<String> getCreatedProjectNames() {
        return fCreatedProjects;
    }

    /**
     * Checks if the import runs in headless mode
     *
     * @return true if headless
     */
    public boolean isHeadlessImport() {
        return fisHeadlessImport;
    }

    @Override
    public String getSourceProjectFile() {
        return fSourceProjectFileName;
    }

    @Override
    public String getSourceProjectPath() {
        return fSourceProjectPath;
    }

    @Override
    public IProject getProject() {
        return fProject;
    }

    /*** setters ***/

    /**
     * @param fisHeadlessImport the fisHeadlessImport to set
     */
    public void setisHeadlessImport(boolean fisHeadlessImport) {
        this.fisHeadlessImport = fisHeadlessImport;
    }

}
