/*******************************************************************************
* Copyright (c) 2017 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.zone.data;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpMemory;

public class CpMpuRegion extends CpZoneItem implements ICpMpuRegion {

	public CpMpuRegion(ICpItem parent, String tag) {
		super(parent, tag);
	}

	public CpMpuRegion(ICpMemory memory) {
		super(null, CmsisConstants.REGION);
		setAttributes(memory);
	}
	
	@Override
	public boolean appendMpuRegion(ICpMpuRegion region) {		
		if(region == null) 
			return false;
		
		if(!isAccessEqual(region))
			return false; //not the same access permissions
		
		if(getPrivilege().isPrivileged() != region.getPrivilege().isPrivileged())
			return false; //not the same privilege
		
		if(region.isShared()!= isShared())
			return false; // not the same shared
		
		if(region.isDma() != isDma())
			return false; // not the same Dma
		
		if(isROM() != region.isROM())
			return false; // not the same memory type		
				
		long start = region.getStart();
		if(start < 0)
			return false;
		
		long thisStop = getStop();
		if(thisStop + 1 != start)
			return false;

		Long size = getSize() + region.getSize();
		attributes().setAttributeHex(CmsisConstants.SIZE, size);
	
		String name = getName();
		if(!name.isEmpty()) {
			name +=", "; //$NON-NLS-1$
		}
		name += region.getName();
		setAttribute(CmsisConstants.NAME, name);
		
		return true;
	}	
	
}
