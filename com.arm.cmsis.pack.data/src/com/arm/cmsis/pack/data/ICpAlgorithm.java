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

/**
 *
 */
public interface ICpAlgorithm extends ICpDeviceProperty {

    /**
     * Returns absolute algorithm filename
     *
     * @return absolute algorithm filename
     */
    String getAlgorithmFile();

    /**
     * Returns available RAM size for executing the algorithm
     *
     * @return available RAM size as long value
     */
    long getRAMStart();

    /**
     * Returns the base address in RAM from where the algorithm is executed
     *
     * @return RAM base address as long value
     */
    long getRAMSize();

}
