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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.CpStringsUI;
import com.arm.cmsis.pack.ui.preferences.CpUIPreferenceConstants;

/**
 * Console to display RTE messages from RteProject 
 *
 */
public class RteConsole extends MessageConsole implements IPropertyChangeListener {

	public static final String CONSOLE_TYPE = "com.arm.cmsis.pack.rte.console";	 //$NON-NLS-1$
	public static final String BASE_NAME = CpStringsUI.RteConsole_BaseName;
	public static final int OUTPUT = 0;
	public static final int INFO = 1;
	public static final int ERROR = 2;
	public static final int STREAM_COUNT = 3;
	
	private Map<Integer, MessageConsoleStream> fStreams = new HashMap<Integer, MessageConsoleStream>();
	
	public RteConsole(String name, ImageDescriptor imageDescriptor) {
		super(name, CONSOLE_TYPE, imageDescriptor, true);
		updateBackGround();
		CpPlugInUI.addPreferenceStoreListener(this);
		initStreams();
	}


	private void initStreams() {
		for(int i = 0;  i < STREAM_COUNT; i++) {
			getStream(i);
		}
	}

	@Override
	protected void dispose() {
		super.dispose();
		CpPlugInUI.removePreferenceStoreListener(this);
		for(MessageConsoleStream stream : fStreams.values())
		try {
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
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
		IPreferenceStore store = CpPlugInUI.getDefault().getPreferenceStore();
		RGB rgb = PreferenceConverter.getColor( store, CpUIPreferenceConstants.CONSOLE_BG_COLOR);
    	setBackground(new Color(Display.getCurrent(), rgb));
	}
	
	private MessageConsoleStream getStream(int streamType) {
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
		MessageConsoleStream stream = getStream(streamType);
		stream.println(msg);
	}

//	static public void output(final IProject project, final int streamType, final String message){
//		// console can be opened only by main GUI thread - use runnable
//		Display.getDefault().asyncExec(new Runnable() {
//			public void run() {
//				RteConsole rteConsole = openConsole(project);
//				if(rteConsole != null)
//					rteConsole.output(streamType, message);
//			}}); 
//	}
//	

	public void output(final String message){
		output(OUTPUT, message);
	}
	
	public void outputInfo( final String message){
		output(INFO, message);
	}

	public void outputError( final String message){
		output( ERROR, message);
	}

	
	/** 
	 * Opens RteConsole for given project 
	 * @param project IProject to open console for
	 * @return RteConsole
	 */
	public static RteConsole openConsole(IProject project) {
		String name = null;
		if(project != null) {
			name = project.getName();
		}
		return openConsole(name);
	}
	
	
	/** 
	 * Opens RteConsole for given project name 
	 * @param projectName name of the project to open console for
	 * @return RteConsole
	 */
	synchronized public static RteConsole openConsole(String projectName) 	{
		// add it if necessary
		String consoleName = BASE_NAME;
		if(projectName != null && !projectName.isEmpty() && !projectName.equals(BASE_NAME))
			consoleName += " [" + projectName + "]"; //$NON-NLS-1$ //$NON-NLS-2$
		
		RteConsole rteConsole = null;
		RteConsole rteBaseConsole = null;
		IConsole[] consoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
		if(consoles != null) {
			for (IConsole console : consoles) {
				if(console.getType() == null)
					continue;
				if(!console.getType().equals(CONSOLE_TYPE))
					continue;
				if(consoleName.equals(BASE_NAME)) {
					rteConsole = (RteConsole) console;
					break;
				}
				String name = console.getName();
				if(name ==null) 
					continue;
				if (name.equals(consoleName)) {
					rteConsole = (RteConsole) console;
					break;
				} else if(name.equals(BASE_NAME)) {
					rteBaseConsole = (RteConsole) console;
				} 
			}
		}
		if (rteConsole == null && rteBaseConsole!= null) {
			rteConsole = rteBaseConsole;
			if(!consoleName.equals(BASE_NAME))
				rteConsole.setName(consoleName);
		} else if (rteConsole == null) {
			ImageDescriptor imageDescriptor = CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_RTE_CONSOLE);
			rteConsole = new RteConsole(consoleName, imageDescriptor);
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { rteConsole });
		}
		
		if(CpPlugInUI.getDefault().getPreferenceStore().getBoolean(CpUIPreferenceConstants.CONSOLE_OPEN_ON_OUT))
			showConsole(rteConsole);
		return rteConsole;
	}

	synchronized public static void showConsole(final RteConsole console) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				ConsolePlugin.getDefault().getConsoleManager().showConsoleView(console);
			}}); 
	}
}
