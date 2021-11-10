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

import com.arm.cmsis.pack.common.CmsisConstants;

/**
 * Interface to a Peripheral tag
 */
public interface ICpPeripheral extends ICpPeripheralItem {

    /**
     * Returns slot name (e.g. "Channels")
     *
     * @return slot name if any or an empty string
     */
    default String getSlotName() {
        return getAttribute(CmsisConstants.SLOT_NAME);
    }

    /**
     * Returns slot type
     *
     * @return slot type if any or an empty string
     */
    default String getSlotType() {
        return getAttribute(CmsisConstants.SLOT_TYPE);
    }

    /**
     * Returns collection of interrupts associated with the peripheral
     *
     * @return Collection<ICpInterrupt>
     */
    default Collection<ICpInterrupt> getInterrupts() {
        return getAllChildrenOfType(null, ICpInterrupt.class);
    }

    /**
     * Returns collection of slots (channels or pins) associated with the peripheral
     *
     * @return Collection<ICpSlot>
     */
    default Collection<ICpSlot> getSlots() {
        return getChildrenOfType(ICpSlot.class);
    }

    /**
     * Returns slot with given name
     *
     * @return ICpSlot if found or null
     */
    default ICpSlot getSlot(String name) {
        Collection<ICpSlot> slots = getSlots();
        for (ICpSlot s : slots) {
            if (s.getName().equals(name))
                return s;
        }
        return null;
    }

    /**
     * Returns collection of modified slots
     *
     * @return Collection<ICpSlot>
     */
    Collection<ICpSlot> getModifiedSlots();

}
