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

package com.arm.cmsis.pack.rte.boards;

import com.arm.cmsis.pack.data.IAllDeviceNames;
import com.arm.cmsis.pack.data.ICpBoard;
import com.arm.cmsis.pack.item.ICmsisMapItem;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;

/**
 * Interface base element for boards's mounted and compatibles devices. Refers
 * to IRteDeviceItem.
 */
/**
 * @author edriouk
 *
 */
public interface IRteBoardDeviceItem extends ICmsisMapItem<IRteBoardDeviceItem>, IAllDeviceNames {

    /**
     * Get the board associated with the item or its parent
     *
     * @return ICpBoard or null
     */
    ICpBoard getBoard();

    /**
     * Returns parent board
     *
     * @return IRteBoardItem
     */
    IRteBoardItem getRteBoard();

    /**
     * Returns underlying device item
     *
     * @return IRteDeviceItem
     */
    IRteDeviceItem getRteDeviceItem();

    /**
     * Returns device leaf
     *
     * @return IRteDeviceItem
     */
    IRteDeviceItem getRteDeviceLeaf();

    /**
     * Add child elements corresponding device children
     *
     * @param deviceItem IRteDeviceItem whose children to add
     */
    void addDeviceChildren(IRteDeviceItem deviceItem);

    /**
     * Return true if this node is the root
     *
     * @return true if this node is the root
     */
    default boolean isRoot() {
        return false;
    }

    /**
     * Checks if this item is a board
     *
     * @return true if is a board
     */
    default boolean isBoard() {
        return false;
    }
}
