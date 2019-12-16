/*******************************************************************************
* Copyright (c) 2019 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.zone.project;

import com.arm.cmsis.pack.error.ICmsisErrorCollection;
import com.arm.cmsis.zone.data.ICpRootZone;

/**
 *  An  interface to validate CMSIS Zone models  
 */
public interface ICmsisZoneValidator extends ICmsisErrorCollection{
	
	/**
	 * Validates supplied azone   
	 * @param aZone ICpRootZone representing aZone
	 * @return true if valid 
	 */
	boolean validate(ICpRootZone aZone);

}