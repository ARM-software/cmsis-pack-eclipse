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

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpMemory;
import com.arm.cmsis.pack.enums.ECoreArchitecture;
import com.arm.cmsis.pack.enums.ESeverity;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.zone.error.CmsisZoneError;

public class CpMpuRegionV7M extends CpMpuRegion {

    protected Long fAddrV7M = null;
    protected Long fSizeV7M = null;
    protected Long fSrdv7 = 0x0L; // initialize with 0 (entire region enabled)

    public CpMpuRegionV7M(ICpItem parent, String tag) {
        super(parent, tag);
    }

    public CpMpuRegionV7M(ICpMpuSetup parent) {
        super(parent);
    }

    Long adjustV7Values(boolean bCalculateSRD) {
        if (fCoreArchitecture != ECoreArchitecture.ARMv7) {
            return fSrdv7;
        }
        Long size = getSize();

        long alignedSize = ICpMpuRegion.alignToMpu7(size);
        if (fMemoryBlocks.size() == 1 && size < alignedSize) {
            size = alignedSize;
            setSize(size);
        }
        Long start = getStart();
        Long end = start + size;
        fSizeV7M = ICpMpuRegion.getMpu7RegionSize(size);
        if (fSizeV7M < 256L) {
            // with 256 byte it is possible to use sub-regions that gives better density
            fSizeV7M = 256L;
        }

        while (true) {
            long alignment_v7M = ICpMpuRegion.getMpu7RegionAlignment(fSizeV7M);
            fAddrV7M = (start / alignment_v7M) * alignment_v7M;
            // check if due to alignment the block if still within the region
            Long offset = start - fAddrV7M;
            Long endV7M = fAddrV7M + fSizeV7M;
            if (offset <= 0 || end <= endV7M || fSizeV7M >= 0x10000000L) {
                break; // fulfilled
            }

            // increase region size
            Long newAddrV7M = ICpMpuRegion.getMpu7RegionSize(size + offset);
            if (newAddrV7M == fSizeV7M || newAddrV7M > 0x10000000L) {
                // new MPU region size is the same as previous or exceeds 2^32 limit
                break;
            }

            fSizeV7M = newAddrV7M;
        }

        if (bCalculateSRD) {
            if (fSizeV7M >= 256L) {
                fSrdv7 = calcSrdV7Mask(this, fAddrV7M, fSizeV7M / 8);
            } else {
                fSrdv7 = 0x00L;
            }
        }
        return fSrdv7;
    }

    @Override
    public ICpItem toFtlModel(ICpItem ftlParent) {

        ICpItem ftlRegion = super.toFtlModel(ftlParent);
        addArmV7ValuesToFtlModel(ftlRegion);
        return ftlRegion;
    }

    protected void addArmV7ValuesToFtlModel(ICpItem ftlRegion) {
        long size = fSizeV7M;

        ICpItem a7m = new CpItem(ftlRegion, CmsisConstants.ADDR_V7M);
        a7m.setText(IAttributes.longToHexString8(fAddrV7M));
        ftlRegion.addChild(a7m);

        // size_v7M : the region size as exponent, in bytes is 2^(SIZE+1)
        long size_v7M = 63L - Long.numberOfLeadingZeros(size) - 1L;

        ICpItem s7m = new CpItem(ftlRegion, CmsisConstants.SIZE_V7M);
        s7m.setText(IAttributes.longToHexString(size_v7M, 2));
        ftlRegion.addChild(s7m);

        ICpItem srd7m = new CpItem(ftlRegion, CmsisConstants.SRD_V7M);
        srd7m.setText(IAttributes.longToHexString(fSrdv7, 2));
        ftlRegion.addChild(srd7m);
    }

