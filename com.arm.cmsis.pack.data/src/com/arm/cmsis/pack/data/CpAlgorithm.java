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

/**
 *
 */
public class CpAlgorithm extends CpDeviceProperty implements ICpAlgorithm {

    public CpAlgorithm(ICpItem parent, String tag) {
        super(parent, tag);
    }

    @Override
    public String getAlgorithmFile() {
        return getAbsolutePath(getName());
    }

    @Override
    public long getRAMStart() {
        return attributes().getAttributeAsLong(CmsisConstants.RAMSTART, 0);
    }

    @Override
    public long getRAMSize() {
        return attributes().getAttributeAsLong(CmsisConstants.RAMSIZE, 0);
    }

}
