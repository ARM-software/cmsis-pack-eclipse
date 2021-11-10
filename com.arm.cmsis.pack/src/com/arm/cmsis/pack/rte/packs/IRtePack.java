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

package com.arm.cmsis.pack.rte.packs;

/**
 * Interface that represents an RTE view on a pack
 *
 */
public interface IRtePack extends IRtePackItem {

    /**
     * Sets pack selected/unselected
     *
     * @param selected true or false
     */
    void setSelected(boolean selected);

    /**
     * Checks if the item is explicitly selected using setSelected
     *
     * @return true if explicitly selected
     */
    boolean isExplicitlySelected();

    /**
     * Checks if item represents the latest pack version
     *
     * @return true if latest
     */
    boolean isLatest();

}
