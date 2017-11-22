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

/**
 *
 */
public class RteProjectNature extends CmsisProjectNature {

	public static final String RTE_NATURE_ID = "com.arm.cmsis.pack.project.RteNature";   //$NON-NLS-1$

	public RteProjectNature() {
		super(RTE_NATURE_ID);
	}
	
	/**
	 * Checks if supplied project has RteNature 
	 * @param project IProject to test 
	 * @return true if RteNature is installed for this project
	 */
	public static boolean hasRteNature(IProject project){
		return hasNature(project, RTE_NATURE_ID);
	}

}
