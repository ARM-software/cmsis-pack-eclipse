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

package com.arm.cmsis.pack.dsq;

/**
 * Default exception for Debug Sequence Engine
 */
public class DsqException extends RuntimeException {

    protected static final long serialVersionUID = 3964836352901582595L;

    public DsqException() {
    }

    /**
     * @param message
     */
    public DsqException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public DsqException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public DsqException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public DsqException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
