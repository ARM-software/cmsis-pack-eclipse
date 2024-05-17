/*******************************************************************************
* Copyright (c) 2021 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.zone.data;

import java.util.Collection;

/**
 * Top-level container of CMSIS-Zone objects
 */
public interface ICpZoneContainer extends ICpZoneItem {

    /**
     * Retrieve all zones
     *
     * @return Collection of zones, might be empty.
     */
    Collection<ICpZone> getZones();

    /**
     * Retrieve a defined zone by name.
     *
     * @param zoneName The name of the zone to be retrieved.
     * @return The zone object, or null if unavailable.
     */
    ICpZone getZone(String zoneName);

}
