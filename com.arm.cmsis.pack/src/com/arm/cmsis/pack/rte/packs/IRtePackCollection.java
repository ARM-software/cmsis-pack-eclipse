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

package com.arm.cmsis.pack.rte.packs;

import java.util.Collection;
import java.util.Map;

import com.arm.cmsis.pack.data.ICpPackFilter;
import com.arm.cmsis.pack.info.ICpPackFilterInfo;
import com.arm.cmsis.pack.info.ICpPackInfo;

/**
 * Interface to represent RTE view on ICpPackCollecion
 */
public interface IRtePackCollection extends IRtePackItem {

    /**
     * Creates Pack filter based on selection
     *
     * @return new ICpPackFilter
     */
    ICpPackFilter createPackFiler();

    /**
     * Sets ICpPackFilterInfo to the collection to initialize selection
     *
     * @param packFilterInfo
     */
    void setPackFilterInfo(ICpPackFilterInfo packFilterInfo);

    /**
     * Creates ICpPackFilterInfo based on selection
     *
     * @return ICpPackFilterInfo
     */
    ICpPackFilterInfo createPackFilterInfo();

    /**
     * Sets if to use only latest versions of all installed packs
     *
     * @param bUseLatest flag if to use latest
     */
    void setUseAllLatestPacks(boolean bUseLatest);

    /**
     * Returns child IRtePackFamily for given family id
     *
     * @param familyId pack family id
     * @return IRtePackFamily
     */
    IRtePackFamily getRtePackFamily(String familyId);

    /**
     * Sets used packs to the collection
     *
     * @param map of used packs (id to ICpPackInfo)
     */
    void setUsedPacks(Map<String, ICpPackInfo> usedPackInfos);

    /**
     * Checks if the pack with the given id is used
     *
     * @param id pack id
     * @return true if used
     */
    boolean isPackUsed(String id);

    /**
     * Returns collection of all pack families in the collection
     *
     * @return collection of IRtePackFamily objects
     */
    Collection<IRtePackFamily> getRtePackFamilies();

    /**
     * Returns collection of pack families used in project
     *
     * @return collection of IRtePackFamily objects
     */
    Collection<IRtePackFamily> getUsedRtePackFamilies();

}
