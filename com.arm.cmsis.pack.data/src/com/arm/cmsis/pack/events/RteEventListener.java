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

/**
 * Listener to handle an RTE event
 */
public class RteEventListener implements IRteEventListener {

    protected IRteEventProxy fRteEventProxy = null;

    @Override
    public void setRteEventProxy(IRteEventProxy rteEventProxy) {
        if (rteEventProxy != null) {
            rteEventProxy.addListener(this);
        }
        fRteEventProxy = rteEventProxy;
    }

    @Override
    public IRteEventProxy getRteEventProxy() {
        return fRteEventProxy;
    }

    @Override
    public void handle(RteEvent event) {
        // default does nothing
    }

}
