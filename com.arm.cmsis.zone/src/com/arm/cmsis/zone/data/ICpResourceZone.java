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

/**
 * Description of a .rzone file
 */
public interface ICpResourceZone extends ICpRootZone {

    /**
     * Adds/updates resources assigned to the supplied zone
     *
     * @param projectZone ICpZone to get assignments from
     * @param tool        string tool ID that creates/updates the rzone file
     * @return true if something has changed
     */
    boolean updateResources(ICpZone zone, String tool);

}
