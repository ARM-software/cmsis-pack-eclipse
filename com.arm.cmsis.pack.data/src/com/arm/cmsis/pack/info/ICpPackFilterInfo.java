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

package com.arm.cmsis.pack.info;

import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPackFilter;

/**
 * Interface to store/load pack filter
 */
public interface ICpPackFilterInfo extends ICpItem {

    /**
     * Creates pack filter based on information stored in the info
     *
     * @return ICpPackFilter
     */
    ICpPackFilter createPackFilter();

    /**
     * Check is to latest versions of all installed packs
     *
     * @return true if the latest versions of packs should be used
     */
    boolean isUseAllLatestPacks();

    /**
     * Sets if to use only latest versions of all installed packs
     *
     * @param bUseLatest flag if to use latest
     */
    void setUseAllLatestPacks(boolean bUseLatest);

}
