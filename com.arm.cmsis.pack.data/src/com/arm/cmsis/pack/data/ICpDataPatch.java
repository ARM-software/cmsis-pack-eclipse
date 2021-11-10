/*******************************************************************************
* Copyright (c) 2021 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.data;

import com.arm.cmsis.pack.enums.EDataPatchAccessType;

/**
 * Convenience interface to access information under "datapatch" device debug
 * property
 */
public interface ICpDataPatch extends ICpDeviceProperty {

    /**
     * Returns data patch address
     *
     * @return data patch address as long
     */
    long getPatchAddress();

    /**
     * Returns data patch value
     *
     * @return data patch value as long
     */
    long getPatchValue();

    /**
     * Returns data patch mask (default is unsigned long 0xFFFFFFFFFFFFFFFF )
     *
     * @return data patch mask as long
     */
    long getPatchMask();

    /**
     * Data patch information string
     *
     * @return patch information string
     */
    EDataPatchAccessType getAccessType();

}
