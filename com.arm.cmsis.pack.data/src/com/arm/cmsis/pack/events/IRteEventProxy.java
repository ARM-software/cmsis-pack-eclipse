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

package com.arm.cmsis.pack.events;

import com.arm.cmsis.pack.generic.IGenericListenerList;

/**
 * Interface that provides communication way from RTE model to clients (e.g.
 * GUI). <br>
 * The implementation is responsible for translating RTE model notifications
 * into events and deliver them to clients. <br>
 * RTE model itself can register only one proxy that in turn should maintain its
 * own listener list, or use other methods to delivering the events to clients.
 */
public interface IRteEventProxy extends IGenericListenerList<IRteEventListener, RteEvent>, IRteEventListener {

    @Override
    default void handle(RteEvent event) { // proxy method
        notifyListeners(event);
    }
}
