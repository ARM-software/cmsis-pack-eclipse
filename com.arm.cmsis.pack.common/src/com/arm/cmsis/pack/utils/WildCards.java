/*******************************************************************************
* Copyright (c) 2022 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.utils;

import java.util.regex.PatternSyntaxException;

/**
 * Utility class provides method to compare two string that can contain wild
 * cards
 *
 * Supported wild cards expressions:
 * <dl>
 * <dt>*
 * <dd>any substring
 * <dt>?
 * <dd>any single character
 * <dt>[abc]
 * <dd>any character in a set
 * </dl>
 *
 * Both stings can be wild card patterns, for example "a*d" and "a*"
 *
 * The main purpose of this class is to support condition evaluation in
 * CMSIS-Packs. It is optimized for small strings, primary for device names like
 * <b>"STM32F4[23]9??</b>".
 * <p/>
 * The method has a limitation:
 * <ul>
 * <li>there is no escape for *, ? [ and ] characters
 * </ul>
 */
public class WildCards {

    protected static final String[] WILDCARDS_CHARS = new String[] { "?", "*", "[", "]" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    /**
     * Private constructor to prevent instantiating the utility class
     */
    private WildCards() {
        throw new IllegalStateException("WildCards is a utility class"); //$NON-NLS-1$
    }

    /**
     * Match two strings containing wild cards (case sensitive)
     *
     * @param str1 first string argument
     * @param str2 second string argument
     * @return <b>true</b> if strings match, <b>false</b> otherwise
     */
    public static boolean match(final String str1, final String str2) {
        // check for empty and null strings
        if (str1 == null || str1.isEmpty()) {
            return (str2 == null || str2.isEmpty()); // return true if both strings are empty or null
        } else if (str2 == null || str2.isEmpty()) { // return false if one of the strings is empty or null
            return false;
        }
        // return true if strings are equal
        if (str1.equals(str2)) {
            return true;
        }
        try {
            // check if str1 contains wildcard
            if (isWildCard(str1) && str2.matches(toRegEx(str1))) {
                return true;
            }
            // check if str2 contains wildcard
            if (isWildCard(str2) && str1.matches(toRegEx(str2))) {
                return true;
            }
        } catch (PatternSyntaxException e) {
            return false; // invalid expression cannot be matched
        }
        return false;
    }

    public static boolean isWildCard(String str) {
        for (int i = 0; i < WILDCARDS_CHARS.length; i++) {
            if (str.indexOf(WILDCARDS_CHARS[i]) >= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts wild card string to regular expression
     *
     * @param str string to convert
     * @return regular expression string
     */
    public static String toRegEx(String str) {
        return str.replace(".", "\\."). //$NON-NLS-1$
                replace("$", "\\$"). //$NON-NLS-1$
                replace("}", "\\}"). //$NON-NLS-1$
                replace("{", "\\{"). //$NON-NLS-1$
                replace('?', '.'). // $NON-NLS-1$
                replace("*", ".*"); //$NON-NLS-1$
    }

    /**
     * Matches strings ignoring cases
     *
     * @param str1 first string argument
     * @param str2 second string argument
     * @return <b>true</b> if strings match, <b>false</b> otherwise
     */
    public static boolean matchNoCase(final String str1, final String str2) {
        String lcstr1 = (str1 == null) ? null : str1.toLowerCase();
        String lcstr2 = (str2 == null) ? null : str2.toLowerCase();
        return match(lcstr1, lcstr2);
    }

    /**
     * Match two strings containing wild cards
     *
     * @param str1 first string argument
     * @param str2 second string argument
     * @param cs   case sensitive flag (true: respect case, false: ignore case)
     * @return <b>true</b> if strings match, <b>false</b> otherwise
     */
    public static boolean match(final String str1, final String str2, boolean cs) {
        if (cs) {
            return matchNoCase(str1, str2);
        } else {
            return match(str1, str2);
        }
    }
}
