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

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpMemory;
import com.arm.cmsis.pack.permissions.IMemoryPermissions;

/**
 * Interface for the MPC (memory protection controller) elements.
 */
public interface ICpMpc extends ICpResourceItem, ICpMemory {

    /**
     * Returns block size
     *
     * @return block size
     */
    Long getMpcBlockSize();

    /**
     * Returns block size as String
     *
     * @return block size string
     */
    default String getBlockSizeSting() {
        return getAttribute(CmsisConstants.BLK_CFG);
    }

    /**
     * Returns type string
     *
     * @return type String
     */
    default String getTypeString() {
        return getAttribute(CmsisConstants.TYPE);
    }

    /**
     * Checks if MPC supports security (default)
     *
     * @return true if security attribute is used by MPC
     */
    boolean supportsSecurity();

    /**
     * Checks if MPC supports security (default)
     *
     * @return true if security attribute is used by MPC
     */
    boolean supportsPrivilege();

    /**
     * Checks if supplied permissions match for given MPC (can be used in the same
     * slot)
     *
     * @param p1 first IMemoryPermissions to match
     * @param p2 first IMemoryPermissions to match
     * @return true if p1 matches p2 for this MPC settings
     */
    boolean matchPermissions(IMemoryPermissions p1, IMemoryPermissions p2);

    /**
     * Returns total block count available in controller
     *
     * @return block count
     */
    Integer getMpcBlockCount();

    /**
     * Returns block count required for given size
     *
     * @param size required size
     * @return required block count or -1 if MPU values are invalid
     */
    Integer getMpcBlockCount(Long size);

}