package com.arm.cmsis.pack.enums;

import com.arm.cmsis.pack.common.CmsisConstants;

/*******************************************************************************
 * Copyright (c) 2021 ARM Ltd and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/

/**
 * Enumeration value corresponding <code>"attr"</code> attribute in pdsc file
 *
 * @see ICpFile
 */
public enum EFileRole {
    NONE, COPY, CONFIG, TEMPLATE, INTERFACE;

    /**
     * @param str value of <code>"attr"</code> attribute
     * @return corresponding enumeration value
     */
    public static EFileRole fromString(final String str) {
        if (str == null)
            return NONE;
        switch (str) {
        case CmsisConstants.COPY:
            return COPY;
        case CmsisConstants.CONFIG:
            return CONFIG;
        case CmsisConstants.TEMPLATE:
            return TEMPLATE;
        case CmsisConstants.INTERFACE:
            return INTERFACE;
        default:
            return NONE;
        }
    }
}
