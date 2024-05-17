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
 * Error severity : fatal, error, warning, information
 */
public enum ESeverity {

    None, Info, Warning, Error, FatalError;

    protected static String[] fNames = new String[] { CmsisConstants.EMPTY_STRING, CmsisConstants.Info,
            CmsisConstants.Warning, CmsisConstants.Error, CmsisConstants.FatalError };

    /**
     * Sets localized names
     *
     * @param names array of localized names, may not be null
     */
    public static void setNames(String[] names) {
        if (names != null)
            fNames = names;
    }

    /**
     * Checks if this is a severe error : Error or FatalError
     *
     * @return true if severe
     */
    public boolean isSevere() {
        return this == Error || this == FatalError;
    }

    /**
     * Checks if this is a warning
     *
     * @return true if warning
     */
    public boolean isWarning() {
        return this == Warning;
    }

    /**
     * Checks if this is an info
     *
     * @return true if info
     */
    public boolean isInfo() {
        return this == Info;
    }

    @Override
    public String toString() {
        int index = ordinal();
        if (index >= 0 && index < fNames.length)
            return fNames[index];

        return CmsisConstants.EMPTY_STRING;
    }

}
