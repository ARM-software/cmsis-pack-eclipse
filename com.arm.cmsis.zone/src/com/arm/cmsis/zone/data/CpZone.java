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
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.MemoryStartComparator;
import com.arm.cmsis.pack.enums.ECoreArchitecture;
import com.arm.cmsis.pack.enums.EMemoryPrivilege;
import com.arm.cmsis.pack.enums.EMemorySecurity;
import com.arm.cmsis.pack.generic.IAttributes;

/**
 *  Project zone description in system zone
 */
public class CpZone extends CpZoneItem implements ICpZone {

	
	public CpZone(ICpItem parent, String tag) {
		super(parent, tag);
	}

	@Override
	public void invalidate() {
		super.invalidate();
	}

	@Override
	public ICpItem getEffectiveParent() {
		return null; // affects getEffectiveAttributes()
	}


	@Override
	public ICpZoneAssignment getZoneAssignment(String zoneName) {
		ICpItem item = getFirstChild(zoneName);
		if(item instanceof ICpZoneAssignment) {
			return (ICpZoneAssignment)item;
		}
		return null;
	}

	@Override 
	public Collection<ICpZoneAssignment> getZoneAssignments() {
		return getChildrenOfType(ICpZoneAssignment.class);
	}
	

	@Override
	protected ICpItem createChildItem(String tag) {
		switch (tag) {
		case CmsisConstants.ASSIGN:
			return new CpZoneAssignment(this, tag);
		default:
			break;
		}
		return super.createChildItem(tag);
	}

    @Override
    public ICpDeviceUnit getTargetDevice() {
    	ICpRootZone root = getRootZone();
    	if(root == null) {
    		return null;
    	}
    	return root.getDeviceUnit();
    }      

    @Override
    public ICpProcessorUnit getTargetProcessor() {
    	  ICpRootZone root = getRootZone();
    	  if(root == null) {
    		  return null;
    	  }
          String pName = getProcessorName();
          return root.getProcessorUnit(pName);
    }
    
	@Override
	public ICpItem toFtlModel(ICpItem ftlParent) {
		ICpItem zoneItem = super.toFtlModel(ftlParent); 
		Collection<ICpZoneAssignment> assignments = getZoneAssignments();	
		
		addAssignmentsToFtlModel(zoneItem, assignments, CmsisConstants.MEMORY_TAG);
		addAssignmentsToFtlModel(zoneItem, assignments, CmsisConstants.PERIPHERAL);

		ICpRootZone rootZone = getRootZone();
		String mode = rootZone.getZoneMode();
		if(CmsisConstants.MPU.equals(mode)) {
			addMPUToFtlModel(zoneItem, assignments);
		}
		
		return zoneItem;
	}
		
