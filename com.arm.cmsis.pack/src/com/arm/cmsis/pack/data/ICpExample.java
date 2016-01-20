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
	 * Get the board of this example
	 * @return the board of this example, or null if it does not exist
	 */
	ICpBoard getBoard();
}
