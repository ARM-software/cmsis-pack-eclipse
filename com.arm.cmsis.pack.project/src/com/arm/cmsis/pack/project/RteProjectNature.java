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
 *
 */
public class RteProjectNature implements IProjectNature {

	public static final String RTE_NATURE_ID = "com.arm.cmsis.pack.project.RteNature";   //$NON-NLS-1$

	
	private IProject project = null;
	
	public RteProjectNature() {
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
	
	
	/**
	 * Checks if supplied project has RteNature 
	 * @param project IProject to test 
	 * @return true if RteNature is installed for this project
	 */
	public static boolean hasRteNature(IProject project){
		try {
			if(project != null && project.isOpen() && project.hasNature(RTE_NATURE_ID))
				return true;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
	}

}
