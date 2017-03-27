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

import java.util.HashMap;
import java.util.Map;

import com.arm.cmsis.pack.dsq.IDsqLogger;
import com.arm.cmsis.pack.project.IRteProject;

/**
 * Reference Logger for Debug Sequence
 */
public class SequenceLogger implements IDsqLogger {

	protected static Map<String, SequenceLogger> instances = new HashMap<>();
	private SequenceConsole seqConsole;
	private String indent = ""; //$NON-NLS-1$
	private final static String INDENT_STEP = "    "; //$NON-NLS-1$
	private boolean enabled = true;

	public static SequenceLogger getInstance(IRteProject rteProject) {
		String name = rteProject.getName();
		if (instances.get(name) == null) {
			instances.put(name, new SequenceLogger(name));
		} else {
			SequenceConsole.openConsole(name);
		}
		return instances.get(name);
	}

	protected SequenceLogger(String name) {
		seqConsole = SequenceConsole.openConsole(name);
	}

	@Override
	public void logSeqStart(String seqName) {
		if (!isEnabled()) {
			return;
		}
		seqConsole.output(indent + "Sequence '" + seqName + "' started."); //$NON-NLS-1$ //$NON-NLS-2$
		indent += INDENT_STEP;
	}

	@Override
	public void logSeqEnd(String seqName) {
		if (!isEnabled()) {
			return;
		}
		indent = indent.substring(INDENT_STEP.length());
		seqConsole.output(indent + "Sequence '" + seqName + "' ended."); //$NON-NLS-1$ //$NON-NLS-2$
		if (indent.isEmpty()) {
			seqConsole.output("---------------------------------------------------------------------"); //$NON-NLS-1$
		}
	}

	@Override
	public void logBlockStart(boolean isAtomic, String blockInfo) {
		if (!isEnabled()) {
			return;
		}
		String message = indent + "Block started."; //$NON-NLS-1$
		if (isAtomic) {
			message += "(Atomic)"; //$NON-NLS-1$
		}
		if (blockInfo != null) {
			message += " Info: " + blockInfo; //$NON-NLS-1$
		}
		seqConsole.output(message);
		indent += INDENT_STEP;
	}

	@Override
	public void logBlockEnd() {
		if (!isEnabled()) {
			return;
		}
		indent = indent.substring(INDENT_STEP.length());
		seqConsole.output(indent + "Block ended."); //$NON-NLS-1$
	}

	@Override
	public void logContorlStart(String controlInfo) {
		if (!isEnabled()) {
			return;
		}
		String message = indent + "Control started."; //$NON-NLS-1$
		if (controlInfo != null) {
			message += " Info: " + controlInfo; //$NON-NLS-1$
		}
		seqConsole.output(message);
		indent += INDENT_STEP;
	}

	@Override
	public void logControlEnd() {
		if (!isEnabled()) {
			return;
		}
		indent = indent.substring(INDENT_STEP.length());
		seqConsole.output(indent + "Control ended."); //$NON-NLS-1$
	}

	@Override
	public void logStatement(String stmt, Long result, int errorCode) {
		if (!isEnabled()) {
			return;
		}
		String message = indent + stmt + " -> " + result; //$NON-NLS-1$
		seqConsole.output(message);
	}

	@Override
	public void logIfStatement(String stmt, Long result, int errorCode) {
		if (!isEnabled()) {
			return;
		}
		String message = indent + "IF: " + stmt + " -> " + result; //$NON-NLS-1$ //$NON-NLS-2$
		seqConsole.output(message);
	}

	@Override
	public void logWhileStatement(String stmt, Long result, int errorCode) {
		if (!isEnabled()) {
			return;
		}
		String message = indent + "WHILE: " + stmt + " -> " + result; //$NON-NLS-1$ //$NON-NLS-2$
		seqConsole.output(message);
	}

	@Override
	public void setEnabled(boolean enable) {
		enabled = enable;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

}
