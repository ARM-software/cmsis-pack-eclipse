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

package com.arm.cmsis.pack.utils;

/**
 * Class to compare version strings according to Semantic Versioning 2.0
 * <p/>
 * The class can be used:
 * <ul>
 * <li>as comparator to sort collections (in descending order by default)
 * <li>to compare version strings directly using <code>versionCompare()</code>
 * static functions
 * </ul>
 *
 * @see <a href="http://semver.org">http://semver.org</a>
 */
public class VersionComparator extends AlnumComparator {

    /**
     * Constructs default case insensitive comparator with descending sort order
     *
     */
    public VersionComparator() {
    }

    /**
     * Constructs case insensitive comparator
     *
     * @param descending - sorting order: true - descending, false - acceding
     */
    public VersionComparator(boolean descending) {
        super(descending);
    }

    /**
     * Constructs comparator
     *
     * @param descending    sorting order: true - descending, false - acceding
     * @param caseSensitive comparison
     */
    public VersionComparator(boolean descending, boolean caseSensitive) {
        super(descending, caseSensitive);
    }

    @Override
    protected int compare(String ver1, String ver2, boolean cs) {
        return versionCompare(ver1, ver2, cs);
    }

    /**
     * Semantically compares two version strings respecting case
     *
     * @param ver1 first version string to compare
     * @param ver2 second version string to compare
     * @param cs   case sensitive flag for non-numeric values
     * @return
     *         <dd><b> 4</b> major of ver1 greater than major ver2</dd>
     *         <dd><b> 3</b> minor of ver1 greater than minor ver2</dd>
     *         <dd><b> 2</b> patch of ver1 greater than patch ver2</dd>
     *         <dd><b> 1</b> release of ver1 greater than release ver2</dd>
     *         <dd><b> 0</b> ver1 equals ver2</dd>
     *         <dd><b>-1</b> release of ver1 less than release of ver2</dd>
     *         <dd><b>-2</b> patch of ver1 less than patch of ver2</dd>
     *         <dd><b>-3</b> minor of ver1 less than minor of ver2</dd>
     *         <dd><b>-4</b> major of ver1 less than major of ver2</dd>
     */

    public static int versionCompare(String ver1, String ver2, boolean cs) {
        // allow comparison of null and empty strings
        if (ver1 == null) {
            if (ver2 == null)
                return 0;
            return -4;
        } else if (ver2 == null) {
            return 4;
        }

        Version v1 = new Version(ver1);
        Version v2 = new Version(ver2);

        return v1.compareTo(v2, cs);
    }

    /**
     * Semantically compares two version strings respecting case
     *
     * @param ver1 - first version string to compare
     * @param ver2 - second version string to compare
     * @return comparison result - see versionCompare(String, String, boolean)
     * @see #versionCompare(String, String, boolean)
     */
    public static int versionCompare(final String str1, final String str2) {
        return versionCompare(str1, str2, true);
    }

    /**
     * Semantically compares two version strings respecting case
     *
     * @param ver1 - first version string to compare
     * @param ver2 - second version string to compare
     * @return comparison result - see versionCompare(String, String, boolean)
     * @see #versionCompare(String, String, boolean)
     */
    public static int versionCompareNoCase(final String str1, final String str2) {
        return versionCompare(str1, str2, true);
    }

    /**
     * Check if supplied version matches supplied version range
     *
     * @param version      version to check
     * @param versionRange string with version range in the form
     *                     <code>"min[:max]"</code>
     * @return true if version is larger or equal to maximum and smaller or equal
     *         optional maximum
     */
    public static boolean matchVersionRange(final String version, final String versionRange) {
        if (version == null || versionRange == null)
            return true;

        if (version.isEmpty() || versionRange.isEmpty())
            return true;

        String verMin = null;
        String verMax = null;
        int i = versionRange.indexOf(':');
        if (i >= 0) {
            verMin = versionRange.substring(0, i);
            verMax = versionRange.substring(i + 1);
        } else {
            verMin = versionRange;
        }

        if (verMin != null && !verMin.isEmpty()) {
            int res = versionCompare(version, verMin);
            if (res < 0)
                return false;
            if (verMin.equals(verMax))
                return res == 0;
        }
        if (verMax != null && !verMax.isEmpty() && versionCompare(version, verMax) > 0) {
            return false;
        }
        return true;
    }

