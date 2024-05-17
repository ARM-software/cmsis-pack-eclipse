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

package com.arm.cmsis.pack.info;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpDebugConfiguration;
import com.arm.cmsis.pack.data.ICpDeviceItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.enums.ECoreArchitecture;
import com.arm.cmsis.pack.enums.IEvaluationResult;

/**
 * Interface representing device used in the configuration
 */
public interface ICpDeviceInfo extends ICpItemInfo, IEvaluationResult {

    /**
     * Returns actual device represented by this info
     *
     * @return actual device
     */
    ICpDeviceItem getDevice();

    /**
     * Sets actual device to this info
     *
     * @param device actual device to set
     */
    void setDevice(ICpDeviceItem device);

    /**
     * Sets actual device to this info
     *
     * @param device         actual device to set
     * @param fullDeviceName devise name in form Dname:Pname
     */
    void setDevice(ICpDeviceItem device, String fullDeviceName);

    /**
     * Returns effective device properties for associated processor
     *
     * @return effective device properties if any
     */
    ICpItem getEffectiveProperties();

    /**
     * Returns device debug configuration for associated processor
     *
     * @return ICpDebugConfiguration
     */
    ICpDebugConfiguration getDebugConfiguration();

    /**
     * Returns brief device description that includes core, clock and memory
     *
     * @return brief device description
     */
    String getSummary();

    /**
     * Returns brief clock description
     *
     * @return brief clock description
     */
    String getClockSummary();

    /**
     * Returns brief description of device memory
     *
     * @return brief memory description
     */
    String getMemorySummary();

    /**
     * Returns absolute path of a dbgconf file copied to project
     *
     * @return absolute dbgconf filename or empty string if no file is available
     */
    default String getDgbConfFileName() {
        return CmsisConstants.EMPTY_STRING;
    }

    /**
     * Return processor architecture
     *
     * @return ECoreArchitecture enum value
     */
    default ECoreArchitecture getCoreArchitecture() {
        return ECoreArchitecture.fromString(getAttribute(CmsisConstants.DCORE));
    }

}
