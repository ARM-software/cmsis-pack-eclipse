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
	 * Returns generator command as written in pdsc file (raw)
	 * @return command string
	 */
	String getCommand(); 
	
	/**
	 * Returns generator working directory as written in pdsc file (raw)
	 * @return working directory string
	 */
	String getWorkingDir();
	
	/**
	 * Returns gpdsc file as it is written in pdsc
	 * @return gpdsc string
	 */
	String getGpdsc();
	
    /**
     * Returns collection of argument items
     * @return collection of arguments 
     */
    Collection<? extends ICpItem> getArguments();
    
    /**
     * Returns fully expanded command line with arguments
     * @return fully qualified command line string
     */
    String getExpandedCommandLine(ICpStringExpander expander);
    
    /**
     * Returns absolute gpdsc filename
     * @return absolute gpdsc filename
     */
    String getExpandedGpdsc(ICpStringExpander expander);
    
    /**
     * Returns absolute working directory path  
     * @return absolute working directory path
     */
    String getExpandedWorkingDir(ICpStringExpander expander);
	
	
}
