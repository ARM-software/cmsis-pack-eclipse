/*******************************************************************************
 * Copyright (c) 2018 ARM Ltd. and others
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
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpEnvironmentProvider;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpExample;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.project.Messages;
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
			popupCopyError(NLS.bind(Messages.CpEclipseExampleImporter_ErrorWhileReadingProjectDescriptionFile,
					examplePath));
			return null;
		}
		String projectName = projDesc.getName();
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IWorkspaceRoot root = workspace.getRoot();
		fProject = root.getProject(projectName);
		File destFile = root.getLocation().append(fProject.getName()).toFile();

		if (!confirmCopyExample(example, destFile, fProject)) {
			return null;
		}

		if (fProject.exists()) {
			try {
				fProject.delete(true, true, new NullProgressMonitor());
			} catch (CoreException e) {
				popupCopyError(NLS.bind(Messages.CpEclipseExampleImporter_ErrorWhileOverwritingExistingProject,
						fProject.getName()));
				return null;
			}
		} else if (destFile.exists()) {
			Utils.deleteFolderRecursive(destFile);
		}

		CpPlugIn.getDefault().emitRteEvent(RteEvent.PRE_IMPORT, null);
		File importSource = new File(projDesc.getLocationURI());
		FileSystemStructureProvider structureProvider = FileSystemStructureProvider.INSTANCE;

		ImportOperation operation = new ImportOperation(fProject.getFullPath(), importSource,
				structureProvider, new OverwriteQuery(null),
				structureProvider.getChildren(importSource));
		operation.setContext(null);
		operation.setOverwriteResources(true); // need to overwrite
		operation.setCreateContainerStructure(false);
		try {
			operation.run(new NullProgressMonitor());
		} catch (InvocationTargetException | InterruptedException e) {
			popupCopyError(NLS.bind(Messages.CpEclipseExampleImporter_FailedImportFilesFromFolder,
					examplePath.removeLastSegments(1)));
			return null;
		}

		Utils.clearReadOnly(fProject.getLocation().toFile(), CmsisConstants.EMPTY_STRING);
		CpPlugIn.getDefault().emitRteEvent(RteEvent.POST_IMPORT, fProject);
		return fProject;
	}

	@Override
	public Collection<String> getCreatedProjectNames() {
		if(fProject != null ){
			return Arrays.asList(fProject.getName());
		}
		return Arrays.asList(CmsisConstants.EMPTY_STRING);
	}

}
