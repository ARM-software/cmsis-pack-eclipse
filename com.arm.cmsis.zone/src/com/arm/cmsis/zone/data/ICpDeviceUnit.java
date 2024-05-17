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

import com.arm.cmsis.pack.info.ICpDeviceInfo;

/**
 * An interface describing system device resource
 */
public interface ICpDeviceUnit extends ICpDeviceResource, ICpDeviceInfo {

    /**
     * Updates device according the project zone data
     *
     * @param projectZone ICpZone as source of update
     * @return true if attributes or resources have changed
     */
    boolean updateDevice(ICpZone projectZone);

}
