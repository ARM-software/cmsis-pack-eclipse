/*******************************************************************************
 * Copyright (c) 2015 ARM Ltd and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/

package com.arm.cmsis.pack.utils;

import java.util.Comparator;

/**
 * Class to compare strings containing decimal digits alpha-numerically. That is
 * in particular useful to compare version strings.
 * <p/>
 * The class can be used:
 * <ul>
 * <li> as comparator to sort collections (in descending order by default) 
 * <li> to compare strings directly using  <code>alnumCompare()</code> static functions
 * </ul>

 * <p/>
 * Groups of digits are converted into numbers for comparison, other characters
 * are compared in standard way.
 * <p> 
 * In contrast, standard lexicographical string comparison treats digits as characters, for example:
 * <dl>
 * <dt>alpha-numeric comparison:</dt>
 *    	<dd>"10.1" > "2.1"</dd>
 *    	<dd>"2.01" == "2.1"</dd>
 * <dt>standard lexicographical comparison:</dt> 
 *		<dd>"10.1" < "2.1"</dd>
 *		<dd>"2.01" < "2.1"</dd>
 * </dl>
 * </p>
 * 
 */
public class AlnumComparator implements Comparator<String> {
	private boolean caseSensitive = false;
	private boolean descending = true;

	/**
	 * Constructs default case insensitive comparator with descending sort order
	 *
	 */
	public AlnumComparator() {
	}

	/**
	 * Constructs case insensitive comparator
	 * @param descending - sorting order: true - descending, false - acceding
	 */
	public AlnumComparator(boolean descending) {
		this.descending = descending;
	}

	/**
	 * Constructs comparator
	 * @param descending    sorting order: true - descending, false - acceding
	 * @param caseSensitive comparison 
	 */
	public AlnumComparator(boolean descending, boolean caseSensitive) {
		this.descending = descending;
		this.caseSensitive = caseSensitive;
	}

	/**
	 * Checks if this comparator is case sensitive
	 * @return true if comparator is case sensitive
	 */
	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	/**
	 * Sets comparator' case sensitivity
	 * @param caseSensitive true if comparator should be case sensitive, false otherwise
	 */
	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	/**
	 * Checks if comparator works in descending order
	 * @return true if comparator works in descending order
	 */
	public boolean isDescending() {
		return descending;
	}

	/**
	 * Sets comparator descending oder 
	 * @param descending true if comparator should work in descending order
	 */
	public void setDescending(boolean descending) {
		this.descending = descending;
	}

	@Override
	public int compare(String str1, String str2) {
		if (isDescending()) {
			return compare(str2, str1, isCaseSensitive());
		}
		return compare(str1, str2, isCaseSensitive());
	}

	/**
	 * Compares two strings 
	 * @param str1 first string to compare
	 * @param str2 second string to compare
	 * @param cs case sensitive flag
	 * @return
	 * 	<dd><b>0</b> if str1 equals str2</dd>
	 * 	<dd><b>>0</b> if str1 greater than str2</dd>
	 * 	<dd><b><0</b> if str1 less than str2</dd>
	 */
	protected int compare(String str1, String str2, boolean cs) {
		return alnumCompare(str1, str2, isCaseSensitive());
	}

	/**
	 * Compares two strings alpha-numerically 
	 * @param str1 first string to compare
	 * @param str2 second string to compare
	 * @param cs case sensitive flag
	 * @return
	 * 	<dd><b>0</b> if str1 equals str2</dd>
	 * 	<dd><b>1</b> if str1 greater than str2</dd>
	 * 	<dd><b>-1</b> if str1 less than str2</dd>
	 */
	public static int alnumCompare(final String str1, final String str2, boolean cs) {
		// allow comparison of null and empty strings 
		if (str1 == null || str1.isEmpty()) {
			if (str2 == null || str2.isEmpty()) {
				return 0;
			}
			return -1;
		} else if (str2 == null || str2.isEmpty()) {
			return 1;
		}

		int l1 = str1.length();
		int l2 = str2.length();
		int i1 = 0;
		int i2 = 0;
		while (i1 < l1 && i2 < l2) {
			char c1 = str1.charAt(i1);
			char c2 = str2.charAt(i2);

			if (Character.isDigit(c1) && Character.isDigit(c2)) {
				int digitBegin1 = i1;
				int digitBegin2 = i2;
				// skip digits
				while (i1 < l1 && Character.isDigit(str1.charAt(i1))) {
					i1++;
				}
				while (i2 < l2 && Character.isDigit(str2.charAt(i2))) {
					i2++;
				}
				// extract "digit" strings
				String s1 = str1.substring(digitBegin1, i1);
				String s2 = str2.substring(digitBegin2, i2);
				// convert to integers
				int val1 = 0;
				int val2 = 0;
				try{
					val1 = Integer.decode(s1);
					val2 = Integer.decode(s2);
				}  catch (NumberFormatException e) {
					return str1.compareTo(str2);
				}
				if (val1 > val2) {
					return 1;
				} else if (val1 < val2) {
					return -1;
				}
			} else {
				if (!cs) {
					c1 = Character.toUpperCase(c1);
					c2 = Character.toUpperCase(c2);
				}
				if ( (c1 == '*' && alnumCompareWildcardMatch(str1.substring(i1), str2.substring(i2))) ||
						(c2 == '*' && alnumCompareWildcardMatch(str2.substring(i2), str1.substring(i1))) ) {
					return 0;
				}
				if (c1 > c2) {
					return 1;
				} else if (c1 < c2) {
					return -1;
				}
				i1++;
				i2++;
			}
		}
		return (l1 - i1) - (l2 - i2);
	}

	/**
	 * Compares two strings alpha-numerically respecting case  
	 * @param str1 - first string to compare
	 * @param str2 - second string to compare
	 * @return str1 > str2 : 1 ; str1 < str2 : -1; str1 == str2 : 0 
	 */
	public static int alnumCompare(final String str1, final String str2) {
		return alnumCompare(str1, str2, true);
	}

	/**
	 * Compares two strings alpha-numerically ignoring case  
	 * @param str1 - first string to compare
	 * @param str2 - second string to compare
	 * @return  str1 > str2 : 1 ; str1 < str2 : -1; str1 == str2 : 0 
	 */
	public static int alnumCompareNoCase(final String str1, final String str2) {
		return alnumCompare(str1, str2, false);
	}

	/**
	 * Check if a String matches a Regex Pattern
	 * @param pattern - the regex pattern
	 * @param string - the string to match
	 * @return  true if string matches pattern, false otherwise
	 */
	public static boolean alnumCompareWildcardMatch(String p, String s) {
		int m = s.length(), n = p.length();
        int count = 0;
        for (int i = 0; i < n; i++) {
            if (p.charAt(i) == '*') {
				count++;
			}
        }
        if (count==0 && m != n) {
			return false;
		} else if (n - count > m) {
			return false;
		}

        boolean[] match = new boolean[m+1];
        match[0] = true;
        for (int i = 0; i < m; i++) {
            match[i+1] = false;
        }
        for (int i = 0; i < n; i++) {
            if (p.charAt(i) == '*') {
                for (int j = 0; j < m; j++) {
                    match[j+1] = match[j] || match[j+1]; 
                }
            } else {
                for (int j = m-1; j >= 0; j--) {
                    match[j+1] = (p.charAt(i) == '?' || p.charAt(i) == s.charAt(j)) && match[j];
                }
                match[0] = false;
            }
        }
        return match[m];
	}
}
