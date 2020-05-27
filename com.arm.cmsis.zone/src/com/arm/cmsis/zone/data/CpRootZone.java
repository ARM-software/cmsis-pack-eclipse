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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpItem;
import com.arm.cmsis.pack.data.CpRootItem;
import com.arm.cmsis.pack.data.ICpDeviceItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpRootItem;
import com.arm.cmsis.pack.data.MemoryStartComparator;
import com.arm.cmsis.pack.enums.ECoreArchitecture;
import com.arm.cmsis.pack.enums.EMemorySecurity;
import com.arm.cmsis.pack.enums.ESeverity;
import com.arm.cmsis.pack.info.ICpPackInfo;
import com.arm.cmsis.pack.utils.AlnumComparator;
import com.arm.cmsis.pack.utils.Utils;
import com.arm.cmsis.zone.error.CmsisZoneError;

/*
 * Implementation of ICpRootZone interface
 */
public class CpRootZone extends CpResourceGroup implements ICpRootZone {

	protected ICpResourceContainer fResources = null; // not saved

	/**
	 * XML constructor
	 * @param parent parent ICpItem
	 * @param tag item's tag
	 */
	public CpRootZone(ICpItem parent, String tag) {
		super(parent, tag);
	}

	protected ICpDeviceItem getDevice() {
		ICpDeviceUnit deviceUnit = getDeviceUnit();
		if(deviceUnit != null){
			return deviceUnit.getDevice();
		}
		return null;
	}

	@Override
	public ICpRootZone getRootZone() {
		return this;
	}


	@Override
	public void clear() {
		fResources = null;
		super.clear();
	}

	@Override
	public void invalidate() {
		super.invalidate();
	}

	@Override
	public ICpResourceContainer getResources() {
		if(fResources == null) {
			init();
		}
		return fResources;
	}

	@Override
	public Map<String, ICpMemoryBlock> getMemoryBlocksAsMap() {
		return getResources().getMemoryBlocksAsMap();
	}

	@Override
	public String getSecurityString() {
		ICpZoneCreator creator = getZoneCreator();
		if(creator != null) {
			return creator.getSecurityString();
		}
		return null;
	}

	@Override
	public void setSecurityString(String privilege) {
		ICpZoneCreator creator = getZoneCreator();
		if(creator != null) {
			creator.setSecurityString(privilege);
		}
	}

	@Override
	public boolean setZoneOption(String type, String key, boolean value) {
		boolean bChanged = ICpRootZone.super.setZoneOption(type, key, value);
		if(bChanged) {
			getResources().invalidateAll();
		}
		return bChanged;
	}

	@Override
	public String getPrivilegeString() {
		ICpZoneCreator creator = getZoneCreator();
		if(creator != null) {
			return creator.getPrivilegeString();
		}
		return null;
	}

	@Override
	public void setPrivilegeString(String privilege) {
		ICpZoneCreator creator = getZoneCreator();
		if(creator != null) {
			creator.setPrivilegeString(privilege);
		}
	}

	protected ICpResourceContainer ensureResourceContainer() {
		ICpResourceContainer resourceContainer = getResourceContainer();
		if(resourceContainer == null) {
			resourceContainer = new CpResourceContainer(this, CmsisConstants.RESOURCES);
			addChild(resourceContainer);
		}
		return resourceContainer;
	}

	protected ICpResourceGroup ensureMemoryContainer() {
		ICpResourceContainer resourceContainer = ensureResourceContainer();
		return resourceContainer.ensureResourceGroup(CmsisConstants.MEMORIES);
	}

	protected ICpResourceGroup ensurePeripheralContainer() {
		ICpResourceContainer resourceContainer = ensureResourceContainer();
		return resourceContainer.ensureResourceGroup(CmsisConstants.PERIPHERALS);
	}

	@Override
	public ICpResourceGroup getMemoryGroup() {
		ICpResourceContainer resourceContainer = getResourceContainer();
		if(resourceContainer != null)
			return resourceContainer.getMemoryGroup();
		return null;
	}

	@Override
	public ICpResourceGroup getPeripheralsGroup() {
		ICpResourceContainer resourceContainer = getResourceContainer();
		if(resourceContainer != null)
			return resourceContainer.getPeripheralsGroup();
		return null;
	}

	protected ICpPartitionGroup ensurePartitionGroup() {
		ICpPartitionGroup blocks = getPartitionGroup();
		if(blocks == null) {
			blocks = new CpPartitionGroup(this, CmsisConstants.PARTITION);
			addChild(blocks);
		}
		return blocks;
	}

