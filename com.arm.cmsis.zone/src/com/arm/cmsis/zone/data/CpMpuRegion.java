/*******************************************************************************
* Copyright (c) 2019 - 2020 ARM Ltd. and others
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

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpMemory;
import com.arm.cmsis.pack.enums.ECoreArchitecture;
import com.arm.cmsis.pack.enums.EMemoryPrivilege;
import com.arm.cmsis.pack.generic.IAttributes;

public class CpMpuRegion extends CpZoneItem implements ICpMpuRegion {

    protected ECoreArchitecture fCoreArchitecture = ECoreArchitecture.UNKNOWN;

    protected ArrayList<ICpMemoryBlock> fMemoryBlocks = new ArrayList<>();

    protected Long fStart = null;
    protected Long fSize = null;
    protected Long fAddrV7M = null;
    protected Long fSizeV7M = null;
    protected Long fSrdv7 = 0x0L; // initialize with 0 (entire region enabled)

    public CpMpuRegion(ICpItem parent, String tag) {
        super(parent, tag);
    }

    public CpMpuRegion(ICpMpuSetup parent) {
        super(null, CmsisConstants.REGION);
        fCoreArchitecture = parent.getCoreArchitecture();
    }

    @Override
    public ArrayList<ICpMemoryBlock> getMemoryBlocks() {
        return fMemoryBlocks;
    }

    protected void adjustV7Values(boolean bCalculateSRD) {
        if (fCoreArchitecture != ECoreArchitecture.ARMv7) {
            return;
        }
        Long size = getSize();
        fSizeV7M = ICpMpuRegion.getMpu7RegionSize(size);
        long alignment_v7M = ICpMpuRegion.getMpu7RegionAlignment(fSizeV7M);
        fAddrV7M = (getStart() / alignment_v7M) * alignment_v7M;

        if (bCalculateSRD) {
            if (fSizeV7M >= 256L) {
                fSrdv7 = calcSrdV7Mask(this, fAddrV7M, fSizeV7M / 8);
            } else {
                fSrdv7 = 0x00L;
            }
        }
    }

    public ECoreArchitecture getCoreArchitecture() {
        return fCoreArchitecture;
    }

    public void setCoreArchitechture(ECoreArchitecture coreArchitechture) {
        this.fCoreArchitecture = coreArchitechture;
    }

    @Override
    public long getStart() {
        if (fStart == null) {
            fStart = ICpMpuRegion.super.getStart();
        }
        return fStart;
    }

    @Override
    public long getSize() {
        if (fSize == null) {
            fSize = ICpMpuRegion.super.getSize();
        }
        return fSize;
    }

    /**
     * Sets size
     *
     * @param size
     */
    protected void setSize(Long size) {
        fSize = size;
        attributes().setAttributeHex(CmsisConstants.SIZE, size);
    }

    @Override
    public boolean arePermissionsEqual(ICpMemory region) {
        if (region == null)
            return false;

        if (region == this)
            return true;

        if (getPrivilege().isPrivileged() != region.getPrivilege().isPrivileged())
            return false; // not the same privilege

        if (region.isShared() != isShared())
            return false; // not the same shared

        if (region.isDma() != isDma())
            return false; // not the same Dma

        if (isROM() != region.isROM())
            return false; // not the same memory type

        return isAccessEqual(region);
    }

    @Override
    public ICpItem toFtlModel(ICpItem ftlParent) {
        ICpItem ftlRegion = new CpItem(ftlParent, CmsisConstants.REGION);

        /*** Add region values ***/

        // Add <start> child
        ICpItem startItem = new CpItem(ftlRegion, CmsisConstants.START);
        String startAddress = getStartString();
        startItem.setText(startAddress);
        ftlRegion.addChild(startItem);

        // Align end memory address to 32 bytes
        Long stop = getStop();
        String endAddress = IAttributes.longToHexString8(stop);

        // Add <end> child
        ICpItem end = new CpItem(ftlRegion, CmsisConstants.END);
        end.setText(endAddress);
        ftlRegion.addChild(end);

        // Add <access> child
        ICpItem access = toFtlModel(ftlRegion, CmsisConstants.ACCESS, getAccessString());
        ftlRegion.addChild(access);

        // Add <privileged> child
        ICpItem privileged = new CpItem(ftlRegion, CmsisConstants.PRIVILEGED);
        String privilege = getPrivilege() == EMemoryPrivilege.PRIVILEGED ? CmsisConstants.ONE : CmsisConstants.ZERO;

        privileged.setText(privilege);
        ftlRegion.addChild(privileged);

        // Add <shared> child
        ICpItem shared = new CpItem(ftlRegion, CmsisConstants.SHARED);
        String isMemoryShared = isShared() ? CmsisConstants.ONE : CmsisConstants.ZERO;
        shared.setText(isMemoryShared);
        ftlRegion.addChild(shared);

        // Add <dma> child
        ICpItem dma = new CpItem(ftlRegion, CmsisConstants.DMA);
        String isMemoryDMA = isDma() ? CmsisConstants.ONE : CmsisConstants.ZERO;
        dma.setText(isMemoryDMA);
        ftlRegion.addChild(dma);

        // Add <rom> child
        ICpItem rom = new CpItem(ftlRegion, CmsisConstants.ROM_TAG);
        String memoryType = isROM() ? CmsisConstants.ONE : CmsisConstants.ZERO;
        rom.setText(memoryType);
        ftlRegion.addChild(rom);

        // Add <info> child. Note: <info> tag is reused to save content of <name> tag
        ICpItem info = new CpItem(ftlRegion, CmsisConstants.INFO);
        StringBuilder infoText = new StringBuilder();
        for (ICpMemoryBlock block : fMemoryBlocks) {
            if (infoText.length() > 0) {
                infoText.append(", "); //$NON-NLS-1$
            }
            infoText.append(block.getName());
        }
        info.setText(infoText.toString());
        ftlRegion.addChild(info);

        if (getCoreArchitecture() == ECoreArchitecture.ARMv7) {
            addArmV7ValuesToFtlModel(ftlRegion);
        }

        return ftlRegion;
    }

    protected void addArmV7ValuesToFtlModel(ICpItem ftlRegion) {
        long size = getSize();

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
        if (!arePermissionsEqual(region))
            return 0xFFL;
        Long size = region.getSize();
        if (size % subRegionSize != 0)
            return 0xFFL;
        Long start = region.getStart();
        Long subRegCount = size / subRegionSize;
        Long offset = start - addrV7M;
        Long idxStart = offset / subRegionSize;
        Long idxEnd = idxStart + subRegCount;
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
        int count = 0;
        for (int i = startIndex; i < memoryBlocks.size(); i++) {
            if (!appendMemoryBlock(memoryBlocks.get(i))) {
                break; // cannot add next region
            }
            count++;
        }
        if (getCoreArchitecture() == ECoreArchitecture.ARMv7) {
            count += addMemoryBlocksV7(memoryBlocks, startIndex + count, previousEnd);
        }
        return count;
    }

    /**
     * Adds memory blocks following armV7 rules
     * @param memoryBlocks array of memory blocks to add
     * @param startIndex start index in the array
     * @param previousEnd end address of previous MPU region to avoid overlap
     * @return number of memory blocks added to the region
     */
    protected int addMemoryBlocksV7(ArrayList<ICpMemoryBlock> memoryBlocks, int startIndex, Long previousEnd) {
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

    /**
     * Appends memory block to this region if it immediately follows the region end
     *
     * @param memoryBlock ICpMemoryBlock to append
     * @return true if appended
     */
    protected boolean appendMemoryBlock(ICpMemoryBlock memoryBlock) {

        if (fMemoryBlocks.isEmpty()) {
            fMemoryBlocks.add(memoryBlock);
            // initialize values
            setAttributes(memoryBlock.attributes());
            fStart = memoryBlock.getStart();
            fSize = memoryBlock.getSize();
            adjustV7Values(true);
            return true;
        }
        if (!arePermissionsEqual(memoryBlock))
            return false;

        long start = memoryBlock.getStart();
        if (start < getStart())
            return false;

        long thisStop = getStop();
        if (thisStop + 1 != start)
            return false;

        fMemoryBlocks.add(memoryBlock);
        Long size = getSize();
        size += memoryBlock.getSize();
        setSize(size);

        adjustV7Values(true);
        return true;
    }
}
