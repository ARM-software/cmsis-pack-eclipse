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

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpMemory;
import com.arm.cmsis.pack.enums.EMemoryPrivilege;
import com.arm.cmsis.pack.enums.EMemorySecurity;
import com.arm.cmsis.pack.permissions.IMemoryPermissions;

/**
 * A memory block interface
 */
public interface ICpMemoryBlock extends ICpResourceItem, ICpMemory {

    /**
     * Clones memory block
     *
     * @return new ICpMemoryBlock that is a clone of this one
     */
    ICpMemoryBlock cloneBlock();

    /**
     * Clones memory block and adds the clone to new parent, does not copy children
     *
     * @param newParent new parent of the clone
     * @return new ICpItem block that is a clone of this one
     */
    ICpMemoryBlock copyBlockTo(ICpItem newParent);

    /**
     * Clones child blocks to specified parent
     *
     * @param newParent new parent of the clone
     */
    void copyChildBlocksTo(ICpItem newParent);

    /**
     * Returns collection of sub-blocks
     *
     * @return collection of ICpMemoryBlock children
     */
    default Collection<ICpMemoryBlock> getSubBlocks() {
        return getChildrenOfType(ICpMemoryBlock.class);
    }

    /**
     * Checks if this block has sub-blocks
     *
     * @return true if the block has sub-blocks
     */
    default boolean hasSubBlocks() {
        return getFirstChildOfType(ICpMemoryBlock.class) != null;
    }

    /**
     * Returns a sub-block with given name
     *
     * @param name block name
     * @return ICpMemoryBlock or null
     */
    ICpMemoryBlock getSubBlock(String name);

    /**
     * Returns physical or logical address
     *
     * @param bPhysical flag to return physical address (if true), otherwise logical
     * @return physical or logical start address
     */
    long getStartAddress(boolean bPhysical);

    /**
     * Calculates free size to allocate child blocks
     *
     * @return free size
     */
    Long getFreeSize();

    /**
     * Returns available free size for given permissions
     *
     * @param permissios
     * @return available size
     */
    Long getFreeSize(IMemoryPermissions permissios);

    /**
     * Set offset value
     *
     * @param offset offset to set
     */
    void setOffset(Long offset);

    /**
     * Check if block has fixed start and/or offset
     *
     * @return true if <code>"fixed"</code> attribute is set to true
     */
    default boolean isFixed() {
        if (hasExplicitStart())
            return true;
        return getAttributeAsBoolean(CmsisConstants.FIXED, false);
    }

    /**
     * Returns if the block is visible to a given processor
     *
     * @return true if visible for processor
     */
    boolean isVisibleToProcessor(String processor);

    /**
     * Returns zone assignment map
     *
     * @return Map<String, ICpZoneItem>
     */
    Map<String, ICpZoneAssignment> getAssignments();

    /**
     * Returns how many zones have assignments for this block
     *
     * @return number of assigned zones
     */
    int getAssignmentCount();

    /**
     * Returns an assignment for specified zone
     *
     * @param zoneName ICpZone name
     * @return ICpZoneItem or null
     */
    ICpZoneAssignment getAssignment(String zoneName);

    /**
     * Checks if block is assigned to at least one zone
     *
     * @return true if assigned
     */
    boolean isAssigned();

    /**
     * Checks if block is assigned to the specified zone
     *
     * @param zoneName ICpZone name
     * @return true if assigned
     */
    boolean isAssigned(String zoneName);

    /**
     * Returns if at least one of the assigned zone belongs to specified processor
     *
     * @param processor ICpProcessorUnit unit
     * @return true if assigned for processor
     */
    boolean isAssigned(ICpProcessorUnit processor);

    /**
     * Returns if at least one of the zone assignments corresponds to specified
     * security permission
     *
     * @param security EMemorySecurity
     * @return true if assigned to a zone with specified security
     */
    boolean isAssigned(EMemorySecurity security);

    /**
     * Returns if at least one of the zone assignments corresponds to specified
     * privilege permission
     *
     * @param privilege EMemoryPrivilege
     * @return true if assigned to a zone with specified privilege
     */
    boolean isAssigned(EMemoryPrivilege privilege);

    /**
     * Returns assigned security string (combination of all assignments)
     *
     * @return combined assigned siring
     */
    String getAssignedSecurity();

    /**
     * Removes an assignment to the specified zone
     *
     * @param zoneName ICpZone name
     */
    void addAssignment(String zoneName, ICpZoneAssignment zoneItem);

    /**
     * Removes an assignment to the specified zone
     *
     * @param zoneName ICpZone name
     */
    void removeAssignment(String zoneName);

    /**
     * Removes all zone assignments for this block
     */
    void removeAssignments();

    /**
     * Renames all zone assignments according to block name
     */
    void renameAssignments();

    /**
     * Get the type specifier for this memory block.
     *
     * @return Type attribute
     */
    String getType();

    /**
     * Checks if the memory block can be deleted
     *
     * @return true if block can be deleted
     */
    boolean isDeletable();

    /**
     * Returns physical region for this block
     *
     * @return PhysicalMemoryRegion or null if block is not in the hierarchy
     */
    PhysicalMemoryRegion getPhysicalRegion();

    /**
     * Checks if the block exists in the resource file
     *
     * @return true if exists
     */
    boolean resourceExists();

    /**
     * Arranges memory blocks according to their sizes
     */
    void arrangeBlocks();

    /**
     * Returns parent region name
     *
     * @return parent region name
     */
    default String getParentRegionName() {
        return getAttribute(CmsisConstants.PARENT);
    }

    /**
     * Constructs memory block ID out of tag, name and group
     *
     * @param tag       element tag: memory or peripheral
     * @param name      block/peripheral name
     * @param groupName peripheral group name, ignored if tag == memory
     * @return constructed ID
     */
    static String constructBlockId(String tag, String name, String groupName) {
        String id = tag + CmsisConstants.COLON;
        if (!CmsisConstants.MEMORY_TAG.contentEquals(tag) && groupName != null && !groupName.isEmpty()) {
            id += groupName + CmsisConstants.COLON;
        }
        id += name;
        return id;
    }

}
