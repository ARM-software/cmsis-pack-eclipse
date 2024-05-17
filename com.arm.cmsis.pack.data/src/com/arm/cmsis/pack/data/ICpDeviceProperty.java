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

package com.arm.cmsis.pack.data;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.generic.IAttributes;

/**
 * Interface represent device property: processor, book, feature, debug, memory,
 * etc.
 *
 */
public interface ICpDeviceProperty extends ICpItem {

    /**
     * Returns item-depended address (default is 0)
     *
     * @return address as long value
     */
    default long getAddress() {
        return IAttributes.stringToLong(getAddressString(), 0);
    }

    /**
     * Returns item-depended start address (default is 0)
     *
     * @return start address as long value
     */
    default long getStart() {
        return IAttributes.stringToLong(getStartString(), 0);
    }

    /**
     * Returns item-depended start address (default is 0)
     *
     * @return start address as long value
     */
    default long getSize() {
        return IAttributes.stringToLong(getSizeString(), 0);
    }

    /**
     * Returns item-depended address offset (default is 0)
     *
     * @return address offset as long value
     */
    default long getOffset() {
        return IAttributes.stringToLong(getOffsetString(), 0);
    }

    /**
     * Returns __dp (Debug Port) index
     *
     * @return __dp index
     */
    default long getDP() {
        return attributes().getAttributeAsLong(CmsisConstants.__DP, 0);
    }

    /**
     * Returns __ap (Access Port) index
     *
     * @return __ap index
     */
    default long getAP() {
        return attributes().getAttributeAsLong(CmsisConstants.__AP, 0);
    }

    /**
     * Returns item-depended address
     *
     * @return address as string value
     */
    default String getAddressString() {
        return getAttribute(CmsisConstants.ADDRESS);
    }

    /**
     * Returns item-depended start address
     *
     * @return start address as string value
     */
    default String getStartString() {
        return getAttribute(CmsisConstants.START);
    }

    /**
     * Returns item-depended start address
     *
     * @return start address as string value
     */
    default String getSizeString() {
        return getAttribute(CmsisConstants.SIZE);
    }

    /**
     * Returns item-depended address offset
     *
     * @return address offset as string value
     */
    default String getOffsetString() {
        return getAttribute(CmsisConstants.OFFSET);
    }

    /**
     * Checks if item is default one (memory, algorithm )
     *
     * @return true if item is default one
     */
    default boolean isDefault() {
        return attributes().getAttributeAsBoolean(CmsisConstants.DEFAULT, false);
    }

    /**
     * Checks if sequence block is atomic
     *
     * @return true if atomic
     */
    default boolean isAtomic() {
        return attributes().getAttributeAsBoolean(CmsisConstants.ATOMIC, false);
    }

}
