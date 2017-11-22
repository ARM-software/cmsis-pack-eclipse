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

package com.arm.cmsis.pack.data;

import java.util.Collection;

/**
 *  Interface describing <code>generator</code> item
 */
public interface ICpGenerator extends ICpItem {


	/**
	 * Returns gpdsc file as it is written in pdsc
	 * @return gpdsc string
	 */
	String getGpdsc();

	
	/**
	 * Returns generator working directory as written in pdsc file (raw)
	 * @return working directory string
	 */
	String getWorkingDir();


	/**
	 * Returns generator command item for requested type
     * @param type one of "exe", "web" or "eclipse", if empty "exe" is assumed   
	 * @return {@link ICpItem} for requested command type or null if not found
	 */
	ICpItem getCommand(String type); 

	/**
     * Returns collection of arguments for requested type and running host 
     * @param type one of "exe", "web" or "eclipse", if empty "exe" is assumed  
     * @return collection of command line arguments 
     */
    Collection<ICpItem> getArguments(String type);

    
	/**
     * Returns collection of available generator types  
     * @return collection of available generator types 
     */
    Collection<String> getAvailableTypes();

    
}
