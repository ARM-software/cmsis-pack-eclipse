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

import java.util.Collection;
import java.util.Set;

import com.arm.cmsis.pack.common.CmsisConstants;

/**
 * Represents CMISIS Pack meta data read from pdsc file Provides access method
 * to underlying structure of the Pack
 */
public interface ICpPack extends ICpRootItem, IAllDeviceNames {

    /**
     * Describes Pack state:
     * <dl>
     * <dd>LOCAL pack is loaded from a local repository
     * <dd>INSTALLED pack is locally installed
     * <dd>DOWNLOADED pack is download, but not installed
     * <dd>AVAILABLE pack is available for download
     * <dd>GENERATED generator pack read from gpdsc file
     * <dd>UNKNOWN pack state is not defined
     * <dd>ERROR pack has error
     * </dl>
     *
     *
     */
    enum PackState {
        LOCAL, INSTALLED, DOWNLOADED, AVAILABLE, GENERATED, UNKNOWN, ERROR;

        public boolean isInstalledOrLocal() {
            return this == INSTALLED || this == LOCAL;
        }
    }

    /**
     * Sets pack state
     *
     * @return <code>PackState<code> of the Pack
     * @see PackState
     */
    PackState getPackState();

    /**
     * Returns pack state
     *
     * @param state PackState to set
     * @see PackState
     */
    void setPackState(PackState state);

    /**
     * Returns absolute path to directory where pack is or must be installed
     *
     * @return absolute path to the pack
     */
    String getInstallDir(String packRoot);

    /**
     * Returns condition corresponding to supplied ID
     *
     * @param conditionId id of the condition to find
     * @return condition as ICpItem or null if condition with such id does not exist
     *         in the pack
     */
    ICpItem getCondition(String conditionId);

    /**
     * Returns generator corresponding to supplied id if any
     *
     * @param id name of the generator to find. If null or empty the first generator
     *           is returned (gpdsc case)
     * @return generator as ICpGenerator or null if not found in the pack
     */
    ICpGenerator getGenerator(String id);

    /**
     * Get names of boards described in the pack
     *
     * @return a set of boar names
     */
    Set<String> getBoardNames();

    /**
     * Checks if pack contains device descriptions
     *
     * @return true if pack contains at least one device item
     */
    default boolean hasDevices() {
        Collection<? extends ICpItem> devices = getGrandChildren(CmsisConstants.DEVICES_TAG);
        return devices != null && !devices.isEmpty();
    }

    /**
     * Checks if pack contains board descriptions
     *
     * @return true if pack contains at least one board item
     */
    default boolean hasBoards() {
        Collection<? extends ICpItem> boards = getGrandChildren(CmsisConstants.BOARDS_TAG);
        return boards != null && !boards.isEmpty();
    }

    /**
     * Check if this pack is generic or not
     *
     * @return true if this pack is generic
     */
    boolean isDevicelessPack();

    /**
     * @return true if this pack is the latest version
     */
    boolean isLatest();

    /**
     * Returns collection of Pack releases (from latest to oldest)
     *
     * @return collection of ICpItem representing pack releases
     */
    Collection<? extends ICpItem> getReleases();

    /**
     * Returns release item corresponding this pack version
     *
     * @return version corresponding this pack version if any
     */
    default ICpItem getThisRelease() {
        String thisVersion = getVersion();
        for (ICpItem release : getReleases()) {
            if (thisVersion.equals(release.getVersion())) {
                return release;
            }
        }
        return null;
    }

    /**
     * Returns collection of required packs of this pack
     *
     * @return collection of required packs of this pack
     */
    Collection<? extends ICpItem> getRequiredPacks();

    /**
     * Returns the download URL of the specified release if specified, otherwise
     * returns EMPTY_STRING
     *
     * @return pack URL
     */
    String getReleaseUrl(String version);

    /**
     * Return the download URL for the specified version of the: this can be the
     * main URL or an alternative one from the release
     *
     * @param version required pack version
     * @return pack URL
     */
    default String getDownloadUrl(String version) {
        String url = getReleaseUrl(version);
        if (!url.isEmpty())
            return url;
        return getUrl();
    }

    /**
     * Return the download URL for this pack: this can be the main URL or an
     * alternative one from the release
     *
     * @return pack URL
     */
    default String getDownloadUrl() {
        return getDownloadUrl(getVersion());
    }

    /**
     * Returns repository URL if specified by "repository" element in pack
     * description
     *
     * @return repository URL or empty String
     */
    default String getRepositoryUrl() {
        ICpItem repo = getFirstChild(CmsisConstants.REPOSITORY);
        if (repo != null)
            return repo.getText();
        return CmsisConstants.EMPTY_STRING;
    }

}
