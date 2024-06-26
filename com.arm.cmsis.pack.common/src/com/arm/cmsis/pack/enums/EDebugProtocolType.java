package com.arm.cmsis.pack.enums;

import com.arm.cmsis.pack.common.CmsisConstants;

/*******************************************************************************
 * Copyright (c) 2021 ARM Ltd and others.
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

/**
 * Enumeration value corresponding "DataPatchAccessTypeEnum" in pdsc file schema
 *
 * @see ICpDataPatch
 */
public enum EDebugProtocolType {
    SWD, JTAG, CJTAG;

    /**
     * @param str value of <code>"attr"</code> attribute
     * @return corresponding enumeration value
     */
    public static EDebugProtocolType fromString(final String str) {
        if (str == null)
            return SWD;
        switch (str) {
        case CmsisConstants.JTAG:
            return JTAG;
        case CmsisConstants.CJTAG:
            return CJTAG;
        case CmsisConstants.SWD:
        default:
            return SWD;
        }
    }

    public static String toString(EDebugProtocolType type) {
        switch (type) {
        case JTAG:
            return CmsisConstants.JTAG;
        case CJTAG:
            return CmsisConstants.CJTAG;
        case SWD:
        default:
            return CmsisConstants.SWD;
        }
    }

    @Override
    public String toString() {
        return toString(this);
    }

}
