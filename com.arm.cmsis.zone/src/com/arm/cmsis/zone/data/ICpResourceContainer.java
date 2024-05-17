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
import java.util.Map;

/**
 * Resource container contains memory and peripherals groups
 */
public interface ICpResourceContainer extends ICpResourceGroup {

    /**
     * Adds memory sub-regions as well as overridden attributes and permissions for
     * regions, groups and peripherals
     *
     * @param pg partition group
     */
    void addPartititionBlocks(ICpPartitionGroup pg);

    /**
     * Adds assignments from the zone container memory regions and blocks from
     * supplied group
     *
     * @param zoneContainer ICpZoneContainer
     */
    void addZoneAssignments(ICpZoneContainer zoneContainer);

    /**
     * Returns all memory regions available excluding peripherals return collection
     * of ICpMemoryBlock
     */
    Collection<ICpMemoryBlock> getAllMemoryRegions();

    /**
     * Returns all regions with STARTUP flag set return collection of ICpMemoryBlock
     */
    Collection<ICpMemoryBlock> getStarupMemoryRegions();

    /**
     * Returns all memory regions available for system/project zone
     *
     * @return collection of ICpPeripheral
     */
    Collection<ICpPeripheral> getAllPeripherals();

    /**
     * Returns all memory regions available for system/project zone
     *
     * @return collection of ICpPeripheralItem
     */
    Collection<ICpPeripheralItem> getAllPeripheralItems();

    /**
     * Returns SAU init element
     *
     * @return ICpSauInit or null
     */
    default ICpSauInit getSauInit() {
        return getFirstChildOfType(ICpSauInit.class); // could only be one
    }

    /**
     * Return MPC regions
     *
     * @return map of MPC regions ordered by their addresses
     */
    Map<Long, IMpcRegion> getMpcRegions();

    /**
     * Returns MPC region for given address
     *
     * @param address region start address
     * @return ICpMpcItem or null if no region starts on the address
     */
    IMpcRegion getMpcRegion(long address);

    /**
     * Return physical memory regions
     *
     * @return map of PhysicalMemoryRegion ordered by their addresses
     */
    Map<Long, PhysicalMemoryRegion> getPhysicalRegions();

    /**
     * Returns physicalMemory region for given address
     *
     * @param address region start address
     * @return PhysicalMemoryRegion or null if no region starts on the address
     */
    PhysicalMemoryRegion getPhysicalRegion(long address);

    /**
     * Arranges memory block in their corresponding memory regions
     *
     * @return true if block arrangement has changed
     */
    boolean arrangeBlocks();

}
