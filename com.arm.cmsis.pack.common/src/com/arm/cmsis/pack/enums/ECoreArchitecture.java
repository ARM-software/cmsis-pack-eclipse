/*******************************************************************************
* Copyright (c) 2022 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.enums;

import com.arm.cmsis.pack.common.CmsisConstants;

/**
 * Enum describing processor core architecture
 */
public enum ECoreArchitecture {

    UNKNOWN, // not yet defined or invalid
    MIXED, // system contains cores with different architectures
    ARMv7, // default one, also assumed for Coretx-M0 that is actually ARMv6
    ARMv8, ARMv81;

    /*
     * Creates enum value from string
     *
     * @param core value of <code>"Dcore"</code> processor attribute
     *
     * @return corresponding enumeration value
     */
    public static ECoreArchitecture fromString(final String core) {
        if (core == null)
            return UNKNOWN;
        if (core.startsWith("ARMV81")) { //$NON-NLS-1$
            return ARMv81;
        }
        if (core.startsWith("ARMV8")) { //$NON-NLS-1$
            return ARMv8;
        }
        switch (core) {
        case CmsisConstants.V7M:
        case "Cortex-M0": //$NON-NLS-1$
        case "Cortex-M0+"://$NON-NLS-1$
        case "Cortex-M3": //$NON-NLS-1$
        case "Cortex-M4": //$NON-NLS-1$
        case "Cortex-M7": //$NON-NLS-1$
        case "SC000": //$NON-NLS-1$
        case "SC300": //$NON-NLS-1$
            return ARMv7;

        case CmsisConstants.MIXED:
            return MIXED;
        case CmsisConstants.UNKNOWN:
            return UNKNOWN;

        case CmsisConstants.V81M:
        case "Cortex-M55": //$NON-NLS-1$
        case "Cortex-M85": //$NON-NLS-1$
            return ARMv81;

        case CmsisConstants.V8M:
        case "Cortex-M23": //$NON-NLS-1$
        case "Cortex-M33": //$NON-NLS-1$
        case "Star-MC1": //$NON-NLS-1$
        default:
            break;
        }
        return ARMv8; // ARMv8 is default for all other core types
    }

    @Override
    public String toString() {
        switch (this) {
        case ARMv7:
            return CmsisConstants.V7M;
        case ARMv8:
            return CmsisConstants.V8M;
        case ARMv81:
            return CmsisConstants.V81M;
        case MIXED:
            return CmsisConstants.MIXED;
        case UNKNOWN:
        default:
            break;
        }
        return CmsisConstants.UNKNOWN;
    }

    /**
     * Checks if this represents ARMv8.1 architecture
     *
     * @return true if ARMv8.1
     */
    public boolean isARMv8_1() {
        return this == ARMv81;
    }

    /**
     * Checks if this represents ARMv8 architecture
     *
     * @return true if ARMv8
     */
    public boolean isARMv8() {
        return this == ARMv8 || this == ARMv81;
    }

    /**
     * Checks if this represents ARMv7 architecture
     *
     * @return true if ARMv7
     */
    public boolean isARMv7() {
        return this == ARMv7;
    }
}
