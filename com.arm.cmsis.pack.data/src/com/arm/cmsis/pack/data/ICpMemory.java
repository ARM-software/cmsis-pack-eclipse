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
import com.arm.cmsis.pack.enums.EMemoryOverlap;
import com.arm.cmsis.pack.enums.EMemoryType;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.permissions.IMemoryPermissions;

/**
 * Interface representing memory element in pdsc
 */
public interface ICpMemory extends IMemoryPermissions, ICpDeviceProperty {

    /**
     * Returns access string corresponding following regular expression pattern:
     * "[rwxpsnc]+"
     * 
     * @return "access" attribute value if present or default derived from ID for
     *         deprecated elements
     */
    @Override
    default String getAccessString() {
        return getEffectiveAttribute(CmsisConstants.ACCESS);
    }

    @Override
    default String getSecurityString() {
        return getEffectiveAttribute(CmsisConstants.SECURITY);
    }

    @Override
    default String getPrivilegeString() {
        return getEffectiveAttribute(CmsisConstants.PRIVILEGE);
    }

    /**
     * Checks if the memory region represents RAM
     * 
     * @return true if RAM
     */
    default boolean isRAM() {
        return getMemoryType() == EMemoryType.RAM;
    }

    /**
     * Checks if the memory region represents ROM
     * 
     * @return true if ROM
     */
    default boolean isROM() {
        return getMemoryType() == EMemoryType.ROM;
    }

    /**
     * Checks if the memory region represents Peripheral
     * 
     * @return true if Peripheral
     */
    default boolean isPeripheral() {
        return getMemoryType() == EMemoryType.PERIPHERAL;
    }

    /**
     * Return memory type : RAM, ROM or Peripheral
     * 
     * @return EMemoryType
     */
    default EMemoryType getMemoryType() {
        if (isPeripheralAccess())
            return EMemoryType.PERIPHERAL;

        EMemoryType type = EMemoryType.fromString(getAttribute(CmsisConstants.TYPE));
        if (type != EMemoryType.UNKNOWN)
            return type;

        type = EMemoryType.fromString(getName());
        if (type != EMemoryType.UNKNOWN)
            return type;

        if (isReadAccess()) {
            if (isWriteAccess())
                return EMemoryType.RAM;
            return EMemoryType.ROM;
        }
        return type;
    }

    /**
     * Returns memory type as string that can be used as an XML tag: "RAM", "ROM" or
     * "peripheral"
     * 
     * @return memory type as string or this item's tag ("memory") if unknown
     */
    default String getMemoryTypeAsString() {
        return getMemoryType().toString();
    }

    /**
     * Returns parent ICpMemory (if parent is ICpMemory)
     * 
     * @return parent item as ICpMemory
     */
    default ICpMemory getParentMemory() {
        return getParentOfType(ICpMemory.class);
    }

    /**
     * Returns alias name
     * 
     * @return alias name is specified or empty string
     */
    default String getAlias() {
        return getAttribute(CmsisConstants.ALIAS);
    }

    /**
     * Checks if the memory (RAM) should not be zero-initialized
     * 
     * @return true if not initialized
     */
    default boolean isNoInit() {
        if (hasAttribute(CmsisConstants.UNINIT) || !hasAttribute(CmsisConstants.INIT)) {
            return getAttributeAsBoolean(CmsisConstants.UNINIT, false);
        }
        // backward compatibility : check deprecated "init" attribute
        return getAttributeAsBoolean(CmsisConstants.INIT, false);
    }

    /**
     * Checks if the memory shall be used for the startup by linker
     * 
     * @return true if startup memory
     */
    default boolean isStartup() {
        if (!isExecuteAccess() || isPeripheralAccess())
            return false;
        return attributes().getAttributeAsBoolean(CmsisConstants.STARTUP, false);
    }

