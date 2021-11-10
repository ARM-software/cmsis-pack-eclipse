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

package com.arm.cmsis.pack.permissions;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.generic.IAttributedItem;

/**
 * Interface defining memory access permissions, security and privilege flags
 */
public interface IMemoryAccess extends IAttributedItem {

    // access permissions
    static final char PERIPHERAL_ACCESS = 'p';
    static final char READ_ACCESS = 'r';
    static final char WRITE_ACCESS = 'w';
    static final char EXECUTE_ACCESS = 'x';

    static final String P = "p"; //$NON-NLS-1$
    static final String R = "r"; //$NON-NLS-1$
    static final String W = "w"; //$NON-NLS-1$
    static final String X = "x"; //$NON-NLS-1$

    static final String RX = "rx"; //$NON-NLS-1$
    static final String RWX = "rwx"; //$NON-NLS-1$
    static final String PRW = "prw"; //$NON-NLS-1$
    static final String RW = "rw"; //$NON-NLS-1$

    static final String DEFAULT_ACCESS = RWX;
    static final String DEFAULT_RO_ACCESS = RX;
    static final String DEFAULT_PERIPHERAL_ACCESS = PRW;

    static final String ACCESS_FLAGS = "prwx"; //$NON-NLS-1$

    /**
     * Returns access string
     *
     * @return access permissions as string
     */
    default String getAccessString() {
        return getAttribute(CmsisConstants.ACCESS);
    }

    /**
     * Sets access string
     *
     * @param access access string to set
     */
    default void setAccessString(String access) {
        updateAttribute(CmsisConstants.ACCESS, normalize(access));
    }

    /**
     * Merge the access permissions of this and another specification.
     *
     * @param access Another access specification to be merged.
     * @return The merged access specification, usually this.
     */
    default IMemoryAccess mergeAccess(IMemoryAccess other) {
        if (other == null)
            return this; // other has no access description = > inherit this
        if (isAccessEqual(other))
            return this; // nothing to merge
        StringBuilder builder = new StringBuilder();
        if (isPeripheralAccess() || other.isPeripheralAccess()) {
            builder.append(PERIPHERAL_ACCESS);
        }
        if (isReadAccess() && other.isReadAccess()) {
            builder.append(READ_ACCESS);
        }
        if (isWriteAccess() && other.isWriteAccess()) {
            builder.append(WRITE_ACCESS);
        }
        if (isExecuteAccess() && other.isExecuteAccess()) {
            builder.append(EXECUTE_ACCESS);
        }
        updateAttribute(CmsisConstants.ACCESS, builder.toString());
        return this;
    }

    /**
     * Copies access from the supplied object
     *
     * @param other IMemoryAccess to copy from
     * @return this
     */
    default IMemoryAccess setAccess(IMemoryAccess other) {
        if (other != null) {
            updateAttribute(CmsisConstants.ACCESS, other.getAccessString());
        }
        return this;
    }

    /**
     * Creates a simple memory access object from supplied string.
     *
     * @param access permissions string (contains only "prwx"), co security and
     *               privilege
     * @return IMemoryAccess object
     */
    static IMemoryAccess fromString(String access) {
        return new MemoryAccess(access);
    }

    /**
     * Checks if access permissions equals to this object
     *
     * @param other The access string to compare.
     */
    default boolean isAccessEqual(String other) {
        String normalized = normalize(other);
        return normalized.equals(normalize(getAccessString()));
    }

    /**
     * Checks if access permissions equals to the supplied object
     *
     * @param other an IMemoryAccess object to compare.
     * @return if access all access flags are equal
     */
    default boolean isAccessEqual(IMemoryAccess other) {
        if (other == this)
            return true;
        if (other == null)
            return false;
        return (isPeripheralAccess() == other.isPeripheralAccess() && isReadAccess() == other.isReadAccess()
                && isWriteAccess() == other.isWriteAccess() && isExecuteAccess() == other.isExecuteAccess());

    }

    /**
     * Normalizes the given access string.
     *
     * <p>
     * Normalization rules are
     * <ul>
     * <li>Each access char listed only once.
     * <li>Access chars listed in default ordering.
     * </ul>
     * </p>
     *
     * @param access The access permissions string to be normalized.
     * @return The normalized access string.
     */
    static String normalize(String access) {
        if (access == null || access.isEmpty())
            return CmsisConstants.EMPTY_STRING;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < ACCESS_FLAGS.length(); i++) {
            char ch = ACCESS_FLAGS.charAt(i);
            if (access.indexOf(ch) >= 0) {
                builder.append(ch);
            }
        }
        return builder.toString();
    }

    /**
     * Creates an mask string giving all permissions that are changeable.
     *
     * <p>
     * Peripheral access is never changeable.<br>
     * Read/write/execute permissions can be restricted but not granted.<br>
     * </p>
     *
     * @return String masking changeable access permissions.
     */
    default String getAccessMask() {
        StringBuilder builder = new StringBuilder();

        if (isReadAccess()) {
            builder.append(READ_ACCESS);
        }
        if (isWriteAccess()) {
            builder.append(WRITE_ACCESS);
        }
        if (isExecuteAccess() && !isPeripheralAccess()) {
            builder.append(EXECUTE_ACCESS);
        }

        return builder.toString();
    }

    /**
     * Checks if memory has specified access explicitly set
     *
     * @param access : one of <code>prwx</code> characters
     * @return true if memory provides specified access
     */
    static boolean isAccessSet(char access, String accessString) {
        if (accessString == null)
            return isAccessSet(access, DEFAULT_ACCESS);
        return accessString.indexOf(access) >= 0;
    }

    /**
     * Checks if memory has specified access permission explicitly set
     *
     * @param access : one of <code>prwx</code> characters
     * @return true if memory provides specified access permission
     */
    default boolean isAccessSet(char access) {
        return isAccessSet(access, getAccessString());
    }

    /**
     * Checks if memory has peripheral access
     *
     * @return true if memory has peripheral access
     */
    default boolean isPeripheralAccess() {
        return isAccessSet(PERIPHERAL_ACCESS);
    }

    /**
     * Checks if memory has read access
     *
     * @return true if memory has read access
     */
    default boolean isReadAccess() {
        return isAccessSet(READ_ACCESS);
    }

    /**
     * Checks if memory has write access
     *
     * @return true if memory has write access
     */
    default boolean isWriteAccess() {
        return isAccessSet(WRITE_ACCESS);
    }

    /**
     * Checks if memory has execute access
     *
     * @return true if memory has execute access
     */
    default boolean isExecuteAccess() {
        return isAccessSet(EXECUTE_ACCESS);
    }

}
