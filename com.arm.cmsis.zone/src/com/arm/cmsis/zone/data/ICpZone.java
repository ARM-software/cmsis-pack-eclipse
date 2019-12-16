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

import com.arm.cmsis.pack.permissions.IMemoryPriviledge;
import com.arm.cmsis.pack.permissions.IMemorySecurity;

/**
 * Interface for a zone (execution or project) represents short zone info inside System Zone or ProjectZone .
 * 
 * <p>A zone defines a set of resources used in a common way.</p> 
 */
public interface ICpZone extends ICpZoneItem, IMemorySecurity, IMemoryPriviledge {

	/**
	 * Retrieve a single assignment by name from the zone definition
	 * @param name assignment name retrieve
	 * @return ICpZoneAssignment, or null if unavailable.
	 */
	ICpZoneAssignment getZoneAssignment(String name);

	/**
	 * Retrieve the complete collection of defined zone assignments. 
	 * @return collection of ICpZoneAssignment items, might be empty.
	 */
    Collection<ICpZoneAssignment> getZoneAssignments();

    /**
     * Retrieve the device this zone makes use of for execution. 
     * @return the assigned ICpDeviceUnit 
     */
    ICpDeviceUnit getTargetDevice();

    /**
     * Retrieve the processor this zone makes use of for execution.
     * @return The assigned processor resource.
     */
    ICpProcessorUnit getTargetProcessor();

    
    /**
     * Checks if a block can be assigned to the zone
     * @param block ICpMemoryBlock to check
     * @return true if can be assigned
     */
    boolean canAssign(ICpMemoryBlock block);
	
}
