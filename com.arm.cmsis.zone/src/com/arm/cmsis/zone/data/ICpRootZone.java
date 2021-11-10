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

import java.util.Collection;

import com.arm.cmsis.pack.enums.ECoreArchitecture;
import com.arm.cmsis.pack.permissions.IMemoryPriviledge;
import com.arm.cmsis.pack.permissions.IMemorySecurity;

/**
 * The to-level zone element describing rzone or azone file
 */
public interface ICpRootZone
        extends ICpZoneConfig, ICpDeviceResource, ICpResourceGroup, IMemorySecurity, IMemoryPriviledge {

    /**
     * Returns a device unit for the zone
     *
     * @return ICpDeviceUnit
     */
    ICpDeviceUnit getDeviceUnit();

    /**
     * Returns resources as ICpResourceContainer
     *
     * @return top resource
     */
    ICpResourceContainer getResources();

    /**
     * Returns creator element
     *
     * @return ICpZoneCreator or null
     */
    default ICpZoneCreator getZoneCreator() {
        return getFirstChildOfType(ICpZoneCreator.class);
    }

    /**
     * Returns "resources" xml element
     *
     * @return ICpResourceContainer
     */
    default ICpResourceContainer getResourceContainer() {
        return getFirstChildOfType(ICpResourceContainer.class);
    }

    /**
     * Returns zone container
     *
     * @return ICpZoneContainer
     */
    default ICpZoneContainer getZoneContainer() {
        return getFirstChildOfType(ICpZoneContainer.class); // could only be one
    }

    /**
     * Returns memory partition group
     *
     * @return ICpPartitionGroup
     */
    default ICpPartitionGroup getPartitionGroup() {
        return getFirstChildOfType(ICpPartitionGroup.class); // could only be one
    }

    /**
     * Store current memory partition
     */
    void updatePartition();

    /**
     * Return the project/execution zone with the given name
     *
     * @param name name of the zone
     * @return ICpZone zone or null if not found
     */
    ICpZone getZone(String name);

    /**
     * Return collection of zones
     *
     * @return zone collection, null if no zones are found.
     */
    Collection<ICpZone> getZones();

    /**
     * Adds new zone to the collection if it does not exists
     *
     * @param name zone name to add
     * @return new ICpZone or existing one
     */
    ICpZone addZone(String name);

    /**
     * Return system processor architecture
     *
     * @return ECoreArchitecture enum value
     */
    @Override
    ECoreArchitecture getArchitecture();

    /**
     * Returns filename of corresponding resourceFile
     *
     * @return fileName
     */
    String getResourceFileName();

    /**
     * Creates MPU setup for all zones, replaces the existing one
     */
    void createMpuSetup();

}
