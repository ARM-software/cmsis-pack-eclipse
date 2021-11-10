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

import com.arm.cmsis.pack.data.ICpItem;

/**
 * Interface that represents meta-information of a pack ICpItem that allows to
 * find that item
 */
public interface ICpItemInfo extends ICpItem {

    /**
     * Returns package info referring to originating pack of this item
     *
     * @return pack info
     */
    ICpPackInfo getPackInfo();

    /**
     * Updates info information to the actual resolved item
     */
    void updateInfo();

    /**
     * Returns parent ICpItemInfo if this file (component, API or device)
     *
     * @return parent ICpItemInfo
     */
    default ICpItemInfo getParentInfo() {
        return getParentOfType(ICpItemInfo.class);
    }

}
