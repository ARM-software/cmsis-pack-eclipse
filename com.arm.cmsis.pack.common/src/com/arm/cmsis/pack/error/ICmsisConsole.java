/*******************************************************************************
* Copyright (c) 2019 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.error;

import java.io.PrintStream;
import java.util.Collection;

import com.arm.cmsis.pack.enums.ESeverity;


/**
 *  Interface for Cmsis Consolse
 */
public interface ICmsisConsole {
	public static final int OUTPUT = 0;
	public static final int INFO = 1;
	public static final int WARNING = 2;
	public static final int ERROR = 3;
	public static final int STREAM_COUNT = 4;

	/**
	 * Outputs the message to specified console stream
	 * @param streamType stream type: OUTPUT, INFO, WARNING, ERROR
	 * @param msg message to output
	 */
	default void output(int streamType, String msg) {
		// default implementation uses standard IO streams 
		PrintStream stream;
		if(streamType == ERROR || streamType == WARNING)
			stream = System.err;
		else
			stream = System.out;
		stream.println(msg);
	}


	/**
	 * Outputs collection of CmsisError messages 
	 * @param errors collection of CmsisError messages
	 */
	default void outputErrors(final Collection<CmsisError> errors) {
		if(errors == null || errors.isEmpty())
			return;
		for(CmsisError err: errors) {
			outputError(err);
		}	
	}
	
	/**
	 * Outputs CmsisError 
	 * @param error CmsisError
	 */
	default void outputError(final CmsisError error) {
		if(error == null)
			return;
		int streamType = toStreamType(error.getSeverity());
		String message = error.toString(); 
		output(streamType, message);
	}

	
	/**
	 * Outputs message
	 * @param message message string
	 */
	default void output(final String message) {
		output(OUTPUT, message);
	}

	/**
	 * Outputs informational message
	 * @param message message string
	 */
	default void outputInfo(final String message) {
		output(INFO, message);
	}


	/**
	 * Outputs warning
	 * @param message warning string
	 */
	default void outputWarning(final String message) {
		output(WARNING, message);
	}

	/**
	 * Outputs error 
	 * @param message error string
	 */
	default void outputError(final String message) {
		output(ERROR, message);
	}

	/**
	 * Converts ESeverity value to integer stream type: OUTPUT, INFO, WARNING, ERROR    
	 * @param severity ESeverity to convert
	 * @return integer stream type  
	 */
	public static int toStreamType(ESeverity severity) {
		switch(severity) {
		case Error:
		case FatalError:
			return ERROR;
		case Info:
			return INFO;
		case Warning:
			return WARNING;
		case None:
		default:
			break;
		}
		return OUTPUT;
	}

	
}
