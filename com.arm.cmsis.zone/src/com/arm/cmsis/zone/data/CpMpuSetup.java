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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.MemoryStartComparator;
import com.arm.cmsis.pack.enums.ECoreArchitecture;

/**
 * Implementation of an MPU setup helper
 */
public class CpMpuSetup extends CpZoneItem implements ICpMpuSetup {

    protected ECoreArchitecture fCoreArchitecture = null;
    protected ArrayList<ICpMpuRegion> fMpuRegions;
    protected int fnMpuRegions = 8; // architectural default

    public CpMpuSetup(ICpZone zone) {
        this(zone, CmsisConstants.MPU_SETUP);
        if (zone != null) {
            setCoreArchitecture(zone.getArchitecture());
        }
    }

    public CpMpuSetup(ICpItem parent, String tag) {
        super(parent, tag);
    }

    @Override
    public int getNumMpuRegions() {
        return fnMpuRegions;
    }

    @Override
    public void setNumMpuRegions(int nRegions) {
        fnMpuRegions = nRegions;
    }

    @Override
    public ECoreArchitecture getCoreArchitecture() {
        if (fCoreArchitecture == null) {
            fCoreArchitecture = ECoreArchitecture.fromString(getAttribute(CmsisConstants.TYPE));
        }
        return fCoreArchitecture;
    }

    @Override
    public void setCoreArchitecture(ECoreArchitecture arch) {
        fCoreArchitecture = arch;
        setAttribute(CmsisConstants.TYPE, fCoreArchitecture.toString());
    }

    @Override
    public Collection<ICpMpuRegion> getMpuRegions() {
        if (fMpuRegions == null) {
            fMpuRegions = new ArrayList<>();
        }
        return fMpuRegions;
    }

    @Override
    public Collection<? extends ICpItem> getChildren() {
        return getMpuRegions();
    }

    @Override
    public ICpItem toFtlModel(ICpItem ftlParent) {
        ICpItem ftlMpusetup = super.toFtlModel(ftlParent);
        for (ICpMpuRegion mpuRegion : getMpuRegions()) {
            ICpItem ftlRegion = mpuRegion.toFtlModel(ftlMpusetup);
            ftlMpusetup.addChild(ftlRegion);
        }
        return ftlMpusetup;
    }

    @Override
    public Collection<ICpMpuRegion> constructMpuRegions(Collection<ICpMemoryBlock> memoryBlocks) {
        if (memoryBlocks == null || memoryBlocks.isEmpty()) {
            return getMpuRegions();
        }

        // create sorted array list out of the memory blocks
        ArrayList<ICpMemoryBlock> memoryArray = new ArrayList<>(memoryBlocks);
        Collections.sort(memoryArray, new MemoryStartComparator());
        fMpuRegions = createMpuRegions(memoryArray);
        return getMpuRegions();
    }

    /**
     * Creates MPU regions out of sorted array of memory blocks
     *
     * @param memoryArray sorted ArrayList of memory blocks
     */
    protected ArrayList<ICpMpuRegion> createMpuRegions(ArrayList<ICpMemoryBlock> memoryArray) {
        ArrayList<ICpMpuRegion> mpuRegions = new ArrayList<>();
        Long previousEnd = 0L;
        int i = 0;
        while (i < memoryArray.size()) {
            ICpMpuRegion r = createMpuRegion();
            mpuRegions.add(r);
            i += r.addMemoryBlocks(memoryArray, i, previousEnd);
            previousEnd = r.getStop();
        }
        return mpuRegions;
    }

    /**
     * Create MPU region depending on architecture
     *
     * @return ICpMpuRegion
     */
    protected ICpMpuRegion createMpuRegion() {
        if (getCoreArchitecture() == ECoreArchitecture.ARMv7) {
            return new CpMpuRegionV7M(this);
        }
        return new CpMpuRegion(this);
    }

}
