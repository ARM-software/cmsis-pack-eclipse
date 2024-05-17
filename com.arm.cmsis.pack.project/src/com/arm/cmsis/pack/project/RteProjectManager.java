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
 *
 * Resource change listener snippet is taken from:
 * https://www.eclipse.org/articles/Article-Resource-deltas/resource-deltas.html
 * *******************************************************************************/

package com.arm.cmsis.pack.project;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpEnvironmentProvider;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.configuration.IRteConfiguration;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPackCollection;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.events.RteEventProxy;
import com.arm.cmsis.pack.project.importer.CpEclipseExampleImporter;
import com.arm.cmsis.pack.project.importer.IRteProjectImporter;
import com.arm.cmsis.pack.project.utils.ProjectUtils;
import com.arm.cmsis.pack.ui.CpPlugInUI;

/**
 * Class that manages RTE projects and their associations to ICproject and
 * IProject
 */
public class RteProjectManager extends RteEventProxy implements IResourceChangeListener, IExecutionListener {

    private RteSetupParticipant rteSetupParticipant = null;
    private Map<String, IRteProject> rteProjects = Collections.synchronizedMap(new HashMap<>());

    private boolean executionListenerRegistered = false;
    private boolean postponeRefresh = false;

    /**
     * Default constructor
     */
    public RteProjectManager() {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
        CpPlugIn.addRteListener(this);
        ICpEnvironmentProvider envProvider = CpPlugIn.getEnvironmentProvider();
        if (envProvider != null && envProvider.getDefaultImporter() == null) {
            envProvider.setDefaultImporter(new CpEclipseExampleImporter());
        }
    }

    /**
     * Clears internal collection of the projects
     */
    public void destroy() {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.removeResourceChangeListener(this);
        CpPlugIn.removeRteListener(this);
        if (executionListenerRegistered) {
            ICommandService commandService = CpPlugInUI.getCommandService();
            if (commandService != null) {
                commandService.removeExecutionListener(this);
            }
        }

        synchronized (rteProjects) { // do it as atomic operation
            for (IRteProject rteProject : rteProjects.values()) {
                rteProject.destroy();
            }
            rteProjects.clear();
        }
    }

    private void registerExecutionListener() {
        if (executionListenerRegistered) {
            return;
        }
        ICommandService commandService = CpPlugInUI.getCommandService();
        if (commandService != null) {
            commandService.addExecutionListener(this);
            executionListenerRegistered = true;
        }
    }

    /**
     * Checks if refresh of RTE projects is postponed
     *
     * @return true if postponed
     */
    public boolean isPostponeRefresh() {
        return postponeRefresh;
    }

    /**
     * Initializes RteSetupParticipant does nothing if already initialized
     */
    public void initRteSetupParticipant() {
        if (rteSetupParticipant == null) {
            rteSetupParticipant = new RteSetupParticipant();
        }
    }

    /**
     * Triggers project index update and notifies that project is updated
     *
     * @param project IProject associated with an RTE project
     */
    public void updateIndex(IProject project) {
        if (!PlatformUI.isWorkbenchRunning()) {
            return;
        }
        if (rteSetupParticipant != null) {
            rteSetupParticipant.updateIndex(project);
        }
        emitRteEvent(RteEvent.PROJECT_UPDATED, getRteProject(project));
    }

    /**
     * Returns IRteProject associated for given name
     *
     * @param fProject IProject object associated with IRteProject
     * @return IRteProject
     */
    public synchronized IRteProject getRteProject(String name) {
        return rteProjects.get(name);
    }

    /**
     * Returns IRteProject associated with given IRteProject if any
     *
     * @param project IProject object associated with IRteProject
     * @return IRteProject
     */
    public IRteProject getRteProject(IProject project) {
        if (project != null) {
            return getRteProject(project.getName());
        }
        return null;
    }

    public Collection<IRteProject> getRteProjects() {
        return rteProjects.values();
    }

    /**
     * Creates or returns existing IRteProject associated with given IProject
     *
     * @param project IProject object to be associated with IRteProject
     * @return existing IRteProject if exists or new one
     */
    public synchronized IRteProject createRteProject(IProject project) {
        IRteProject rteProject = getRteProject(project);
        if (rteProject == null) {
            rteProject = new RteProject(project);
            addRteProject(rteProject);
            registerExecutionListener(); // ensure refresh action is attached
        }
        return rteProject;
    }

