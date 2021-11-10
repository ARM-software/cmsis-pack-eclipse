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

/**
 * Interface for the MPU (memory protection unit) setup
 */
public interface ICpMpuSetup extends ICpZoneItem {

    /**
     * Returns MPU architecture
     *
     * @return ECoreArchitecture
     */
    ECoreArchitecture getCoreArchitecture();

    /**
     * Sets MPU architecture
     *
     * @param arch ECoreArchitecture
     */
    void setCoreArchitecture(ECoreArchitecture arch);

    /**
     * Returns number of MPU regions
     *
     * @return number of MPU regions
     */
    int getNumMpuRegions();

    /**
     * Sets number of available MPU regions
     *
     * @param nRegions number of MPU regions
     */
    void setNumMpuRegions(int nRegions);

    /**
     * Returns MPU regions in the MPU setup
     *
     * @return collection of ICpMpRegion objects
     */
    Collection<ICpMpuRegion> getMpuRegions();

    /**
     * Creates MPU regions in the MPU setup
     *
     * @param memoryBlocks collection of memory blocks to add to created MPU regions
     * @return collection of ICpMpRegion objects
     */
    Collection<ICpMpuRegion> constructMpuRegions(Collection<ICpMemoryBlock> memoryBlocks);

}
