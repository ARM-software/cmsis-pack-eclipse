/*******************************************************************************
* Copyright (c) 2017 ARM Ltd. and others
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
import com.arm.cmsis.pack.enums.ECoreArchitecture;

/**
 * A processor unit declares a single CPU element.
 */
public interface ICpProcessorUnit extends ICpZoneItem {

	/**
	 * Returns device parent
	 * @return ICpDeviceUnit
	 */
	default ICpDeviceUnit getParentDeviceUnit(){
		return getParentOfType(ICpDeviceUnit.class);
	}


	/**
	 * Returns processor architecture
	 * @return ECoreArchitecture
	 */
	default ECoreArchitecture getArchitecture() {
		return ECoreArchitecture.fromString(getAttribute(CmsisConstants.DCORE));
	}

	/**
	 * Checks if processor has MPU
	 * @return true if processor has MPU
	 */
	default boolean hasMPU() {
		String dmpuValue = getAttribute(CmsisConstants.DMPU);
		return (dmpuValue.equals(CmsisConstants.MPU) || dmpuValue.equals(CmsisConstants.ONE));
	}

	/**
	 * Returns number of MPU regions
	 * @return number of MPU regions
	 */
	default int getNumMpuRegions() { return hasMPU() ? getAttributeAsInt(CmsisConstants.DnumMpuRegions, 8) : 0;}


	/**
	 * Checks if the processor unit has TrustZone
	 * @return true if Dtz attribute equals "TZ"
	 */
	default boolean hasTrustZone() { return CmsisConstants.TZ.equals(getAttribute(CmsisConstants.DTZ));}

	/**
	 * Returns number of interrupts
	 * @return number of interrupts
	 */
	default int genNumInterrups() {	return getAttributeAsInt(CmsisConstants.DnumInterrupts, 0);}

	/**
	 * Returns number of SAU regions
	 * @return number of SAU regions
	 */
	default int genNumSauRegions() { return getAttributeAsInt(CmsisConstants.DnumSauRegions, 0);}
}
