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
 * Interface for pack family: collection of pack with the same name, but
 * different versions
 *
 */
public interface ICpPackFamily extends ICpPackGroup {

    /**
     * Returns Pack releases, those that are not found in collection returned by
     * getPacks()
     *
     * @return an ICpItem that contains collection of previous pack releases
     * @see #getPacks()
     */
    ICpItem getPreviousReleases();

    /**
     * Returns latest installed pack if any
     *
     * @return {@link ICpPack} object or null if not found
     */
    default ICpPack getLatestInstalledPack() {
        for (ICpPack pack : getPacks()) {
            if (pack.getPackState().isInstalledOrLocal()) {
                return pack;
            }
        }
        return null;
    }

    /**
     * Returns the latest installed pack if any, the latest available otherwise
     *
     * @return {@link ICpPack} object or null if not found
     */
    default ICpPack getLatestEffectivePack() {
        ICpPack pack = getLatestInstalledPack();
        if (pack != null)
            return pack;
        return getPack();
    }

    /**
     * Returns pack of specified version if any
     *
     * @param version version range string to get pack (empty to get the latest
     *                version)
     * @return {@link ICpPack} object or null if not found
     */
    default ICpPack getPackByVersionRange(final String versionRange) {
        if (versionRange == null || versionRange.isEmpty())
            return getPack();// returns latest version

        return getPack(versionRange); // treats argument as an ID or version
    }

}
