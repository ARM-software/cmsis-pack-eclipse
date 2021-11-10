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

package com.arm.cmsis.pack.data;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.utils.AlnumComparator;

/**
 * Class to collect pack families
 */
public class CpPackCollection extends CpItem implements ICpPackCollection {

    // fChildren from CpItem class is not used
    protected Map<String, ICpPackFamily> fPackFamilies = null;
    protected Set<String> fLatestPackIDs = null;

    public CpPackCollection() {
        super(null, CmsisConstants.PACKAGES_TAG);
    }

    public CpPackCollection(String name) {
        super(null, name);
    }

    @Override
    public ICpPack getPack(String packId) {
        if (fPackFamilies != null) {
            String familyId = CpPack.familyFromId(packId);
            ICpPackFamily f = fPackFamilies.get(familyId);
            if (f != null) {
                String version = CpPack.versionFromId(packId);
                if (version == null || version.isEmpty()) {
                    return f.getPack();
                }
                return f.getPack(version);
            }
        }
        return null;
    }

    @Override
    public ICpPack getPack(IAttributes attributes) {
        if (fPackFamilies != null) {
            String familyId = attributes.getAttribute(CmsisConstants.VENDOR) + "." //$NON-NLS-1$
                    + attributes.getAttribute(CmsisConstants.NAME);
            ICpPackFamily f = fPackFamilies.get(familyId);
            if (f != null) {
                return f.getPackByVersionRange(attributes.getAttribute(CmsisConstants.VERSION));
            }
        }
        return null;
    }

    @Override
    public Collection<? extends ICpItem> getChildren() {
        if (fPackFamilies != null) {
            return fPackFamilies.values();
        }
        return Collections.emptyList();
    }

    @Override
    public boolean hasChildren() {
        return fPackFamilies != null && !fPackFamilies.isEmpty();
    }

    @Override
    public ICpItem getFirstChild(String packId) {
        return getPack(packId);
    }

    @Override
    public void addChild(ICpItem item) {
        if (item == null) {
            return;
        }
        if (!(item instanceof ICpPack)) {
            return;
        }
        cachedChildArray = null;
        ICpPack pack = (ICpPack) item;
        if (fPackFamilies == null) {
            fPackFamilies = new TreeMap<>(new AlnumComparator(false, false));
        }

        String familyId = pack.getPackFamilyId();
        ICpPackFamily family = fPackFamilies.computeIfAbsent(familyId, k -> new CpPackFamily(this, familyId));
        family.addChild(item);
    }

    @Override
    public Collection<ICpPack> getPacks() {
        Set<ICpPack> packs = new TreeSet<>(new CpPackComparator());
        if (fPackFamilies != null) {
            for (ICpPackFamily f : fPackFamilies.values()) {
                for (ICpPack pack : f.getPacks()) {
                    if (pack.getPackState().isInstalledOrLocal() || pack.isLatest()) {
                        packs.add(pack);
                    }
                }
            }
        }
        return packs;
    }

    @Override
    public Collection<ICpPack> getFilteredPacks(ICpPackFilter packFilter) {
        if (packFilter == null || packFilter.isUseAllLatestPacks()) {
            return getLatestInstalledPacks();
        }

        Set<ICpPack> filteredPacks = new TreeSet<>(new CpPackComparator());
        if (fPackFamilies == null) {
            return filteredPacks;
        }
        for (ICpPackFamily f : fPackFamilies.values()) {
            String familyId = f.getPackFamilyId();
            if (packFilter.isExcluded(familyId)) {
                continue; // skip entire family
            }

            if (packFilter.isUseLatest(familyId)) {
                ICpPack pack = f.getLatestInstalledPack();
                if (pack != null) {
                    filteredPacks.add(pack);
                }
                continue;
            }
            filteredPacks.addAll(packFilter.filter(f.getPacks()));
        }
        return filteredPacks;
    }

    @Override
    public Collection<ICpPack> getLatestInstalledPacks() {
        Set<ICpPack> latestPacks = new TreeSet<>(new CpPackComparator());
        if (fPackFamilies == null) {
            return latestPacks;
        }
        for (ICpPackFamily f : fPackFamilies.values()) {
            ICpPack pack = f.getLatestInstalledPack();
            if (pack != null) {
                latestPacks.add(pack);
            }
        }
        return latestPacks;
    }

    @Override
    public Collection<ICpPack> getLatestEffectivePacks() {
        Set<ICpPack> latestPacks = new TreeSet<>(new CpPackComparator());
        if (fPackFamilies == null) {
            return latestPacks;
        }
        for (ICpPackFamily f : fPackFamilies.values()) {
            ICpPack pack = f.getLatestEffectivePack();
            if (pack != null) {
                latestPacks.add(pack);
            }
        }
        return latestPacks;
    }

    @Override
    public synchronized Set<String> getLatestPackIDs() {
        if (fLatestPackIDs == null) {
            fLatestPackIDs = new TreeSet<>(new CpPackIdComparator());
            if (fPackFamilies != null) {
                for (ICpPackFamily f : fPackFamilies.values()) {
                    String packId = f.getPackId();
                    if (packId != null && !packId.isEmpty()) {
                        fLatestPackIDs.add(packId);
                    }
                }
            }
        }
        return fLatestPackIDs;
    }

    @Override
    public ICpPack getPackByFilename(String pdscFile) {
        if (fPackFamilies != null) {
            for (ICpPackFamily f : fPackFamilies.values()) {
                ICpPack pack = f.getPackByFilename(pdscFile);
                if (pack != null) {
                    return pack;
                }
            }
        }
        return null;
    }

    @Override
    public Collection<ICpPack> getPacksByPackFamilyId(String packFamilyId) {
        ICpPackFamily family = getFamily(packFamilyId);
        if (family == null) {
            return Collections.emptyList();
        }
        return family.getPacks();
    }

    @Override
    public ICpPackFamily getFamily(String packFamilyId) {
        if (fPackFamilies == null) {
            return null;
        }
        return fPackFamilies.get(packFamilyId);
    }

    @Override
    public Map<String, ICpPackFamily> getFamilies() {
        return fPackFamilies;
    }

}
