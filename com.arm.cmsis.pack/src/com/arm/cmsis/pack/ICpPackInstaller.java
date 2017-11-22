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

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import com.arm.cmsis.pack.data.ICpExample;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.repository.RtePackJobResult;

/**
 *  Interface responsible for installing/uninstalling packs
 */
public interface ICpPackInstaller extends IAdaptable {

	/**
	 * Type of Pack Manager's Console output
	 * <dl>
	 * 	<dd>OUTPUT output message
	 *  <dd>INFO information message
	 *	<dd>ERROR error message
	 *	<dd>WARNING warning message
	 * </dl>
	 */
	enum ConsoleType {
		OUTPUT,
		INFO,
		ERROR,
		WARNING
	}

	/**
	 * Installs pack with given ID.
	 * Specify full id to install the specific version or a family ID to install the latest version.
	 * @param packId full or family pack ID
	 */
	void installPack(final String packId);

	/**
	 * Installs pack with given ID.
	 * Specify full id to install the specific version or a family ID to install the latest version.
	 * @param packId full or family pack ID
	 * @param installRequiredPacks True if the required pack should also be installed
	 */
	void installPack(final String packId, boolean installRequiredPacks);

	/**
	 * Installs pack with given attributes, now only used when
	 * refreshing RTE project.
	 * @param packAttributes pack attributes
	 */
	void installPack(final IAttributes packAttributes);

	/**
	 * Installs required packs of the given pack.
	 * @param pack Pack whose required packs needs to be installed
	 * @return A collection of the required packs' IDs to be installed
	 */
	Collection<String> installRequiredPacks(ICpPack pack);

	/**
	 * Imports .pack file
	 * @param filePath full path of the .pack file
	 */
	void importPack(final String filePath);

	/**
	 * Import packs from a folder
	 * @param rootPath Root path of the folder
	 */
	void importFolderPacks(final String rootPath);

	/**
	 * Check for updates in the internet
	 * @param monitor the progress monitor
	 */
	void updatePacks(IProgressMonitor monitor);

	/**
	 * Uninstalls installed pack
	 * @param pack installed ICpPack to remove
	 * @param delete true if also delete the .pack file in ${cmsis_root_folder}/.Download folder
	 */
	void removePack(ICpPack pack, boolean delete);

	/**
	 * Copy the example to the workspace
	 * @param example ICpExample to copy
	 * @return the adaptable object that is created from the example, for instance IProject
	 */
	IAdaptable copyExample(ICpExample example);

	/**
	 * @param archiveFile the source .zip file
	 * @param destPath the destination path
	 * @param monitor the monitor
	 * @return True if the unzip is successful, false otherwise
	 * @throws IOException
	 */
	boolean unzip(File archiveFile, IPath destPath, IProgressMonitor monitor) throws IOException;

	/**
	 * The Actions to take after a pack job is finished
	 * @param jobId the job's ID, could be a pack ID
	 * @param jobTopic the job topic, e.g. install, unpack, remove, ...
	 * @param jobData the help data to this job
	 */
	void jobFinished(String jobId, String jobTopic, RtePackJobResult jobData);

	/**
	 * Set the repository service provider {@link ICpRepoServiceProvider}
	 * @param repoServiceProvider
	 */
	void setRepoServiceProvider(ICpRepoServiceProvider repoServiceProvider);

	/** Get the repository service provider {@link ICpRepoServiceProvider}
	 * @return the repository service provider
	 */
	ICpRepoServiceProvider getRepoServiceProvider();

	/**
	 * Returen true if there is job going on
	 * @return true if there is job going on
	 */
	boolean isBusy();

	/**
	 * checks if the pack installer is processing pack
	 * @param packId the pack's id
	 * @return true if the pack with this pack id is being processed
	 */
	boolean isProcessing(String packId);

	/**
	 * checks if the pack installer is processing pack
	 * @param packAttributes pack attributes
	 * @return true if the pack with this pack id is being processed
	 */
	boolean isProcessing(final IAttributes packAttributes);

	/**
	 * Print the message in the console
	 * @param message the message to print in the console
	 * @param type the type of the message to print
	 */
	void printInConsole(String message, ConsoleType type);

	/**
	 * Cancel all the processing jobs
	 */
	void reset();

}
