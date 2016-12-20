/*******************************************************************************
 * Copyright (c) 2015 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.commands.ICommandService;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackInstaller;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.configuration.IRteConfiguration;
import com.arm.cmsis.pack.data.CpPack;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPackCollection;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.events.RteEventProxy;
import com.arm.cmsis.pack.info.ICpPackInfo;
import com.arm.cmsis.pack.ui.CpPlugInUI;

/**
 * Class that manages RTE projects and their associations to ICproject and IProject
 */
public class RteProjectManager extends RteEventProxy implements IResourceChangeListener, IExecutionListener{

	private RteSetupParticipant rteSetupParticipant = null;
	private Map<String, IRteProject> rteProjects = Collections.synchronizedMap(new HashMap<>());
	private Map<String, IRteProject> fUpdateQueue = Collections.synchronizedMap(new HashMap<>());
	private Set<String> fMissingPacks = Collections.synchronizedSet(new HashSet<>());
	boolean fMissingPacksQueryInProgress = false; // to prevent multiple dialogs at a time


	private boolean executionListenerRegistered = false;
	boolean postponeRefresh = false;
	/**
	 *  Default constructor
	 */
	public RteProjectManager() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
		CpPlugIn.addRteListener(this);
	}


	/**
	 *  Clears internal collection of the projects
	 */
	public void destroy() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeResourceChangeListener(this);
		CpPlugIn.removeRteListener(this);
		if(executionListenerRegistered) {
			ICommandService commandService = CpPlugInUI.getCommandService();
			if(commandService != null) {
				commandService.removeExecutionListener(this);
			}
		}

		synchronized (fUpdateQueue) {
			fUpdateQueue.clear();
		}

		synchronized (rteProjects) { // do it as atomic operation
			for(IRteProject rteProject : rteProjects.values()) {
				rteProject.destroy();
			}
			rteProjects.clear();
		}
	}

	private void registerExecutionListener() {
		if(executionListenerRegistered) {
			return;
		}
		ICommandService commandService = CpPlugInUI.getCommandService();
		if(commandService != null) {
			commandService.addExecutionListener(this);
			executionListenerRegistered = true;
		}
	}

	/**
	 *  Initializes RteSetupParticipant does nothing if already initialized
	 */
	public void initRteSetupParticipant() {
		if(rteSetupParticipant == null) {
			rteSetupParticipant = new RteSetupParticipant();
		}
	}

	/**
	 * Triggers project index update and notifies that project is updated
	 * @param project IProject associated with an RTE project
	 */
	public void updateIndex(IProject project) {
		if(rteSetupParticipant != null) {
			rteSetupParticipant.updateIndex(project);
		}
		emitRteEvent(RteEvent.PROJECT_UPDATED, getRteProject(project));
	}

	/**
	 * Returns IRteProject associated for given name
	 * @param project IProject object associated with IRteProject
	 * @return IRteProject
	 */
	synchronized public IRteProject getRteProject(String name) {
		return rteProjects.get(name);
	}


	/**
	 * Returns IRteProject associated with given IRteProject if any
	 * @param project IProject object associated with IRteProject
	 * @return IRteProject
	 */
	public IRteProject getRteProject(IProject project) {
		if(project != null) {
			return getRteProject(project.getName());
		}
		return null;
	}

	public Collection<IRteProject> getRteProjects() {
		return rteProjects.values();
	}

	/**
	 * Creates or returns existing IRteProject associated with given IRteProject
	 * @param project IProject object to be associated with IRteProject
	 * @return existing IRteProject if exists or new one
	 */
	synchronized public IRteProject createRteProject(IProject project) {
		IRteProject rteProject = getRteProject(project);
		if(rteProject == null) {
			rteProject = new RteProject(project);
			addRteProject(rteProject);
			registerExecutionListener(); // ensure refresh action is attached
		}
		return rteProject;
	}


	/**
	 * Adds RTE project to the internal collection
	 * @param rteProject IRteProject to add
	 */
	synchronized public void addRteProject(IRteProject rteProject) {
		if(rteProject != null) {
			rteProjects.put(rteProject.getName(), rteProject);
			emitRteEvent(RteEvent.PROJECT_ADDED, rteProject);
		}
	}

	/**
	 * Removes RTE project from internal collection
	 * @param rteProject IRteProject to remove
	 */
	synchronized public void deleteRteProject(IRteProject rteProject) {
		if(rteProject != null) {
			rteProjects.remove(rteProject.getName());
			rteProject.destroy();
			emitRteEvent(RteEvent.PROJECT_REMOVED, rteProject);
		}
	}


	/**
	 * Renames RTE project and updates collection
	 * @param rteProject IRteProject to remove
	 */
	public void renameRteProject(String oldName, String newName) {
		IRteProject rteProject = getRteProject(oldName);
		if(rteProject != null) {
			synchronized(rteProjects) { // do it as atomic operation
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
			refreshProjects();
			break;
		case RteEvent.GPDSC_CHANGED:
			refreshGpdscProjects((String) event.getData());
			break;
		case RteEvent.PRE_IMPORT:
			postponeRefresh = true;
			break;
		case RteEvent.POST_IMPORT:
			postponeRefresh = false;
			IProject project = (IProject) event.getData();
			if (project != null) {
				IRteProject rteProject = getRteProject(project);
				if (rteProject != null) {
					rteProject.refresh();
				}
			}
		default:
			return;
		}
	}


	protected void refreshProjects() {
		synchronized(rteProjects) {
			for(IRteProject rteProject : rteProjects.values()) {
				if (rteProject.getProject().isOpen()) {
					rteProject.refresh();
				}
			}
		}
	}

	protected void refreshGpdscProjects(String file) {
		synchronized(rteProjects) {
			for(IRteProject rteProject : rteProjects.values()) {
				if (rteProject.getProject().isOpen()) {
					IRteConfiguration rteConf = rteProject.getRteConfiguration();
					if(rteConf != null && rteConf.isGeneratedPackUsed(file))
						rteProject.refresh();
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
						if(newPath == null)
							return false;
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
								rteProject.refresh();
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
		if(selService == null) {
			return;
		}
		// refresh RTE project in the selection
		ISelection selection = selService.getSelection();
		Collection<IProject> projects = CpPlugInUI.getProjectsFromSelection(selection);
		if(projects == null || projects.isEmpty()) {
			return;
		}
		for(IProject project : projects) {
			IRteProject rteProject = getRteProject(project);
			if(rteProject != null && rteProject.isUpdateCompleted()) {
				rteProject.refresh();
			}
		}
	}

	synchronized public void updateProject(RteProject rteProject, int updateFlags) {
		if(rteProject == null)
			return;
		IProject project = rteProject.getProject();
		if(project == null || !project.isOpen())
			return;
		
		String name = rteProject.getName();
		if(fUpdateQueue.containsKey(name))
			return;
		rteProject.setUpdateCompleted(false);
		fUpdateQueue.put(name, rteProject);
		RteProjectUpdater updater = new RteProjectUpdater(rteProject, updateFlags);
		updater.schedule();
	}

	
	synchronized public void updateFinished(IRteProject rteProject) {
		if(rteProject == null)
			return;
		String name = rteProject.getName();
		fUpdateQueue.remove(name);
		rteProject.setUpdateCompleted(true);
		collectMissingPacks(rteProject);
		if(!fUpdateQueue.isEmpty())
			return;
		queryInstallMissingPacks();
		fMissingPacks.clear();
	}
	
	private void collectMissingPacks(IRteProject rteProject) {
		if (rteProject == null || !rteProject.getProject().isOpen()) 
			return;
				IRteConfiguration conf = rteProject.getRteConfiguration();
				if(conf == null)
			return;
				Collection<ICpPackInfo> packs = conf.getMissingPacks();
				if(packs == null || packs.isEmpty())
			return;
				for (ICpPackInfo pi : packs) {
					String packId = CpPack.constructPackId(pi.attributes());
			fMissingPacks.add(packId);
					}
				}
		
	
	class QueryInstallMissingPacksDlg  extends MessageDialog {

		public QueryInstallMissingPacksDlg(Shell parentShell, String dialogTitle, Image dialogTitleImage,
				String dialogMessage, int dialogImageType, String[] dialogButtonLabels, int defaultIndex) {
			super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels, 0);
			setShellStyle(getShellStyle() | SWT.SHEET);
			setReturnCode(defaultIndex);
			}
		}

	private void queryInstallMissingPacks() {
		if(fMissingPacksQueryInProgress)
			return;

		if(fMissingPacks.isEmpty())
			return;

		final ICpPackInstaller packInstaller = CpPlugIn.getPackManager().getPackInstaller();
		if(packInstaller == null) {
			return;
		}
		
		String packRoot = CpVariableResolver.getCmsisPackRoot();
		if(packRoot == null || packRoot.isEmpty()){
			return;
		}

		Display display = Display.getDefault();
		if(display == null)
			return;

		final Set<String> missingPacks = new HashSet<>();
		StringBuilder sb = new StringBuilder(System.lineSeparator());
		for (String packId : fMissingPacks) {
			if (packInstaller.isProcessing(packId) || isInstalled(packId)) 
				continue;
			missingPacks.add(packId);
			sb.append(System.lineSeparator());
			sb.append(packId);
		}
		if(missingPacks.isEmpty())
			return;
		fMissingPacksQueryInProgress = true;
		sb.append(System.lineSeparator()).append(System.lineSeparator());
		final String message = NLS.bind(Messages.RteProjectUpdater_InstallMissinPacksMessage, sb.toString());
		display.asyncExec(new Runnable() {
			@Override
			public void run() {

				String [] dialogButtonLabels = new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL };

				MessageDialog dialog = new QueryInstallMissingPacksDlg(Display.getDefault().getActiveShell(), Messages.RteProjectUpdater_InstallMissinPacksTitle, 
						null, message, MessageDialog.QUESTION, dialogButtonLabels, 1);

				boolean install = dialog.open() == 0;
				if (install) {
					for (String packId : missingPacks) {
						packInstaller.installPack(packId);
					}
				}
				fMissingPacksQueryInProgress = false;
			}
		});
	}
		

	/**
	 * Check if the pack manager contains the pack and it is already installed
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
