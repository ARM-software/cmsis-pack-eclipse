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

import com.arm.cmsis.pack.enums.ESequenceControlType;

/**
 *
 */
public interface ICpSequenceControl extends ICpDeviceProperty {

    /**
     * Returns Control type
     *
     * @return ESequenceControlType.WHILE or ESequenceControlType.IF (default)
     */
    ESequenceControlType getControlType();

    /**
     * Returns control expression
     *
     * @return control expression string
     */
    String getExpression();

    /**
     * Returns timeout (default is 0)
     *
     * @return timeout as long value
     */
    long getTimeout();

}
