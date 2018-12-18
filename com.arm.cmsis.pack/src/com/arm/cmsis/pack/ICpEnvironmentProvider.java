/*******************************************************************************
* Copyright (c) 2016 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack;

import java.util.Optional;

import org.eclipse.core.runtime.IAdaptable;

import com.arm.cmsis.pack.build.IBuildSettings;
import com.arm.cmsis.pack.data.ICpExample;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.events.IRteEventListener;
import com.arm.cmsis.pack.info.ICpConfigurationInfo;

/**
 *  Interface to bind CMISIS plug-in with concrete environment.
 *  It extends IAdaptable interface to allow querying arbitrary environment-specific interfaces.
 */
public interface ICpEnvironmentProvider extends IRteEventListener, IAdaptable {

	/**
	 *  Context of environment element definition
	 */
	enum EnvironmentContext {
		DEVICE,   // device description context
		EXAMPLE,  // example description context
	};


	/**
	 * Returns environment name as it is used it CMSIS packs, e.g. <code>"uv"</code>, <code>"ds5"</code>, etc.
	 * @return environment name
	 */
	String getName();
	/**
	 * Method called from CpPlugIn.start() to initialize the provider and let the provider to register itself for RTE events if needed.<br>
	 * The method can also explicitly set environment-specific ICpPackManager or/and ICpPackInstaller to CpPlugIn
	 */
	void init();

	/**
	 * Method called from CpPlugIn.stop() to let the provider remove its listeners and perform clean-up
	 */
	void release();


	/**
	 * Returns environment-specific provider of  CMSIS Pack root directory
	 * @return ICpPackRootProvider or null (default) to use the default one
	 */
	default ICpPackRootProvider getCmsisRootProvider() {return null ;}


	/**
	 * Returns array of all environment names supported by this provider 
	 * @return array of supported names ordered by priority (highest to lowest), default returns this provider name 
	 */
	default String[] getSupportedNames() { return new String[]{getName()};} 


	/**
	 * Checks if a given environment is supported by this provider. A provider can support several environments.
	 * @param name environment name from an environment element in a pdsc file
	 * @param context EnvironmentContext
	 * @return true if an environment with given name is supported under given context
	 */
	boolean isEnvironmentSupported(String name, EnvironmentContext context);

	/**
	 * Check if given ICpItem is supported by this provider
	 * @param item ICpItem representing an element in pdsc file
	 * @return true if supported
	 */
	boolean isSupported(ICpItem item);

	
	/**
	 * Checks if given example is supported by this provider and can be instantiated.    
	 * @param example ICpExample to check
	 * @return true if supported
	 */
	boolean isExampleSupported(ICpExample example);
	
	
	/**
	 * Copies the example and/or creates an environment-specific project out of it
	 * @param example ICpExample to copy
	 * @return the adaptable object that is created from the example, for instance IProject
	 * @see #getImporter(ICpExample)
	 */
	IAdaptable copyExample(ICpExample example);

	
	/**
	 * Returns importer to import the supplied example
	 * @param example ICpExample to import
	 * @return ICpExampleImporter capable to import the example, null if none exists
	 */
	default ICpExampleImporter getImporter(ICpExample example) { return getDefaultImporter();}
	
	
	/**
	 * Returns default importer to import Eclipse-based examples (those that end on .project in the load path)
	 * @return ICpExampleImporter capable to import the example, null no default importer is supported
	 */
	default ICpExampleImporter getDefaultImporter() { return null;}
	
	/**
	 * Sets default importer to import Eclipse-based examples (those that end on .project in the load path).
	 * The purpose of this method is to avoid dependency on com.arm.cmmsis.pack.project plug-in.    
	 * @param ICpExampleImporter capable to serve as a default, null if no default importer is needed
	 */
	default void setDefaultImporter(ICpExampleImporter exampleImporter) {;}
	
	
    /**
     * Returns an optional id of a perspective to switch to after copying an
     * example.
     * 
     * @return An optional containing a perspective id.
     * @since 2.3.2
     */
    default Optional<String> getCopyExamplePerspectiveSwitchId() {
        return Optional.of("org.eclipse.cdt.ui.CPerspective"); //$NON-NLS-1$
    }

	/**
	 * Get the absolute load (source) path of supplied example, used when copying examples
	 * @param  example ICpExample to get path from 
	 * @return load path of this example, or <code>null</code> if example is not supported
	 */
	default String getAbsoluteLoadPath(ICpExample example) {
		if(example == null)
			return null;
		return example.getAbsoluteLoadPath(getName());
	}
	
	/**
	 * Get the primary supported environment of supplied example, used when copying examples
	 * @param  example ICpExample to get environmant from 
	 * @return primary environment of this example, or <code>null</code> if example is not supported
	 */
	default String getEnvironment(ICpExample example) {
		if(example == null)
			return null;
		return getName();
	}
	
	/**
	 * Contributes to newly created CMSIS RTE project. It could be an additional project nature, additional files, etc.
	 * @param projectName name of the created project, can be used to obtain IProject or IRteProject 
	 * @note The method is called when the IRteProject is already created   
	 */
	default void contibuteToNewProject(String projectName) {;}
	
	/**
	 * Adjusts build settings every time RTE changes
	 * @param buildSettings IBuildSettings to adjust
	 * @param configInfo ICpConfigurationInfo object describing RTE configuration
	 */
	void adjustBuildSettings(IBuildSettings buildSettings, ICpConfigurationInfo configInfo);

	/**
	 * Adjusts initial build settings
	 * @param buildSettings IBuildSettings to adjust
	 * @param configInfo ICpConfigurationInfo object describing RTE configuration
	 */
	void adjustInitialBuildSettings(IBuildSettings buildSettings, ICpConfigurationInfo configInfo);

	/**
	 * Expand key sequences, place-holders and environment variables to those that Eclipse understands
	 * @param input string to expand
	 * @param configInfo ICpConfigurationInfo
	 * @param bAsolute boolean flag if to expand to an absolute value (true) or an Eclipse variable (false)
	 * @return expanded string
	 */
	String expandString(String input, ICpConfigurationInfo configInfo, boolean bAsolute);
}
