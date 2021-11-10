/*******************************************************************************
* Copyright (c) 2021 ARM Ltd. and others
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
import java.util.Map;
import java.util.Map.Entry;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpDeviceItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpMemory;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.utils.Utils;
import com.arm.cmsis.zone.parser.MemoryRegionCollector;
import com.arm.cmsis.zone.svd.ISvdInterrupt;
import com.arm.cmsis.zone.svd.ISvdPeripheral;

/*
 * Resource Zone
 */
public class CpResourceZone extends CpRootZone implements ICpResourceZone {

    /**
     * XML constructor
     *
     * @param parent parent ICpItem
     * @param tag    item's tag
     */
    public CpResourceZone(ICpItem parent, String tag) {
        super(parent, tag);
    }

    /**
     * System Zone constructor
     *
     * @param parent     item parent
     * @param tool       string ID of the tool creating the resource file
     * @param deviceInfo
     */
    public CpResourceZone(ICpItem parent, ICpDeviceInfo deviceInfo, String tool) {
        super(parent, CmsisConstants.RZONE);
        ICpZoneCreator creator = new CpZoneCreator(this, CmsisConstants.CREATOR);
        creator.setTool(tool);
        addChild(creator);
        ICpDeviceItem device = deviceInfo.getDevice();
        ICpDeviceUnit du = new CpDeviceUnit(parent, device);
        addChild(du);
        addResources(device);
    }

    /**
     * Adds resources from device
     */
    protected void addResources(ICpDeviceItem device) {
        if (device == null)
            return;
        Map<String, ICpItem> processors = device.getProcessors();
        if (processors.size() > 1) {
            // first add processor-independent resources
            addResources(device, CmsisConstants.EMPTY_STRING);
        }
        for (Entry<String, ICpItem> item : processors.entrySet()) {
            String pname = item.getKey();
            addResources(device, pname);
        }
    }

    protected void addResources(ICpDeviceItem device, String processorName) {
        ICpResourceGroup memoryContainer = ensureMemoryContainer();
        ICpResourceGroup peripheralContainer = ensurePeripheralContainer();

        MemoryRegionCollector collector = new MemoryRegionCollector();
        collector.collectMemoryFromPdsc(device, processorName);
        // first insert peripherals
        for (Entry<String, Map<String, ISvdPeripheral>> entry : collector.getSvdPeripherals().entrySet()) {
            String groupName = entry.getKey();
            Map<String, ISvdPeripheral> group = entry.getValue();
            ICpItem parent = peripheralContainer;
            if (!groupName.isEmpty() && group.size() > 1) {
                parent = new CpPeripheralGroup(peripheralContainer, CmsisConstants.GROUP);
                parent.setAttribute(CmsisConstants.NAME, groupName);
                peripheralContainer.addChild(parent);
            }
            for (Entry<String, ISvdPeripheral> pe : group.entrySet()) {
                String name = pe.getKey();
                ISvdPeripheral svdPeripheral = pe.getValue();
                ICpItem p = createPeripheral(parent, name, svdPeripheral, processorName);
                // add interrupts if any
                Collection<ISvdInterrupt> interrupts = svdPeripheral.getInterrups();
                for (ISvdInterrupt i : interrupts) {
                    ICpInterrupt cpInterrupt = new CpInterrupt(p, CmsisConstants.INTERRUPT);
                    p.addChild(cpInterrupt);
                    cpInterrupt.setAttribute(CmsisConstants.NAME, i.getName());
                    cpInterrupt.setAttribute(CmsisConstants.IRQN, i.getValueString());
                    cpInterrupt.setAttribute(CmsisConstants.INFO, i.getDescription());
                }
            }
        }

        // add memories
        // insert memory regions
        for (Entry<String, ICpMemory> me : collector.getRegions().entrySet()) {
            String regionName = me.getKey();
            ICpMemory mem = me.getValue();
            if (mem.isPeripheralAccess()) {
                createPeripheral(peripheralContainer, regionName, mem, processorName);
                continue;
            }
            ICpMemoryBlock r = new CpMemoryBlock(memoryContainer, mem);
            memoryContainer.addChild(r);
            r.setAttribute(CmsisConstants.NAME, regionName);
            if (processorName != null && !processorName.isEmpty())
                r.setAttribute(CmsisConstants.PNAME, processorName);

            String alias = mem.getAttribute(CmsisConstants.ALIAS);
            if (!alias.isEmpty()) {
                String physical = collector.getPhysicalAddress(mem, null);
                if (physical != null && !physical.isEmpty()) {
                    r.setAttribute(CmsisConstants.PHYSICAL, physical);
                }
            }
        }
    }

    private ICpItem createPeripheral(ICpItem parent, String name, ICpMemory memory, String processorName) {
        ICpItem p = parent.getFirstItem(name);
        if (p != null) {
            // already inserted => no new entry
            return p; // actually an error
        }
        p = new CpPeripheral(parent, memory);
        parent.addChild(p);
        p.setAttribute(CmsisConstants.NAME, name);
        if (processorName != null && !processorName.isEmpty())
            p.setAttribute(CmsisConstants.PNAME, processorName);
        return p;
    }

