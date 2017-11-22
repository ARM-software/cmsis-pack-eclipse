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

package com.arm.cmsis.pack.rte.devices;

import com.arm.cmsis.pack.data.ICpPack;

/**
 * Interface base element for Device tree elements: vendor, family, sub-family, device, variant, processor
 * This hierarchy is similar to ICpDeviceItem one, but works across Packs
 */
public interface IRteDeviceRoot extends IRteDeviceItem  {

	/**
	 * Adds devices (family, sub-family device or variant) from supplied pack
	 * @param pack IcpPack to add devices from
	 */
	void addDevices(ICpPack pack);

	/**
	 * Removes device (family, sub-family device or variant) from supplied pack
	 * @param pack IcpPack to add devices from
	 */
	void removeDevices(ICpPack pack);
	
}
