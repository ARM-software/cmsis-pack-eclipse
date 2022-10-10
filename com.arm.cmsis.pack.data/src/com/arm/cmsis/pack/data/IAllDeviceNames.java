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

package com.arm.cmsis.pack.data;

import java.util.Set;

/**
 * Helper interface to get all device names fount in an item (pack, board,
 * device)
 */
public interface IAllDeviceNames {

    /**
     * Get names of all devices declared in the item
     *
     * @return a set of device names, includes family, sub-family, device and
     *         variant levels.
     */
    Set<String> getAllDeviceNames();

}
