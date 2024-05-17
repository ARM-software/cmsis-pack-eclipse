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

import com.arm.cmsis.pack.data.ICpMemory;
import com.arm.cmsis.pack.permissions.IMemoryPermissions;

/**
 * Interface to setup MPC (memory protection controller)
 */
public interface IMpcRegion extends ICpMpc {

    /**
     * Checks if supplied permissions match for given MPC (can be used in the same
     * slot)
     *
     * @param p     IMemoryPermissions to match
     * @param index MPC block index
     * @return true if matches
     */
    boolean matchPermissions(IMemoryPermissions p, Integer index);

    /**
     * Checks if supplied permissions match required number of MPC blocks
     *
     * @param p      IMemoryPermissions to match
     * @param offset offset to begin
     * @param size   required size
     * @return true if matches
     */
    boolean matchPermissions(IMemoryPermissions p, Long offset, Long size);

    /**
     * Returns array of permissions for all MPC blocks
     *
     * @return array of IMemoryPermissions
     */
    ArrayList<IMemoryPermissions> getMpcPermissionsArray();

    /**
     * Returns permissions of particular block
     *
     * @param index MPC block index
     * @return block permissions or null if index out of range
     */
    IMemoryPermissions getMpcBlockPermissions(Integer index);

    /**
     * Sets permissions to an MPC block of index
     *
     * @param index  MPC block index
     * @param memory ICpMemory to set
     * @return true if set, false if index is out of range
     */
    boolean setMpcBlockPermissions(Integer index, ICpMemory memory);

    /**
     * Set permissions to an MPC block(s) occupied by memory
     *
     * @param memory    ICpMemory providing offset, size and permissions
     * @param mpcOffset block's region offset from the MPC address
     * @return true if set, false if memory is null or index is out of range
     */
    boolean setMpcPermissions(ICpMemory memory, Long mpcOffset);

    /**
     * Returns MPC block offset for given index
     *
     * @param index block index
     * @return offset or null if index is out of range;
     */
    Long getMpcBlockOffset(Integer index);

    /**
     * Returns block index for given offset
     *
     * @param offset memory offset
     * @return block index or -1 if block is outside boundaries
     */
    Integer getMpcBlockIndex(Long offset);

    /**
     * Returns MPC block offset of the next block following one containing this
     * offest
     *
     * @param offset memory offset
     * @return block index or -1 if block is outside boundaries
     */
    Long getNextMpcBlockOffset(Long offset);

    /**
     * Calculates next available index for given permissions and size
     *
     * @param permissions IMemoryPermissions
     * @param offset      memory offset
     * @param size        required size
     * @return available index or -1 if not enough space is available
     */
    Integer getNextAvailableIndex(IMemoryPermissions permissions, Long offset, Long size);

    /**
     * Calculates next available offset for given permissions, size and initial
     * offset
     *
     * @param permissions IMemoryPermissions
     * @param offset      initial memory offset
     * @param size        required size
     * @return available offset or null if not enough space is available
     */
    Long getNextAvailableOffset(IMemoryPermissions permissions, Long offset, Long size);

    /**
     * Calculates next available offset providing maximum size for for given
     * permissions and initial offset
     *
     * @param permissions IMemoryPermissions
     * @param offset      initial memory offset
     * @return available offset or null if no space is available
     */
    Long getNextAvailableOffset(IMemoryPermissions permissions, Long offset);

    /**
     * Invalidates MPC permissions for all blocks starting with the offset
     *
     * @param offset start offset to invalidate
     * @param size   size to invalidate
     */
    void invalidatePermissions(Long offset, Long size);

}