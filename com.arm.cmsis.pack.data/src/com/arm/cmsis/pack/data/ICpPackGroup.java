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

package com.arm.cmsis.pack.data;

import java.util.Collection;

import com.arm.cmsis.pack.generic.IAttributes;

/**
 * Base interface for pack container family or collection
 *
 */
public interface ICpPackGroup extends ICpItem {
    /**
     * Returns pack of specified version if any
     *
     * @param packId pack ID or version string to get pack
     * @return {@link ICpPack} object or null if not found
     */
    ICpPack getPack(final String packId);

    /**
     * Returns latest pack that satisfies the attributes
     *
     * @param attributes an Attributes that contain vendor, name and version
     * @return {@link ICpPack} object or null if not found
     */
    ICpPack getPack(IAttributes attributes);

    /**
     * Searches for pack loaded from given pdsc file
     *
     * @param pdscFile file name corresponding to pack
     * @return {@link ICpPack} object or null if not found
     */
    ICpPack getPackByFilename(final String pdscFile);

    /**
     * Returns collection of packs in the group sorted by ID (ascending and version
     * descending)
     *
     * @return collection of {@link ICpPack} objects
     */
    Collection<ICpPack> getPacks();

}
