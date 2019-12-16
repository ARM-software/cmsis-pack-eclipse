/*******************************************************************************
* Copyright (c) 2017-2019 ARM Ltd. and others
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.MemoryStartComparator;
import com.arm.cmsis.pack.enums.ESeverity;
import com.arm.cmsis.pack.item.ICmsisVisitor.VisitResult;
import com.arm.cmsis.zone.error.CmsisZoneError;
/**
 * 
 */
public class CpResourceContainer extends CpResourceGroup implements ICpResourceContainer  {

	protected Map<Long, PhysicalMemoryRegion> fPhysicalRegions = null; 
	protected Map<Long, IMpcRegion> fMpcRegions = null; 
	
	
	public CpResourceContainer(ICpItem parent, String tag) {
		super(parent, tag);
	}

	@Override
	public void clear() {
		fPhysicalRegions = null;
		fMpcRegions = null;
		super.clear();
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
	}

	
	@Override
	public void init() {
		collectMpcRegions();
		constructMemoryBlockMap(); 
		constructPhysicalRegionMap(); // will trigger arrange
	}
	
	
	@Override
	protected boolean isShowBlock(ICpMemoryBlock block, boolean bShowRAM, boolean bShowROM, boolean bShowPeripheral) {
		if(block instanceof ICpPeripheralGroup)
			return false;
		return super.isShowBlock(block, bShowRAM, bShowROM, bShowPeripheral);
	}

	@Override
	protected Map<String, ICpMemoryBlock> constructMemoryBlockMap() {
		Map<String, ICpMemoryBlock> memoryBlocks = new TreeMap<>();
		accept((item) -> {
			if(item instanceof ICpMemoryBlock) {
				ICpMemoryBlock block = (ICpMemoryBlock)item;
				String id = block.getId();
				if(!memoryBlocks.containsKey(id)){
					memoryBlocks.put(id, block);
				}
			}
			return VisitResult.CONTINUE;
		});
		return memoryBlocks;
	}
	
	
	@Override
	public PhysicalMemoryRegion getPhysicalRegion(long address) {
		return getPhysicalRegions().get(address);
	}
	
	@Override
	public Map<Long, PhysicalMemoryRegion> getPhysicalRegions() {
		if(fPhysicalRegions == null) {
			constructPhysicalRegionMap();	
		}
		return fPhysicalRegions;
	}
	
	protected void constructPhysicalRegionMap() {
		fPhysicalRegions = new HashMap<>();
		for( ICpMemoryBlock block: getMemoryBlocksAsMap().values()) {
			if(block instanceof ICpMemoryRegion && block.getParentBlock() == null) {
				ICpMemoryRegion r = (ICpMemoryRegion)block;
				Long address = r.getAddress(); // physical address

				PhysicalMemoryRegion region = fPhysicalRegions.get(address);
				if(region == null) {
					region = new PhysicalMemoryRegion();
					fPhysicalRegions.put(address, region);
				}
				region.addRegion(r);
			}
		}
	}
	
	@Override
	public boolean arrangeBlocks() {
		 Map<Long, PhysicalMemoryRegion> physicalRegions = getPhysicalRegions(); 
		 boolean bModified = false;
		 for(PhysicalMemoryRegion region : physicalRegions.values()) {
				if(region.arrangeBlocks())
					bModified = true;
			}
		return bModified;
	}
	
	@Override
	public Collection<ICpMemoryRegion> getAllMemoryRegions() {
		Collection<ICpMemoryRegion> regions = new HashSet<>();
		for( ICpMemoryBlock block: getMemoryBlocksAsMap().values()) {
			if(block instanceof ICpMemoryRegion && !block.isPeripheral()) {
				regions.add((ICpMemoryRegion)block);
			}
		}
		return regions;
	}
	
	@Override
	public Collection<ICpMemoryRegion> getStarupMemoryRegions() {
		Collection<ICpMemoryRegion> regions = new HashSet<>();
		for( ICpMemoryBlock block: getMemoryBlocksAsMap().values()) {
			if(block instanceof ICpMemoryRegion && block.isStartup()) {
				regions.add((ICpMemoryRegion)block);
			}
		}
		return regions;
	}



	@Override
	public Collection<ICpPeripheral> getAllPeripherals() {
		Collection<ICpPeripheral> peripherals = new HashSet<>();
		for( ICpMemoryBlock block: getMemoryBlocksAsMap().values()) {
			if(block instanceof ICpPeripheral ) {
				peripherals.add((ICpPeripheral)block);
			}
		}
		return peripherals;
	}
	
	
	@Override
	public Collection<ICpPeripheralItem> getAllPeripheralItems() {
		Collection<ICpPeripheralItem> peripheralItems = new HashSet<>();
		for( ICpMemoryBlock block: getMemoryBlocksAsMap().values()) {
			if(block instanceof ICpPeripheralItem ) {
				peripheralItems.add((ICpPeripheralItem)block);
			}
		}
		return peripheralItems;
	}
	
	
	@Override
	public void addPartititionBlocks(ICpPartitionGroup pg) {
		if(pg == null)
			return;

		Collection<ICpMemoryRegion> regions = pg.getChildrenOfType(ICpMemoryRegion.class);
		if(regions != null && !regions.isEmpty()) {
			for(ICpMemoryRegion r : regions) {
				if(!r.isPeripheral()) {
					addPartititionRegion(r);
					continue;
				}
				ICpPeripheral p = addPeripheralItem(r);
				p.getOriginalAttributes(); // ensure to store the original attributes
				p.updateAttributes(r);
				p.updatePermissions(r);
				Collection<ICpSlot> slots = r.getChildrenOfType(ICpSlot.class);
				if(slots != null && !slots.isEmpty()) {
					for(ICpSlot s : slots) {
						ICpSlot slot = p.getSlot(s.getName());
						if(slot == null) {
							// we can neither recover nor tell user what to do
							continue;  
						}
						slot.updateAttributes(s);
					}
				}
			}
		}
		
		invalidate();
	}

