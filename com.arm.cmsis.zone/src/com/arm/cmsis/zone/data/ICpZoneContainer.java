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

package com.arm.cmsis.zone.data;

import java.util.Collection;

/**
 *  Top-level container of CMSIS-Zone objects
 */
public interface ICpZoneContainer extends ICpZoneItem {
	
	/**
	 * Retrieve all zones
	 * @return Collection of zones, might be empty.
	 */
	Collection<ICpZone> getZones();
	
	/**
	 * Retrieve a defined zone by name.
	 * @param zoneName The name of the zone to be retrieved. 
	 * @return The zone object, or null if unavailable.
	 */
	ICpZone getZone(String zoneName);
	
}
