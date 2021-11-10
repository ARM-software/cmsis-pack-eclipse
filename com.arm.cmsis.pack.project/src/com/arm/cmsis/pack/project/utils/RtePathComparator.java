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

package com.arm.cmsis.pack.project.utils;

import java.util.Comparator;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.utils.AlnumComparator;

/**
 * Comparator that sorts collection of include or library paths<br>
 * Project - local includes are preceding those from CMSIS-Pack root folder on
 * the top<br>
 * Newer versions are preceding the older ones
 */
public class RtePathComparator implements Comparator<String> {

    public RtePathComparator() {
    }

    @Override
    public int compare(String arg0, String arg1) {
        if (arg0 == null || arg1 == null) {
            return 0; // should actually never happen
        }

        if (arg0.startsWith(CmsisConstants.RTE)) {
            if (arg1.startsWith(CmsisConstants.RTE))
                return AlnumComparator.alnumCompare(arg0, arg1);
            return -1;
        } else if (arg1.startsWith(CmsisConstants.RTE))
            return 1;

        // for non-local paths use descending order (puts newer version above older)
        return AlnumComparator.alnumCompare(arg1, arg0);
    }
}
