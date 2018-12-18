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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import com.arm.cmsis.pack.data.ICpExample;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.repository.RtePackJobResult;
import com.arm.cmsis.pack.utils.Utils;

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
	 * Installs packs with given IDs.
	 * Specify full id to install the specific version or a family ID to install the latest version.
	 * @param packIds collection of full or family pack IDs
	 */
	default void installPacks(Collection<String> packIds) {
		if(packIds == null || packIds.isEmpty())
			return;
		for(String packId : packIds) {
			installPack(packId);
		}
	};

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
	 * Check for updates in the internet and updates all packs asynchronously  
	 */
	default void updatePacksAsync() {/*default does nothing */}
	
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
	 * Extracts files from archive overwriting existing entries and setting read-only attributes
	 * @param archiveFile the source .zip file
	 * @param destPath the destination path
	 * @param monitor the monitor
	 * @return True if the unzip is successful, false otherwise
	 * @throws IOException
	 */
	default boolean unzip(File archiveFile, IPath destPath, IProgressMonitor monitor) throws IOException {
		if(archiveFile == null || destPath == null)
			return false;
		SubMonitor progress = SubMonitor.convert(monitor, Utils.getFilesCount(archiveFile));
		InputStream archiveStream = new FileInputStream(archiveFile);
		return unzip(archiveStream, destPath, true, true, progress);
	}
	
	/**
	 * Extracts files from archive 
	 * @param archiveInput InputStream for the source .zip file
	 * @param destPath the destination path
	 * @param overwrite boolean flag specifies if to overwrite existing entries
	 * @param setReadOnly boolean flag specifies if to sets read-only attribute the destination entries 
	 * @param monitor IProgressMonitor
	 * @return True if the unzip is successful, false otherwise
	 * @throws IOException
	 */
	default boolean unzip(InputStream archiveInput, IPath destPath, boolean overwrite, boolean setReadOnly, IProgressMonitor monitor) throws IOException {
		return unzipArchive(archiveInput, destPath, overwrite, setReadOnly, monitor);
	}
	
	/**
	 * Extracts files from archive 
	 * @param archiveInput InputStream for the source .zip file
	 * @param destPath the destination path
	 * @param overwrite boolean flag specifies if to overwrite existing entries
	 * @param setReadOnly boolean flag specifies if to sets read-only attribute the destination entries 
	 * @param monitor IProgressMonitor
	 * @return True if the unzip is successful, false otherwise
	 * @throws IOException
	 */
	public static boolean unzipArchive(InputStream archiveInput, IPath destPath, boolean overwrite, boolean setReadOnly, IProgressMonitor monitor) throws IOException {
		if(archiveInput == null || destPath == null)
			return false;
		if(monitor == null)
			monitor = new NullProgressMonitor();

		File destFile = destPath.toFile();
		if (overwrite && destFile.exists()) {
			Utils.deleteFolderRecursive(destFile);
		}

		int countBytes = 0;
		boolean result = true;
		ZipInputStream zipInput = new ZipInputStream(archiveInput);
		ZipEntry zipEntry = zipInput.getNextEntry();
		for (; zipEntry != null; zipEntry = zipInput.getNextEntry()) {
			if (monitor.isCanceled()) {
				result = false;
				break;
			}
			if (zipEntry.isDirectory())
				continue;
			String fileName = zipEntry.getName();
			IPath path = destPath.append(fileName);
			File outFile = new File(path.toOSString());
			if (!overwrite && outFile.exists()){
				continue;
			}
			if (!outFile.getParentFile().exists()) {
				outFile.getParentFile().mkdirs();
			}
			try {
				OutputStream output = new FileOutputStream(outFile);
				byte[] buf = new byte[4096]; // 4096 is a common NTFS block size
				int bytesRead;
				while ((bytesRead = zipInput.read(buf)) > 0) {
					output.write(buf, 0, bytesRead);
					countBytes += bytesRead;
				}
				output.close();
				if(setReadOnly)
					outFile.setReadOnly();
				monitor.worked(1);
			} catch (IOException e) {
				if (e.getMessage().contains("Access is denied")) {	//$NON-NLS-1$
					if (!outFile.exists()) {
						zipInput.closeEntry();
						zipInput.close();
						throw e;
					}
				}
			}				
		}
		zipInput.closeEntry();
		zipInput.close();
		if(countBytes  == 0) { // something went wrong, empty archive? 
			throw new IOException(); // caller adds message
		}
		return result;
	}
	

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

	
	/**
	 * Checks if the installer updates the packs
	 * @return true if updates
	 */
	default boolean isUpdatingPacks() { return false;}
	
	/**
	 * Checks if console and pop-up messages are suppressed 
	 * @return true if suppressed 
	 */
	default boolean isSuppressMessages() { return false; }
	
	/**
	 * Enables/disables console and pop-up messages
	 * @param bSuppress suppress messages flag
	 */
	default void setSuppressMessages(boolean bSuppress) { /* default does nothing*/}
	
}
