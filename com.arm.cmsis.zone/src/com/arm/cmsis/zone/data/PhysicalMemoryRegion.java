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
import java.util.Collections;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpMemory;
import com.arm.cmsis.pack.data.MemoryOffsetComparator;
import com.arm.cmsis.pack.data.MemorySizeComparator;
import com.arm.cmsis.pack.enums.ECoreArchitecture;
import com.arm.cmsis.pack.enums.EMemoryOverlap;
import com.arm.cmsis.pack.enums.ESeverity;
import com.arm.cmsis.pack.error.CmsisError;
import com.arm.cmsis.pack.permissions.IMemoryPermissions;
import com.arm.cmsis.zone.error.CmsisZoneError;

/**
 * Physical memory region
 */
public class PhysicalMemoryRegion {

    protected Long fAddress = null;
    protected Long fSize = 0L; // max size

    protected IMpcRegion fMpcRegion = null;
    protected Long fMpcRegionOffset = 0L;

    protected Set<ICpMemoryBlock> fLogicalRegions = new TreeSet<>(new MemorySizeComparator()); // logical regions with
                                                                                               // the same physical
                                                                                               // address, key: ID
    protected Set<ICpMemoryBlock> fLogicalBlocks = null; // child blocks from all logical blocks in the order of
                                                         // offset/size/name

    protected ICpRootZone fRootZone = null; // for alignment and granularity info
    protected ICpMemoryBlock fInitialBlock = null;
    protected boolean fMpuMode = false;
    protected ECoreArchitecture fArchitecture = ECoreArchitecture.UNKNOWN;

    public boolean isMpu7Mode() {
        return fMpuMode && fArchitecture == ECoreArchitecture.ARMv7;
    }

    /**
     * Adds logical region to the physical region
     *
     * @param region ICpMemoryBlock to add
     * @return true if added
     */
    public boolean addRegion(ICpMemoryBlock region) {
        if (region == null)
            return false;
        if (region instanceof ICpPeripheralGroup)
            return false;

        if (fRootZone == null) {
            fRootZone = region.getRootZone();
            fMpuMode = CmsisConstants.MPU.equals(fRootZone.getZoneMode());
            fArchitecture = fRootZone.getArchitecture();
        }

        if (fLogicalRegions.contains(region)) {
            return false; // already added
        }

        Long address = region.getAddress();
        if (fAddress == null) {
            fAddress = address;
            if (!fMpuMode) {
                fMpcRegion = fRootZone.getResources().getMpcRegion(address);
                if (fMpcRegion != null) {
                    fMpcRegionOffset = address - fMpcRegion.getAddress();
                }
            }
        } else if (!fAddress.equals(address)) {
            return false; // should not happen if outside logic is correct
        }

        fLogicalRegions.add(region);
        long size = region.getSize();
        if (size > fSize)
            fSize = size;
        return true;
    }

    public Collection<ICpMemoryBlock> getRegions() {
        return fLogicalRegions;
    }

    public Collection<ICpMemoryBlock> getBlocks() {
        return fLogicalBlocks;
    }

    public ICpMpc getMpcRegion() {
        return fMpcRegion;
    }

    public Long getMpcRegionOffset() {
        return fMpcRegionOffset;
    }

    public long getAddress() {
        if (fAddress == null)
            return 0L;
        return fAddress;
    }

    public long getSize() {
        return fSize;
    }

    public long getStop() {
        long size = getSize();
        if (size > 0)
            size--;
        return getAddress() + size;
    }

    /**
     * Arranges blocks in the array by changing their offsets
     *
     * @param zone ICpRootZone required to obtain address granularity
     * @return true if arrangements has modified
     */
    public boolean arrangeBlocks() {
        if (fLogicalBlocks == null)
            fLogicalBlocks = new TreeSet<>(new MemoryOffsetComparator());

        // save logical block collection to check modification later
        Set<ICpMemoryBlock> savedLogicalBlocks = new TreeSet<>(new MemoryOffsetComparator());
        savedLogicalBlocks.addAll(fLogicalBlocks);

        fLogicalBlocks.clear();
        fInitialBlock = null;

        if (fMpcRegion != null) {
            fMpcRegion.invalidatePermissions(fMpcRegionOffset, getSize());
        }

        List<ICpMemoryBlock> sortedBlocks = new ArrayList<>();
        // collect all child blocks and sort them
        for (ICpMemoryBlock region : fLogicalRegions) {

            if (fMpcRegion != null && region.isAssigned() && !region.hasSubBlocks()) {
                // also put the entire region in the list with fixed offset == 0
                addLogicalBlock(region); // only directly add to the final collection
            }
            if (region.isPeripheral())
                continue;
            Collection<ICpMemoryBlock> blocks = region.getSubBlocks();
            if (blocks == null || blocks.isEmpty())
                continue;
            for (ICpMemoryBlock block : blocks) {
                if (!checkBlock(block))
                    continue;
                if (block.isFixed()) {
                    addLogicalBlock(block); // only directly add to the final collection
                } else {
                    sortedBlocks.add(block); // add to collection for later arrangement
                }
            }

        }

        // check overlaps of fixed blocks
        checkOverlaps();

        if (!sortedBlocks.isEmpty()) {
            // add non-fixed blocks
            Collections.sort(sortedBlocks, new MemorySizeComparator());
            while (!sortedBlocks.isEmpty()) {
                arrangeBlocks(sortedBlocks);
            }
        }
        // check if modified
        if (fLogicalBlocks.size() != savedLogicalBlocks.size())
            return true;
        // check if the order is correct
        Iterator<ICpMemoryBlock> it = fLogicalBlocks.iterator();
        Iterator<ICpMemoryBlock> its = savedLogicalBlocks.iterator();
        while (it.hasNext() && its.hasNext()) {
            if (!it.next().equalsAttributes(its.next().attributes()))
                return true;
        }

        return false;
    }

