/*******************************************************************************
* Copyright (c) 2019-2020 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/
package com.arm.cmsis.zone.data;

import java.util.ArrayList;

import com.arm.cmsis.pack.data.ICpMemory;

/**
 * Interface for MPU region
 *
 */
public interface ICpMpuRegion extends ICpZoneItem, ICpMemory {

    /**
     * Tries to add other MPU regions to this one, startegy depend on core
     * architecture
     *
     * @param mpuRegions  collection of ICpMpuRegions to add
     * @param startIndex  stating index in the collection to proceed
     * @param previousEnd end address of the previous MPU region, needed for ArmV7
     *                    calculations
     * @return number of regions added
     */
    int addMemoryBlocks(ArrayList<ICpMemoryBlock> memoryBlocks, int startIndex, Long previousEnd);

    /**
     * Checks if permissions and flags of this region equal the supplied memory
     *
     * @param memory ICpMemory to check
     * @return true if permissions are the same
     */
    boolean arePermissionsEqual(ICpMemory region);

    /**
     * Returns sorted collection of the memory blocks assigned to this region
     *
     * @return ArrayList of memory blocks sorted by start address
     */
    ArrayList<ICpMemoryBlock> getMemoryBlocks();

	/**
	 * Returns ArmV7 MPU region size required to accommodate memory block of given size
	 * @param size size to accommodate
	 * @return MPU region size
	 */
	static long getMpu7RegionSize(long size) {
		return ICpMemory.alignTo2n(size);
	}

	/**
	 * Returns ArmV7 MPU region alignment
	 * @param size size to accommodate in MPU
	 * @return MPU memory alignment
	 */
	static long getMpu7RegionAlignment(long size) {
		long regionSize  = getMpu7RegionSize(size);
		if(regionSize == size)
			return size;
		long alignment = 32; // 32 byte alignment anyway
		if(regionSize > 256L) {
			alignment = regionSize / 8;
		}
		return alignment;
	}

	/**
	 * Returns size aligned to ArmV7 MPU requirements
	 * @param size size to align
	 * @return aligned size
	 */
	static long alignToMpu7(long size) {
		if(size == 0L)
			return size;

		long regionSize  = getMpu7RegionSize(size);
		if(regionSize == size)
			return size;
		long alignment = 32; // 32 byte alignment anyway
		if(regionSize > 256L) {
			alignment = regionSize / 8;
		}
		return ICpMemory.alignTo(size, alignment);
	}



}
