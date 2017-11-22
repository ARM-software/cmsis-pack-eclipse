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

import com.arm.cmsis.pack.enums.EDebugProtocolType;

/**
 *  Convenience interface to access information under "swd", "jtag" and "cjtag" device port properties   
 */
public interface ICpDebugProtocol extends ICpDeviceProperty {
	
	/**
	 * Returns protocol type as enumerated value
	 * @return EDebugProtocolType
	 */
	EDebugProtocolType getProtocolType();

	/**
	 * IDCODE , 0 if not specified
	 * @return IDCODE code as long value
	 */
	long getIdCode(); 
		
	/**
	 * TARGETSEL value for DP v2 with multi-drop, 0 if not specified
	 * @return TARGETSEL as long value  
	 */
	long getTargetSel();

	/**
	 * JTAG: index of the TAP in the JTAG chain, default is 0
	 * @return TAP index as long value
	 */
	int getTapIndex(); 

	/**
	 * JTAG : Instruction register length, 0 if not specified
	 * @return instruction register length, 
	 */
	int getIRlen(); 
	

}
