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
 * Interface for a zone creator element
 */
public interface ICpZoneCreator extends ICpZoneItem, IMemorySecurity, IMemoryPriviledge {

    /**
     * Retrieves name of a tool created the zone
     *
     * @return String
     */
    String getTool();

    /**
     * Sets tool name
     *
     * @param tool tool name and version
     */
    void setTool(String tool);

}
