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

import java.util.Collection;

/**
 * Convenience interface to access information under "debug" device property
 */
public interface ICpDebug extends ICpDeviceProperty {

    /**
     * Returns absolute SVD filename
     *
     * @return absolute SVD filename
     */
    String getSvdFile();

    /**
     * Returns collection of ICpDataPatch items
     *
     * @return collection of ICpDataPatch items
     */
    Collection<ICpDataPatch> getDataPacthes();

}
