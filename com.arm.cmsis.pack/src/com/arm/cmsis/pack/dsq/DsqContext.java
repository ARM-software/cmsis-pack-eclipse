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

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link IDsqContext}
 */
public class DsqContext implements IDsqContext {

	protected Map<String, Long> predefinedVariables = new HashMap<>();

	/**
	 * Default Constructor
	 */
	public DsqContext() {
	}

	/**
	 * Constructor that copies provided predefined variables from the supplied map
	 * @param variables a map of predefined variables to values
	 */
	public DsqContext(Map<String, Long> variables) {
		predefinedVariables.putAll(variables);
	}

	@Override
	public Long getPredefinedVariableValue(String name) {
		return predefinedVariables.get(name);
	}

	@Override
	public void setPredefinedVariableValue(String name, long value) {
		if (name == null) {
			return;
		}
		predefinedVariables.put(name, value);
	}

}
