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

package com.arm.cmsis.zone.parser;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpDebug;
import com.arm.cmsis.pack.data.ICpDeviceItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpMemory;
import com.arm.cmsis.pack.parser.ICpXmlParser;
import com.arm.cmsis.pack.utils.AlnumComparator;
import com.arm.cmsis.zone.svd.ISvdPeripheral;
import com.arm.cmsis.zone.svd.ISvdRoot;
import com.arm.cmsis.zone.svd.SvdParser;

/**
 * Helper class to collect memory and peripheral information from SVD file
 */
public class MemoryRegionCollector {
    private Map<String, ICpMemory> regions = new TreeMap<>(new AlnumComparator(false));
    private Map<String, Map<String, ISvdPeripheral>> svdPeripherals = new TreeMap<>(new AlnumComparator(false));

    public Map<String, ICpMemory> getRegions() {
        return regions;
    }

    public Map<String, Map<String, ISvdPeripheral>> getSvdPeripherals() {
        return svdPeripherals;
    }

    public void collectMemoryFromPdsc(ICpDeviceItem device, String processorName) {

        ICpItem ep = device.getEffectiveProperties(processorName);
        if (ep == null)
            return;

        Collection<? extends ICpItem> children = ep.getChildren();

        for (ICpItem p : children) {
            String pname = p.getProcessorName();
            if (!pname.equals(processorName))
                continue; // only process required processor

            if (p instanceof ICpDebug) {
                ICpDebug dbgItem = (ICpDebug) p;
                String svdFile = dbgItem.getSvdFile();
                if (svdFile.isEmpty())
                    continue;
                addSvdItems(svdFile);
            } else if (p instanceof ICpMemory) {
                ICpMemory memory = (ICpMemory) p;
                addMemory(memory);
            }
        }
    }

    private void addSvdItems(String svdFile) {
        ICpXmlParser parser = new SvdParser();
        ICpItem root = parser.parseFile(svdFile);
        if (!(root instanceof ISvdRoot))
            return;

        ISvdRoot svdRoot = (ISvdRoot) root;
        Map<String, ISvdPeripheral> peripherals = svdRoot.getPeripheralMap();
        for (ISvdPeripheral p : peripherals.values()) {
            addPeripheral(p);
        }
    }

    protected void addMemory(ICpMemory memory) {
        if (memory == null)
            return;
        String name = memory.getName();
        regions.putIfAbsent(name, memory);
    }

    public String getPhysicalAddress(ICpMemory memory, Set<ICpMemory> viewed) {
        if (!memory.hasAttribute(CmsisConstants.ALIAS)) {
            return memory.getStartString();
        }
        if (viewed != null && viewed.contains(memory)) {
            return memory.getStartString(); // recursion end
        }
        String alias = memory.getAttribute(CmsisConstants.ALIAS);
        ICpMemory region = regions.get(alias);
        if (region == null)
            return null; // no reference region is found : own address will be physical
        // ask region
        if (viewed == null) {
            viewed = new HashSet<>();
        }
        viewed.add(memory);
        return getPhysicalAddress(region, viewed);
    }

    protected void addPeripheral(ISvdPeripheral p) {
        if (p == null)
            return;
        String name = p.getName();
        String groupName = p.getGroupName();
        Map<String, ISvdPeripheral> group = svdPeripherals.computeIfAbsent(groupName,
                k -> new TreeMap<>(new AlnumComparator(false)));
        group.put(name, p);
    }
}