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

package com.arm.cmsis.pack.permissions;

/**
 * A simple implementation of IMemoryPermissions interface
 *
 * <p>
 * An access permission specification holds information about permissions
 * granted to access a certain memory area.
 * </p>
 *
 */
public class MemoryPermissions extends MemoryAccess implements IMemoryPermissions {

    /**
     * Creates an access specification with default access permissions : read,
     * write, execute. Security and privilege is not set
     */
    public MemoryPermissions() {
        super();
    }

    /**
     * Creates an access specification from access string.
     *
     * @param access The access string to initialize the spec with.
     */
    public MemoryPermissions(String access) {
        super(access);
    }

    /**
     * Copy constructor
     *
     * @param permissions The permissions to copy
     */
    public MemoryPermissions(IMemoryPermissions permissions) {
        setPermissions(permissions);
    }

    /**
     * Returns the string representation of this access specification.
     *
     * @return Normalized access string representing the permissions.
     */
    @Override
    public String toString() {
        return getAccessString() + ',' + getSecurityString() + ',' + getPrivilegeString();
    }
}
