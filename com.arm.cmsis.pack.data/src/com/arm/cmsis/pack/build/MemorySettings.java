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

package com.arm.cmsis.pack.build;

import java.util.Map;
import java.util.Map.Entry;

import com.arm.cmsis.pack.data.ICpMemory;

/**
 * Default implementation of IMemorySettings interface
 */
public class MemorySettings implements IMemorySettings {

	protected Map<String, ICpMemory> fRegions;
	protected String fStartupId = ""; //$NON-NLS-1$

	public MemorySettings() {
	}
	
	
	public MemorySettings(Map<String, ICpMemory> regions) {
		setRegions(regions);
	}

	@Override
	public Map<String, ICpMemory> getRegions() {
		return fRegions;
	}

	@Override
	public void setRegions(Map<String, ICpMemory> regions) {
		fRegions = regions;
		fStartupId = ""; //$NON-NLS-1$
		if(fRegions == null || fRegions.isEmpty()) 
			return;
		for(Entry<String, ICpMemory> e : fRegions.entrySet()){
			String id = e.getKey(); 
			ICpMemory m = e.getValue();
			if(m.isStartup()){
				fStartupId = id;
			} 
		}
	}

	@Override
	public ICpMemory getRegion(String id) {
		if(fRegions != null && id != null)
			return fRegions.get(id);
		return null;
	}

	@Override
	public String getStartupRegionId() {
		return fStartupId;
	}

}
