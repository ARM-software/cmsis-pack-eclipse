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
import com.arm.cmsis.pack.enums.EDataPatchAccessType;

/**
 * Convenience class to access information under "datapatch" device debug
 * property
 */
public class CpDataPatch extends CpDeviceProperty implements ICpDataPatch {

    public CpDataPatch(ICpItem parent, String tag) {
        super(parent, tag);
    }

    @Override
    public long getPatchAddress() {
        return getAddress();
    }

    @Override
    public long getPatchValue() {
        return attributes().getAttributeAsLong(CmsisConstants.VALUE, 0);
    }

    @Override
    public long getPatchMask() {
        return attributes().getAttributeAsLong(CmsisConstants.MASK, CmsisConstants.DEFAULT_DATAPATCH_MASK);
    }

    @Override
    public EDataPatchAccessType getAccessType() {
        return EDataPatchAccessType.fromString(getAttribute(CmsisConstants.TYPE));
    }
}