	protected void addPartititionRegion(ICpMemoryRegion r) {
		ICpResourceGroup memoryGroup = ensureMemoryGroup();
		String regionName = r.getParentRegionName();
		boolean updateParentPermissions = false;
		if(regionName.isEmpty()) {
			regionName = r.getName();
			updateParentPermissions = true;
		}
		ICpMemoryRegion region = memoryGroup.getMemoryRegion(regionName);
		if(region == null) {
			region = new CpMemoryRegion(memoryGroup, CmsisConstants.MEMORY_TAG);
			memoryGroup.addChild(region);
			region.setName(regionName);
			region.addError(new CmsisZoneError(ESeverity.Error, CmsisZoneError.Z201));
		}
		if(updateParentPermissions) { // only update permissions of the parent
			region.getOriginalAttributes(); // ensure to store the original attributes
			region.updateAttributes(r);
			region.updatePermissions(r);
		} else { // sub-region
			r.setParent(region);
			region.replaceChild(r);
		}
	}

	
	/**
	 * Adds/updates a peripheral item from partition or assignment
	 * @param a ICpPeripheral or ICpZoneAssignment
	 * @return ICpPeripheral
	 */
	protected ICpPeripheral addPeripheralItem(ICpZoneItem a) {
		ICpResourceGroup group = ensurePeripheralsGroup();
		ICpItem parent = group;
		String groupName = a.getGroupName();
		String name = a.getPeripheralName();
		ICpPeripheral p = null;
		if(groupName != null && !groupName.isEmpty()) {
			ICpPeripheralGroup pGroup = group.getPeripheralGroup(groupName);
			if(pGroup == null) {
				pGroup = group.ensurePeripheralGroup(groupName);
				pGroup.addError(new CmsisZoneError(ESeverity.Error, CmsisZoneError.Z202));
			}
			parent = pGroup;
			p = pGroup.getPeripheral(name);
		} else {
			p = group.getPeripheral(name);
		}
		if(p == null) {
			p = new CpPeripheral(parent, CmsisConstants.PERIPHERAL);
			parent.addChild(p);
			p.setName(name);
			p.addError(new CmsisZoneError(ESeverity.Error, CmsisZoneError.Z202));
			invalidate();
		}
		return p;
	}

	
	@Override
	public void addZoneAssignments(ICpZoneContainer zoneContainer) {
		constructMemoryBlockMap();
		if(zoneContainer == null)
			return;
		Collection<ICpZone> zones = zoneContainer.getZones();
		if(zones == null || zones.isEmpty())
			return;
		for(ICpZone z : zones) {
			if(z.isRemoved())
				continue;
			Collection<ICpZoneAssignment> assignments = z.getZoneAssignments();
			if(assignments == null || assignments.isEmpty())
				continue;
			String zoneName = z.getName();
			for(ICpZoneAssignment a : assignments){
				if(a.isRemoved())
					continue;
				addZoneAssignment(a, zoneName);
			}
		}
		
	}

	protected void addZoneAssignment(ICpZoneAssignment a, String zoneName) {
		if(a.isPeripheral()) {
			addPeripheralZoneAssignment(a, zoneName);
		} else {
			addMemoryZoneAssignment(a, zoneName);
		}
	}

	protected void addMemoryZoneAssignment(ICpZoneAssignment a, String zoneName) {
		ICpResourceGroup group = ensureMemoryGroup();
		String id = a.getId();
		if(id == null || id.isEmpty())
			return;
			
		if(getMemoryBlock(id) != null) {
			return; // already there
		}
		ICpMemoryRegion r = group.getMemoryRegion(id);
		if(r == null) {
			r = new CpMemoryRegion(group, CmsisConstants.MEMORY_TAG);
			group.addChild(r);
			r.setName(id);
			r.mergeAccess(a);
			r.addError(new CmsisZoneError(ESeverity.Error, CmsisZoneError.Z201));
			invalidate();
		}
		r.addAssignment(zoneName, a);
	}

	protected void addPeripheralZoneAssignment(ICpZoneAssignment a, String zoneName) {
		ICpPeripheral p = addPeripheralItem(a);
		p.addAssignment(zoneName, a);
		p.mergeAccess(a);
	}

	
	@Override
	public Map<Long, IMpcRegion> getMpcRegions() {
		if(fMpcRegions == null) {
			collectMpcRegions();
		}
		return fMpcRegions;
	}

	protected void collectMpcRegions() {
		fMpcRegions = new TreeMap<>();
		ICpResourceGroup memory = getMemoryGroup();
		if(memory != null) {
			List<ICpMpc> mpcItems = new LinkedList<>(memory.getChildrenOfType(ICpMpc.class));
			Collections.sort(mpcItems, new MemoryStartComparator());
			MpcRegion region = null; 
			for(ICpMpc mpc : mpcItems) {
				if(region != null) {
					if(region.addMpc(mpc)) 
						continue; // the mpc is appended to the previous region
				}
				region = new MpcRegion(this, mpc); // create new region
				fMpcRegions.put(region.getStart(), region);
			}
		}
	}

	
	@Override
	public IMpcRegion getMpcRegion(long address) {
		for(IMpcRegion region : getMpcRegions().values()) {
			long regionOffset = address - region.getAddress();
			if(regionOffset == 0L)
				return region; // exact address
			// check if the address is inside the MPC region
			if(regionOffset > 0 && regionOffset < region.getSize()) {
				return region;
			}
		}
		return null;
	}

}
