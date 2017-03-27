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

package com.arm.cmsis.pack.dsq;

import java.util.List;

/**
 * Describes the command to be executed by {@link IDsqClient}
 */
public interface IDsqCommand extends IDsqContext {

	final static String DSQ_READ_8 = "Read8"; //$NON-NLS-1$
	final static String DSQ_READ_16 = "Read16"; //$NON-NLS-1$
	final static String DSQ_READ_32 = "Read32"; //$NON-NLS-1$
	final static String DSQ_READ_64 = "Read64"; //$NON-NLS-1$
	final static String DSQ_READ_AP = "ReadAP"; //$NON-NLS-1$
	final static String DSQ_READ_DP = "ReadDP"; //$NON-NLS-1$
	final static String DSQ_WRITE_8 = "Write8"; //$NON-NLS-1$
	final static String DSQ_WRITE_16 = "Write16"; //$NON-NLS-1$
	final static String DSQ_WRITE_32 = "Write32"; //$NON-NLS-1$
	final static String DSQ_WRITE_64 = "Write64"; //$NON-NLS-1$
	final static String DSQ_WRITE_AP = "WriteAP"; //$NON-NLS-1$
	final static String DSQ_WRITE_DP = "WriteDP"; //$NON-NLS-1$
	final static String DSQ_DAP_DELAY = "DapDelay"; //$NON-NLS-1$
	final static String DSQ_DAP_WRITE_ABORT = "DapWriteABORT"; //$NON-NLS-1$
	final static String DSQ_DAP_SWJ_PINS = "DapSwjPins"; //$NON-NLS-1$
	final static String DSQ_DAP_SWJ_CLOCK = "DapSwjClock"; //$NON-NLS-1$
	final static String DSQ_DAP_SWJ_SEQUENCE = "DapSwjSequence"; //$NON-NLS-1$
	final static String DSQ_DAP_JTAG_SEQUENCE = "DapJtagSequence"; //$NON-NLS-1$
	final static String DSQ_LOAD_DEBUG_INFO = "LoadDebugInfo"; //$NON-NLS-1$
	final static String DSQ_MESSAGE = "Message"; //$NON-NLS-1$

	/**
	 * Get the command name
	 * @return Command Name
	 */
	String getCommandName();

	/**
	 * Get the list of Long command arguments
	 * @return a list of Long command arguments
	 */
	List<Long> getArguments();

	/**
	 * Get the list of String command arguments
	 * @return a list of String command arguments
	 */
	List<String> getStringArguments();

	/**
	 * Get the execution output
	 * @return Command execution output
	 */
	long getOutput();

	/**
	 * Set the output
	 * @param output Command's output
	 */
	void setOutput(final long output);

	/**
	 * Get the error code of the command
	 * @return Error code
	 */
	int getError();

	/**
	 * Set the error code
	 * @param error The error code
	 */
	void setError(int error);
}