    @Override
    public boolean updateResources(ICpZone zone, String tool) {
        boolean bChanged = false;
        ICpZoneCreator creator = getZoneCreator();
        if (creator == null) {
            creator = new CpZoneCreator(this, CmsisConstants.CREATOR);
            addChild(creator);
        }
        creator.setTool(tool);
        ICpRootZone rootZone = zone.getRootZone();
        String azone = "../" + Utils.extractFileName(rootZone.getRootFileName()); //$NON-NLS-1$
        creator.setAttribute(CmsisConstants.AZONE, azone);
        String rzone = "../" + Utils.extractFileName(rootZone.getResourceFileName()); //$NON-NLS-1$
        creator.setAttribute(CmsisConstants.RZONE, rzone);
        creator.setSecurity(zone.getSecurity());
        creator.setPrivilige(zone.getPrivilege());
        creator.setAttribute(CmsisConstants.INFO, zone.getInfo());

        ICpDeviceUnit d = getDeviceUnit();
        if (d == null) {
            d = new CpDeviceUnit(this, CmsisConstants.DEVICE_TAG);
            addChild(d);
            bChanged = true;
        }
        if (d.updateDevice(zone)) {
            bChanged = true;
        }
        if (updateMemoryResources(zone)) {
            bChanged = true;
        }

        if (updateMpcItems(zone)) {
            bChanged = true;
        }

        if (updateSauInit(zone)) {
            bChanged = true;
        }
        return bChanged;
    }

    protected boolean updateSauInit(ICpZone zone) {
        // add sau_init TODO : check if equal
        ICpRootZone thatRootZone = zone.getRootZone();
        ICpResourceContainer thatResources = thatRootZone.getResourceContainer();
        ICpSauInit thatSauInit = (thatResources != null) ? thatResources.getSauInit() : null;

        ICpResourceContainer resources = ensureResourceContainer();
        ICpSauInit thisSauInit = resources.getSauInit();

        if (thisSauInit == null) {
            if (thatSauInit == null) {
                return false;
            }
            thatSauInit.copyTo(resources);
            return true;
        }

        if (thatSauInit == null) {
            resources.removeChild(thisSauInit);
            return true;
        }

        return thisSauInit.updateItem(thatSauInit);
    }

    protected boolean updateMpcItems(ICpZone zone) {
        // add mpc elements TODO : check if equal
        ICpRootZone thatRootZone = zone.getRootZone();
        ICpResourceGroup memoryContainer = ensureMemoryContainer();

        Collection<ICpMpc> mpcItems = memoryContainer.getChildrenOfType(ICpMpc.class);
        for (ICpMpc mpc : mpcItems) {
            memoryContainer.removeChild(mpc);
        }

        ICpResourceGroup thatMemoryContainer = thatRootZone.getMemoryGroup();
        mpcItems = thatMemoryContainer.getChildrenOfType(ICpMpc.class);
        for (ICpMpc mpc : mpcItems) {
            mpc.copyTo(memoryContainer);
        }
        return true;
    }

    /**
     * Adds resources assigned to the supplied project Zone
     *
     * @param zone ICpZone to get assignments from
     */
    protected boolean updateMemoryResources(ICpZone zone) {
        boolean bChanged = false;
        ICpResourceContainer resources = ensureResourceContainer();
        resources.clear();
        ICpResourceGroup memoryContainer = ensureMemoryContainer();
        ICpResourceGroup peripheralContainer = ensurePeripheralContainer();

        Map<String, ICpMemoryBlock> regions = resources.getMemoryBlocksAsMap();

        Collection<ICpZoneAssignment> assignments = zone.getZoneAssignments();
        for (ICpZoneAssignment a : assignments) {
            ICpMemoryBlock block = a.getAssignedBlock();
            String id = block.getId();
            if (id.isEmpty()) {
                bChanged = true;
                continue;
            }
            ICpMemoryBlock region = regions.get(id);
            if (region == null) {
                if (block.isPeripheralAccess()) {
                    ICpItem parentContainer = peripheralContainer;
                    // preserve hierarchy when adding peripherals
                    if (block.hasAttribute(CmsisConstants.GROUP)) {
                        String groupName = block.getAttribute(CmsisConstants.GROUP);
                        parentContainer = peripheralContainer.getFirstItem(groupName);
                        if (parentContainer == null) {
                            parentContainer = new CpPeripheralGroup(peripheralContainer, CmsisConstants.GROUP);
                            parentContainer.setAttribute(CmsisConstants.NAME, groupName);
                            peripheralContainer.addChild(parentContainer);
                            ICpPeripheralGroup thatPeripheralGroup = block.getParentPeripheralGroup();
                            Collection<ICpPeripheralSetup> setups = thatPeripheralGroup.getPeripheralSetups();
                            for (ICpPeripheralSetup setup : setups) {
                                setup.copyTo(parentContainer);
                            }
                        }
                    }
                    region = (ICpMemoryBlock) block.copyTo(parentContainer);
                } else {
                    region = new CpMemoryBlock(memoryContainer, CmsisConstants.MEMORY_TAG);
                    memoryContainer.addChild(region);
                    region.setAttributes(block);
                }
                regions.put(id, region);
                bChanged = true;
            } else if (!region.equalsAttributes(block)) {
                region.setAttributes(block);
                bChanged = true;
            }
        }

        return bChanged;
    }

}
