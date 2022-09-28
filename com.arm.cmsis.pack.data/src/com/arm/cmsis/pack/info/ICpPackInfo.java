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

package com.arm.cmsis.pack.info;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.utils.VersionComparator;

/**
 * Interface describing pack meta data
 */
public interface ICpPackInfo extends ICpItemInfo {

    /**
     * Sets actual pack to this info
     *
     * @param pack actual CMSIS pack
     */
    void setPack(ICpPack pack);

    /**
     * Constructs an effective pack ID from supplied attributes
     *
     * @param packAttributes IAttributes describing Pack
     * @return effective pack ID
     */
    static String constructEffectivePackID(IAttributes packAttributes) {
        if (packAttributes == null)
            return CmsisConstants.EMPTY_STRING;
        String vendor = packAttributes.getAttribute(CmsisConstants.VENDOR);
        String name = packAttributes.getAttribute(CmsisConstants.NAME);
        String packId = vendor + '.' + name;
        String mode = packAttributes.getAttribute(CmsisConstants.VERSION_MODE);
        if (CmsisConstants.FIXED.equals(mode)) { // use fixed version of the pack
            String version = packAttributes.getAttribute(CmsisConstants.VERSION);
            if (version != null && !version.isEmpty())
                packId += '.' + VersionComparator.removeMetadata(version);
        }
        return packId;
    }

}
