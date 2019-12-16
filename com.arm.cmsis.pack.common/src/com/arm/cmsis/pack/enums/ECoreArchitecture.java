/*******************************************************************************
* Copyright (c) 2018 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.enums;

/**
 *  Enum describing processor core architecture 
 */
public enum ECoreArchitecture {
	
	UNKNOWN, // not yet defined or invalid
	MIXED,   // system contains cores with different architectures 
	ARMv7,   // default one, also assumed for Coretx-M0 that is actually ARMv6
	ARMv8;
	
	/*
	 * Creates enum value from string
	 * @param core value of <code>"Dcore"</code> processor attribute 
	 * @return corresponding enumeration value
	 */
	public static ECoreArchitecture fromString(final String core) {
		if(core == null)
			return UNKNOWN;
		if(core.startsWith("ARMV8")) { //$NON-NLS-1$
			return ARMv8;
		}
		switch(core) {
		case "Cortex-M33": //$NON-NLS-1$
		case "Cortex-M23": //$NON-NLS-1$
			return ARMv8;
		case "Cortex-M0": //$NON-NLS-1$
		case "Cortex-M0+"://$NON-NLS-1$
		case "Cortex-M3": //$NON-NLS-1$
		case "Cortex-M4": //$NON-NLS-1$
		case "Cortex-M7": //$NON-NLS-1$
		case "SC000": 	  //$NON-NLS-1$
		case "SC300":	  //$NON-NLS-1$
			return ARMv7;
		}
		return ARMv8; // ARMv8 is default for all other core types
	}
	
}
