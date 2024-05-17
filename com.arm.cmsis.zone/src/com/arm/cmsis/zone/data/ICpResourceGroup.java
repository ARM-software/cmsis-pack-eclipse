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

import java.util.Map;

import com.arm.cmsis.pack.common.CmsisConstants;

/**
 * Resource group contains other resources (memory or peripherals)
 */
public interface ICpResourceGroup extends ICpResourceItem {

    /**
     * Returns peripheral group for the given name
     *
     * @param name group name
     * @return ICpPeripheralGroup if found
     */
    ICpPeripheralGroup getPeripheralGroup(String name);

    /**
     * Returns group for the given tag
     *
     * @param name group name for named groups, tag otherwise
     * @return ICpResourceGroup if found
     */
    ICpResourceGroup getResourceGroup(String name);

    /**
     * Returns group for the given tag, creates group if does not exists
     *
     * @param tag group tag
     * @return ICpResourceGroup
     */
    ICpResourceGroup ensureResourceGroup(String tag);

    /**
     * Returns peripheral group for the given name, creates group if does not exists
     *
     * @param name group name
     * @return ICpPeripheral
     */
    ICpPeripheralGroup ensurePeripheralGroup(String name);

    /**
     * Returns memory group
     *
     * @return ICpResourceGroup
     */
    default ICpResourceGroup getMemoryGroup() {
        return getResourceGroup(CmsisConstants.MEMORIES);
    }

    /**
     * Returns memory group, creates one if not available
     *
     * @return ICpResourceGroup
     */
    default ICpResourceGroup ensureMemoryGroup() {
        return ensureResourceGroup(CmsisConstants.MEMORIES);
    }

    /**
     * Returns peripherals group
     *
     * @return ICpResourceGroup
     */
    default ICpResourceGroup getPeripheralsGroup() {
        return getResourceGroup(CmsisConstants.PERIPHERALS);
    }

    /**
     * Returns peripherals group, creates one if not available
     *
     * @return ICpResourceGroup
     */
    default ICpResourceGroup ensurePeripheralsGroup() {
        return ensureResourceGroup(CmsisConstants.PERIPHERALS);
    }

    /**
     * Returns all blocks in the container including regions and peripherals
     *
     * @return map of memory blocks
     */
    Map<String, ICpMemoryBlock> getMemoryBlocksAsMap();

    /**
     * Return all end-leaf blocks including peripherals as array
     *
     * @return array of ICpMemoryBlock
     */
    ICpMemoryBlock[] getMemoryBlocksAsArray();

    /**
     * Returns first memory block name matching given id
     *
     * @param name memory block id
     * @return first block that matches given name or null if not found
     */
    ICpMemoryBlock getMemoryBlock(String id);

    /**
     * Initialize group
     */
    default void init() {
        /* default does nothing */};
}