	@Override
	public void init() {
		getZoneConfig(); //ensure configure element
		fResources = ensureResourceContainer();
		ICpPartitionGroup pg = getPartitionGroup();
		fResources.addPartititionBlocks(pg);
		ICpZoneContainer zones = getZoneContainer();
		fResources.addZoneAssignments(zones);

		purge(); // remove unused invalid blocks
		getRootFileName(); // ensure root file name
		checkDevice();

		fResources.init();
		fResources.getPhysicalRegions(); // will trigger arrange
		fResources.arrangeBlocks();
		createMpuSetup();
	}


	protected void checkDevice() {
		ICpDeviceUnit deviceUnit = getDeviceUnit();
		if(deviceUnit == null) {
			addError(new CmsisZoneError(getRootFileName(), ESeverity.Error, CmsisZoneError.Z401));
			return;
		}
		ICpPackInfo packInfo = deviceUnit.getPackInfo();
		if(packInfo == null) {
			deviceUnit.addError(new CmsisZoneError(getResourceFileName(), ESeverity.Warning, CmsisZoneError.Z402));
		} else if(!packInfo.hasAttribute(CmsisConstants.NAME) || !packInfo.hasAttribute(CmsisConstants.VENDOR)){
			deviceUnit.addError(new CmsisZoneError(getResourceFileName(), ESeverity.Warning, CmsisZoneError.Z403));
		}

		Map<String, ICpProcessorUnit> processors = deviceUnit.getProcessorUnits();
		if(processors == null || processors.isEmpty()) {
			deviceUnit.addError(new CmsisZoneError(getResourceFileName(), ESeverity.Error, CmsisZoneError.Z406));
			return;
		}

		for(ICpProcessorUnit processor : processors.values()) {
			if(processor.hasTrustZone()) {
				if(processor.genNumInterrups() <= 0 ) {
					deviceUnit.addError(new CmsisZoneError(getResourceFileName(), ESeverity.Error, CmsisZoneError.Z407));
				}
				if(processor.genNumSauRegions() <= 0 ) {
					deviceUnit.addError(new CmsisZoneError(getResourceFileName(), ESeverity.Error, CmsisZoneError.Z408));
				}
			}
		}
	}


	@Override
	public void updatePartition() {
		ICpPartitionGroup pg = ensurePartitionGroup();
		pg.clear();
		ICpResourceContainer resources = getResourceContainer();
		if(resources == null){
			return;
		}
		Collection<ICpMemoryBlock> regions = resources.getAllMemoryRegions();
		if(regions != null && !regions.isEmpty()) {
			for(ICpMemoryBlock r : regions) {
				if(r.getParentBlock() != null)
					continue; // gets copied over parent
				if(r.isModified()) {
					r.copyBlockTo(pg);
				}
				if(r.hasSubBlocks()) {
					r.copyChildBlocksTo(pg);
				}
			}
		}

		Collection<ICpPeripheral> peripherals = resources.getAllPeripherals();
		if(peripherals != null && !peripherals.isEmpty()) {
			for(ICpPeripheral p : peripherals) {
				if(p.isModified()) {
					p.copyBlockTo(pg);
				}
			}
		}
	}

	@Override
	protected String constructName() {
		if(hasAttribute(CmsisConstants.NAME))
			return getAttribute(CmsisConstants.NAME);
		return Utils.extractBaseFileName(getRootFileName());
	}

	@Override
	protected ICpItem createChildItem(String tag) {
		switch (tag) {
		case CmsisConstants.DEVICE_TAG:
			return new CpDeviceUnit(this, tag);
		case CmsisConstants.RESOURCES:
			return new CpResourceContainer(this, tag);
		case CmsisConstants.PARTITION:
			return new CpPartitionGroup(this, tag);
		case CmsisConstants.ZONES:
			return new CpZoneContainer(this, tag);
		case CmsisConstants.CONFIGURE:
			return new CpZoneConfig(this, tag);
		case CmsisConstants.CREATOR:
			return new CpZoneCreator(this, tag);
		default:
			break;
		}
		return super.createChildItem(tag);
	}

	/**
	 * Returns zone container, creates one if does not exist
	 * @return ICpZoneContainer
	 */
	protected ICpZoneContainer ensureZoneContainer() {
		ICpZoneContainer container = getZoneContainer();
		if(container == null) {
			container = new CpZoneContainer(this);
			addChild(container);
		}
		return container;
	}

	@Override
	public ICpZone addZone(String name) {
		ICpZone zone = getZone(name);
		if(zone == null) {
			ICpZoneContainer container = ensureZoneContainer();
			String tag = CmsisConstants.ZONE;
			zone = new CpZone(container, tag);
			zone.setAttribute(CmsisConstants.NAME, name);
			container.addChild(zone);
		}
		return zone;
	}

	@Override
	public ICpDeviceUnit getDeviceUnit() {
		return getFirstChildOfType(ICpDeviceUnit.class);
	}

	@Override
	public ICpProcessorUnit getProcessorUnit(String processorName) {
		 ICpDeviceUnit d = getDeviceUnit();
		 if(d != null)
			 return d.getProcessorUnit(processorName);
		return null;
	}

