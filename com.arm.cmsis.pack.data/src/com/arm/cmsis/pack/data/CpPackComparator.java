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

import java.util.Comparator;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.utils.AlnumComparator;
import com.arm.cmsis.pack.utils.VersionComparator;

/**
 * Comparator to sort packs by their deprecation, vendor (any vendor < Keil ),
 * name, version
 */
public class CpPackComparator implements Comparator<ICpPack> {

    @Override
    public int compare(ICpPack pack0, ICpPack pack1) {
        return comparePacks(pack0, pack1);
    }

    public static int comparePacks(ICpPack pack0, ICpPack pack1) {
        boolean deprecated0 = pack0.isDeprecated();
        boolean deprecated1 = pack1.isDeprecated();
        if (deprecated0 != deprecated1) {
            return deprecated0 ? 1 : -1;
        }

        String vendor0 = pack0.getVendor();
        String vendor1 = pack1.getVendor();

        boolean bKeil0 = vendor0.equals(CmsisConstants.KEIL);
        boolean bKeil1 = vendor1.equals(CmsisConstants.KEIL);
        if (bKeil0 != bKeil1) {
            int res = bKeil0 ? 1 : -1;
            return res;
        }

        int res = AlnumComparator.alnumCompare(vendor0, vendor1);
        if (res != 0) {
            return res;
        }

        res = AlnumComparator.alnumCompare(pack0.getName(), pack1.getName());
        if (res != 0) {
            return res;
        }

        res = VersionComparator.versionCompare(pack0.getVersion(), pack1.getVersion());
        return res;
    }

}
