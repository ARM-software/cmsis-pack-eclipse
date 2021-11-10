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

package com.arm.cmsis.zone.data;

import java.util.Collection;

/**
 * Interface to a Peripheral group tag
 */
public interface ICpPeripheralGroup extends ICpPeripheralItem {

    /**
     * Returns group peripherals
     *
     * @return collection of peripherals
     */
    default Collection<ICpPeripheral> getPeripherals() {
        return getChildrenOfType(ICpPeripheral.class);
    }
}
