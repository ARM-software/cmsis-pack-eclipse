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

package com.arm.cmsis.pack.data;

import java.util.Collection;

import com.arm.cmsis.pack.common.CmsisConstants;

/**
 * Default implementation of ICpGenerator interface 
 */
public class CpGenerator extends CpItem implements ICpGenerator {

	public CpGenerator(ICpItem parent) {
		super(parent);
	}

	public CpGenerator(ICpItem parent, String tag) {
		super(parent, tag);
	}

	@Override
	public String getCommand() {
		return getFirstChildText(CmsisConstants.COMMAND_TAG);
	}

	@Override
	public String getWorkingDir() {
		return getFirstChildText(CmsisConstants.WORKING_DIR_TAG);
	}

	@Override
	public String getGpdsc() {
		return getFirstChildText(CmsisConstants.GPDSC_TAG);
	}

	@Override
	public Collection<? extends ICpItem> getArguments() {
		return getGrandChildren(CmsisConstants.ARGUMENTS_TAG);
	}


	@Override
	public String getExpandedCommandLine(ICpStringExpander expander) {
		String cmd = getCommand();
		if(cmd == null)
			return null;
		cmd = expander.expand(cmd);
		
		String fullCmd = cmd;
		return fullCmd;
	}

	@Override
	public String getExpandedGpdsc(ICpStringExpander expander) {
		String raw = getGpdsc();
		return expander.expand(raw);
	}

	@Override
	public String getExpandedWorkingDir(ICpStringExpander expander) {
		String raw = getWorkingDir();
		if(raw == null )
			return null;
		return expander.expand(raw);
	}

	protected String expand(String raw) {
		if(raw == null )
			return null;
		String expanded = raw;
		return expanded;  // TODO: expand
	}
}
