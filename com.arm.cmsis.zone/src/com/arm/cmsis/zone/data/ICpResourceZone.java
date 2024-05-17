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
