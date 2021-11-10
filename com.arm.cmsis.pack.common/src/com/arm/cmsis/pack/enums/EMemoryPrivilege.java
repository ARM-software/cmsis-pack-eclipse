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

package com.arm.cmsis.pack.enums;

import com.arm.cmsis.pack.common.CmsisConstants;

/**
 * Enumeration of memory privilege access : privileged, privileged, and
 * undefined
 */
public enum EMemoryPrivilege {

    NOT_SPECIFIED, PRIVILEGED, UNPRIVILEGED;

    public static final String u = "u"; //$NON-NLS-1$
    public static final String p = "p"; //$NON-NLS-1$

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
    public static String toString(EMemoryPrivilege type) {
        switch (type) {
        case PRIVILEGED:
            return u;
        case UNPRIVILEGED:
            return p;
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
    public static char toChar(EMemoryPrivilege type) {
        switch (type) {
        case PRIVILEGED:
            return 'p';
        case UNPRIVILEGED:
            return 'u';
        case NOT_SPECIFIED:
        default:
            break;
        }
        return 0;
    }

    /**
     * Creates EMemoryPrivilege out of supplied string
     *
     * @param str privilege String
     * @return EMemoryPrivilege
     */
    public static EMemoryPrivilege fromString(final String str) {
        if (str == null || str.isEmpty())
            return NOT_SPECIFIED;
        char ch = str.charAt(0);
        switch (ch) {
        case 'p':
            return PRIVILEGED;
        case 'u':
            return UNPRIVILEGED;
        default:
            break;
        }
        return NOT_SPECIFIED;
    }

    /**
     * Returns mask : a string listing allowed privilege values
     *
     * @return mask String
     */
    public String getMask() {
        switch (this) {
        case PRIVILEGED:
            return "pu"; // only switch between privileged and unprivileged is allowed //$NON-NLS-1$
        case UNPRIVILEGED:
            return CmsisConstants.EMPTY_STRING; // no change is allowed
        case NOT_SPECIFIED:
        default:
            break;
        }
        return null; // all available
    }

    /**
     * Adjusts this value to the parent's one
     *
     * @param parent EMemoryPrivilege to adjust to
     * @return adjusted EMemoryPrivilege
     */
    public EMemoryPrivilege adjust(EMemoryPrivilege parent) {
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
    public boolean matches(EMemoryPrivilege other) {
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

    public boolean isPrivileged() {
        return this == PRIVILEGED;
    }
}
