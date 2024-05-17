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

package com.arm.cmsis.pack.parser;

import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.info.CpConfigurationInfo;

/**
 * Simple parser to read ICpConfigurationInfo from a file
 */
public class CpConfigParser extends CpXmlParser {

    /**
     * Default constructor
     */
    public CpConfigParser() {
    }

    /**
     * @param xsdFile schema file
     */
    public CpConfigParser(String xsdFile) {
        super(xsdFile);
    }

    @Override
    public ICpItem createItem(ICpItem parent, String tag) {
        if (parent != null) {
            return CpConfigurationInfo.createChildItem(parent, tag);
        }
        return super.createItem(parent, tag);
    }

    @Override
    public ICpItem createRootItem(String tag) {
        return new CpConfigurationInfo(tag, file);
    }

    @Override
    protected boolean isItemIgnored(ICpItem item) {
        if (item == null)
            return true;
        if (item.isGenerated()) {
            return true;
        }
        return super.isItemIgnored(item);
    }

}
