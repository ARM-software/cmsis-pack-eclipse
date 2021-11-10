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
import com.arm.cmsis.pack.enums.EFileCategory;
import com.arm.cmsis.pack.enums.EFileRole;

/**
 * Default implementation of ICpFile interface
 */
public class CpFile extends CpItem implements ICpFile {

    protected EFileRole role = null;
    protected EFileCategory category = null;

    /**
     * @param parent
     * @param tag
     */
    public CpFile(ICpItem parent, String tag) {
        super(parent, tag);
    }

    @Override
    public synchronized EFileCategory getCategory() {
        if (category == null) {
            category = EFileCategory.fromString(getAttribute(CmsisConstants.CATEGORY));
        }
        return category;
    }

    @Override
    public synchronized EFileRole getRole() {
        if (role == null) {
            role = EFileRole.fromString(getAttribute(CmsisConstants.ATTR));
        }
        return role;
    }

    @Override
    public boolean isDeviceDependent() {
        if (super.isDeviceDependent())
            return true;
        ICpComponent c = getParentComponent();
        if (c != null)
            return c.isDeviceDependent();
        return false;
    }

}
