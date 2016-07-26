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


/**
 * Interface describing example
 */
public interface ICpExample extends ICpItem {

	/**
	 * Get the folder pathname of this example
	 * @return the folder pathname of this example, or null if it does not exist
	 */
	String getFolder();

	/**
	 * Get the load (source) path of this example relative to getFolder()
	 * @param environmentName name of environment element to get the path 
	 * @return load path of this example, or <code>null</code> if it doesn't exist
	 */
	String getLoadPath(String environmentName);

	/**
	 * Get the absolute load (source) path of this example, used when copying examples
	 * @param environmentName name of environment element to get the path 
	 * @return load path of this example, or <code>null</code> if it doesn't exist
	 */
	String getAbsoluteLoadPath(String environmentName);
	
	/**
	 * Get the board of this example
	 * @return the board of this example, or null if it does not exist
	 */
	ICpBoard getBoard();
}
