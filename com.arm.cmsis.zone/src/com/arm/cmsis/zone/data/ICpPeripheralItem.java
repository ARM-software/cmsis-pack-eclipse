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

package com.arm.cmsis.zone.data;

import java.util.Collection;

/**
 * Base interface to peripherals and peripheral groups
 */
public interface ICpPeripheralItem extends ICpMemoryRegion {

	/**
	 * Returns collection of register setup elements associated with peripheral  
	 * @return Collection<ICpPeripheralSetup>
	 */
	default Collection<ICpPeripheralSetup> getPeripheralSetups() {
		return getChildrenOfType(ICpPeripheralSetup.class);
	}
	
}
