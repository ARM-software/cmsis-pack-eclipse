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

package com.arm.cmsis.zone.data;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;

/* (non-Javadoc)
 * @see com.arm.cmsis.zone.data.ICpProcessorUnit
 */
public class CpProcessorUnit extends CpResourceItem implements ICpProcessorUnit {

    public CpProcessorUnit(ICpItem parent, String tag) {
        super(parent, tag);
    }

    @Override
    public void invalidate() {
        super.invalidate();
    }

    @Override
    protected String constructName() {
        if (hasAttribute(CmsisConstants.PNAME)) {
            return getAttribute(CmsisConstants.PNAME);
        }
        if (hasAttribute(CmsisConstants.DCORE)) {
            return getAttribute(CmsisConstants.DCORE);
        }
        return getTag();
    }

    @Override
    public String getDeviceName() {
        return getParentDeviceUnit().getDeviceName();
    }
}
