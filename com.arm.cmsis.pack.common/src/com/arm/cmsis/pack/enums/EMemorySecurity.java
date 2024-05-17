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
import com.arm.cmsis.pack.permissions.IMemoryAccess;

/**
 * Enumeration of memory security : secure, non-secure, non-secure callable, and
 * undefined
 */
public enum EMemorySecurity {

    NOT_SPECIFIED, NON_SECURE, CALLABLE, SECURE, COMBINED;

    public static final String s = "s"; //$NON-NLS-1$
    public static final String c = "c"; //$NON-NLS-1$
    public static final String n = "n"; //$NON-NLS-1$
    public static final String sn = "sn"; //$NON-NLS-1$

    @Override
    public String toString() {
        return toString(this);
    }

    /**
     * Converts enum value to string
     *
     * @param value enum value to convert
     * @return string representation of the enum value
     */
    public static String toString(EMemorySecurity type) {
        switch (type) {
        case CALLABLE:
            return c;
        case NON_SECURE:
            return n;
        case SECURE:
            return s;
        case COMBINED:
            return sn;
        case NOT_SPECIFIED:
        default:
            break;
        }
        return null;
    }

    /**
     * Converts enum value to char
     *
     * @return char representation of the enum value
     */
    public char toChar() {
        return toChar(this);
    }

    /**
     * Converts enum value to char
     *
     * @param value enum value to convert
     * @return char representation of the enum value
     */
    public static char toChar(EMemorySecurity type) {
        switch (type) {
        case CALLABLE:
            return 'c';
        case NON_SECURE:
            return 'n';
        case SECURE:
            return 's';
        case COMBINED:
        case NOT_SPECIFIED:
        default:
            break;
        }
        return 0;
    }

    /**
     * Constructs the enum from supplied string
     *
     * @param str security string
     * @return EMemorySecurity
     */
    public static EMemorySecurity fromString(final String str) {
        if (str == null || str.isEmpty())
            return NOT_SPECIFIED;
        switch (str) {
        case CmsisConstants.S:
            return SECURE;
        case CmsisConstants.N:
            return NON_SECURE;
        case CmsisConstants.C:
        case CmsisConstants.NSC:
            return CALLABLE;
        case "sn": //$NON-NLS-1$
        case "ns": //$NON-NLS-1$
            return COMBINED;
        default:
            break;
        }
        return NOT_SPECIFIED;
    }

    /**
     * Returns mask : a string restricting allowed security values
     *
     * @return mask String
     */
    public String getMask(IMemoryAccess access) {
        boolean bAllowCallable = access == null || (!access.isPeripheralAccess() && access.isExecuteAccess());
        switch (this) {
        case NON_SECURE:
            if (bAllowCallable)
                return "ncs"; // can use all but undefined //$NON-NLS-1$
            //$FALL-THROUGH$ else fall through
        case COMBINED:
            return "ns"; // can use either secure or non-secure //$NON-NLS-1$
        case CALLABLE:
            return "cs"; // can upgrade to secure only //$NON-NLS-1$
        case SECURE:
            return CmsisConstants.EMPTY_STRING; // cannot change
        case NOT_SPECIFIED:
        default:
            break;
        }
        if (!bAllowCallable)
            return " ns"; // all but callable //$NON-NLS-1$
        return null; // all available
    }

    /**
     * Adjusts this value to the parent's one
     *
     * @param parent EMemorySecurity to adjust to
     * @return adjusted EMemorySecurity
     */
    public EMemorySecurity adjust(EMemorySecurity parent) {
        if (parent != null && parent.ordinal() > this.ordinal()) {
            return parent;
        }
        return this;
    }

    /**
     * Checks if this enum equals to another one or one of them has NOT_SPECIFIED
     * value
     *
     * @param other EMemorySecurity to compare to this
     * @return true if match
     */
    public boolean matches(EMemorySecurity other) {
        if (other == null)
            return false;
        if (this == NOT_SPECIFIED || other == NOT_SPECIFIED) {
            return true;
        }
        return this == other;
    }

    public boolean isSpecified() {
        return this != NOT_SPECIFIED;
    }

    /**
     * Checks if security is non-secure or undefined
     *
     * @return true if non-secure
     */
    public boolean isNonSecure() {
        switch (this) {
        case NOT_SPECIFIED:
        case NON_SECURE:
        case COMBINED:
            return true;
        case CALLABLE:
        case SECURE:
            break;
        default:
            break;
        }
        return false;
    }

    /**
     * Checks if this enum represents secure value
     *
     * @return true if secure
     */
    public boolean isSecure() {
        switch (this) {
        case CALLABLE:
        case SECURE:
        case COMBINED:
            return true;
        case NON_SECURE:
        case NOT_SPECIFIED:
        default:
            break;
        }
        return false;
    }

    /**
     * Checks if memory is relevant for SAU setup
     *
     * @return true if relevant for SAU
     */
    public boolean isSauRelevant() {
        switch (this) {
        case CALLABLE:
        case NON_SECURE:
        case NOT_SPECIFIED:
        case COMBINED:
            return true;
        case SECURE:
        default:
            break;
        }
        return false;
    }
}