    /**
     * Removes build meta data from version string (after +)
     *
     * @param ver version string
     * @return version without meta data
     */
    public static String removeMetadata(String ver) {
        if (ver == null)
            return ver;
        int pos = ver.indexOf('+');
        if (pos >= 0)
            return ver.substring(0, pos);
        return ver;
    }

    /**
     * Internal helper class
     */
    private static class Version implements Comparable<Version> {
        private static final String ZERO_STRING = "0"; //$NON-NLS-1$
        private String[] segments = null; // first three version segments : MAJOR.MINOR.PATCH
        private String release = null; // remainder after '-'
        private int fLevel;

        Version(String ver) {
            this(ver, 0);
        }

        Version(String ver, int level) {
            fLevel = level;
            if (ver == null)
                throw new IllegalArgumentException("Version can not be null"); //$NON-NLS-1$

            // 1. drop build metadata
            ver = VersionComparator.removeMetadata(ver);

            // 2. extract release
            int pos = ver.indexOf('-');
            if (pos >= 0) {
                release = ver.substring(pos + 1);
                ver = ver.substring(0, pos);
            } else if (fLevel == 0 && !ver.isEmpty()) {
                // check for special ST case without dash like 1.2.3b < 1.2.3
                int lastIndex = ver.length() - 1;
                for (pos = lastIndex; pos >= 0; pos--) {
                    char ch = ver.charAt(pos);
                    if (ch == '.')
                        break;
                    if (!Character.isDigit(ch))
                        continue;
                    if (pos < lastIndex) {
                        release = ver.substring(pos);
                        ver = ver.substring(0, pos);
                    }
                    break;
                }
            }
            // 3. split segments
            if (ver != null) {
                segments = ver.split("\\."); //$NON-NLS-1$
            }
        }

        public String getRelease() {
            return release;
        }

        public int getSegmentCount() {
            return segments != null ? segments.length : 0;
        }

        public String getSegment(int index) {
            if (index >= 0 && index < getSegmentCount())
                return segments[index];
            return ZERO_STRING;
        }

        @Override
        public int compareTo(Version that) {
            return compareTo(that, true);
        }

        @Override
        public boolean equals(Object arg0) {
            if (arg0 == this)
                return true;
            if (arg0 instanceof Version)
                return compareTo((Version) arg0) == 0;
            return false;
        }

        public int compareTo(Version that, boolean cs) {
            int result = 4;
            if (that == null)
                return result;
            int length = Math.max(this.getSegmentCount(), that.getSegmentCount());
            for (int i = 0; i < length; i++) {
                String thisSegment = this.getSegment(i);
                String thatSegment = that.getSegment(i);
                int res = alnumCompare(thisSegment, thatSegment, cs, false);
                if (res != 0)
                    return res > 0 ? result : -result;
                if (result > 1)
                    result--;
            }

            String thisRelease = this.getRelease();
            String thatRelease = that.getRelease();

            if (thisRelease == null && thatRelease == null)
                return 0;
            else if (thisRelease == null)
                return 1;
            else if (thatRelease == null)
                return -1;

            // compare releases
            Version v1 = new Version(thisRelease, fLevel + 1);
            Version v2 = new Version(thatRelease, fLevel + 1);
            result = v1.compareTo(v2, false); // case insensitive compare for release revision

            if (result < 0) {
                return -1;
            } else if (result > 0) {
                return 1;
            }
            return 0;
        }
    }

}
