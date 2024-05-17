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
