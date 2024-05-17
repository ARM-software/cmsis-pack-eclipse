/*******************************************************************************
* Copyright (c) 2021-2024 ARM Ltd. and others
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

package com.arm.cmsis.pack.generic;

/**
 * This interface defines an editable object where changes should be committed
 * before save
 */
public interface ICommitable {

    /**
     * Commits object changes
     */
    void commit();

    /**
     * Check is the object has been modified since last commit
     *
     * @return true if modified
     */
    boolean isModified();

    /**
     * Check is the object is read-only
     *
     * @return true if read-only
     */
    default boolean isReadOnly() {
        return false;
    }

    /**
     * Sets the read-only flag
     *
     * @param bReadOnly read-only flag
     */
    default void setReadOnly(boolean bReadOnly) {
    }
}
