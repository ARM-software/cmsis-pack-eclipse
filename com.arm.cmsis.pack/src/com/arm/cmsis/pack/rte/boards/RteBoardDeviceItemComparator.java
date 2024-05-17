/*******************************************************************************
 * Copyright (c) 2022 ARM Ltd. and others
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

package com.arm.cmsis.pack.rte.boards;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.utils.AlnumComparator;

/**
 * Comparator class to sort board items and their devices
 *
 */
public class RteBoardDeviceItemComparator extends AlnumComparator {

    /**
     * Default constructor
     */
    public RteBoardDeviceItemComparator() {
        super(false, false);
    }

    @Override
    public int compare(String str1, String str2) {
        if (str1 != null && str1.equals(str2)) {
            return 0;
        }

        if (CmsisConstants.MOUNTED_DEVICES.equals(str1)) {
            return -1;
        }

        if (CmsisConstants.MOUNTED_DEVICES.equals(str2)) {
            return 1;
        }

        if (CmsisConstants.COMPATIBLE_DEVICES.equals(str1)) {
            return -1;
        }

        if (CmsisConstants.COMPATIBLE_DEVICES.equals(str2)) {
            return 1;
        }
        return compare(str1, str2, isCaseSensitive());
    }

}
