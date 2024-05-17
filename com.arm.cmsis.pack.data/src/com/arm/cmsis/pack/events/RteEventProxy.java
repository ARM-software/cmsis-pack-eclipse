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

import com.arm.cmsis.pack.generic.GenericListenerList;

/**
 * Convenience base class implementing of IRteEventProxy interface
 */
public class RteEventProxy extends GenericListenerList<IRteEventListener, RteEvent> implements IRteEventProxy {

    @Override
    public IRteEventProxy getRteEventProxy() {
        return this;
    }

}
