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

import java.util.Collection;
import java.util.Map;

import com.arm.cmsis.pack.data.ICpBoard;
import com.arm.cmsis.pack.data.ICpExample;
import com.arm.cmsis.pack.data.ICpPackCollection;
import com.arm.cmsis.pack.events.IRteEventProxy;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.parser.ICpXmlParser;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;

/**
 *  Interface to a Pack manager responsible for loading CMSIS-Packs  
 */ 
public interface ICpPackManager {


	/**
	 * Sets IRteEventProxy to be used by this manger to fire notifications 
	 * @param rteEventProxy IRteEventProxy object 
	 */
	void setRteEventProxy(IRteEventProxy rteEventProxy);

	/**
	 * Returns IRteEventProxy object set by setRteEventProxy()
	 * @return IRteEventProxy object or null if none has been set
	 * @see #setRteEventProxy(IRteEventProxy)
	 */
	IRteEventProxy getRteEventProxy();


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
	 * Returns collection of the installed packs
	 * @return all installed packs as ICpPackCollection
	 */
	ICpPackCollection getPacks();

	/**
	 * Returns collection of the installed packs
	 * @return all installed packs as ICpPackCollection
	 */
	ICpPackCollection getDevicePacks();

	/**
	 * Returns collection of the installed packs
	 * @return all installed packs as ICpPackCollection
	 */
	ICpPackCollection getGenericPacks();

	/**
	 * Returns hierarchical collection of all devices found in installed packs 
	 * @return device collection as IRteDeviceItem
	 */
	IRteDeviceItem getDevices();


	/**
	 * Returns collection of all board descriptions found in installed packs
	 * @return map of boards - id to ICpBoard item
	 */
	Map<String, ICpBoard> getBoards();


	/**
	 * Returns collection of boards that contain mounted or compatible device matching suppled device attributes
	 * @return collection of compatible boards
	 */
	Collection<ICpBoard> getCompatibleBoards(IAttributes deviceAttributes);


	/**
	 * Returns collection of all example descriptions found in installed packs
	 * @return
	 */
	Collection<ICpExample> getExamples();

	/**
	 * Loads packs found in a supplied directory and sub-directories (up to 3 levels deep) 
	 * @param rootDirectory directory to search for pdsc files 
	 * @return true if all packs loaded successfully
	 */
	boolean loadPacks(String rootDirectory);

	/**
	 * Loads a list of pdsc files 
	 * @param fileNames collection of pdsc files to load
	 * @return true if all packs loaded successfully
	 */
	boolean loadPacks(Collection<String> fileNames);

	/**
	 * Loads a single pdsc file
	 * @param file pdsc file to load
	 * @return true if loaded successfully
	 */
	boolean loadPack(String file);

	/**
	 * Returns CMSIS-Pack directory to load packs from 
	 * @return the defaultPackDirectory
	 */
	String getCmsisPackRootDirectory();

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
	 *  Triggers reload of the pack if the have already been loaded
	 */
	void reload();

}