    /**
     * Adds RTE project to the internal collection
     *
     * @param rteProject IRteProject to add
     */
    public synchronized void addRteProject(IRteProject rteProject) {
        if (rteProject != null) {
            rteProjects.put(rteProject.getName(), rteProject);
            emitRteEvent(RteEvent.PROJECT_ADDED, rteProject);
        }
    }

    /**
     * Removes RTE project from internal collection
     *
     * @param rteProject IRteProject to remove
     */
    public synchronized void deleteRteProject(IRteProject rteProject) {
        if (rteProject != null) {
            rteProjects.remove(rteProject.getName());
            rteProject.destroy();
            emitRteEvent(RteEvent.PROJECT_REMOVED, rteProject);
        }
    }

    /**
     * Renames RTE project and updates collection
     *
     * @param rteProject IRteProject to remove
     */
    public void renameRteProject(String oldName, String newName) {
        IRteProject rteProject = getRteProject(oldName);
        if (rteProject != null) {
            synchronized (rteProjects) { // do it as atomic operation
                rteProject.setName(newName);
                rteProjects.remove(oldName);
                rteProjects.put(newName, rteProject);
                emitRteEvent(RteEvent.PROJECT_UPDATED, rteProject);
            }
        }
    }

    @Override
    public void handle(RteEvent event) {
        switch (event.getTopic()) {
        case RteEvent.PACKS_RELOADED:
        case RteEvent.PACKS_UPDATED:
            refreshProjects(RteProjectUpdater.CAUSE_PACKS_CHANGED);
            break;
        case RteEvent.GPDSC_CHANGED:
            refreshGpdscProjects((String) event.getData());
            break;
        case RteEvent.PRE_IMPORT:
            postponeRefresh = true;
            break;
        case RteEvent.POST_IMPORT:
            postponeRefresh = false;
            completeImport(event);
        default:
            return;
        }
    }

    /**
     * Completes project import
     *
     * @param event RteEvent
     */
    protected void completeImport(RteEvent event) {
        postponeRefresh = false;
        Object data = event.getData();
        if (data == null)
            return;
        if (data instanceof IProject) {
            IProject project = (IProject) data;
            if (project != null) {
                IRteProject rteProject = getRteProject(project);
                if (rteProject != null) {
                    rteProject.refresh(RteProjectUpdater.CAUSE_IMPORT_COMPLETED);
                }
            }
            ProjectUtils.openRteConfigFile(project);
            return;
        }
        if (data instanceof IRteProjectImporter) {
            IRteProjectImporter importer = (IRteProjectImporter) data;
            Collection<String> projects = importer.getCreatedProjectNames();
            if (projects == null || projects.isEmpty())
                return;

            for (String projectName : projects) {
                IProject project = ProjectUtils.getProject(projectName);
                // first try to open project import report if any
                if (ProjectUtils.openProjectRelativeFile(project, CmsisConstants.IMPORT_REPORT_TXT)) {
                    continue;
                }
                ProjectUtils.openRteConfigFile(project);
            }
        }
    }

    @Deprecated
    protected void refreshProjects() {
        refreshProjects(RteProjectUpdater.NONE);
    }

    /**
     * Refreshes all RTE projects
     *
     * @param cause refresh cause
     */
    protected void refreshProjects(int cause) {
        synchronized (rteProjects) {
            for (IRteProject rteProject : rteProjects.values()) {
                if (rteProject.getProject().isOpen()) {
                    rteProject.refresh(cause);
                }
            }
        }
    }

