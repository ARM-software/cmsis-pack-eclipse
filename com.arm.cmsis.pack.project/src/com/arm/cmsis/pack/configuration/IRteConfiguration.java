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

package com.arm.cmsis.pack.configuration;

import java.util.Collection;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;

import com.arm.cmsis.pack.build.IBuildSettings;
import com.arm.cmsis.pack.data.ICpCodeTemplate;
import com.arm.cmsis.pack.data.ICpDebugConfiguration;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.enums.IEvaluationResult;
import com.arm.cmsis.pack.info.ICpComponentInfo;
import com.arm.cmsis.pack.info.ICpConfigurationInfo;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.info.ICpFileInfo;
import com.arm.cmsis.pack.info.ICpPackInfo;

/**
 * The interface provides data needed for project update and build system   
 */
public interface IRteConfiguration extends IAdaptable, IEvaluationResult {
	
	/**
	 * Returns underlying ICpConfigurationInfo object
	 * @return underlying ICpConfigurationInfo
	 */
	ICpConfigurationInfo getConfigurationInfo();
	
	
	/**
	 * Sets ICpConfigurationInfo object, initializes model and collects settings
	 * @return ICpConfigurationInfo read from .rteconfig file
	 */
	void setConfigurationInfo(ICpConfigurationInfo info);

	/**
	 * Returns device info stored in the configuration
	 * @return ICpDeviceInfo stored in the configuration
	 */
	ICpDeviceInfo getDeviceInfo();

	/**
	 * Returns device debug configuration for associated processor 
	 * @return ICpDebugConfiguration 
	 */
	ICpDebugConfiguration getDebugConfiguration();
	
	/**
	 * Returns collection of files to add to project  
	 * @return map of files to add to project: project relative path to ICpFileInfo 
	 */
	Map<String, ICpFileInfo> getProjectFiles();
	
	
	/**
	 * Returns ICpFileInfo associated with project file resource  
	 * @param fileName project relative path of a file 
	 * @return ICpFileInfo if exists
	 */
	ICpFileInfo getProjectFileInfo(String fileName);
	
	/**
	 * Returns ICpFileInfos associated with project file resource  
	 * @param fileName project relative path of a file (can contain *)
	 * @return ICpFileInfos if exists
	 */
	ICpFileInfo[] getProjectFileInfos(String fileName);
	
	/**
	 * Checks if file needs to be added to project (will appear in Project Explorer view) 
	 * @param fi ICpFileInfo that represents file to check 
	 * @return true if file is to be added to project
	 */
	boolean isAddToProject(ICpFileInfo fi);

	
	/**
	 * Returns IBuildSettings to set IConfiguration build options via toolchain adapter 
	 * @return IBuildSettings
	 */
	IBuildSettings getBuildSettings();
	

	/**
	 * Returns paths to library sources (for use by debugger/indexer) 
	 * @return collection of library source paths
	 */
	Collection<String> getLibSourcePaths();

	/**
	 * Return collection of strings to be placed in RteComponents.h file 
	 * @return collection of strings to be copied to RteComponents.h
	 */
	Collection<String> getRteComponentsHCode();

	/**
	 * Returns collection of headers with component names they come from
	 * @return map of header file names (without path) to comment
	 */
	Map<String, String> getHeaders();

	/**
	 * Documents and links relevant for the configuration
	 * @return map of documents: url to title
	 */
	Map<String, String> getDocs();

	/**
	 * Returns device header name
	 * @return device header name 
	 */
	String getDeviceHeader();
	
	/**
	 * Returns SVD (System View Description) file
	 * @return SVD file 
	 */
	String getSvdFile();
	
	/**
	 * Returns Device Family Pack used by configuration   
	 * @return ICpPack if DFP is installed or null   
	 */
	ICpPack getDfp();

	/**
	 * Returns path to the directory where Device Family Pack is installed   
	 * @return path to DFP installation directory or null if DFP is not installed  
	 */
	String getDfpPath();
	
	/**
	 * Returns startup component used by configuration (Cclass="Device", Cgroup="Startup", Csub="") 
	 * @return startup ICpComponentInfo or null if not used 
	 */
	ICpComponentInfo getDeviceStartupComponent();
	
	/**
	 * Returns CMSIS Core component used by configuration (Cclass="CMSIS", Cgroup="Core", CSub="") 
	 * @return CMSIS Core ICpComponentInfo or null if not used
	 */
	ICpComponentInfo getCmsisCoreComponent();

	/**
	 * Returns CMSIS RTOS component used by configuration (Cclass="CMSIS", Cgroup="RTOS" ) 
	 * @return CMSIS RTOS ICpComponentInfo  or null if not used
	 */
	ICpComponentInfo getCmsisRtosComponent();
	
	/**
	 * Returns the root of CMSIS User Code Template
	 * @return Root of CMSIS User Code Template
	 */
	ICpCodeTemplate getCmsisCodeTemplate();
	
	/**
	 * Returns a list of missing packs
	 * @return A list of missing packs
	 */
	Collection<ICpPackInfo> getMissingPacks();
	
	/**
	 * Check if the configuration is valid - device and all components are resolved
	 * @return true if configuration is valid
	 * @see #validate()
	 */
	boolean isValid();
	
	/**
	 * Validates loaded configuration and reports error messages
	 * @return collection of error messages or null if validation is successful
	 * @see #isValid()
	 */
	Collection<String> validate();
	
	
	/**
	 * Checks if the configuration requires given gpdsc file 
	 * @param gpdsc absolute gpdsc file name   
	 * @return true if given gpdsc file is needed  
	 */
	boolean isGeneratedPackUsed(String gpdsc);

	
}
