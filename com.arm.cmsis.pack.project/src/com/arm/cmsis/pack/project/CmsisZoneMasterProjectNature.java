/*******************************************************************************
* Copyright (c) 2017 ARM Ltd. and others
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
public class CmsisZoneMasterProjectNature extends CmsisProjectNature {

	public static final String CMSIS_ZONE_MASTER_NATURE_ID = "com.arm.cmsis.pack.project.CmsisZoneMasterProjectNature";   //$NON-NLS-1$

	public CmsisZoneMasterProjectNature() {
		super(CMSIS_ZONE_MASTER_NATURE_ID);
	}
	
	/**
	 * Checks if supplied project has CmsisZoneMasterProjectNature 
	 * @param project IProject to test 
	 * @return true if CmsisZoneMasterProjectNature is installed for this project
	 */
	public static boolean hasCmsisZoneMasterNature(IProject project){
		return hasNature(project, CMSIS_ZONE_MASTER_NATURE_ID);
	}

}
