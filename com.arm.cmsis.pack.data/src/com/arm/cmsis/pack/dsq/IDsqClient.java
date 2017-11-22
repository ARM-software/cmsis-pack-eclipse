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
 * Interface to execute {@link IDsqCommand}
 */
public interface IDsqClient extends IDsqApiVersion {

	final static long QUERY_VALUE_TYPE = 0x10000; // used as the starting value for QueryValue's type

	/**
	 * Execute an atomic block with a list of commands.
	 * Set the execution results into the commands with {@link IDsqCommand#setOutput(long)}
	 * @param commands A list of commands
	 * @param atomic True if this list of commands must be executed atomically
	 * @throws DsqException
	 */
	void execute(final List<IDsqCommand> commands, boolean atomic) throws DsqException;

	/**
	 * Query user input. The sequence execution stalls depending on the used type.
	 * If the debugger runs in a batch mode, this function returns the value default.
	 * @param type Query type. Can be one of:
	 * <dl>
	 * 	<dd>0 : Query_Ok, displays an informative message which has to be confirmed by the user. This type allows the result OK.
	 * 	<dd>1 : Query_YesNo, displays a query with the allowed results Yes and No.
	 * 	<dd>2 : Query_YesNoCancel, displays a query with the allowed results Yes, No, and Cancel.
	 * 	<dd>3 : Query_OkCancel, displays a query with the allowed results OK and Cancel.
	 * </dl>
	 * @param message A constant string with the query message to display. It must not be an expression and it must be enclosed by quotes.
	 * @param defaultValue The default value to return if the debugger runs in batch mode. See Return Values for a list of allowed values.
	 * @return The result of the query. The user input maps to the following numbers:
	 * <dl>
	 * 	<dd>Error : 0
	 * 	<dd>OK : 1
	 * 	<dd>Cancel : 2
	 * 	<dd>Yes : 3
	 *  <dd>No : 4
	 * </dl>
	 */
	long query(final long type, final String message, final long defaultValue) throws DsqException;

}
