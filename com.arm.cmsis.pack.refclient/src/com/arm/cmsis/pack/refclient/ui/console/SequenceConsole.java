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

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.arm.cmsis.pack.refclient.Activator;
import com.arm.cmsis.pack.refclient.ui.SequenceView;

/**
 * Reference Console for Debug Sequences
 */
public class SequenceConsole extends MessageConsole {

	public static final String CONSOLE_TYPE = "com.arm.cmsis.pack.refclient.sequence.console";	 //$NON-NLS-1$
	public static final String BASE_NAME = "CMSIS Sequence console"; //$NON-NLS-1$
	public static final String ICON_SEQ_CONSOLE = "icons/launch.gif"; //$NON-NLS-1$

	MessageConsoleStream fStream;

	public SequenceConsole(String name, ImageDescriptor imageDescriptor) {
		super(name, CONSOLE_TYPE, imageDescriptor, true);
		initStream();
	}

	private void initStream() {
		fStream = newMessageStream();
	}

	@Override
	protected void dispose() {
		super.dispose();
		try {
			fStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void output(final String message){
		Display.getDefault().asyncExec(() -> fStream.println(message));
	}

	public static SequenceConsole openConsole(IProject project) {
		String name = null;
		if(project != null) {
			name = project.getName();
		}
		return openConsole(name);
	}

	synchronized public static SequenceConsole openConsole(String projectName) 	{
		// add it if necessary
		String consoleName = BASE_NAME;
		if(projectName != null && !projectName.isEmpty() && !projectName.equals(BASE_NAME)) {
			consoleName += " [" + projectName + "]"; //$NON-NLS-1$ //$NON-NLS-2$
		}

		SequenceConsole seqConsole = null;
		SequenceConsole seqBaseConsole = null;
		IConsole[] consoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
		if(consoles != null) {
			for (IConsole console : consoles) {
				if(console.getType() == null) {
					continue;
				}
				if(!console.getType().equals(CONSOLE_TYPE)) {
					continue;
				}
				if(consoleName.equals(BASE_NAME)) {
					seqConsole = (SequenceConsole) console;
					break;
				}
				String name = console.getName();
				if(name ==null) {
					continue;
				}
				if (name.equals(consoleName)) {
					seqConsole = (SequenceConsole) console;
					break;
				} else if(name.equals(BASE_NAME)) {
					seqBaseConsole = (SequenceConsole) console;
				}
			}
		}
		if (seqConsole == null && seqBaseConsole!= null) {
			seqConsole = seqBaseConsole;
			if(!consoleName.equals(BASE_NAME)) {
				seqConsole.setName(consoleName);
			}
		} else if (seqConsole == null) {
			ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, ICON_SEQ_CONSOLE);
			seqConsole = new SequenceConsole(consoleName, imageDescriptor);
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { seqConsole });
		}

		IViewPart seqView = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(SequenceView.ID);
		if(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().isPartVisible(seqView)) {
			showConsole(seqConsole);
		}
		return seqConsole;
	}

	synchronized public static void showConsole(final SequenceConsole console) {
		Display.getDefault().asyncExec(() ->
		ConsolePlugin.getDefault().getConsoleManager().showConsoleView(console)
				);
	}

}
