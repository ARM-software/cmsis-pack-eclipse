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

/**
 * Provides execution context for {@link IDsqSequence} or {@link IDsqCommand}
 */
public interface IDsqContext {

	static final String AP = "__ap"; //$NON-NLS-1$
	static final String DP = "__dp"; //$NON-NLS-1$
	static final String PROTOCOL = "__protocol"; //$NON-NLS-1$
	static final String CONNECTION = "__connection"; //$NON-NLS-1$
	static final String TRACEOUT = "__traceout"; //$NON-NLS-1$
	static final String ERRORCONTROL = "__errorcontrol"; //$NON-NLS-1$

	/**
	 * Get the value of pre-defined variables
	 * @param name Name of the pre-defined variable
	 * @return The value of the pre-defined variable, null if it is not defined
	 */
	Long getPredefinedVariableValue(String name);

	/**
	 * Set the value of pre-defined variables
	 * @param name Name of the pre-defined variable
	 * @param value Value of the pre-defined variable
	 */
	void setPredefinedVariableValue(String name, long value);

}
