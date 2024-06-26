/*******************************************************************************
* Copyright (c) 2021 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.build;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpMemory;

/**
 * Default implementation of IMemorySettings interface
 */
public class MemorySettings implements IMemorySettings {

    protected Map<String, ICpMemory> fRegions = null;
    protected String fStartupId = CmsisConstants.EMPTY_STRING;

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
        if (fRegions == null) {
            fRegions = new HashMap<>();
        }
        fStartupId = CmsisConstants.EMPTY_STRING;
        if (fRegions.isEmpty())
            return;
        for (Entry<String, ICpMemory> e : fRegions.entrySet()) {
            String id = e.getKey();
            ICpMemory m = e.getValue();
            if (m.isStartup()) {
                fStartupId = id;
            }
        }
    }

    @Override
    public ICpMemory getRegion(String id) {
        if (fRegions != null && id != null)
            return fRegions.get(id);
        return null;
    }

    @Override
    public String getStartupRegionId() {
        return fStartupId;
    }

}
