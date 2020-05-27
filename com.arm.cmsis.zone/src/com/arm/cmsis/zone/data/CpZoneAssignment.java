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
import com.arm.cmsis.pack.enums.EMemorySecurity;


/**
 *  Zone assignment  containing resource descriptions
 */
public class CpZoneAssignment extends CpZoneItem implements ICpZoneAssignment {

	protected ICpMemoryBlock fAssignedBlock = null; // block with fully qualified attributes 
	
	public CpZoneAssignment(ICpItem parent) {
		this(parent, CmsisConstants.ASSIGN);
	}

	
	public CpZoneAssignment(ICpItem parent, String tag) {
		super(parent, tag);
	}
	
	@Override
	public void invalidate() {
		fAssignedBlock = null;
		super.invalidate();
	}
	
	@Override
	public String getDeviceName() {
		return getZone().getDeviceName();
	}

	@Override
	public String getProcessorName() {
		return getZone().getProcessorName();
	}

	@Override
	public String getName() {
		return getId();
	}
	
	@Override
	public String constructId() {
		if(hasAttribute(CmsisConstants.MEMORY_TAG)){
			return ICpMemoryBlock.constructBlockId(CmsisConstants.MEMORY_TAG, getAttribute(CmsisConstants.MEMORY_TAG), null);
		}
		return ICpMemoryBlock.constructBlockId(CmsisConstants.PERIPHERAL, getPeripheralName(), getGroupName());
	}
	
	@Override
	public boolean isPeripheral() {
		return hasAttribute(CmsisConstants.PERIPHERAL);
	}

	@Override
	public boolean isPeripheralAccess() {
		return isPeripheral();
	}


	@Override
	public ICpMemoryBlock getAssignedBlock() {
		if(isRemoved())
			return null;
		if(fAssignedBlock == null) {
			fAssignedBlock = createAssignedBlock(findMemoryBlock());
		}
		return fAssignedBlock;
	}

	protected ICpMemoryBlock findMemoryBlock() {
		ICpZone zone = getZone();
		ICpResourceContainer resources = zone.getRootZone().getResources();
		return resources.getMemoryBlock(getId());
	}

	@Override
	public String getStartString() {
		ICpMemoryBlock assignedBlock = getAssignedBlock();
		if(assignedBlock == null)
			return CmsisConstants.EMPTY_STRING;
		return assignedBlock.getStartString();
	}

	@Override
	public String getSizeString() {
		ICpMemoryBlock assignedBlock = getAssignedBlock();
		if(assignedBlock == null)
			return CmsisConstants.EMPTY_STRING;
		return assignedBlock.getSizeString();
	}

	@Override
	public String getDescription() {
		ICpMemoryBlock mappedBlock = getAssignedBlock();
		if(mappedBlock != null && mappedBlock.hasAttribute(CmsisConstants.INFO)) {
			return mappedBlock.getAttribute(CmsisConstants.INFO);
		}
		return super.getDescription();
	}


	@Override
	public void assign(ICpMemoryBlock block) {
		if(block == null) {
			return;
		}
		invalidate();
		String name = block.getName();
		if(block.isPeripheral()) {
			setAttribute(CmsisConstants.PERIPHERAL, name);
			String groupName = block.getGroupName();
			setAttribute(CmsisConstants.GROUP, groupName);
			removeAttribute(CmsisConstants.MEMORY_TAG);
		} else {
			setAttribute(CmsisConstants.MEMORY_TAG, name);
			removeAttribute(CmsisConstants.PERIPHERAL);
			removeAttribute(CmsisConstants.GROUP);
		}
	}
	
	protected ICpMemoryBlock createAssignedBlock(ICpMemoryBlock block) {
		if(block == null) {
			return null;
		}
		ICpMemoryBlock assignedBlock = block.cloneBlock();
		assignedBlock.mergeAccess(this);  // merges only prwx 
		assignedBlock.attributes().mergeAttributes(block.getEffectiveAttributes(null));
		 
		if(assignedBlock.isPeripheral()) {
			// adjust peripheral security to the zone 
			EMemorySecurity security = getSecurity(); // effective one : the zone or the assignment security 	
			EMemorySecurity blockSecurity = block.getSecurity().adjust(security); 
			assignedBlock.setSecurity(blockSecurity);
			block.copyChildrenTo(assignedBlock); // copy interrupts
		}
		
		// remove anused attributes  
		assignedBlock.removeAttribute(CmsisConstants.OFFSET); // not used since START is set
		assignedBlock.removeAttribute(CmsisConstants.FIXED); // not used since START is set
		assignedBlock.removeAttribute(CmsisConstants.PARENT); // not used exported resource becomes a parent itself
		return assignedBlock;
	}
	
	
}