    /**
     * Checks if the memory is external to the device
     * 
     * @return true if external
     */
    default boolean isExternal() {
        return attributes().getAttributeAsBoolean(CmsisConstants.EXTERNAL, false);
    }

    /**
     * Checks if the memory is shared across applications and threads
     * 
     * @return true if shared
     */
    default boolean isShared() {
        return attributes().getAttributeAsBoolean(CmsisConstants.SHARED, false);
    }

    /**
     * Checks if the memory is accesses by DMA (multiple bus masters)
     * 
     * @return true if accessed by DMA
     */
    default boolean isDma() {
        return attributes().getAttributeAsBoolean(CmsisConstants.DMA, false);
    }

    /**
     * Returns true if memory has explicitly defined "start" attribute
     * 
     * @return true if explicit
     */
    default boolean hasExplicitStart() {
        return hasAttribute(CmsisConstants.START);
    }

    /**
     * Returns stop address calculated from start and size
     * 
     * @return stop address as long
     */
    default long getStop() {
        long start = getStart();
        if (start < 0)
            return -1L;

        long size = getSize();
        if (size < 0)
            size = 0;
        else if (size > 0)
            size--;
        return start + size;
    }

    /**
     * Returns end address calculated from address and size
     * 
     * @return end address as long
     */
    default long getEndAddress() {
        long start = getAddress();
        if (start < 0)
            return -1L;

        long size = getSize();
        if (size < 0)
            size = 0;
        else if (size > 0)
            size--;
        return start + size;
    }

    /**
     * Returns stop address calculated from start and stop
     * 
     * @return stop address as String
     */
    default String getStopString() {
        return IAttributes.longToHexString8(getStop());
    }

    /**
     * Returns start-stop string
     * 
     * @return start-stop as String
     */
    default String getStartStopString() {
        return getStartString() + '-' + getStopString();
    }

    /**
     * Checks if this memory overlaps another one
     * 
     * @return EMemoryOverlap
     */
    default EMemoryOverlap checkOverlap(ICpMemory other) {
        if (other == null || other == this) {
            return EMemoryOverlap.NO_OVERLAP;
        }

        long start = getAddress();
        long end = getEndAddress();

        long thatStart = other.getAddress();
        long thatEnd = other.getEndAddress();

        if (start == thatStart) {
            if (end == thatEnd) {
                return EMemoryOverlap.FULL;
            }
            if (end < thatEnd) {
                return EMemoryOverlap.INSIDE;
            }
            return EMemoryOverlap.OUTSIDE;
        }

        if (end == thatEnd) {
            if (start > thatStart) {
                return EMemoryOverlap.INSIDE;
            }
            return EMemoryOverlap.OUTSIDE;
        }

        if (start > thatStart && start < thatEnd)
            return EMemoryOverlap.INTERSECT;

        if (thatStart > start && thatStart < end)
            return EMemoryOverlap.INTERSECT;
        return EMemoryOverlap.NO_OVERLAP;
    }

    /**
     * Returns value aligned to 2^n
     * 
     * @param value size, offset or address to align
     * @return aligned value
     */
    static long alignTo2n(long value) {
        if (value == 0L)
            return value;
        long alignedValue = Long.highestOneBit(value);
        if (alignedValue < value)
            alignedValue <<= 1;
        return alignedValue;
    }

    /**
     * Returns aligned value
     * 
     * @param value     size, address of offset to align
     * @param alignment alignment granularity (must be itself 32 bite aligned!)
     * @return aligned value
     */
    static long alignTo(long value, long alignment) {
        if (value == 0L || alignment == 0L)
            return value;
        if (value < alignment)
            return alignment;

        return (value + alignment - 1) / alignment * alignment;
    }

    /**
     * Returns "linker_control" attribute value if any
     * 
     * @return value of "linker_control" attribute or empty string
     */
    default String getLinkerControl() {
        return getAttribute(CmsisConstants.LINKER_CONTROL);
    }
}
