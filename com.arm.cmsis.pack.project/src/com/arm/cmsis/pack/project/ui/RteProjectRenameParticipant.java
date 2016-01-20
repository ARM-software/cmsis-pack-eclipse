package com.arm.cmsis.pack.project.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.project.CpProjectPlugIn;
import com.arm.cmsis.pack.project.IRteProject;
import com.arm.cmsis.pack.project.Messages;
import com.arm.cmsis.pack.project.RteProjectManager;
import com.arm.cmsis.pack.project.utils.ProjectUtils;


public class RteProjectRenameParticipant extends RenameParticipant {

	IResource resource;
	IProject  project;
	IRteProject rteProject;
	int type;
	
	@Override
	protected boolean initialize(Object element) {
	
		resource = ProjectUtils.getRteResource(element);
		if(resource == null)
			return false;

		resource = (IResource)element;
		type = resource.getType();
		project = resource.getProject();
		RteProjectManager rteProjectManager = CpProjectPlugIn.getRteProjectManager();
		rteProject = rteProjectManager.getRteProject(project);
		if(rteProject == null)
			return false;

		return true;
	}

	@Override
	public String getName() {
		return Messages.RteProjectRenameParticipant_CMSIS_RTE_project_rename_handler;
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm,
			CheckConditionsContext context) throws OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		try{
			pm.beginTask(Messages.RteProjectRenameParticipant_CheckingPreconditions, 1);
			IPath path = resource.getProjectRelativePath();
			if(type != IResource.PROJECT && path.segment(0).startsWith(CmsisConstants.RTE)) {
				String msg = Messages.RteProjectRenameParticipant_RenameOfRteFolderIsNotAllowed;
				status.merge(RefactoringStatus.createFatalErrorStatus(msg));
			}
		} finally {
			pm.done();
		}
		return status;
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		if(type != IResource.PROJECT)
			return null;
		try{
			pm.beginTask(Messages.RteProjectRenameParticipant_CreatingChange, 1);
			String rteConfigName = project.getName() + CmsisConstants.DOT_RTECONFIG;
			IFile iFile = project.getFile(rteConfigName);
			RenameArguments args = getArguments();
			String newProjectName = args.getNewName();
			String newRteConfigName = newProjectName + CmsisConstants.DOT_RTECONFIG;
			Change change = new  RenameResourceAfterProjectChange(iFile.getFullPath(), newProjectName, newRteConfigName);
			return change;
		} finally {
			pm.done();
		}
	}

}
