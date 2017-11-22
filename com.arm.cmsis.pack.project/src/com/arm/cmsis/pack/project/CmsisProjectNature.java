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

package com.arm.cmsis.pack.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

/**
 * Base class for CMSIS and RTE project natures 
 */
public class CmsisProjectNature implements IProjectNature {

	protected String natureID;
	
	protected IProject project = null;
	
	public CmsisProjectNature(String id) {
		natureID = id;
	}
	
	@Override
	public void configure() throws CoreException {
		// does nothing
	}

	@Override
	public void deconfigure() throws CoreException {
		// does nothing
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;

	}
	
	public String getNatureId() {
		return natureID;
	}
	
	/**
	 * Checks if supplied project has given nature 
	 * @param project IProject to test 
	 * @param id nature ID
	 * @return true if nature is installed for this project
	 */
	public static boolean hasNature(IProject project, String id){
		try {
			if(project != null && project.isOpen() && project.hasNature(id))
				return true;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean hasNature(IProject project){
		return hasNature(project, natureID);
	}
}
