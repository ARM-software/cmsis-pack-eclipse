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

import com.arm.cmsis.pack.data.CpPack;
import com.arm.cmsis.pack.data.ICpItem;

/**
 * Class to parse pdsc files
 */
public class PdscParser extends CpXmlParser {

    public PdscParser() {
    }

    public PdscParser(String xsdFile) {
        super(xsdFile);
    }

    @Override
    public ICpItem createRootItem(String tag) {
        return new CpPack(tag, file);
    }
}
