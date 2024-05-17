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

import com.arm.cmsis.pack.enums.EDebugProtocolType;

/**
 * Convenience interface to access information under "swd", "jtag" and "cjtag"
 * device port properties
 */
public interface ICpDebugProtocol extends ICpDeviceProperty {

    /**
     * Returns protocol type as enumerated value
     *
     * @return EDebugProtocolType
     */
    EDebugProtocolType getProtocolType();

    /**
     * IDCODE , 0 if not specified
     *
     * @return IDCODE code as long value
     */
    long getIdCode();

    /**
     * TARGETSEL value for DP v2 with multi-drop, 0 if not specified
     *
     * @return TARGETSEL as long value
     */
    long getTargetSel();

    /**
     * JTAG: index of the TAP in the JTAG chain, default is 0
     *
     * @return TAP index as long value
     */
    int getTapIndex();

    /**
     * JTAG : Instruction register length, 0 if not specified
     *
     * @return instruction register length,
     */
    int getIRlen();

}