	@Override
	public int getProcessorCount() {
		 ICpDeviceUnit d = getDeviceUnit();
		 if(d != null)
			 return d.getProcessorCount();
		return 0;
	}


	@Override
	public Map<String, ICpProcessorUnit> getProcessorUnits() {
		ICpDeviceUnit d = getDeviceUnit();
		if(d != null)
			return d.getProcessorUnits();
		return null;
	}

	@Override
	public boolean hasSecureCore() {
		 ICpDeviceUnit d = getDeviceUnit();
		 if(d != null)
			 return d.hasSecureCore();
		return false;
	}

	@Override
	public ECoreArchitecture getArchitecture() {
		 ICpDeviceUnit d = getDeviceUnit();
		 if(d != null)
			 return d.getArchitecture();
		 return ECoreArchitecture.UNKNOWN;
	}


	@Override
	public Collection<ICpZone> getZones() {
		ICpZoneContainer zonesItem = getZoneContainer();
		if(zonesItem == null) {
			return null;
		}
		return zonesItem.getZones();
	}


	@Override
	public ICpZone getZone(String name) {
		ICpZoneContainer zones = getFirstChildOfType(ICpZoneContainer.class);
		if(zones == null)
			return null;
		ICpZone zone = zones.getZone(name);
		return zone;
	}


	@Override
	public String getResourceFileName() {
		ICpItem rzoneRef = getFirstChildByTag(CmsisConstants.RZONE);
		String fileName;
		if(rzoneRef != null) {
			fileName = rzoneRef.getName();
			if(!fileName.isEmpty()) {
				return getAbsolutePath(fileName);
			}
		}
		// if no such element derive resource name from  azone name
		fileName = Utils.removeFileExtension(getRootFileName());
		if(fileName != null && !fileName.isEmpty())
			fileName += CmsisConstants.DOT_RZONE;
		return fileName;
	}

	@Override
	public ICpItem toFtlModel(ICpItem ftlParent) {
		ICpRootItem root = new CpRootItem(ftlParent, CmsisConstants.FZONE);

		//System resources
		ICpItem systemItem = createFtlSystemElement(root);
		root.addChild(systemItem);

		//Zone info
		ICpZoneContainer zones = getZoneContainer();
		if(zones != null) {
			zones.toFtlModel(root);
		}

		return root;
	}

	protected ICpItem createFtlSystemElement(ICpItem ftlParent) {
		ICpItem systemItem = new CpItem(ftlParent, CmsisConstants.SYSTEM);
		createFtlProcessorElements(systemItem);
		createFtlMemoryElements(systemItem);
		createFtlPeripheralElements(systemItem);
		return systemItem;
	}

	protected void createFtlProcessorElements(ICpItem systemItem) {
		Map<String, ICpProcessorUnit> processors = getProcessorUnits();
		for(Entry<String, ICpProcessorUnit> e : processors.entrySet()) {
			ICpProcessorUnit processor = e.getValue();
			ICpItem p = new CpItem(systemItem, CmsisConstants.PROCESSOR_TAG);
			systemItem.addChild(p);
			ICpItem pname = new CpItem(p, CmsisConstants.PNAME);
			pname.setText(e.getKey());
			p.addChild(pname);
			ICpItem dnumInt = new CpItem(p, CmsisConstants.DnumInterrupts);
			dnumInt.setText(processor.getAttribute(CmsisConstants.DnumInterrupts));
			p.addChild(dnumInt);
			ICpItem dnumSau = new CpItem(p, CmsisConstants.DnumSauRegions);
			dnumSau.setText(processor.getAttribute(CmsisConstants.DnumSauRegions));
			p.addChild(dnumSau);
		}

	}

