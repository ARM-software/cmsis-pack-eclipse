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

package com.arm.cmsis.zone.svd;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;

/*
 * Interrupt SVD item
 */
public class SvdInterrupt extends SvdItem implements ISvdInterrupt {

    public SvdInterrupt(ICpItem parent) {
        super(parent);
    }

    public SvdInterrupt(ICpItem parent, String tag) {
        super(parent, tag);
    }

    @Override
    public String getValueString() {
        ICpItem valueItem = getFirstChild(CmsisConstants.VALUE);
        if (valueItem != null)
            return valueItem.getText();
        return null;
    }
}
