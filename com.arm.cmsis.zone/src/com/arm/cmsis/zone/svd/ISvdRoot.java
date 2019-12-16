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

import java.util.Map;

/**
 * Base item for SVD file parser  
 *   
 */
public interface ISvdRoot extends ISvdItem{

	/**
	 * Returns peripheral corresponding given name 
	 * @param name peripheral name
	 * @return
	 */
	ISvdPeripheral getPeripheral(String name);
	
	/**
	 * Returns full peripherals map
	 * @return map of ISvdPeripheral objects
	 */
	Map<String, ISvdPeripheral> getPeripheralMap();
	
}
