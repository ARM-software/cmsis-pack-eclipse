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

package com.arm.cmsis.pack.refclient.ui.console;

import org.eclipse.ui.console.IConsoleFactory;

/**
 * Factory for Debug Sequence Console
 */
public class SequenceConsoleFactory implements IConsoleFactory {

    @Override
    public void openConsole() {
        SequenceConsole seqConsole = SequenceConsole.openConsole(SequenceConsole.BASE_NAME);
        SequenceConsole.showConsole(seqConsole);
    }

}
