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

package com.arm.cmsis.pack.dsq;

import com.arm.cmsis.pack.info.ICpDeviceInfo;

/**
 * Interface of Debug Sequence Engine Factory to create Debug Sequence Engine to
 * execute sequences
 */
public interface IDsqEngineFactory {

    /**
     * produce an IDsqEngine
     *
     * @param di  device information
     * @param dce debug sequence command executor
     * @param log a logger to generate log while executing sequences
     * @return a Debug Sequence Engine interface, or null if no engine is defined
     */
    IDsqEngine create(ICpDeviceInfo di, IDsqClient dce, IDsqLogger log);

}
