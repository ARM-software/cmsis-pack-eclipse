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

/**
 *  Interface to provide default value of CMSIS Pack root directory 
 */
public interface ICpPackRootProvider {

	/**
	 * Returns default value for CMSIS Pack root directory
	 * @return CMSIS Pack root directory
	 */
	String getPackRoot();
	
}
