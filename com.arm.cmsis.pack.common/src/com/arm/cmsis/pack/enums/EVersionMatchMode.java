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

package com.arm.cmsis.pack.enums;

import com.arm.cmsis.pack.common.CmsisConstants;

/**
 * Enumeration describing behavior when selecting an Item:
 * <dl>
 * <dt>LATEST</dt>
 * <dd>use the latest available item (pack, device, component, etc.)
 * <dt>FIXED</dt>
 * <dd>use only version defined by getVersion() method</dd>
 * <dt>EXCLUDED</dt>
 * <dd>do not use the item</dd>
 * </dl>
 * </p>
 *
 */
public enum EVersionMatchMode {
    LATEST, FIXED, EXCLUDED;

    public static EVersionMatchMode fromString(final String str) {
        if (str != null && !str.isEmpty()) {
            if (str.equals(CmsisConstants.FIXED))
                return FIXED;
            else if (str.equals(CmsisConstants.EXCLUDED))
                return EXCLUDED;
        }
        return LATEST;
    }

    public static String toString(EVersionMatchMode mode) {
        switch (mode) {
        case FIXED:
            return CmsisConstants.FIXED;
        case EXCLUDED:
            return CmsisConstants.EXCLUDED;
        case LATEST:
        default:
            break;
        }
        return null;
    }

    @Override
    public String toString() {
        return toString(this);
    }

}
