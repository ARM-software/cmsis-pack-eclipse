/*******************************************************************************
 * Copyright (c) 2014 Liviu Ionescu.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Liviu Ionescu - initial implementation.
 *     ARM Ltd and ARM Germany GmbH - application-specific implementation
 *******************************************************************************/

package com.arm.cmsis.pack.installer.console;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import com.arm.cmsis.pack.ICpPackInstaller.ConsoleColor;

public class ConsoleStream {

	public static final String CONSOLE_NAME = "CMSIS Pack Manager console"; //$NON-NLS-1$

	private static Map<ConsoleColor, MessageConsoleStream> fStreams = new HashMap<>();

	synchronized public static MessageConsoleStream getConsoleOut(ConsoleColor color) {
		MessageConsoleStream stream = fStreams.get(color);
		if (stream == null) {
			stream = findConsole(CONSOLE_NAME).newMessageStream();
			fStreams.put(color, stream);
		}
		return stream;
	}

	synchronized public static void dispose() {
		for(MessageConsoleStream stream : fStreams.values()) {
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		fStreams.clear();
	}



	public static MessageConsole findConsole(String name) {

		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager consoleManager = plugin.getConsoleManager();
		IConsole[] existing = consoleManager.getConsoles();
		for (int i = 0; i < existing.length; i++) {
			if (name.equals(existing[i].getName())) {
				return (MessageConsole) existing[i];
			}
		}

		// no console found, so create a new one
		MessageConsole console = new MessageConsole(name, null);
		consoleManager.addConsoles(new IConsole[] { console });

		return console;
	}
}
