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

import java.util.Collection;

/**
 * SAU init interface
 */
public interface ICpSauInit extends ICpZoneItem {

    /**
     * Retrieve all SAU regions
     *
     * @return Collection of ICpSauRegion, might be empty.
     */
    Collection<ICpSauRegion> getSauRegions();

    /**
     * Retrieve a SAU region by name.
     *
     * @param name SAU region name
     * @return ICpSauRegion, or null if unavailable.
     */
    ICpSauRegion getSauRegion(String name);

}
