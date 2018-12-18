/*******************************************************************************
 * Copyright (c) 2016 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