    private void checkOverlaps() {
        for (ICpMemoryBlock block : fLogicalBlocks) {
            if (block.getParentBlock() == null || !block.isFixed())
                continue;
            for (ICpMemoryBlock other : fLogicalBlocks) {
                if (block == other)
                    continue; // do not compare with itself
                if (other.getParentBlock() == null || !other.isFixed())
                    continue;
                EMemoryOverlap overlap = block.checkOverlap(other);
                if (!overlap.isOverlap())
                    continue;

                String errId;
                if (overlap.isFull()) {
                    errId = CmsisZoneError.Z107;
                } else {
                    errId = CmsisZoneError.Z108;
                }
                CmsisError err = new CmsisZoneError(ESeverity.Warning, errId);
                err.setDetail(other.getName());
                block.addError(err);
            }
        }

    }

    private void arrangeBlocks(Collection<ICpMemoryBlock> sortedBlocks) {
        if (sortedBlocks.isEmpty())
            return;
        ICpMemoryBlock curBlock = fInitialBlock; // first use initial permissions if any
        fInitialBlock = null;
        for (Iterator<ICpMemoryBlock> it = sortedBlocks.iterator(); it.hasNext();) {
            ICpMemoryBlock block = it.next();
            if (curBlock == null) {
                curBlock = block;
            } else if (!matchMemoryBlockProperties(curBlock, block)) {
                continue;
            }
            Long offset = getNextSlot(block);
            if (offset == null || offset < 0L) {
                // no slot is found
                offset = 0L;
                block.addError(new CmsisZoneError(ESeverity.Error, CmsisZoneError.Z106));
            }
            block.setOffset(offset);
            addLogicalBlock(block);
            it.remove();
        }
    }

    public Long getNextSlot(ICpMemoryBlock blockToAllocate) {
        if (fLogicalBlocks == null) {
            arrangeBlocks();
        }

        Long requiredSize = getAlignedBlockSize(blockToAllocate.getSize());

        Long offset = 0L; // "end of previous block" + 1 or 0 if no previous block yet
        ICpMemoryBlock previousBlock = null;
        for (ICpMemoryBlock block : fLogicalBlocks) {
            Long blockOffset = block.getOffset();
            if (previousBlock != null) {
                if (previousBlock.getOffset() == blockOffset) // same address, but smaller or equal size, skip
                    continue;
            }
            Long size = blockOffset - offset;
            if (size >= requiredSize && matchPermissions(blockToAllocate, offset, requiredSize))
                return offset;
            Long blockSize = getAlignedBlockSize(block.getSize());
            offset = blockOffset + blockSize;
            previousBlock = block;
        }
        if (isMpu7Mode() && previousBlock != null) {
            // we are allocating next MPU slot : adjust offset first to the end of previous
            // MPU size
            long alignedOffset = previousBlock.getOffset() + ICpMpuRegion.getMpu7RegionSize(previousBlock.getSize());
            if (offset < alignedOffset) {
                offset = alignedOffset;
            }

            // adjust to own alignment
            long alignment = ICpMpuRegion.getMpu7RegionAlignment(requiredSize);
            alignedOffset = ICpMemory.alignTo(offset, alignment);
            if (offset < alignedOffset) {
                offset = alignedOffset;
            }

        } else {
            offset = adjustOffsetToMpc(blockToAllocate, offset, requiredSize);
        }

        if (offset == -1L || offset + requiredSize > getSize())
            return -1L; // cannot find a slot

        return offset;
    }

    private Long getAlignedBlockSize(long size) {
        if (isMpu7Mode()) {
            return ICpMpuRegion.alignToMpu7(size);
        }
        return size;
    }

    private Long adjustOffsetToMpc(ICpMemoryBlock blockToAllocate, Long offset, Long requiredSize) {
        if (fMpcRegion == null)
            return offset;
        if (matchPermissions(blockToAllocate, offset, requiredSize))
            return offset; // already fits

        Long mpcOffset = offset + getMpcRegionOffset();
        Long adjustedOffset = fMpcRegion.getNextAvailableOffset(blockToAllocate, mpcOffset, requiredSize);
        if (adjustedOffset == null || adjustedOffset < 0L)
            return -1L;
        adjustedOffset -= getMpcRegionOffset();
        if (adjustedOffset < offset)
            return offset;
        return adjustedOffset;
    }

