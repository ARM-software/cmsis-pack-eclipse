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

package com.arm.cmsis.zone.svd;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpRootItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.parser.CpXmlParser;

/**
 *
 */
public class SvdParser extends CpXmlParser {

    public SvdParser() {
    }

    public SvdParser(String xsdFile) {
        super(xsdFile);
    }

    @Override
    public ICpItem createRootItem(String tag) {
        ICpItem root = new CpRootItem(CmsisConstants.EMPTY_STRING, getXmlFile()); // pseudo-root
        ICpItem svdItem = new SvdRoot(root);
        root.addChild(svdItem);
        return svdItem;
    }

}
