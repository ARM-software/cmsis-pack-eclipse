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

package com.arm.cmsis.pack.data;

/**
 * Interface for pack family: collection of pack with the same name, but different versions
 * 
 */
public interface ICpPackFamily extends ICpPackGroup {
	
	/**
	 * Returns Pack releases, those that are not found in collection returned by getPacks()  
	 * @return an ICpItem that contains collection of previous pack releases 
	 * @see #getPacks()
	 */
	ICpItem getPreviousReleases();
	
}
