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
package com.arm.cmsis.zone.svd;

import java.util.Collection;

import com.arm.cmsis.pack.data.ICpMemory;

/**
 * Peripheral description in SVD file
 */
public interface ISvdPeripheral extends ISvdItem, ICpMemory {

    /**
     * Returns peripheral's group name
     *
     * @return peripheral's group name
     */
    String getGroupName();

    /**
     * Returns sorted collection of peripheral's interrupts
     *
     * @return collection if ISvdInterrupt items
     */
    Collection<ISvdInterrupt> getInterrups();
}
