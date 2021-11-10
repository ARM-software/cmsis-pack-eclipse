/*******************************************************************************
* Copyright (c) 2021 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.zone.data;

import com.arm.cmsis.pack.data.ICpMemory;

/**
 * A single zone assignment : an assignment of a resource to a zone
 */
public interface ICpZoneAssignment extends ICpZoneItem, ICpMemory {

    /**
     * Get the zone this item is defined in.
     *
     * @return The parent zone.
     */
    default ICpZone getZone() {
        return getParentOfType(ICpZone.class);
    }

    /**
     * Retrieves the assigned memory block for this zone
     *
     * @return assigned ICpMemoryBlock
     */
    ICpMemoryBlock getAssignedBlock();

    /**
     * Assigns block to the zone
     *
     * @param block
     */
    void assign(ICpMemoryBlock block);

}
