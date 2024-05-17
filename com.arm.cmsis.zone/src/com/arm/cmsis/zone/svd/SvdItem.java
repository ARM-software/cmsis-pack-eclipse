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
import com.arm.cmsis.pack.data.CpItem;
import com.arm.cmsis.pack.data.ICpItem;

/*
 * Base SVD item
 */
public class SvdItem extends CpItem implements ISvdItem {

    public SvdItem(ICpItem parent) {
        super(parent);
    }

    public SvdItem(ICpItem parent, String tag) {
        super(parent, tag);
    }

    @Override
    protected ICpItem createChildItem(String tag) {
        if (tag.equals(CmsisConstants.PERIPHERAL))
            return new SvdPeripheral(this, tag);
        if (tag.equals(CmsisConstants.INTERRUPT))
            return new SvdInterrupt(this, tag);
        return new SvdItem(this, tag);
    }

    @Override
    public String getName() {
        ICpItem nameChild = getFirstChild(CmsisConstants.NAME);
        if (nameChild != null)
            return nameChild.getText();
        return getTag();
    }

    @Override
    public ISvdRoot getSvdRoot() {
        return getParentOfType(ISvdRoot.class);
    }

}
