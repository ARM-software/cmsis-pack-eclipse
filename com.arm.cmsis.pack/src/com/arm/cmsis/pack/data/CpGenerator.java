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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
	protected String constructName() {
		return constructId(); 
	}
	
	@Override
	public String getWorkingDir() {
		return getFirstChildText(CmsisConstants.WORKING_DIR_TAG);
	}

	@Override
	public String getGpdsc() {
		if(isGenerated()) {
			// the generated pack can only contain one generator for this file
			ICpPack pack = getPack();
			if(pack != null )
				return pack.getFileName();
		}
		ICpItem gpdscItem = getFirstChild(CmsisConstants.GPDSC_TAG);
		if(gpdscItem == null)
			return null;
		return gpdscItem.getName();
	}

	@Override
	public ICpItem getCommand(String type) {
		if(type == null || type.isEmpty())
			return null;
		ICpItem item = getFirstChild(type); 
		if(item == null) {
			// search deprecated  <command> item
			if(type.equals(CmsisConstants.EXE))
				return getFirstChild(CmsisConstants.COMMAND_TAG);
			return null;
		}
		if(type.equals(CmsisConstants.EXE)){
			Collection<? extends ICpItem> children = item.getChildren();
			if(children == null || children.isEmpty())
				return null;
			for(ICpItem cmd : children) {
				if(!cmd.getTag().equals(CmsisConstants.COMMAND_TAG))
					continue;
				if(!item.matchesHost())
					continue;
				return cmd;
			}
			return null; // no command available for running host
		}
		return item;
	}

	@Override
	public Collection<ICpItem> getArguments(String type) {
		List<ICpItem> arguments = new LinkedList<>();
		if(type == null || type.isEmpty()) {
			return arguments;
		}

		ICpItem item = getFirstChild(type);
		if(item == null && type.equals(CmsisConstants.EXE)) {
			item = getFirstChild(CmsisConstants.ARGUMENTS_TAG);
		}
		if(item == null){
			return arguments;
		}
		
		Collection<? extends ICpItem> children = item.getChildren();
		if(children == null || children.isEmpty())
			return arguments;

		for(ICpItem arg : children) {
			if(!arg.getTag().equals(CmsisConstants.ARGUMENT_TAG)) 
				continue;
			if(!arg.matchesHost())
				continue;
			arguments.add(arg);
		}
		return arguments;
	}

	@Override
	public Collection<String> getAvailableTypes() {
		Set<String> types = new HashSet<>();
		for(String launchType: CmsisConstants.LAUNCH_TYPES) {
			if(getCommand(launchType) != null) {
				types.add(launchType);
			}
		}
		return types;
	}
	
	
}
