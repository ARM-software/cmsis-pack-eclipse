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

package com.arm.cmsis.pack.data;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.enums.EDebugProtocolType;

/**
 *
 */
public class CpDebugPort extends CpDeviceProperty implements ICpDebugPort {

    public CpDebugPort(ICpItem parent) {
        this(parent, CmsisConstants.DEBUGPORT_TAG);
    }

    public CpDebugPort(ICpItem parent, String tag) {
        super(parent, tag);
    }

    @Override
    public boolean isProtocolImplemented(EDebugProtocolType protocolType) {
        return getProtocol(protocolType) != null;
    }

    @Override
    public ICpDebugProtocol getProtocol(EDebugProtocolType protocolType) {
        return getProtocol(protocolType.toString());
    }

    @Override
    public ICpDebugProtocol getProtocol(String protocolType) {
        ICpItem child = getFirstChild(protocolType);
        if (child instanceof ICpDebugProtocol)
            return (ICpDebugProtocol) child;
        return null;
    }

    @Override
    public String getName() {
        return getAttribute(CmsisConstants.__DP);
    }

}
