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
public enum ESequenceControlType {
    IF, WHILE;

    /**
     * @param str value of <code>"attr"</code> attribute
     * @return corresponding enumeration value
     */
    public static ESequenceControlType fromString(final String str) {
        if (CmsisConstants.WHILE.equals(str))
            return WHILE;
        return IF;
    }

    public static String toString(ESequenceControlType type) {
        if (type == WHILE)
            return CmsisConstants.WHILE;
        return CmsisConstants.IF;
    }

    @Override
    public String toString() {
        return toString(this);
    }

}
