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

package com.arm.cmsis.pack.ui.console;

import org.eclipse.ui.console.IConsoleFactory;

/**
 * Console factory to open a console via user interface
 */
public class RteConsoleFactory implements IConsoleFactory {

    @Override
    public void openConsole() {
        RteConsole rteConsole = RteConsole.openGlobalConsole();
        RteConsole.showConsole(rteConsole);
    }

}
