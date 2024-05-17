/*******************************************************************************
 * Copyright (c) 2021 ARM Ltd and others.
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

package com.arm.cmsis.pack.utils;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class AlnumComparatorTest {

    private static final ArrayList<String> INPUT_LIST = new ArrayList<>(Arrays.asList("1", "2", "2.1", "2.01", "3",
            "10", "a1", "a2", "a2.1", "a2.01", "a3", "a10", "b1", "b2", "b2.1", "b2.01", "b3", "b10"));

    private int listSize = INPUT_LIST.size();
    private int lastIndex = listSize - 1;

    private ArrayList<String> sortedList = new ArrayList<>(INPUT_LIST);

    private void compareCollections(boolean descending, boolean cs, String failMessage) {
        int nEqual = 0;
        ArrayList<String> input = INPUT_LIST;
        for (int i = 0; i < listSize; i++) {
            int index = descending ? lastIndex - i : i;
            String expected = input.get(index);
            String actual = sortedList.get(i);
            if (!expected.equals(actual))
                break;
            nEqual++;
        }
        assertEquals(failMessage, listSize, nEqual);

    }

    @Test
    public void testAlnumComparatorDescending() {
        Collections.sort(sortedList, new AlnumComparator()); // default is descending order
        compareCollections(true, false, "Sorting in default (descending) order failed");
    }

    @Test
    public void testAlnumComparatorAcending() {
        Collections.sort(sortedList, new AlnumComparator(false)); // ascending
        compareCollections(false, false, "Sorting in ascending order failed");
    }

    @Test
    public void testAlnumComparatorDescendingCaseSeinsitive() {
        Collections.sort(sortedList, new AlnumComparator(true, true)); // descending case sensitive
        compareCollections(true, true, "Sorting in descending (cs) order failed");
    }

    @Test
    public void testAlnumComparatorAscendingCaseSeinsitiveS() {
        Collections.sort(sortedList, new AlnumComparator(false, true)); // ascending case sensitive
        compareCollections(false, true, "Sorting in ascending (cs) order failed");
    }

}