	protected void addMPUToFtlModel(ICpItem zoneItem, Collection<ICpZoneAssignment> assignments) {
		if(assignments == null || assignments.isEmpty())
			return;

		ICpProcessorUnit processor = getTargetProcessor();
		if(processor == null)
			return;
		
		//Create <mpu_setup> item
		ICpItem mpu_setup = new CpItem(zoneItem, CmsisConstants.MPU_SETUP);
		zoneItem.addChild(mpu_setup);		
				
		//Add <type> child
		ICpItem type = new CpItem(mpu_setup, CmsisConstants.TYPE);
		ECoreArchitecture coreArchitecture = processor.getArchitecture();
		String mpuType = coreArchitecture == ECoreArchitecture.ARMv8 ? CmsisConstants.MPU_TYPE_V8M : CmsisConstants.MPU_TYPE_V7M;
		type.setText(mpuType);	
		mpu_setup.addChild(type);
		
		//Prepare MPU regions
		Set<ICpMpuRegion>  mpuRegions = new TreeSet<>(new MemoryStartComparator());
		
		//Iterate over assignments
		for(ICpZoneAssignment a: assignments){			
			ICpMemoryBlock block = a.getAssignedBlock();
			if(block == null) {
				continue;
			}
			
			//Save memory block as 'Mpu' <region> item
			ICpMpuRegion mpuRegion = new CpMpuRegion(block);			
			mpuRegions.add(mpuRegion);			
		}
		
		
		//if(coreArchitecture == ECoreArchitecture.ARMv8) {
			ICpMpuRegion prevSr = null;
			//Compact MPU regions : group 'Mpu' <region> items
			for(Iterator<ICpMpuRegion> iterator = mpuRegions.iterator(); iterator.hasNext(); ) {
				ICpMpuRegion r = iterator.next();
				if(prevSr != null  && prevSr.appendMpuRegion(r)) {
					iterator.remove(); // appended => remove it
				} else {
					prevSr = r;
				}
			}
		//}
			
		//TODO: Add compactness for sub-regions if processor has ARMv7 architecture
			
		
		//Add <region> items after doing comparison of attributes
		for(ICpMpuRegion mpuRegion : mpuRegions) {
			//Create <region> item							
			ICpItem region = new CpItem(mpu_setup, CmsisConstants.REGION);	
			//Add <region> child to the <mpu_setup> item
			mpu_setup.addChild(region);
					
			/*** Add region values ***/				
					
			//Add <start> child
			ICpItem startItem = new CpItem(region, CmsisConstants.START);
			String startAddress = mpuRegion.getStartString();
			startItem.setText(startAddress);
			region.addChild(startItem);
			
			//Align end memory address to 32 bytes
			Long stop = mpuRegion.getStop();
			stop &= ~0x1FL; //32 bytes alignment as required by MPU
			String endAddress = IAttributes.longToHexString8(stop);			
			
			//Add <end> child
			ICpItem end = new CpItem(region, CmsisConstants.END);
			end.setText(endAddress);
			region.addChild(end);
			
			//Add <access> child
			ICpItem access = mpuRegion.toFtlModel(region, CmsisConstants.ACCESS, mpuRegion.getAccessString());			
			region.addChild(access);
			
			//Add <privileged> child
			ICpItem privileged = new CpItem(region, CmsisConstants.PRIVILEGED);				
			String privilege = mpuRegion.getPrivilege() == EMemoryPrivilege.PRIVILEGED ? CmsisConstants.ONE : CmsisConstants.ZERO;
			
			privileged.setText(privilege);
			region.addChild(privileged);		
			
			//Add <shared> child
			ICpItem shared = new CpItem(region, CmsisConstants.SHARED);
			String isMemoryShared = mpuRegion.isShared() ? CmsisConstants.ONE : CmsisConstants.ZERO;
			shared.setText(isMemoryShared);
			region.addChild(shared);
			
			//Add <dma> child
			ICpItem dma = new CpItem(region, CmsisConstants.DMA);
			String isMemoryDMA = mpuRegion.isDma() ? CmsisConstants.ONE : CmsisConstants.ZERO;
			dma.setText(isMemoryDMA);
			region.addChild(dma);
			
			//Add <rom> child
			ICpItem rom = new CpItem(region, CmsisConstants.ROM_TAG);
			String memoryType = mpuRegion.isROM() ?  CmsisConstants.ONE : CmsisConstants.ZERO ;
			rom.setText(memoryType);
			region.addChild(rom);
			
			//Add <info> child. Note: <info> tag is reused to save content of  <name> tag
			ICpItem info = new CpItem(region, CmsisConstants.INFO);				
			String name = mpuRegion.getAttribute(CmsisConstants.NAME);			
			info.setText(name);
			region.addChild(info);						
			
			// add v7 values
			if(coreArchitecture == ECoreArchitecture.ARMv7) {
				long size = mpuRegion.getSize();
				long size_v7M = ICpMemoryBlock.getMpu7RegionSize(size);

				long alignment_v7M = ICpMemoryBlock.getMpu7RegionAlignment(size);
				long start = mpuRegion.getStart();
				long addr_v7M = start / alignment_v7M * alignment_v7M;     
				Long offset = start - addr_v7M;

				ICpItem a7m = new CpItem(region, CmsisConstants.addr_v7M);
				a7m.setText(IAttributes.longToHexString8(addr_v7M));
				region.addChild(a7m);
				
				ICpItem s7m = new CpItem(region, CmsisConstants.size_v7M);
				s7m.setText(IAttributes.longToHexString8(size_v7M));
				region.addChild(s7m);

				Long srd_v7M = 0x0L; // first initialize
				if(size_v7M > size && size_v7M >= 256L) {
					srd_v7M = 0xFFL; // first initialize with ones 
					Long count = ICpMemoryBlock.alignTo(size, alignment_v7M) / alignment_v7M;
					Long index = offset / alignment_v7M;
					for(Long i = index; i < count; i++) {
						srd_v7M &= ~(1 << i); // reset disable bit 
					}
				}
				
				ICpItem srd7m = new CpItem(region, CmsisConstants.srd_v7M);
				srd7m.setText(IAttributes.longToHexString8(srd_v7M));
				region.addChild(srd7m);
				
				
			}
			
		}
	}
		
	protected void addAssignmentsToFtlModel(ICpItem zoneItem, Collection<ICpZoneAssignment> assignments, String tag) {
		if(assignments == null || assignments.isEmpty())
			return;
		
		for(ICpZoneAssignment a: assignments){
			ICpMemoryBlock block = a.getAssignedBlock();
			if(block == null) {
				continue;
			}			
			if(!block.getTag().equals(tag))
				continue;			
				
			ICpItem item = block.toFtlModel(zoneItem);			
			zoneItem.addChild(item);
		}		
	}

	@Override
	public boolean canAssign(ICpMemoryBlock block) {
		if(block == null)
			return false;
		if(block instanceof ICpPeripheralGroup)
			return false;
		if(getSecurity() == EMemorySecurity.NON_SECURE ) {
			// only non-secure memory can be assigned to no-secure zone  
			EMemorySecurity blockSecurity = block.getSecurity();
			return blockSecurity.isNonSecure();
		}
		return true; 
	}
}
