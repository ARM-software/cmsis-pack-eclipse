/*******************************************************************************
 * Copyright (c) 2022 ARM Ltd and others.
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WildCardsTest {

    private String[] baseStrings = new String[] { "STM32F10[123]?[CDE]", "*", "?*", "*?*", "**?" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    private String[] matchedStrings = new String[] { "STM32F103ZE", "*?", "?*?", "**", "**" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

    @Test
    public void testMatch() {
        for (int i = 0; i < baseStrings.length; i++) {
            assertTrue(WildCards.match(baseStrings[i], matchedStrings[i]));
        }
    }

    @Test
    public void testMatchStrings() {
        assertTrue(WildCards.match("a", "a")); //$NON-NLS-1$ //$NON-NLS-2$
        assertFalse(WildCards.match("a", "")); //$NON-NLS-1$ //$NON-NLS-2$
        assertFalse(WildCards.match("", "d")); //$NON-NLS-1$ //$NON-NLS-2$
        assertFalse(WildCards.match("", "*")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("a*", "a*d")); //$NON-NLS-1$ //$NON-NLS-2$
        assertFalse(WildCards.match("a*", "*d")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("a*", "abcd")); //$NON-NLS-1$ //$NON-NLS-2$
        assertFalse(WildCards.match("a*", "xycd")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("a*d", "*d")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("a*d", "abcd")); //$NON-NLS-1$ //$NON-NLS-2$
        assertFalse(WildCards.match("a*d", "abxx")); //$NON-NLS-1$ //$NON-NLS-2$
        assertFalse(WildCards.match("a*d", "abxyz")); //$NON-NLS-1$ //$NON-NLS-2$
        assertFalse(WildCards.match("a*d", "xycd")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("*d", "abcd")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("*d", "d")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("*c*", "abcd")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("abcd", "a**d")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("abcd", "a??d")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("abcd", "?bc?")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("abc?", "abc*")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("ab?d", "ab??")); //$NON-NLS-1$ //$NON-NLS-2$
        assertFalse(WildCards.match("ab?d", "abc??")); //$NON-NLS-1$ //$NON-NLS-2$
        assertFalse(WildCards.match("?bcd", "abc??")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("?bcd", "*bcd")); //$NON-NLS-1$ //$NON-NLS-2$
        assertFalse(WildCards.match("?bcd", "abc???")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("abc?", "ab??")); //$NON-NLS-1$ //$NON-NLS-2$
        assertFalse(WildCards.match("abc?", "abc??")); //$NON-NLS-1$ //$NON-NLS-2$
        assertFalse(WildCards.match("ab??", "abc??")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("abc*", "ab*?")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("abc*", "abc?*")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("ab*?", "abc?*")); //$NON-NLS-1$ //$NON-NLS-2$
        assertFalse(WildCards.match("abcX-1", "abcX-2")); //$NON-NLS-1$ //$NON-NLS-2$
        assertFalse(WildCards.match("abcX-1", "abcX-3")); //$NON-NLS-1$ //$NON-NLS-2$
        assertFalse(WildCards.match("abcX-1", "abcY-1")); //$NON-NLS-1$ //$NON-NLS-2$
        assertFalse(WildCards.match("abcX-1", "abcY-2")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("{*}", "{somestring}")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("$*", "$somestring")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("abcX-1", "abc[XY]-[12]")); //$NON-NLS-1$ //$NON-NLS-2$
        assertFalse(WildCards.match("abcZ-1", "abc[XY]-[12]")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("abcY-2", "abc[XY]-[12]")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("Prefix_*_Suffix", "Prefix_Mid_Suffix")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("Prefix_*_Suffix", "Prefix_Mid_V_Suffix")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("Prefix_*_Suffix", "Prefix_Mid_Suffix_Suffix")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("Prefix*_Suffix", "Prefix_Mid_Suffix")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("Prefix*Suffix", "Prefix_Mid_Suffix")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("Prefix*Suffix", "Prefix_Mid_Suffix_Suffix")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("Prefix_*Suffix", "Prefix_Mid_Suffix")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("Prefix.*.Suffix", "Prefix.Mid.Suffix")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testMatchStringsNoCase() {
        assertTrue(WildCards.match("a", "A", true)); //$NON-NLS-1$ //$NON-NLS-2$
        assertFalse(WildCards.match("a", "A", false)); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("a*", "A*?", true)); //$NON-NLS-1$ //$NON-NLS-2$
        assertFalse(WildCards.match("a*", "A*?", false)); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("?*a", "?*?A", true)); //$NON-NLS-1$ //$NON-NLS-2$
        assertFalse(WildCards.match("?*a", "?*?A", false)); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("*?*a", "**A", true)); //$NON-NLS-1$ //$NON-NLS-2$
        assertFalse(WildCards.match("*?*a", "**A", false)); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("**?a", "**A", true)); //$NON-NLS-1$ //$NON-NLS-2$
        assertFalse(WildCards.match("**?a", "**A", false)); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("Prefix_*_Suffix", "PREFIX_Mid_SUFFIX", true)); //$NON-NLS-1$ //$NON-NLS-2$
        assertFalse(WildCards.match("Prefix_*_Suffix", "PREFIX_MID_SUFFIX", false)); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("Prefix_*_Suffix", "PREFIX_Mid_SUFFIX", true)); //$NON-NLS-1$ //$NON-NLS-2$
        assertFalse(WildCards.match("Prefix_*_Suffix", "PREFIX_MID_SUFFIX", false)); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("Prefix_*_Suffix", "PREFIX_MID_V_SUFFIX", true)); //$NON-NLS-1$ //$NON-NLS-2$
        assertFalse(WildCards.match("Prefix_*_Suffix", "PREFIX_MID_V_SUFFIX", false)); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("Prefix_*_Suffix", "PREFIX_MID_SUFFIX_SUFFIX", true)); //$NON-NLS-1$ //$NON-NLS-2$
        assertFalse(WildCards.match("Prefix_*_Suffix", "PREFIX_MID_SUFFIX_SUFFIX", false)); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("Prefix*_Suffix", "PREFIX_MID_SUFFIX", true)); //$NON-NLS-1$ //$NON-NLS-2$
        assertFalse(WildCards.match("Prefix*_Suffix", "PREFIX_MID_SUFFIX", false)); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("Prefix*Suffix", "PREFIX_MID_SUFFIX_SUFFIX", true)); //$NON-NLS-1$ //$NON-NLS-2$
        assertFalse(WildCards.match("Prefix*Suffix", "PREFIX_MID_SUFFIX_SUFFIX", false)); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("Prefix_*Suffix", "PREFIX_MID_SUFFIX", true)); //$NON-NLS-1$ //$NON-NLS-2$
        assertFalse(WildCards.match("Prefix_*Suffix", "PREFIX_MID_SUFFIX", false)); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(WildCards.match("Prefix.*.Suffix", "PREFIX.MID.SUFFIX", true)); //$NON-NLS-1$ //$NON-NLS-2$
        assertFalse(WildCards.match("Prefix.*.Suffix", "PREFIX.MID.SUFFIX", false)); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
