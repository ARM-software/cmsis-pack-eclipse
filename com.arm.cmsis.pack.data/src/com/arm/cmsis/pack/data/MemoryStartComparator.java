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

/**
 * Compares ICpMemory object by start (or address), size and name
 */
public class MemoryStartComparator extends MemorySizeComparator {

    private boolean fbPhysical;

    /**
     * Default constructor, compares logical addresses
     */
    public MemoryStartComparator() {
        this(false);
    }

    /**
     * Constructor to select comparison mode
     *
     * @param bPhysical flag if to compare physical (true) or logical (false)
     *                  addresses
     */
    public MemoryStartComparator(boolean bPhysical) {
        fbPhysical = bPhysical;
    }

    public boolean isPhysical() {
        return fbPhysical;
    }

    @Override
    public int compare(ICpMemory m1, ICpMemory m2) {
        if (m1 == m2)
            return 0;
        int result = isPhysical() ? compareByAddress(m1, m2) : compareByStart(m1, m2);
        if (result != 0)
            return result;
        return super.compare(m1, m2);
    }

    /**
     * Compares two ICpMemory objects by their start address
     *
     * @param m1 first ICpMemory
     * @param m2 second ICpMemory
     * @return negative value if first memory has lower address, positive if higher
     *         and 0 if equal
     */
    public static int compareByStart(ICpMemory m1, ICpMemory m2) {
        if (m1 == null && m2 == null)
            return 0;
        if (m1 == null)
            return -1;
        if (m2 == null)
            return 1;

        return Long.compareUnsigned(m1.getStart(), m2.getStart());
    }

    /**
     * Compares two ICpMemory objects by their physical addresses
     *
     * @param m1 first ICpMemory
     * @param m2 second ICpMemory
     * @return negative value if first memory has lower address, positive if higher
     *         and 0 if equal
     */
    public static int compareByAddress(ICpMemory m1, ICpMemory m2) {
        if (m1 == null && m2 == null)
            return 0;
        if (m1 == null)
            return -1;
        if (m2 == null)
            return 1;

        return Long.compareUnsigned(m1.getAddress(), m2.getAddress());
    }
}
