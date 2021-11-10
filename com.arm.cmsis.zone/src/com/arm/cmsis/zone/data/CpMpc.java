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
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.permissions.IMemoryPermissions;
import com.arm.cmsis.pack.permissions.IMemoryPriviledge;
import com.arm.cmsis.pack.permissions.IMemorySecurity;

/**
 * Implementation of an mpc tag
 */
public class CpMpc extends CpResourceItem implements ICpMpc {

    protected Long fBlockSize = null;
    protected Long fStart = null;

    public CpMpc(ICpItem parent) {
        super(parent, CmsisConstants.MPC);
    }

    public CpMpc(ICpItem parent, String tag) {
        super(parent, tag);
    }

    @Override
    public void invalidate() {
        fBlockSize = null;
        fStart = null;
        super.invalidate();
    }

    @Override
    public String constructId() {
        return getName();
    }

    @Override
    public long getStart() {
        if (fStart == null)
            fStart = ICpMpc.super.getStart();
        return fStart;
    }

    @Override
    public long getAddress() {
        return getStart();
    }

    @Override
    public String getAddressString() {
        return getStartString();
    }

    @Override
    public Long getMpcBlockSize() {
        if (fBlockSize == null) {
            fBlockSize = getAttributeAsLong(CmsisConstants.BLK_SIZE, 0L);
            if (fBlockSize == 0L)
                fBlockSize = getSize();
        }
        return fBlockSize;
    }

    @Override
    public Integer getMpcBlockCount() {
        return getMpcBlockCount(getSize());
    }

    @Override
    public Integer getMpcBlockCount(Long size) {
        Long blockSize = getMpcBlockSize();
        if (blockSize <= 0)
            return -1;
        Long count = size / blockSize;
        if (count == 0 || size % blockSize != 0)
            count++;

        return count.intValue();
    }

    @Override
    public boolean supportsSecurity() {
        String type = getTypeString();
        if (type.isEmpty())
            return true; // default

        return type.indexOf(IMemorySecurity.SECURE_ACCESS) >= 0;
    }

    @Override
    public boolean supportsPrivilege() {
        String type = getTypeString();
        if (type.isEmpty())
            return false; // default

        return type.indexOf(IMemoryPriviledge.PRIVILEGED_ACCESS) >= 0;
    }

    @Override
    public boolean matchPermissions(IMemoryPermissions p1, IMemoryPermissions p2) {
        if (p1 == null || p2 == null)
            return true;
        if (supportsSecurity()) {
            if (p1.isSecure() != p2.isSecure())
                return false;
        }
        if (supportsPrivilege()) {
            if (p1.isPrivilegedAccess() != p2.isPrivilegedAccess())
                return false;
        }
        return true;
    }

    @Override
    public ICpItem toFtlModel(ICpItem ftlParent) {
        ICpItem mpc_setup = super.toFtlModel(ftlParent); // will copy all attributes
        mpc_setup.setTag(CmsisConstants.MPC_SETUP);
        return mpc_setup;
    }

}
