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

        return ftlRegion;
    }

    @Override
    public int addMemoryBlocks(ArrayList<ICpMemoryBlock> memoryBlocks, int startIndex, Long previousEnd) {
        if (memoryBlocks == null || startIndex < 0 || startIndex >= memoryBlocks.size()) {
            return 0;
        }

        // add adjusted regions
        int count = 0;
        for (int i = startIndex; i < memoryBlocks.size(); i++) {
            if (!appendMemoryBlock(memoryBlocks.get(i))) {
                break; // cannot add next region
            }
            count++;
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
            // very first block: initialize values
            fMemoryBlocks.add(memoryBlock);
            setAttributes(memoryBlock.attributes());
            fStart = memoryBlock.getStart();
            fSize = memoryBlock.getSize();
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

        return true;
    }
}
