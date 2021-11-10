/*******************************************************************************
* Copyright (c) 2021 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.data;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Interface to collection of pack families
 *
 * @see ICpPackFamily
 */
public interface ICpPackCollection extends ICpPackGroup {

    /**
     * Returns pack collection of the latest packs
     *
     * @return collection of the latest packs
     */
    Collection<ICpPack> getLatestInstalledPacks();

    /**
     * Returns pack collection of the latest packs (installed have precedence over
     * available and deprecated)
     *
     * @return collection of the latest packs
     */
    Collection<ICpPack> getLatestEffectivePacks();

    /**
     * Returns set of latest packs IDs
     *
     * @return set of latest pack IDs
     */
    Set<String> getLatestPackIDs();

    /**
     * Returns pack collection of filtered packs according to supplied filter
     *
     * @return collection of filtered packs, if filter is null, the entire
     *         collection is returned
     */
    Collection<ICpPack> getFilteredPacks(ICpPackFilter packFilter);

    /**
     * Returns pack collection of the packFamilyId
     *
     * @param packFamilyId pack family id to get the pack
     * @return collection of packs that belong to given packFamilyId, or null if no
     *         such packFamilyId exists
     */
    Collection<ICpPack> getPacksByPackFamilyId(String packFamilyId);

    /**
     * Returns pack family for the packFamilyId
     *
     * @return ICpPackFamily for given packFamilyId, or null if no such packFamilyId
     *         exists
     */
    ICpPackFamily getFamily(String packFamilyId);

    /**
     * Return pack family map (family id to family)
     *
     * @return Map<String, ICpPackFamily>
     */
    Map<String, ICpPackFamily> getFamilies();
}
