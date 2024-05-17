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
package com.arm.cmsis.zone.data;

import com.arm.cmsis.pack.permissions.IMemoryPriviledge;
import com.arm.cmsis.pack.permissions.IMemorySecurity;

/**
 * Interface for an interrupt tag.
 */
public interface ICpPeripheralSetup extends ICpResourceItem, IMemoryPriviledge, IMemorySecurity {

    /**
     * Get the setup register index as string
     *
     * @return register index string
     */
    String getIndexString();

    /**
     * Get register index decoded as long
     *
     * @return register index
     */
    Long getIndex();

    /**
     * Checks if this setup element matches used permissions
     *
     * @return
     */
    boolean matchesPermissions();
}