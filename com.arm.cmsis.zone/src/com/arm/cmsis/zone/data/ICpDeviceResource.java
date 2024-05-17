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

import java.util.Map;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.enums.ECoreArchitecture;

/**
 * An interface describing system device resource
 */
public interface ICpDeviceResource extends ICpZoneItem {

    /**
     * Return map of processor resources
     *
     * @return map of processors sorted by name
     */
    Map<String, ICpProcessorUnit> getProcessorUnits();

    /**
     * Returns number of processor descriptions in the device
     *
     * @return number of ICpProcessorUnit
     */
    int getProcessorCount();

    /**
     * Returns processor resource for given name
     *
     * @param pname processor name (Pname attribute) or null to get the single
     *              processor
     * @return ICpProcessorUnit
     */
    ICpProcessorUnit getProcessorUnit(String pname);

    /**
     * Return system processor architecture
     *
     * @return ECoreArchitecture enum value
     */
    ECoreArchitecture getArchitecture();

    /**
     * Checks if device has at least one processor with secure extension (Dtz="TZ")
     *
     * @return true if system has a secure processor
     */
    boolean hasSecureCore();

    /**
     * Checks if device has exactly one processor and that processor has MPU
     *
     * @return true if device has MPU
     */
    default boolean hasMPU() {
        ICpProcessorUnit processor = getProcessorUnit(null); // returns single processor
        if (processor == null)
            return false;
        String dmpuValue = processor.getAttribute(CmsisConstants.DMPU);
        return (dmpuValue.equals(CmsisConstants.MPU) || dmpuValue.equals(CmsisConstants.ONE));
    }

}
