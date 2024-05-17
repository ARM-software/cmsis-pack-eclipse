/*******************************************************************************
* Copyright (c) 2019 ARM Ltd. and others
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

import java.util.Map;

import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.generic.Attributes;
import com.arm.cmsis.pack.generic.IAttributedItem;
import com.arm.cmsis.pack.generic.IAttributes;

/**
 * Peripheral's slot (channel or pin) item
 */
public class CpSlot extends CpResourceItem implements ICpSlot {
    protected IAttributes fOriginalAttributes = null;

    public CpSlot(ICpItem parent, String tag) {
        super(parent, tag);
    }

    @Override
    public IAttributes getOriginalAttributes() {
        if (fOriginalAttributes == null) {
            fOriginalAttributes = new Attributes(attributes());
        }
        return fOriginalAttributes;
    }

    @Override
    public boolean updateAttribute(String key, String value) {
        getOriginalAttributes(); // ensure stored original attributes
        return super.updateAttribute(key, value);
    }

    @Override
    public boolean updateAttributes(IAttributes newAttributes) {
        getOriginalAttributes(); // ensure stored original attributes
        return super.updateAttributes(newAttributes);
    }

    @Override
    public boolean updateAttributes(IAttributedItem item) {
        getOriginalAttributes(); // ensure stored original attributes
        return super.updateAttributes(item);
    }

    @Override
    public boolean updateAttributes(Map<String, String> attributes) {
        getOriginalAttributes(); // ensure stored original attributes
        return super.updateAttributes(attributes);
    }

}
