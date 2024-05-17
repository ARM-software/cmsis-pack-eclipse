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

import com.arm.cmsis.pack.data.ICpMemory;

/**
 * Interrupt description in SVD file
 */
public interface ISvdInterrupt extends ISvdItem, ICpMemory {

    /**
     * Returns interrupt value as string
     *
     * @return value as string
     */
    String getValueString();
}
