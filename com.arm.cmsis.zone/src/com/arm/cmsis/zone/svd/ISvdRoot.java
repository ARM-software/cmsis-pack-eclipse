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

import java.util.Map;

/**
 * Base item for SVD file parser
 *
 */
public interface ISvdRoot extends ISvdItem {

    /**
     * Returns peripheral corresponding given name
     *
     * @param name peripheral name
     * @return
     */
    ISvdPeripheral getPeripheral(String name);

    /**
     * Returns full peripherals map
     *
     * @return map of ISvdPeripheral objects
     */
    Map<String, ISvdPeripheral> getPeripheralMap();

}
