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

package com.arm.cmsis.pack;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpBoard;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPackCollection;
import com.arm.cmsis.pack.data.ICpPackFamily;
import com.arm.cmsis.pack.events.IRteEventListener;
import com.arm.cmsis.pack.events.IRteEventProxy;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.parser.ICpXmlParser;
import com.arm.cmsis.pack.repository.CpRepositoryList;
import com.arm.cmsis.pack.rte.boards.IRteBoardItem;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;
import com.arm.cmsis.pack.rte.examples.IRteExampleItem;

/**
 *  Interface to a Pack manager responsible for loading CMSIS-Packs
 */
public interface ICpPackManager extends IRteEventListener {

	/**
	 * Sets ICpPackInstaller to be used by this manger to install packs
	 * @param packInstaller ICpPackInstaller object
	 */
	void setPackInstaller(ICpPackInstaller packInstaller);


	/**
	 * Returns ICpPackInstaller object set by setPackInstaller()
	 * @return ICpPackInstaller object or null if none has been set
	 * @see #setRteEventProxy(IRteEventProxy)
	 */
	ICpPackInstaller getPackInstaller();


	/**
	 * Initializes XML parser, optionally sets XSD schema file to use
	 * @param xsdFile schema file or null if no schema check should be used
	 * @return true if successful
	 */
	boolean initParser(String xsdFile);


	/**
	 * Sets XML parser to be used to load packs
	 * @param xmlParser parser to use
	 */
	void setParser(ICpXmlParser xmlParser);

	/**
	 * Returns current XML parser used to load packs
	 * @return current XML parser
	 */
	ICpXmlParser getParser();


	/**
	 *  Clears the manager
	 */
	void clear();


	/**
	 *  Clears the manager and deletes the parser
	 */
	void destroy();

	/**
	 * Returns collection of all the packs
	 * @return all packs as ICpPackCollection
	 */
	ICpPackCollection getPacks();

	/**
	 * Returns collection of the installed packs
	 * @return collection of the installed packs
	 */
	ICpPackCollection getInstalledPacks();

	/**
	 * Returns collection of the device-specific packs
	 * @return all device specific installed packs as ICpPackCollection
	 */
	ICpPackCollection getDevicePacks();

	/**
	 * Returns collection of the generic packs
	 * @return all generic installed packs as ICpPackCollection
	 */
	ICpPackCollection getGenericPacks();

	/**
	 * Returns collection of the error packs
	 * @return all error packs as ICpPackCollection
	 */
	ICpPackFamily getErrorPacks();

	/**
	 * Returns hierarchical collection of all devices found in all packs
	 * @return device collection as IRteDeviceItem
	 */
	IRteDeviceItem getDevices();

	/**
	 * Returns hierarchical collection of all devices found in installed packs
	 * @return
	 */
	IRteDeviceItem getInstalledDevices();

	/**
	 * Returns collection of all board descriptions found in installed packs
	 * @return map of boards - id to ICpBoard item
	 */
	Map<String, ICpBoard> getBoards();

	/**
	 * Returns ICpBoard for supplied board ID
	 * @param boardId board ID string
	 * @return ICpBoard object or null if not found
	 */
	ICpBoard getBoard(String boardId);

	/**
	 * Returns collection of all items, which contains
	 * all mounted and compatible devices
	 * @return IRteBoardItem root
	 */
	IRteBoardItem getRteBoards();


	/**
	 * Returns collection of boards that contain mounted or compatible device matching suppled device attributes
	 * @return collection of compatible boards
	 */
	Collection<ICpBoard> getCompatibleBoards(IAttributes deviceAttributes);


	/**
	 * Returns collection of all available example descriptions
	 * @return IRteExampleItem
	 */
	IRteExampleItem getExamples();

	/**
	 * Loads packs found in a supplied directory and sub-directories (up to 3 levels deep)
	 * @param rootDirectory directory to search for pdsc files
	 * @return true if all packs loaded successfully
	 */
	boolean loadPacks(String rootDirectory);

	/**
	 * Loads specified pdsc files
	 * @param fileNames collection of pdsc files to load
	 * @return true if all packs loaded successfully
	 */
	boolean loadPacks(Collection<String> fileNames);

