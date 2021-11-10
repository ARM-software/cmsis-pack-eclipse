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

package com.arm.cmsis.zone.data;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;

/**
 * Element describing creator properties
 */
public class CpZoneCreator extends CpZoneItem implements ICpZoneCreator {

    public CpZoneCreator(ICpItem parent, String tag) {
        super(parent, tag);
    }

    @Override
    public String getTool() {
        return getAttribute(CmsisConstants.TOOL);
    }

    @Override
    public void setTool(String tool) {
        setAttribute(CmsisConstants.TOOL, tool);
    }

}
