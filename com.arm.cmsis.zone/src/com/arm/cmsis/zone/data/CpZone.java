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
import java.util.LinkedList;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.enums.ECoreArchitecture;
import com.arm.cmsis.pack.enums.EMemorySecurity;
import com.arm.cmsis.pack.enums.ESeverity;
import com.arm.cmsis.zone.error.CmsisZoneError;

/**
 * Individual zone
 */
public class CpZone extends CpZoneItem implements ICpZone {

    protected ICpMpuSetup fMpuSetup = null;

    public CpZone(ICpItem parent, String tag) {
        super(parent, tag);
    }

    @Override
    public ICpItem getEffectiveParent() {
        return null; // affects getEffectiveAttributes()
    }

    @Override
    public void invalidate() {
        fMpuSetup = null;
        super.invalidate();
    }

    @Override
    public ICpMpuSetup getMpuSetup() {
        if (fMpuSetup == null)
            createMpuSetup();
        return fMpuSetup;
    }

    @Override
    public void createMpuSetup() {
        fMpuSetup = new CpMpuSetup(this); // always overwrite
        clearErrors(CmsisZoneError.Z11_MASK);
        int numRegions = getNumMpuRegions();
        if (numRegions > 0) {
            Collection<ICpMemoryBlock> memoryBlocks = getAssignedMemoryBlocks();
            Collection<ICpMpuRegion> mpuRegions = fMpuSetup.constructMpuRegions(memoryBlocks);
            if (mpuRegions.size() > numRegions) {
                addError(new CmsisZoneError(this, ESeverity.Warning, CmsisZoneError.Z110));
            }
        }
    }

    @Override
    public ICpZoneAssignment getZoneAssignment(String zoneName) {
        ICpItem item = getFirstChild(zoneName);
        if (item instanceof ICpZoneAssignment) {
            return (ICpZoneAssignment) item;
        }
        return null;
    }

    @Override
    public Collection<ICpMemoryBlock> getAssignedMemoryBlocks() {

        Collection<ICpMemoryBlock> memoryBlocks = new LinkedList<>();
        Collection<ICpZoneAssignment> assignments = getZoneAssignments();
        if (assignments == null || assignments.isEmpty())
            return memoryBlocks;

        // Iterate over assignments and create MPU regions as individual item
        for (ICpZoneAssignment a : assignments) {
            ICpMemoryBlock block = a.getAssignedBlock();
            if (block == null) {
                continue;
            }
            memoryBlocks.add(block);
        }
        return memoryBlocks;
    }

    @Override
    public Collection<ICpZoneAssignment> getZoneAssignments() {
        return getChildrenOfType(ICpZoneAssignment.class);
    }

    @Override
    protected ICpItem createChildItem(String tag) {
        if (CmsisConstants.ASSIGN.equals(tag)) {
            return new CpZoneAssignment(this, tag);
        }
        return super.createChildItem(tag);
    }

    @Override
    public ICpDeviceUnit getTargetDevice() {
        ICpRootZone root = getRootZone();
        if (root == null) {
            return null;
        }
        return root.getDeviceUnit();
    }

    @Override
    public ICpProcessorUnit getTargetProcessor() {
        ICpRootZone root = getRootZone();
        if (root == null) {
            return null;
        }
        String pName = getProcessorName();
        return root.getProcessorUnit(pName);
    }

    @Override
    public ECoreArchitecture getArchitecture() {
        ICpProcessorUnit processor = getTargetProcessor();
        if (processor == null) {
            return ECoreArchitecture.UNKNOWN;
        }
        return processor.getArchitecture();
    }

    /**
     * Checks if zone is associated with processor that has MPU
     *
     * @return true if associated processor has MPU
     */
    public boolean hasMPU() {
        ICpProcessorUnit processor = getTargetProcessor();
        if (processor == null) {
            return false;
        }
        return processor.hasMPU();
    }

    /**
     * Returns number of MPU regions supported by associated processor
     *
     * @return number of MPU regions
     */
    public int getNumMpuRegions() {
        ICpProcessorUnit processor = getTargetProcessor();
        if (processor == null) {
            return 0;
        }
        return processor.getNumMpuRegions();
    }

    @Override
    public ICpItem toFtlModel(ICpItem ftlParent) {
        ICpItem ftlZoneItem = super.toFtlModel(ftlParent);
        Collection<ICpZoneAssignment> assignments = getZoneAssignments();
        if (assignments == null || assignments.isEmpty())
            return ftlZoneItem;

        addAssignmentsToFtlModel(ftlZoneItem, assignments, CmsisConstants.MEMORY_TAG);
        addAssignmentsToFtlModel(ftlZoneItem, assignments, CmsisConstants.PERIPHERAL);

        ICpRootZone rootZone = getRootZone();
        String mode = rootZone.getZoneMode();
        if (CmsisConstants.MPU.equals(mode)) {
            ICpItem mpu_setup = getMpuSetup().toFtlModel(ftlZoneItem);
            ftlZoneItem.addChild(mpu_setup);
        }

        return ftlZoneItem;
    }

    /**
     * Adds zone assignments to FTL model
     *
     * @param ftlZoneItem ICpItem to add assignments to
     * @param assignments collection of assignments to add
     * @param tag         assignment tag to consider : " memory" or "peripheral"
     */
    protected void addAssignmentsToFtlModel(ICpItem ftlZoneItem, Collection<ICpZoneAssignment> assignments,
            String tag) {
        if (assignments == null || assignments.isEmpty())
            return;

        for (ICpZoneAssignment a : assignments) {
            ICpMemoryBlock block = a.getAssignedBlock();
            if (block == null) {
                continue;
            }
            if (!block.getTag().equals(tag))
                continue;

            ICpItem item = block.toFtlModel(ftlZoneItem);
            ftlZoneItem.addChild(item);
        }
    }

    @Override
    public boolean canAssign(ICpMemoryBlock block) {
        if (block == null)
            return false;
        if (block instanceof ICpPeripheralGroup)
            return false;
        if (getSecurity() == EMemorySecurity.NON_SECURE) {
            // only non-secure memory can be assigned to no-secure zone
            EMemorySecurity blockSecurity = block.getSecurity();
            return blockSecurity.isNonSecure();
        }
        return true;
    }
}
