/*******************************************************************************
* Copyright (c) 2015 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.build.settings;

import java.util.Map;
import java.util.Map.Entry;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.generic.IAttributes;

/**
 * Default implementation of IMemorySettings interface
 */
public class MemorySettings implements IMemorySettings {

	protected Map<String, IAttributes> fRegions;
	protected String fStartupId = ""; //$NON-NLS-1$

	public MemorySettings() {
	}
	
	
	public MemorySettings(Map<String, IAttributes> regions) {
		setRegions(regions);
	}

	@Override
	public Map<String, IAttributes> getRegions() {
		return fRegions;
	}

	@Override
	public void setRegions(Map<String, IAttributes> regions) {
		fRegions = regions;
		fStartupId = ""; //$NON-NLS-1$
		if(fRegions == null || fRegions.isEmpty()) 
			return;
		for(Entry<String, IAttributes> e : fRegions.entrySet()){
			String id = e.getKey(); 
			IAttributes a = e.getValue();
			if(a.getAttributeAsBoolean(CmsisConstants.STARTUP, false)){
				fStartupId = id;
			} 
		}
	}

	@Override
	public IAttributes getRegion(String id) {
		if(fRegions != null && id != null)
			return fRegions.get(id);
		return null;
	}

	@Override
	public String getStartupRegionId() {
		return fStartupId;
	}

}
