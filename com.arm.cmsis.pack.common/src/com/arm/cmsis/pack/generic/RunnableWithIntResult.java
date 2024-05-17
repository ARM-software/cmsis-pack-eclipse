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

package com.arm.cmsis.pack.generic;

/**
 * An abstract convenience class implementing IRunnableWithResult interface for
 * Integer result. That can be useful for runnables displaying a dialog and
 * returning its result
 */
public abstract class RunnableWithIntResult extends RunnableWithResult<Integer> {

    /**
     * Default constructor assigns 0 to result
     */
    public RunnableWithIntResult() {
        result = 0;
    }

    /**
     * Constructor with initial (default) result value
     *
     * @param defaultResult to be returned if run() is not called or failed
     */
    public RunnableWithIntResult(Integer defaultResult) {
        super(defaultResult);
    }
}
