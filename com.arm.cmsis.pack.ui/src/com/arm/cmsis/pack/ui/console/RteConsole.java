/*******************************************************************************
 * Copyright (c) 2015 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/

package com.arm.cmsis.pack.ui.console;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IBuildConsoleManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.events.IRteEventListener;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.CpStringsUI;
import com.arm.cmsis.pack.ui.preferences.CpUIPreferenceConstants;

/**
 * Console to display RTE messages from RteProject
 *
 */
public class RteConsole extends MessageConsole implements IPropertyChangeListener, IRteEventListener {

	public static final String CONSOLE_TYPE = "com.arm.cmsis.pack.rte.console";	 //$NON-NLS-1$
	public static final String BASE_NAME = CpStringsUI.RteConsole_BaseName;
	public static final String GLOBAL_NAME = CpStringsUI.RteConsole_GlobalName;
	public static final int OUTPUT = 0;
	public static final int INFO = 1;
	public static final int WARNING = 2;
	public static final int ERROR = 3;
	public static final int STREAM_COUNT = 4;
	public boolean redirectToCDT = false;
	protected IProject fProject = null;

	private Map<Integer, MessageConsoleStream> fStreams = new HashMap<Integer, MessageConsoleStream>();

	public RteConsole(String name, ImageDescriptor imageDescriptor) {
		super(name, CONSOLE_TYPE, imageDescriptor, true);
		updateBackGround();
		CpPlugInUI.addPreferenceStoreListener(this);
		initStreams();
	}

	public RteConsole(String name, IProject project) {
		super(name, null);
		fProject = project;
		redirectToCDT = project != null ? true : false;
	}
	
	public boolean isRedirectToCDT() {
		return redirectToCDT && fProject != null;
	}
	

	private void initStreams() {
		asyncExec(() -> {
			for(int i = 0;  i < STREAM_COUNT; i++) {
				getStream(i);
			}
		});
	}

