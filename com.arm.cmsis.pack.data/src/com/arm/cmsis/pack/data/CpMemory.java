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

package com.arm.cmsis.pack.data;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.permissions.IMemoryAccess;

/**
 * Implementation of ICpMemory interface
 */
public class CpMemory extends CpDeviceProperty implements ICpMemory {

    public CpMemory(ICpItem parent, String tag) {
        super(parent, tag);
    }

    @Override
    public String constructId() {
        String id = getAttribute(CmsisConstants.ID);
        if (id.isEmpty())
            id = getAttribute(CmsisConstants.NAME);
        return id;
    }

    @Override
    protected String constructName() {
        if (hasAttribute(CmsisConstants.NAME)) {
            return getAttribute(CmsisConstants.NAME);
        } else if (hasAttribute(CmsisConstants.ID)) {
            return getAttribute(CmsisConstants.ID);
        }
        return super.constructName();
    }

    @Override
    public String getAccessString() {
        String access = getAttribute(CmsisConstants.ACCESS);
        if (access.isEmpty()) {
            ICpMemory parentRegion = getParentMemory();
            if (parentRegion != null) {
                access = parentRegion.getAccessString();
            } else {
                // construct access from ID
                access += READ_ACCESS;
                String id = attributes().getAttribute(CmsisConstants.ID);
                if (id != null) {
                    if (id.startsWith(CmsisConstants.IRAM))
                        access += WRITE_ACCESS;
                } else {
                    // actually error situation, should not happen by correctly written pdsc
                }
                access += EXECUTE_ACCESS;
            }
            attributes().setAttribute(CmsisConstants.ACCESS, IMemoryAccess.normalize(access)); // cache attribute
        }
        return access;
    }
}
