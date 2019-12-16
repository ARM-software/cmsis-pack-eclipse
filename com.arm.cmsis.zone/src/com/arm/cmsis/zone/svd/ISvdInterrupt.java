/*******************************************************************************
* Copyright (c) 2017 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/
package com.arm.cmsis.zone.svd;

import com.arm.cmsis.pack.data.ICpMemory;

/**
 * Interrupt description in SVD file  
 */
public interface ISvdInterrupt extends ISvdItem, ICpMemory {
	
	/**
	 * Returns interrupt value as string
	 * @return value as string 
	 */
	String getValueString();
}
