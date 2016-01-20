/*******************************************************************************
* Copyright (c) 2015 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.project.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.resource.RenameResourceChange;

import com.arm.cmsis.pack.project.Messages;

/**
 *  This class renames a resource after containing project is renamed.
 */
public class RenameResourceAfterProjectChange extends RenameResourceChange {

	private IPath     fSrcResourcePath;  // the original resource path with original name 
	private IPath     fDstResourcePath;  // the destination resource path, still with original resource 
	private String    fNewProjectName;   // new (destination) project name  
	private String 	  fOldProjectName;

	private boolean   fUndo;     // for undo the resource is renamed before project rename, for do/redo after project change 

	/**
	 * Constructs the change
	 * @param resource the original .rteconfig file: /MyProj/MyProj.rteconfig 
	 * @param srcPath the original resource path with old name: /MyNewProj/MyProj.rteconfig
	 * @param newProjectName name of the destination project MyNewProj
	 * @param newName new resource name: MyNewProj.rteconfig
	 */
	public RenameResourceAfterProjectChange(IPath srcPath, String newProjectName, String newName) {
		this(srcPath, newProjectName, newName, false);
	}

	
	/**
	 * Constructs the change
	 * @param resource the original .rteconfig file: /MyProj/MyProj.rteconfig 
	 * @param srcPath the original resource path with old name: /MyNewProj/MyProj.rteconfig
	 * @param newProjectName name of the destination project MyNewProj
	 * @param bUndo the "undo" direction of change
	 * @param newName new resource name: MyNewProj.rteconfig
	 */
	public RenameResourceAfterProjectChange(IPath srcPath, String newProjectName, String newName, boolean bUndo) {
		super(resourcePath(srcPath, newProjectName, bUndo), newName);
		fUndo = bUndo;
		fSrcResourcePath = srcPath;
		fDstResourcePath = resourcePath(srcPath, newProjectName, bUndo);
		fNewProjectName = newProjectName;
		fOldProjectName = srcPath.segment(0);
	}
	
	/**
	 * Returns resource path used by underlying RenameResorceChange 
	 * @param srcPath the original resource path with old name: /MyNewProj/MyProj.rteconfig
	 * @param newProjectName name of the destination project MyNewProj
	 * @param bUndo the "undo" direction of change
	 * @return resource path for rename operation
	 */
	private static IPath resourcePath(IPath srcPath, String newProjectName, boolean bUndo) {
		if(bUndo) {
			return srcPath;
		}
		IPath dstPath = new Path("/" + newProjectName + "/" + srcPath.removeFirstSegments(1)); //$NON-NLS-1$ //$NON-NLS-2$
		return dstPath;
		
	}
	
	private static IPath renamedResourcePath(IPath path, String newName) {
		return path.removeLastSegments(1).append(newName);
	}

	
	@Override
	protected IResource getModifiedResource() {
		return getResource(fSrcResourcePath);  
	}

	private static IResource getResource(IPath path) {
		return ResourcesPlugin.getWorkspace().getRoot().findMember(path);
	}
	
	
	@Override
	public Change perform(IProgressMonitor pm) throws CoreException {
		try {
			pm.beginTask(Messages.RteConfigRenameResourceChange_RenamingFile, 1);

			IResource resource= getResource(fDstResourcePath);
			IPath newPath= renamedResourcePath(fDstResourcePath, getNewName());
			resource.move(newPath, IResource.FORCE, pm);
			String oldName = fSrcResourcePath.lastSegment();
			IPath srcPath = resourcePath(newPath, fNewProjectName, !fUndo);
			return new RenameResourceAfterProjectChange(srcPath, fOldProjectName, oldName, !fUndo);
		} finally {
			pm.done();
		}		
	}
	
	
}
