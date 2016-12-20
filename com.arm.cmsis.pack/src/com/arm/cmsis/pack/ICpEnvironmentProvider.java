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
	 * @return ICpPackRootProvider or null to use the default one
	 */
	ICpPackRootProvider getCmsisRootProvider();


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
	 * Copies the example and/or creates an environment-specific project out of it
	 * @param example ICpExample to copy
	 * @return the adaptable object that is created from the example, for instance IProject
	 */
	IAdaptable copyExample(ICpExample example);

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
