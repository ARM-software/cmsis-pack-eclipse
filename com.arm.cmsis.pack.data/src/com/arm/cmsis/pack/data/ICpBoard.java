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

import java.util.Collection;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.generic.IAttributes;

/**
 * Interface describing board
 */
public interface ICpBoard extends ICpItem {

    /**
     * Checks if this board contains mounted device matching suppled one device
     * attributes
     *
     * @param deviceAttributes attributes of device to match
     * @return true if board contains matching mounted device
     */
    boolean hasMountedDevice(IAttributes deviceAttributes);

    /**
     * Checks if this board contains mounted or compatible device matching suppled
     * one device attributes
     *
     * @param deviceAttributes attributes of device to match
     * @return true if board contains matching device
     */
    boolean hasCompatibleDevice(IAttributes deviceAttributes);

    /**
     * Returns list of mounted devices on this board
     *
     * @return collection of ICpItem objects
     */
    Collection<ICpItem> getMountedDevices();

    /**
     * Returns compatible devices on this board
     *
     * @return collection of ICpItem objects
     */
    Collection<ICpItem> getCompatibleDevices();

    /**
     * Helper method to construct board ID
     *
     * @param item ICpItem representing ICpBoard or ICpBoardInfo
     * @return constructed board ID
     */
    static String constructBoardId(ICpItem item) {
        String id = CmsisConstants.EMPTY_STRING;
        if (item != null) {
            id = item.getVendor() + CmsisConstants.DOUBLE_COLON + constructBoardDisplayName(item);
        }
        return id;
    }

    /**
     * Helper method to construct board ID
     *
     * @param item ICpItem representing ICpBoard or ICpBoardInfo
     * @return constructed board display name
     */
    static String constructBoardDisplayName(ICpItem item) {
        String name = CmsisConstants.EMPTY_STRING;
        if (item != null) {
            name = item.getName();
            String rev = item.getRevision();
            if (!rev.isEmpty()) {
                name += " (" + rev + ")";
            }
        }
        return name;
    }

}