	/**
	 * Parses  a single pdsc file
	 * @param absolute file pdsc file to load
	 * @return {@link ICpPack} is successful, null otherwise
	 */
	ICpPack readPack(String file);


	/**
	 * Readfs and loads a single gpdsc file if it is not yet loaded
	 * @param file absolute gpdsc file to load
	 * @return ICpPack if loaded successfully, null otherwise
	 */
	ICpPack loadGpdsc(String file);


	/**
	 * Returns CMSIS-Pack directory to load packs from
	 * @return the CMSIS-Pack directory
	 */
	String getCmsisPackRootDirectory();

	/**
	 * Return CMSIS-Pack Download Directory
	 * @return absolute download Directory of all the Packs
	 */
	default String getCmsisPackDownloadDir() {
		String root = getCmsisPackRootDirectory();
		if(root != null) {
			IPath path = new Path(root).append(CmsisConstants.DOT_DOWNLOAD);
			File f = path.toFile();
			if (!f.exists()) {
				f.mkdirs();
			}
			return path.toOSString();
		}
		return null;
	}

	/**
	 * Return CMSIS-Pack Web directory (available packs)
	 * @return absolute web directory of all the Packs
	 */
	default String getCmsisPackWebDir() {
		String root = getCmsisPackRootDirectory();
		if(root != null) {
			IPath path = new Path(root).append(CmsisConstants.DOT_WEB);
			File f = path.toFile();
			if (!f.exists()) {
				f.mkdirs();
			}
			return path.toOSString();
		}
		return null;
	}

	/**
	 * Return CMSIS-Pack Local directory (local packs)
	 * @return absolute local directory of all the Packs
	 */
	default String getCmsisPackLocalDir() {
		String root = getCmsisPackRootDirectory();
		if(root != null) {
			IPath path = new Path(root).append(CmsisConstants.DOT_LOCAL);
			File f = path.toFile();
			if (!f.exists()) {
				f.mkdirs();
			}
			return path.toOSString();
		}
		return null;
	}

	/**
	 * Returns CMSIS-Pack directory as URI
	 * @return CMSIS-Pack directory as URI
	 */
	URI getCmsisPackRootURI();

	/**
	 * Returns the list of CMSIS-Pack repository
	 * @return the list of CMSIS-Pack repository
	 */
	CpRepositoryList getCpRepositoryList();

	/**
	 * Sets CMSIS-Pack root directory to load packs from
	 * @param packRootDirectory pack root directory
	 */
	void setCmsisPackRootDirectory(String packRootDirectory);

	/**
	 * Checks is packs are already loaded
	 * @return true if packs are already loaded
	 */
	boolean arePacksLoaded();

	/**
	 *  Triggers reload of the pack descriptions
	 */
	void reload();

	/**
	 * Check if all packs required by supplied pack are installed
	 * @return true if all required packs are installed
	 */
	boolean isRequiredPacksInstalled(ICpPack pack);

	/**
	 * Check if pack is a local repository. Local repository means that
	 * pack is installed outside RTE root path.
	 * @return true if pack is a local repository
	 */
	boolean isLocalRepository(ICpPack pack);

	/**
	 * Checks if search for pack updates is pending
	 * @return true if search for pack updates is pending
	 */
	boolean isCheckForUpdates();

	/**
	 * Sets flag to schedule check for updates
	 * @param bCheck flag if to check for updates
	 */
	void setCheckForUpdates(boolean bCheck);


	/**
	 * Checks if pack is published on the web (does have a pdsc entry in .web folder)
	 * @param pack ICpPack to check
	 * @return true if pack is on web
	 */
	default boolean isWebPack(ICpPack pack) {
		if(pack == null)
			return false;
		String webPdscFileName = getCmsisPackWebDir() + '/' + pack.getPackFamilyId() + CmsisConstants.EXT_PDSC;
		File webPdscFile = new File(webPdscFileName);
		return webPdscFile.exists();
	}

	/**
	 * Checks if pack is a local one (does not have a pdsc entry in .web folder)
	 * @param pack ICpPack to check
	 * @return true if pack is local
	 */
	default boolean isLocalPack(ICpPack pack) {
		return pack != null && !isWebPack(pack);
	}


}