	@Override
	protected void dispose() {
		super.dispose();
		CpPlugInUI.removePreferenceStoreListener(this);
		CpPlugIn.removeRteListener(this);
		for(MessageConsoleStream stream : fStreams.values()) {
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		fStreams.clear();
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if(!property.startsWith(CpUIPreferenceConstants.CONSOLE_PREFIX)){
			return;
		}
		if(property.startsWith(CpUIPreferenceConstants.CONSOLE_COLOR_PREFIX)) {
			int streamType = getStreamType(property);
			MessageConsoleStream stream = fStreams.get(streamType);
			if(stream != null) {
				updateStreamColor(stream, property);
			}
		} else 	if(property.equals(CpUIPreferenceConstants.CONSOLE_OPEN_ON_OUT)) {
			IPreferenceStore store = CpPlugInUI.getDefault().getPreferenceStore();
			boolean activateOnWrite = store.getBoolean(CpUIPreferenceConstants.CONSOLE_OPEN_ON_OUT);
			for(MessageConsoleStream stream : fStreams.values()) {
				stream.setActivateOnWrite(activateOnWrite);
			}
		} else if (property.equals(CpUIPreferenceConstants.CONSOLE_BG_COLOR)) {
			updateBackGround();
		}
	}

	private void updateBackGround() {
		asyncExec(() -> {
			IPreferenceStore store = CpPlugInUI.getDefault().getPreferenceStore();
			RGB rgb = PreferenceConverter.getColor( store, CpUIPreferenceConstants.CONSOLE_BG_COLOR);
			setBackground(new Color(Display.getCurrent(), rgb));
		});
	}

	MessageConsoleStream getStream(int streamType) {
		MessageConsoleStream stream = fStreams.get(streamType);
		if(stream == null) {
			stream = newMessageStream();
			initStream(stream, streamType);
			fStreams.put(streamType, stream);
		}
		return stream;
	}

	private void initStream(MessageConsoleStream stream, int streamType) {
		IPreferenceStore store = CpPlugInUI.getDefault().getPreferenceStore();
		boolean activateOnWrite = store.getBoolean(CpUIPreferenceConstants.CONSOLE_OPEN_ON_OUT);
		stream.setActivateOnWrite(activateOnWrite);
		updateStreamColor(stream, getColorPreferenceConstant(streamType));
	}

	private void updateStreamColor(MessageConsoleStream stream, String preferenceConstant) {
		stream.setColor(new Color(Display.getCurrent(), getStreamColor(preferenceConstant)));
	}

	private RGB getStreamColor(String preferenceConstant) {
		IPreferenceStore store = CpPlugInUI.getDefault().getPreferenceStore();
		return PreferenceConverter.getColor( store, preferenceConstant);
	}

	private String getColorPreferenceConstant(int streamType) {
		switch(streamType) {
		case INFO:
			return CpUIPreferenceConstants.CONSOLE_INFO_COLOR;
		case WARNING:
			return CpUIPreferenceConstants.CONSOLE_WARNING_COLOR;
		case ERROR:
			return  CpUIPreferenceConstants.CONSOLE_ERROR_COLOR;
		case OUTPUT:
		default:
			return CpUIPreferenceConstants.CONSOLE_OUT_COLOR;
		}
	}

	private int getStreamType(String preferenceConstant) {
		switch(preferenceConstant) {
		case CpUIPreferenceConstants.CONSOLE_INFO_COLOR:
			return INFO;
		case CpUIPreferenceConstants.CONSOLE_WARNING_COLOR:
			return WARNING;
		case CpUIPreferenceConstants.CONSOLE_ERROR_COLOR:
			return ERROR;
		case CpUIPreferenceConstants.CONSOLE_OUT_COLOR:
		default:
			return OUTPUT;
		}
	}

	/**
	 * Outputs the message to specified console stream
	 * @param streamType stream type: OUTPUT, INFO, ERROR
	 * @param msg message to output
	 */
	public void output(int streamType, String msg) {
		if(!PlatformUI.isWorkbenchRunning()) {
			PrintStream stream;
			if(streamType == ERROR || streamType == WARNING)
				stream = System.err;
			else
				stream = System.out;
			stream.println(msg);
			return;
		}
		
		if (isRedirectToCDT()) {
			writeToCDTConsole(streamType, msg + '\n', fProject);
		} else {
			MessageConsoleStream stream = getStream(streamType);
			stream.println(msg);
		}
	}

	public void output(final String message) {
		output(OUTPUT, message);
	}

	public void outputInfo(final String message) {
		output(INFO, message);
	}

	public void outputInfo(final String message, IProject project) {
		output(INFO, message);
	}

	public void outputWarning(final String message) {
		output(WARNING, message);
	}

	public void outputError(final String message) {
		output(ERROR, message);
	}

	public static void writeToCDTConsole(int streamType, String msg, IProject project) {
		IBuildConsoleManager manager = CUIPlugin.getDefault().getConsoleManager();
		if (manager == null) {
			return;
		}

		org.eclipse.cdt.core.resources.IConsole console = manager.getConsole(project);
		if (console == null) {
			return;
		}

		ConsoleOutputStream infoStream = null;
		try {
			switch (streamType) {
			case INFO:
				infoStream = console.getInfoStream();
				break;
			case ERROR:
				infoStream = console.getErrorStream();
				break;
			case WARNING:				
			case OUTPUT:
			default:
				infoStream = console.getOutputStream();
				break;
			}
			infoStream.write(msg.getBytes());
		} catch (IOException | CoreException e) {
		} finally {
			if (infoStream != null) {
				try {
					infoStream.close();
				} catch (IOException exception) {
					// Can't do much about it.
				}
			}
		}
	}


	/**
	 * Opens RteConsole for given project
	 * @param project IProject to open console for
	 * @return RteConsole
	 */
	public static RteConsole openConsole(IProject project) {
		return openConsole(GLOBAL_NAME, project);
	}


	/**
	 * Opens RteConsole for given project name
	 * @param consoleName name of console to open console for
	 * @return RteConsole
	 */
	synchronized protected static RteConsole openConsole(String consoleName, IProject project) 	{
		if(!PlatformUI.isWorkbenchRunning()) {
			return new RteConsole(BASE_NAME, project); // will output to stdout
		}
		if(consoleName == null)
			consoleName = GLOBAL_NAME;
		if(project != null)	{
			if (CpPlugInUI.getDefault().getPreferenceStore()
					.getBoolean(CpUIPreferenceConstants.CONSOLE_PRINT_IN_CDT)) {
				return new RteConsole(BASE_NAME, project); //simple console just for redirection to CDT 
			}
		}		
		
		RteConsole rteConsole = null;
		IConsole[] consoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
		if(consoles != null) {
			for (IConsole console : consoles) {
				if(!CONSOLE_TYPE.equals(console.getType())) {
					continue;
				}
				String name = console.getName();
				if (consoleName.equals(name)) {
					rteConsole = (RteConsole) console;
					break;
				}
			}
		}
		if (rteConsole == null) {
			ImageDescriptor imageDescriptor = CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_RTE_CONSOLE);
			rteConsole = new RteConsole(consoleName, imageDescriptor);
			if(GLOBAL_NAME.equals(consoleName)) {
				CpPlugIn.addRteListener(rteConsole);
			}
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { rteConsole });
		}

		if(CpPlugInUI.getDefault().getPreferenceStore().getBoolean(CpUIPreferenceConstants.CONSOLE_OPEN_ON_OUT)) {
			showConsole(rteConsole);
		}
		return rteConsole;
	}

	synchronized public static RteConsole openGlobalConsole() {
		return openConsole(GLOBAL_NAME, null);
	}

	synchronized public static void showConsole(final RteConsole console) {
		asyncExec(() -> ConsolePlugin.getDefault().getConsoleManager().showConsoleView(console));
	}

	protected static void asyncExec(Runnable runnable) {
		if(Display.getDefault() != null) {
			Display.getDefault().asyncExec(runnable);
		}
	}


	@Override
	public void handle(RteEvent event) {
		String topic = event.getTopic();
		final int type;
		switch (topic) {
		case RteEvent.PRINT :
		case RteEvent.PRINT_OUTPUT :
			type = OUTPUT;
			break;
		case RteEvent.PRINT_INFO:
			type = INFO;
			break;
		case RteEvent.PRINT_WARNING:
			type = WARNING;
			break;
		case RteEvent.PRINT_ERROR:
		case RteEvent.GPDSC_LAUNCH_ERROR:			
			type = ERROR;
			break;
		default :
			return;
		}
		String message = (String) event.getData();
		if(PlatformUI.isWorkbenchRunning()) {
			asyncExec(() -> output(type, message));
		} else {
			 output(type, message);
		}
	}
}
