/*******************************************************************************
* Copyright (c) 2023 ARM Ltd. and others
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

package com.arm.cmsis.pack.parser;

import com.arm.cmsis.pack.enums.ESeverity;
import com.arm.cmsis.pack.error.CmsisError;

/**
 *
 */
public class CpYmlParserError extends CmsisError {

    private static final long serialVersionUID = 1L;

    public static final String Y401 = "Y401"; // Error //$NON-NLS-1$
    public static final String Y402 = "Y402"; //$NON-NLS-1$
    public static final String Y403 = "Y403"; //$NON-NLS-1$

    public CpYmlParserError(String file, String id, ESeverity severity, String message, Throwable e) {
        super(e, severity, id, message);
    }
}
