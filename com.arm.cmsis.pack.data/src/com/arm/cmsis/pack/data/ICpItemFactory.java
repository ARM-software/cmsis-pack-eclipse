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

/**
 * Provides factory method to create items implementing ICpItem interface
 *
 * @see ICpItem
 */
public interface ICpItemFactory {
    /**
     * Factory method to create ICpItem-derived instances
     *
     * @param parent item that contains this one
     * @param tag    XML tag for the item
     * @return created or existing ICpItem
     */
    public ICpItem createItem(ICpItem parent, String tag);
}
