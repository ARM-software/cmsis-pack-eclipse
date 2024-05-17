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

import com.arm.cmsis.pack.data.ICpMemory;

/**
 * Interface for SAU region
 *
 */
public interface ICpSauRegion extends ICpZoneItem, ICpMemory {

    /**
     * Tries to appends another SAU region if it has the same security and follows
     * this without gap
     *
     * @param memory ICpSauRegion to append
     * @return true if appended
     */
    boolean appendSauRegion(ICpSauRegion region);
}
