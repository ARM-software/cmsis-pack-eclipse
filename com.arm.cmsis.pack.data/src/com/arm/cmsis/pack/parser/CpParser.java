/*******************************************************************************
* Copyright (c) 2023 ARM Ltd. and others
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

package com.arm.cmsis.pack.parser;

import com.arm.cmsis.pack.data.CpRootItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.error.CmsisErrorCollection;

/**
 *
 */
public abstract class CpParser extends CmsisErrorCollection implements ICpParser {

    protected ICpItem rootItem = null; // represents top-level item being constructed
    protected String file = null; // current file

    @Override
    public ICpItem createItem(ICpItem parent, String tag) {
        if (parent != null) {
            return parent.createItem(parent, tag);
        }
        if (rootItem == null) {
            rootItem = createRootItem(tag);
        }
        return rootItem;
    }

    @Override
    public ICpItem createRootItem(String tag) {
        return new CpRootItem(tag, file);
    }

}
