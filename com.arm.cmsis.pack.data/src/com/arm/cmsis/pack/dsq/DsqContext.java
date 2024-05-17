/*******************************************************************************
* Copyright (c) 2021 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
     *
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