    protected void refreshGpdscProjects(String file) {
        synchronized (rteProjects) {
            for (IRteProject rteProject : rteProjects.values()) {
                if (rteProject.getProject().isOpen()) {
                    IRteConfiguration rteConf = rteProject.getRteConfiguration();
                    if (rteConf != null && rteConf.isGeneratedPackUsed(file)) {
                        rteProject.refresh(RteProjectUpdater.CAUSE_GPDSC_CHANGED);
                        ProjectUtils.openRteConfigFile(rteProject.getProject());
                    }
                }
            }
        }
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        // consider only POST_CHANGE events
        if (event.getType() != IResourceChangeEvent.POST_CHANGE) {
            return;
        }
        IResourceDelta resourseDelta = event.getDelta();
        IResourceDeltaVisitor deltaVisitor = new IResourceDeltaVisitor() {
            @Override
            public boolean visit(IResourceDelta delta) {
                IResource resource = delta.getResource();
                int type = resource.getType();
                if (type == IResource.ROOT) {
                    return true; // workspace => visit children
                }

                IProject project = resource.getProject();

                int kind = delta.getKind();
                int flags = delta.getFlags();

                if (type == IResource.PROJECT && kind == IResourceDelta.REMOVED) {
                    IRteProject rteProject = getRteProject(project);
                    if (rteProject == null) {
                        return false; // not an RTE project or not loaded => ignore
                    }
                    if ((flags & IResourceDelta.MOVED_TO) == IResourceDelta.MOVED_TO) {
                        // renamed
                        IPath newPath = delta.getMovedToPath();
                        if (newPath == null) {
                            return false;
                        }
                        String newName = newPath.lastSegment();
                        renameRteProject(project.getName(), newName);
                        return false;
                    }
                    // removed
                    deleteRteProject(rteProject);
                    return false;
                }

                // only consider RTE projects
                if (!RteProjectNature.hasRteNature(project)) {
                    return false; // skip children
                }

                if (type == IResource.PROJECT) {
                    // is project renamed?
                    if (kind == IResourceDelta.REMOVED) {
                        if ((flags & IResourceDelta.CHANGED) != 0) {
                            return false;
                        } else if ((flags & IResourceDelta.MOVED_TO) != 0) {
                            return false;
                        }
                        return true;
                    }

                } else if (type == IResource.FILE) {
                    // is resource changed?
                    if (kind != IResourceDelta.CHANGED) {
                        return true;
                    }

                    // is content changed?
                    if ((flags & IResourceDelta.CONTENT) == 0) {
                        return true;
                    }
                    // check only RTE configuration files with ".rteconfig" extension
                    if (CmsisConstants.RTECONFIG.equalsIgnoreCase(resource.getFileExtension())) {
                        IRteProject rteProject = getRteProject(project);
                        if (rteProject != null) {
                            String relName = resource.getProjectRelativePath().toString();
                            if (!postponeRefresh && relName.equals(rteProject.getRteConfigurationName())) {
                                rteProject.refresh(RteProjectUpdater.CAUSE_CONFIG_CHANGED);
                            }
                        }
                        return false;
                    }
                }
                return true;
            }
        };

        try {
            resourseDelta.accept(deltaVisitor);
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void preExecute(String commandId, ExecutionEvent event) {
        if (!org.eclipse.ui.IWorkbenchCommandConstants.FILE_REFRESH.equals(commandId)) {
            return;
        }
        ISelectionService selService = CpPlugInUI.getSelectionService();
        if (selService == null) {
            return;
        }
        // refresh RTE project in the selection
        ISelection selection = selService.getSelection();
        Collection<IProject> projects = CpPlugInUI.getProjectsFromSelection(selection);
        if (projects == null || projects.isEmpty()) {
            return;
        }
        for (IProject project : projects) {
            IRteProject rteProject = getRteProject(project);
            if (rteProject != null && rteProject.isUpdateCompleted()) {
                rteProject.refresh(RteProjectUpdater.NONE);
            }
        }
    }

    public void updateProject(IRteProject rteProject, int updateFlags) {
        if (rteProject == null) {
            return;
        }
        IProject project = rteProject.getProject();
        if (project == null || !project.isOpen()) {
            return;
        }
        // Do not update project if RTE project refresh is postponed
        if (isPostponeRefresh())
            return;

        rteProject.setUpdateCompleted(false);
        RteProjectUpdater updater = new RteProjectUpdater(rteProject, updateFlags);
        updater.schedule();
        if (PlatformUI.isWorkbenchRunning()) {
            return;
        }
        // wait for job to complete
        try {
            updater.join();
        } catch (OperationCanceledException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if the pack manager contains the pack and it is already installed
     *
     * @param packAttributes pack attributes
     * @return true if the pack installer has already installed the pack
     */
    protected boolean isInstalled(String packId) {
        ICpPackCollection installedPacks = CpPlugIn.getPackManager().getInstalledPacks();
        if (installedPacks == null) {
            return false;
        }
        ICpPack pack = installedPacks.getPack(packId);
        return pack != null;
    }

    @Override
    public void notHandled(String commandId, NotHandledException exception) {
        // does nothing
    }

    @Override
    public void postExecuteFailure(String commandId, ExecutionException exception) {
        // does nothing
    }

    @Override
    public void postExecuteSuccess(String commandId, Object returnValue) {
        // does nothing
    }

}
