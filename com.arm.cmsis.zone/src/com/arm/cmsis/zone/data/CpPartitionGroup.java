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

/**
 * Class representing partition element in azone file
 */
public class CpPartitionGroup extends CpResourceGroup implements ICpPartitionGroup {

    public CpPartitionGroup(ICpItem parent, String tag) {
        super(parent, tag);
    }

    @Override
    protected ICpItem createChildItem(String tag) {
        switch (tag) {
        case CmsisConstants.MEMORY_TAG:
            return new CpMemoryBlock(this, tag);
        case CmsisConstants.SLOT:
            return new CpSlot(this, tag);
        default:
            break;
        }
        return super.createChildItem(tag);
    }

}