	protected void createFtlMemoryElements(ICpItem systemItem) {
		ICpResourceContainer resources = getResources();
		if(resources == null)
			return;

		//Prepare SAU regions
		Set<ICpSauRegion>  sauRegions = new TreeSet<>(new MemoryStartComparator());

		//Collect init regions if any
		ICpSauInit sauInit = resources.getSauInit();
		if(sauInit != null) {
			Collection<ICpSauRegion> initSauRegions = sauInit.getSauRegions();
			if(initSauRegions != null && !initSauRegions.isEmpty()) {
				for(ICpSauRegion isr : initSauRegions ) {
					ICpSauRegion sr = new CpSauRegion(isr);
					sauRegions.add(sr);
				}
			}
		}

		//Returns all memory regions available for system zone excluding peripherals
		Collection<ICpMemoryBlock> regions = resources.getAllMemoryRegions();

		//Add memory
		for(ICpMemoryBlock r : regions) {
			ICpMemoryBlock parentBlock = r.getParentBlock();
			//Add only assigned regions
			if(r.getAssignmentCount() > 0) {
				EMemorySecurity security = r.getSecurity();
				if(security.isSauRelevant()) {
					ICpSauRegion sr = new CpSauRegion(r);
					sauRegions.add(sr);
				}
			}
			if(parentBlock != null)
				continue;
			//Creates a FTL model with all required attributes and children expanded as separate ICpItems
			ICpItem m = r.toFtlModel(systemItem);
			systemItem.addChild(m);
		}

		//Add MPC regions
		Map<Long, IMpcRegion> mpcRegions = resources.getMpcRegions();
		for(IMpcRegion mpc : mpcRegions.values()) {
			mpc.toFtlModel(systemItem);
		}

		ICpSauRegion prevSr = null;
		 //Compact SAU regions : group addresses
		for(Iterator<ICpSauRegion> iterator = sauRegions.iterator(); iterator.hasNext(); ) {
			ICpSauRegion sr = iterator.next();
			if(prevSr != null  && prevSr.appendSauRegion(sr)) {
				iterator.remove(); // appended => remove it
			} else {
				prevSr = sr;
			}
		}

		//Add SAU elements
		for(ICpSauRegion sr : sauRegions ) {
			systemItem.addChild(sr.toFtlModel(systemItem));
		}

	}

	protected void createFtlPeripheralElements(ICpItem systemItem) {
		ICpResourceContainer resources = getResources();
		if(resources == null)
			return;

		// add peripherals and collect interrupts
		Map<String, ICpInterrupt> interrupts = new TreeMap<>(new AlnumComparator(false));
		Map<String, ICpItem> reg_setups = new TreeMap<>(new AlnumComparator(false));
		Collection<ICpPeripheralItem> peripheralItems = resources.getAllPeripheralItems();

		for(ICpPeripheralItem pItem : peripheralItems) {
			if(pItem instanceof ICpPeripheral) {
				ICpPeripheral p = (ICpPeripheral)pItem;
				ICpItem item = p.toFtlModel(systemItem);
				systemItem.addChild(item);
				Collection<ICpInterrupt> itrps = p.getInterrupts();
				if(itrps != null) {
					for(ICpInterrupt irq : itrps) {
						interrupts.put(irq.getId(), irq);
					}
				}
			}

			if(!pItem.isAssigned())
				continue;

			Collection<ICpPeripheralSetup> setups = pItem.getPeripheralSetups();
			if(setups != null && !setups.isEmpty()) {
				for(ICpPeripheralSetup setup : setups) {
					if(!setup.matchesPermissions()) {
						continue;
					}
					String slotName = CmsisConstants.EMPTY_STRING;
					String id = setup.getId();
					ICpItem reg_setup = reg_setups.get(id);
					ICpItem parentSetup = setup.getParent();
					if(parentSetup instanceof ICpSlot) {
						slotName = parentSetup.getName();
					}

					if(reg_setup == null) {
						reg_setup = new CpItem(systemItem, CmsisConstants.REG_SETUP);
						reg_setup.addChild(setup.toFtlModel(reg_setup, CmsisConstants.NAME, setup.getName()));
						reg_setup.addChild(setup.toFtlModel(reg_setup, CmsisConstants.INDEX, setup.getAttribute(CmsisConstants.INDEX)));
						reg_setups.put(id, reg_setup);
					}
					reg_setup.addChild(setup.toFtlModel(reg_setup, CmsisConstants.PERIPHERAL, pItem.getName()));
					reg_setup.addChild(setup.toFtlModel(reg_setup, CmsisConstants.SLOT, slotName));
					reg_setup.addChild(setup.toFtlModel(reg_setup, CmsisConstants.VALUE, setup.getValue()));
				}
			}
		}

		for(ICpInterrupt interrupt : interrupts.values() ) {
			ICpItem i = interrupt.toFtlModel(systemItem);
			systemItem.addChild(i);
		}

		for(ICpItem reg_setup : reg_setups.values() ) {
			systemItem.addChild(reg_setup); // just add as child elements
		}
	}

	@Override
	public ICpZoneConfig getZoneConfig() {

		ICpZoneConfig config = getFirstChildOfType(ICpZoneConfig.class);
		if(config == null) {
			config = new CpZoneConfig(this, CmsisConstants.CONFIGURE);
			addChild(config);
		}
		return config;
	}

	@Override
	public String getZoneMode() {
		String mode = ICpRootZone.super.getZoneMode();
		if(CmsisConstants.MPU.equals(mode) && !hasMPU()) {
			return CmsisConstants.PROJECT;
		}
		return mode;
	}

	@Override
	public void createMpuSetup() {
		if(!CmsisConstants.MPU.equals(getZoneMode()))
				return;
		for(ICpZone zone : getZones()) {
			zone.createMpuSetup();
		}
	}

}
