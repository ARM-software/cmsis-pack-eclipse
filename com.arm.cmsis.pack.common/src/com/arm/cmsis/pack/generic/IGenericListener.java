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

package com.arm.cmsis.pack.generic;

/**
 * Generic listener interface
 *
 * @param <E> type of event to handle
 */
public interface IGenericListener<E> {

    /**
     * Handles event
     *
     * @param event event to handle
     */
    void handle(E event);

}
