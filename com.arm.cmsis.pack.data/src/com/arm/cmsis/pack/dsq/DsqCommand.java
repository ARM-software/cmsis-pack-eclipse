/**
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
 */
package com.arm.cmsis.pack.dsq;

import java.util.List;
import java.util.Map;

/**
 * Default implementation of ICommand
 */
public class DsqCommand extends DsqContext implements IDsqCommand {
    protected String name;

    protected List<Long> inputs;
    protected List<String> strings;

    protected long output = 0;

    protected int errorCode = 0;

    /**
     * DsqCommand constructor
     *
     * @param cmdName
     * @param inputs         list of Long arguments
     * @param strings        list of string arguments
     * @param predefinedVars list of predefined variables
     */
    public DsqCommand(String cmdName, final List<Long> inputs, final List<String> strings,
            Map<String, Long> predefinedVars) {
        super(predefinedVars);
        this.name = cmdName;
        this.inputs = inputs;
        this.strings = strings;
    }

    /**
     * DsqCommand constructor
     *
     * @param cmdName command name
     * @param inputs  list of Long arguments
     */
    public DsqCommand(String cmdName, final List<Long> inputs, Map<String, Long> predefinedVars) {
        this(cmdName, inputs, null, predefinedVars);
    }

    @Override
    public String getCommandName() {
        return this.name;
    }

    @Override
    public List<Long> getArguments() {
        return this.inputs;
    }

    @Override
    public long getOutput() {
        return this.output;
    }

    @Override
    public void setOutput(final long output) {
        this.output = output;
    }

    @Override
    public int getError() {
        return errorCode;
    }

    @Override
    public void setError(int errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public List<String> getStringArguments() {
        return strings;
    }

}