    /**
     * Calculates SDR (sub-region disable) mask to add an memory region to this one
     * as a sub-region
     *
     * @param region MPU region or memory block to calculate
     * @return calculated SDR mask, 0xFF if not successful
     */
    protected long calcSrdV7Mask(ICpMemory region, long addrV7M, long subRegionSize) {
        if (!arePermissionsEqual(region)) {
            return 0xFFL;
        }

        Long start = region.getStart();
        if (start < addrV7M) {
            return 0xFFL;
        }

        Long size = region.getSize();
        if (size % subRegionSize != 0) {
            return 0xFFL;
        }

        Long subRegCount = size / subRegionSize;
        if (subRegCount > 8) {
            return 0xFFL;
        }
        Long offset = start - addrV7M;
        Long idxStart = offset / subRegionSize;
        Long idxEnd = idxStart + subRegCount;
        if (idxEnd > 8) {
            return 0xFFL;
        }

        Long mask = 0xFFL; // initialize with 1 (disabled)
        for (Long i = idxStart; i < idxEnd; i++) {
            mask &= ~(1 << i); // reset disable bit
        }
        return mask;
    }

    @Override
    public int addMemoryBlocks(ArrayList<ICpMemoryBlock> memoryBlocks, int startIndex, Long previousEnd) {
        if (memoryBlocks == null || startIndex < 0)
            return 0;

        // first add adjusted regions
        int count = super.addMemoryBlocks(memoryBlocks, startIndex, previousEnd);
        if (count == 0) {
            return 0;
        }
        // adjust MPU values and remove already appended blocks if not aligned
        for (Long srd = adjustV7Values(true); srd == 0xFFL; srd = adjustV7Values(true)) {
            if (fMemoryBlocks.size() <= 1) {
                // only one block in the region and it is unaligned => error
                addError(new CmsisZoneError(this, ESeverity.Warning, CmsisZoneError.Z111));
                break;
            }
            // remove appended block
            ICpMemoryBlock memoryBlock = fMemoryBlocks.remove(fMemoryBlocks.size() - 1);
            // adjust size to accommodate
            Long size = getSize();
            size -= memoryBlock.getSize();
            setSize(size);
            count--;
        }

        // try to add additional blocks to sub-regions
        count += addMemoryBlocksToSubRegions(memoryBlocks, startIndex + count, previousEnd);
        return count;
    }

    /**
     * Adds memory blocks to MPU sub-regions following armV7 rules
     *
     * @param memoryBlocks array of memory blocks to add
     * @param startIndex   start index in the array
     * @param previousEnd  end address of previous MPU region to avoid overlap
     * @return number of memory blocks added to the region
     */
    protected int addMemoryBlocksToSubRegions(ArrayList<ICpMemoryBlock> memoryBlocks, int startIndex,
            Long previousEnd) {
        if (memoryBlocks == null || startIndex >= memoryBlocks.size()) {
            return 0;
        }
        int count = 0;
        // try to allocate as large region as possible
        for (long factor = 8L; factor >= 1L; factor >>= 1L) {
            long size = fSizeV7M;
            long sizeV7M = size * factor;
            long alignment_v7M = ICpMpuRegion.getMpu7RegionAlignment(sizeV7M);
            long start = getStart();
            long addrV7M = (start / alignment_v7M) * alignment_v7M;
            if (Long.compareUnsigned(addrV7M, previousEnd) < 0) {
                continue; // cannot expand over previous region
            }
            long subRegionSize = sizeV7M / 8;

            Long srd_v7M = calcSrdV7Mask(this, addrV7M, subRegionSize);
            // try to allocate other regions
            for (int i = startIndex; i < memoryBlocks.size(); i++) {
                ICpMemoryBlock block = memoryBlocks.get(i);
                long mask = calcSrdV7Mask(block, addrV7M, subRegionSize);
                if (mask == 0xFFL) { // does not fit
                    break;
                }
                fMemoryBlocks.add(block);
                count++;
                srd_v7M &= mask;
            }
            if (count > 0) {
                fStart = addrV7M;
                fSize = sizeV7M;
                adjustV7Values(false);
                fSrdv7 = srd_v7M;
                break;
            }
        }
        return count;
    }

}
