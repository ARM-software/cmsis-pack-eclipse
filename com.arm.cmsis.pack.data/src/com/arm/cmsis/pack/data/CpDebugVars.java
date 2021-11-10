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
 * Debugvars element
 */
public class CpDebugVars extends CpDeviceProperty implements ICpDebugVars {

    public CpDebugVars(ICpItem parent, String tag) {
        super(parent, tag);
    }

    @Override
    public String getName() {
        return getAttribute(CmsisConstants.CONFIGFILE);
    }

    @Override
    public EFileCategory getCategory() {
        return EFileCategory.OTHER;
    }

    @Override
    public EFileRole getRole() {
        return EFileRole.CONFIG;
    }

}