    protected boolean checkBlock(ICpMemoryBlock block) {
        block.clearErrors(CmsisZoneError.Z1_MASK); // clear all allocation errors

        // check if block exceeds size
        long blockSize = block.getSize();
        if (blockSize <= 0) {
            block.addError(new CmsisZoneError(ESeverity.Error, CmsisZoneError.Z101));
            return false;
        }

        if (isMpu7Mode()) {
            long alignedSize = ICpMpuRegion.alignToMpu7(blockSize);
            if (alignedSize > blockSize) {
                block.addError(new CmsisZoneError(ESeverity.Warning, CmsisZoneError.Z112));
                blockSize = alignedSize;
            }
        }

        if (blockSize > getSize()) {
            block.addError(new CmsisZoneError(ESeverity.Error, CmsisZoneError.Z102));
            return false;
        }

        if (!block.isFixed()) {
            return true;
        }

        long offset = block.getOffset();
        if (offset == 0) {
            return true;
        }

        if (offset < 0) {
            block.addError(new CmsisZoneError(ESeverity.Error, CmsisZoneError.Z103));
            return false;
        }
        if (offset > getSize()) {
            block.addError(new CmsisZoneError(ESeverity.Error, CmsisZoneError.Z104));
            return false;
        }
        if ((offset + blockSize) > getSize()) {
            block.addError(new CmsisZoneError(ESeverity.Error, CmsisZoneError.Z105));
            return false;
        }

        return true;
    }

    protected void addLogicalBlock(ICpMemoryBlock block) {
        fLogicalBlocks.add(block);
        if (fInitialBlock == null && block.isFixed() && block.getOffset() == 0L) {
            fInitialBlock = block;
        }
        if (fMpcRegion != null) {
            fMpcRegion.setMpcPermissions(block, getMpcRegionOffset());
        }
    }

    /**
     * Checks if block properties (permissions, logical address space) match
     * depending on zone mode
     *
     * @param b1 first ICpMemoryBlock to match
     * @param b2 second ICpMemoryBlock to match
     * @return true if matches
     */
    protected boolean matchMemoryBlockProperties(ICpMemoryBlock b1, ICpMemoryBlock b2) {
        if (b1 == null || b2 == null)
            return true;

        if (fMpuMode) {
            ICpMemoryBlock p1 = b1.getParentBlock();
            ICpMemoryBlock p2 = b2.getParentBlock();
            if (p1 == p2)
                return true; // the same parent => same base address
            if (p1 == null || p2 == null)
                return true;
            return false;
        } else if (fMpcRegion != null) {
            return fMpcRegion.matchPermissions(b1, b2);
        }
        return true;
    }

    protected boolean matchPermissions(IMemoryPermissions p, Long offset, Long size) {
        if (fMpuMode) {
            return true;
        } else if (fMpcRegion != null) {
            return fMpcRegion.matchPermissions(p, offset + getMpcRegionOffset(), size);
        }
        return true;
    }

    public Long getMaxSlotSize(IMemoryPermissions permissions, Long parentRegionSize) {
        if (fLogicalBlocks == null) {
            arrangeBlocks();
        }

        if (fLogicalBlocks.isEmpty())
            return getSize();

        Long prevOffset = 0L;
        long maxSize = 0L;
        ICpMemoryBlock previousBlock = null;
        for (ICpMemoryBlock block : fLogicalBlocks) {
            Long blockSize = block.getSize();
            Long offset = block.getOffset();
            if (previousBlock != null) {
                if (previousBlock.getOffset() == offset) // same address, but smaller or equal size, skip
                    continue;
                Long size = offset - prevOffset;
                if (matchPermissions(permissions, prevOffset, size)) {
                    if (maxSize < size)
                        maxSize = size;
                }
            }
            prevOffset = offset + blockSize;
            previousBlock = block;
        }
        if (prevOffset < parentRegionSize) {
            Long size = adjustSizeToMpc(permissions, prevOffset, parentRegionSize);
            if (size != null && maxSize < size)
                maxSize = size;
        }
        return maxSize;
    }

    private Long adjustSizeToMpc(IMemoryPermissions permissions, Long offset, Long parentRegionSize) {
        Long size = parentRegionSize - offset;
        if (fMpcRegion == null)
            return size;

        Long mpcOffset = offset + getMpcRegionOffset();
        if (matchPermissions(permissions, mpcOffset, size))
            return size; // already fits
        Long nextOffset = fMpcRegion.getNextAvailableOffset(permissions, mpcOffset);
        if (nextOffset == null)
            return 0L;
        nextOffset -= getMpcRegionOffset();

        Long adjustedSize = parentRegionSize - nextOffset;
        adjustedSize -= adjustedSize % 32; // must be 32-bit aligned
        return adjustedSize;
    }

}
