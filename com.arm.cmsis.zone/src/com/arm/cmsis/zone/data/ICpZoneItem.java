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
package com.arm.cmsis.zone.data;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;

/**
 * Base for all items in CMSIS-Zone framework
 *
 */
public interface ICpZoneItem extends ICpItem {

    /**
     * Returns top ICpRootZone item
     *
     * @return top ICpRootZone item
     */
    default ICpRootZone getRootZone() {
        return getParentOfType(ICpRootZone.class);
    }

    /**
     * Returns associated block's peripheral name
     *
     * @return peripheral name
     */
    default String getPeripheralName() {
        return getAttribute(CmsisConstants.PERIPHERAL);
    }

    /**
     * Returns associated block's peripheral group
     *
     * @return group name or null
     */
    default String getGroupName() {
        return attributes().getAttribute(CmsisConstants.GROUP);
    }

    /**
     * Returns associated Pref
     *
     * @return Pref string name or null
     */
    default String getPref() {
        return attributes().getAttribute(CmsisConstants.PREF);
    }

    /**
     * Sets name for this item
     *
     * @param name name to set
     */
    default void setName(String name) {
        setAttribute(CmsisConstants.NAME, name);
    }

    /**
     * Creates a FTL model with all required attributes and children expanded as
     * separate ICpItems
     *
     * @param ftlParent parent item in FTL tree
     * @return created ICpItem
     */
    default ICpItem toFtlModel(ICpItem ftlParent) {
        return toSimpleTree(ftlParent);
    }

    /**
     * Creates a simple FTL model ICpItem out of attribute value
     *
     * @param ftlParent parent item in FTL tree
     * @param key       attribute key (cannot be null or empty))
     * @param value     attribute value
     * @return created ICpItem
     */
    default ICpItem toFtlModel(ICpItem ftlParent, String key, String value) {
        return toSimpleItem(ftlParent, key, value);
    }

}
