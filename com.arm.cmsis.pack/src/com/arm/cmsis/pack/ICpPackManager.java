/*******************************************************************************
* Copyright (c) 2014 ARM Ltd.
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/

package com.arm.cmsis.pack;

import java.util.Collection;

import com.arm.cmsis.pack.data.ICpPackCollection;
import com.arm.cmsis.pack.events.IRteEventProxy;
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
	 * Returns hierarchical collection of all devices found in installed packs 
	 * @return device collection as IRteDeviceItem
	 */
	IRteDeviceItem getDevices();

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
	 * Returns default CMSIS-Pack directory to load packs from 
	 * @return the defaultPackDirectory
	 */
	String getDefaultPackDirectory();

	/**
	 * Sets default CMSIS-Pack directory to load packs from
	 * @param defaultPackDirectory default directory to load packs from
	 */
	 void setDefaultPackDirectory(String defaultPackDirectory);

	/**
	 * Checks is packs are already loaded
	 * @return true if packs are already loaded
	 */
	boolean arePacksLoaded();

}