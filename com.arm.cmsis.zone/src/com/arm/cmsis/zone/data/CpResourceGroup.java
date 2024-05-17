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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.MemoryStartComparator;
import com.arm.cmsis.zone.error.Messages;

/**
 * Resource group
 */
public class CpResourceGroup extends CpResourceItem implements ICpResourceGroup {

    protected Map<String, ICpMemoryBlock> fMemoryBlocks = null; // all blocks including peripherals and peripheral
                                                                // groups
    protected ICpMemoryBlock[] fMemoryBlockArray = null; // cached array of the memory block children

    public CpResourceGroup(ICpItem parent, String tag) {
        super(parent, tag);
    }

    @Override
    public void clear() {
        fMemoryBlocks = null;
        fMemoryBlockArray = null;
        super.clear();
    }

    @Override
    public void invalidate() {
        fMemoryBlocks = null;
        fMemoryBlockArray = null;
        super.invalidate();
    }

    @Override
    public String getEffectiveName() {
        String tag = getTag();
        if (tag.equals(CmsisConstants.MEMORIES))
            return Messages.getString("CpResourceGroup.Memory"); //$NON-NLS-1$
        if (tag.equals(CmsisConstants.PERIPHERALS))
            return Messages.getString("CpResourceGroup.Peripherals"); //$NON-NLS-1$
        return super.getEffectiveName();
    }

    @Override
    public ICpResourceGroup getResourceGroup(String name) {
        ICpItem child = getFirstChild(name);
        if (child instanceof ICpResourceGroup) {
            return (ICpResourceGroup) child;
        }
        return null;
    }

    @Override
    public ICpResourceGroup ensureResourceGroup(String tag) {
        ICpResourceGroup group = getResourceGroup(tag);
        if (group == null) {
            group = new CpResourceGroup(this, tag);
            addChild(group);
        }
        return group;
    }

    @Override
    public ICpPeripheralGroup ensurePeripheralGroup(String name) {
        ICpPeripheralGroup group = getPeripheralGroup(name);
        if (group == null) {
            group = new CpPeripheralGroup(this, CmsisConstants.GROUP);
            group.setName(name);
            addChild(group);
        }
        return group;
    }

    @Override
    public ICpPeripheralGroup getPeripheralGroup(String name) {
        return getFirstChildOfType(name, ICpPeripheralGroup.class);
    }

    @Override
    public Object[] getEffectiveChildArray() {
        return getMemoryBlocksAsArray();
    }

    @Override
    public ICpMemoryBlock[] getMemoryBlocksAsArray() {
        if (fMemoryBlockArray == null) {
            ICpRootZone rootZone = getRootZone();
            boolean bShowPeripheral = rootZone.getZoneOption(CmsisConstants.PERIPHERAL, CmsisConstants.SHOW);
            boolean bShowROM = rootZone.getZoneOption(CmsisConstants.ROM, CmsisConstants.SHOW);
            boolean bShowRAM = rootZone.getZoneOption(CmsisConstants.RAM, CmsisConstants.SHOW);
            List<ICpMemoryBlock> blocks = new ArrayList<>();
            for (ICpMemoryBlock block : getMemoryBlocksAsMap().values()) {
                if (isShowBlock(block, bShowRAM, bShowROM, bShowPeripheral)) {
                    blocks.add(block);
                }
            }

            Collections.sort(blocks, new MemoryStartComparator(true));
            fMemoryBlockArray = blocks.toArray(new ICpMemoryBlock[0]);
        }
        return fMemoryBlockArray;
    }

    protected boolean isShowBlock(ICpMemoryBlock block, boolean bShowRAM, boolean bShowROM, boolean bShowPeripheral) {
        if (!bShowPeripheral && block.isPeripheral())
            return false;
        if (!bShowROM && block.isROM())
            return false;
        if (!bShowRAM && block.isRAM())
            return false;
        return true;
    }

    @Override
    public Map<String, ICpMemoryBlock> getMemoryBlocksAsMap() {
        if (fMemoryBlocks == null) {
            fMemoryBlocks = constructMemoryBlockMap();
        }
        return fMemoryBlocks;
    }

    @Override
    public ICpMemoryBlock getMemoryBlock(String id) {
        return getMemoryBlocksAsMap().get(id);
    }

    /**
     * Constructs memory block map for this group
     *
     * @return Map<String, ICpMemoryBlock>
     */
    protected Map<String, ICpMemoryBlock> constructMemoryBlockMap() {
        Map<String, ICpMemoryBlock> memoryBlocks = new TreeMap<>();
        Collection<ICpMemoryBlock> blocks = getChildrenOfType(ICpMemoryBlock.class);
        for (ICpMemoryBlock block : blocks) {
            memoryBlocks.put(block.getId(), block);
        }
        return memoryBlocks;
    }
}
