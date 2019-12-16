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

import java.util.Collection;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpMemory;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.permissions.IMemoryPermissions;
import com.arm.cmsis.pack.permissions.MemoryPermissions;

/**
 * 
 */
public class CpMemoryRegion extends CpMemoryBlock implements ICpMemoryRegion {
	
	protected IMemoryPermissions fOriginalAttributesAndPermissions = null; // IMemoryPermissions is derived from attributed item 
	protected PhysicalMemoryRegion fPhysicalRegion = null;
	
	/**
	 * XML constructor
	 * @param parent parent ICpItem
	 * @param tag element tag
	 */
	public CpMemoryRegion(ICpItem parent, String tag) {
		super(parent, tag);
	}

	/**
	 * Copy constructor
	 * @param realRegion region to copy
	 */
	public CpMemoryRegion(ICpMemoryRegion realRegion) { 
		super(realRegion);
	}

	/**
	 * Constructs region from ICpMemory device property 
	 * @param parent parent ICpItem
	 * @param memory ICpMemory to get information from
	 */
	public CpMemoryRegion(ICpItem parent, ICpMemory memory) {
		super(parent, CmsisConstants.MEMORY_TAG);
		setMemory(memory);
		initItem();
	}

	
	@Override
	public void initItem() {
		getOriginalAttributes(); // ensures we get initial attributes
	}

	@Override
	public ICpMemoryRegion getRegion() {
		return this;
	}
	
	
	@Override
	public boolean isPeripheral() {
		return false;
	}
	
	@Override
	public boolean isPeripheralAccess() {
		return false;
	}
	
	
	@Override
	public IMemoryPermissions getParentPermissions() {
		ICpMemoryBlock parent = getParentBlock();
		if(parent != null)
			return parent;
		if(fOriginalAttributesAndPermissions == null) {
			getOriginalAttributes(); // ensure we create the stored ones
		}
		return fOriginalAttributesAndPermissions;
	}
	
	
	@Override
	public IAttributes getOriginalAttributes() {
		if(fOriginalAttributesAndPermissions == null) {
			fOriginalAttributesAndPermissions = new MemoryPermissions(this); // make a copy
			fOriginalAttributesAndPermissions.updateAttributes(this);
		}
		return fOriginalAttributesAndPermissions.attributes();
	}

	@Override
	public boolean updatePermissions(IMemoryPermissions other) {
		if(!super.updatePermissions(other))
			return false;
		Collection<ICpMemoryBlock> subBlocks = getSubBlocks();
		if(subBlocks != null) {
			for( ICpMemoryBlock block : getSubBlocks()) {
				block.adjustPermissions(this);
			}
		}
		return true;
	}

	@Override
	public void invalidate() {
		super.invalidate();
	}


	@Override
	public void arrangeBlocks() {
		PhysicalMemoryRegion pr = getPhysicalRegion();
		if(pr != null)
			pr.arrangeBlocks();
	}
	
	
	@Override
	public PhysicalMemoryRegion getPhysicalRegion() {
		ICpMemoryBlock parent = getParentBlock();
		if(parent != null) {
			return parent.getPhysicalRegion();
		}
		
		if(fPhysicalRegion == null) {
			ICpRootZone rootZone = getRootZone();
			if(rootZone == null)
				return null;
			fPhysicalRegion = rootZone.getResources().getPhysicalRegion(getAddress()); 
		}
		return fPhysicalRegion;
	}

}
