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

import com.arm.cmsis.pack.common.CmsisConstants;

/**
 *
 */
public class CpDeviceItemContainer extends CpItem {

    /**
     * @param parent
     * @param tag
     */
    public CpDeviceItemContainer(ICpItem parent, String tag) {
        super(parent, tag);
    }

    @Override
    protected ICpItem createChildItem(String tag) {
        switch (tag) {
        // device hierarchy
        case CmsisConstants.FAMILY_TAG:
        case CmsisConstants.SUBFAMILY_TAG:
        case CmsisConstants.DEVICE_TAG:
        case CmsisConstants.VARIANT_TAG:
            return new CpDeviceItem(this, tag);
        // device properties
        case CmsisConstants.DEBUG_TAG:
            return new CpDebug(this, tag);
        case CmsisConstants.DEBUGPORT_TAG:
            return new CpDebugPort(this, tag);
        case CmsisConstants.DEBUGVARS_TAG:
            return new CpDebugVars(this, tag);
        case CmsisConstants.DATAPATCH_TAG:
            return new CpDataPatch(this, tag);
        case CmsisConstants.SEQUENCE_TAG:
            return new CpSequence(this, tag);
        case CmsisConstants.CONTROL_TAG:
            return new CpSequenceControl(this, tag);
        case CmsisConstants.MEMORY_TAG:
            return new CpMemory(this, tag);
        case CmsisConstants.ALGORITHM_TAG:
            return new CpAlgorithm(this, tag);
        case CmsisConstants.TRACE_TAG:
            return new CpTrace(this, tag);

        // debug port properties
        case CmsisConstants.SWD:
        case CmsisConstants.JTAG:
        case CmsisConstants.CJTAG:
            return new CpDebugProtocol(this, tag);
        default:
            return new CpDeviceProperty(this, tag);
        }
    }

}
