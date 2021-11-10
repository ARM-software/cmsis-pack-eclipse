/*******************************************************************************
 * Copyright (c) 2021 ARM Ltd and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

public class VersionComparatorTest {

    private static final ArrayList<String> INPUT_LIST = new ArrayList<>(
            Arrays.asList("1.0.0-alpha", "1.0.0-alpha.1", "1.0.0-alpha.beta", "1.0.0-beta", "1.0.0-beta.2",
                    "1.0.0-beta.11", "1.0.0-rc.1", "1.0.0", "1.9.0", "1.10.0", "1.11.0"));

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
    public void testVersionComparatorDescending() {
        Collections.sort(sortedList, new VersionComparator());
        compareCollections(true, false, "Sorting in default (descending) order failed"); // default is descending order
    }

    @Test
    public void testVersionComparatorAcending() {
        Collections.sort(sortedList, new VersionComparator(false)); // ascending
        compareCollections(false, false, "Sorting in ascending order failed");
    }

    @Test
    public void testVersionComparatorDescendingCaseSeinsitive() {
        Collections.sort(sortedList, new VersionComparator(true, true)); // descending case sensitive
        compareCollections(true, true, "Sorting in descending (cs) order failed");
    }

    @Test
    public void testVersionComparatorAscendingCaseSeinsitiveS() {
        Collections.sort(sortedList, new VersionComparator(false, true)); // ascending case sensitive
        compareCollections(false, true, "Sorting in ascending (cs) order failed");
    }

}
