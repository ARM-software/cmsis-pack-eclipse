/*******************************************************************************
* Copyright (c) 2016-2018 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;

import com.arm.cmsis.pack.common.CmsisConstants;

/**
 *  Interface to provide default value and content of CMSIS Pack root directory, specify remote server  
 */
public interface ICpPackRootProvider {

	/**
	 * Returns default value for CMSIS Pack root directory as absolute path
	 * @return CMSIS Pack root directory or null if the directory should be set by the user explicitly
	 */
	default String getPackRoot() { return null;}
	
	
	/**
	 * Checks if the user can edit CMSIS Pack root preference supplied by the provider 
	 * @return true if the user can edit the preference  
	 */
	default boolean isUserEditable() { return true; }
	

	/**
	 * Returns remote location to download index.pidx file  
	 * @return URL without index.pidx, default returns ""http://www.keil.com/pack/index.pidx"  
	 */
	default String getPackIndexUrl() { return CmsisConstants.REPO_KEIL_INDEX_URL; }

	
	/**
	 * Returns remote location to download pdsc files, return null to use URLs from index.pidx file   
	 * @return URL without to download pdsc files, null if to use URLs from index.pidx       
	 */
	default String getPackPdscUrl() { return CmsisConstants.REPO_KEIL_PACK_SERVER; }


	/**
	 * Method to initialize or update CMSIS pack root directory.<br>
	 * It is called when the CMSIS Pack root directory is set (on startup or changed in preferences)<br>
	 * The method can copy or update pre-installed packs or its descriptions.<br>
	 * One of the most command task is to populate .Web directory by coping index.pindx and pdsc files there in order to reduce initial pack update time<br> 
	 * The client is responsible to check if the directory already contains required data and perform the actions accordingly<br>
	 * Default does nothing.  
	 * @param cmsisPackRootDirectory CMSIS pack root directory to initialize/update
	 * @throws IOException
	 */
	default void initPackRoot(String cmsisPackRootDirectory, IProgressMonitor monitor) throws IOException {;}

}
