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

package com.arm.cmsis.pack.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Class filtering packs
 */
public class CpPackFilter implements ICpPackFilter {

    protected boolean fbUseAllLatestPacks = true;
    protected Map<String, Set<String>> fPackVersions = null; // filtered pack IDs (common id , set of versions)
    protected Set<String> fLatestPackIDs = null;

    /**
     * Default empty constructor
     */
    public CpPackFilter() {
        fPackVersions = new HashMap<>();
    }

    /**
     * Copy constructor
     *
     * @param filter ICpPackFilter to copy from
     */
    public CpPackFilter(ICpPackFilter filter) {
        this();
        if (filter == null)
            return;
        setUseAllLatestPacks(filter.isUseAllLatestPacks());
        setLatestPackIDs(filter.getLatestPackIDs());
        Map<String, Set<String>> packVersions = filter.getFilterdPackVersions();
        if (packVersions != null) {
            for (Entry<String, Set<String>> e : packVersions.entrySet()) {
                Set<String> versions = e.getValue();
                if (versions != null)
                    versions = new HashSet<>(versions); // make copy
                fPackVersions.put(e.getKey(), versions);
            }
        }
    }

    @Override
    public Map<String, Set<String>> getFilterdPackVersions() {
        return fPackVersions;
    }

    @Override
    public boolean passes(ICpPack pack) {
        if (pack == null)
            return false;
        return passes(pack.getId());
    }

    @Override
    public boolean passes(final String packId) {
        String familyId = CpPack.familyFromId(packId);
        if (fbUseAllLatestPacks) {
            if (familyId.equals(packId))
                return true;
            return isLatest(packId);
        }

        if (!fPackVersions.containsKey(familyId))
            return false;

        if (familyId.equals(packId))
            return true;

        Set<String> versions = fPackVersions.get(familyId);
        if (versions == null || versions.isEmpty())
            return isLatest(packId);

        return isFixed(packId);
    }

    @Override
    public Set<String> getLatestPackIDs() {
        return fLatestPackIDs;
    }

    public String getLatestPackId(String familyId) {
        if (fLatestPackIDs != null) {
            for (String id : fLatestPackIDs) {
                if (id.startsWith(familyId))
                    return id;
            }
        }
        return null;
    }

    @Override
    public void setLatestPackIDs(Set<String> latestPackIDs) {
        fLatestPackIDs = latestPackIDs;
    }

    public boolean isLatest(final String packId) {
        if (fLatestPackIDs != null)
            return fLatestPackIDs.contains(packId);
        return false;
    }

    @Override
    public Collection<ICpPack> filter(final Collection<ICpPack> packs) {
        Collection<ICpPack> filtered = new HashSet<>();
        if (packs != null) {
            for (ICpPack pack : packs) {
                if (passes(pack))
                    filtered.add(pack);
            }
        }
        return filtered;
    }

    @Override
    public boolean isUseAllLatestPacks() {
        return fbUseAllLatestPacks;
    }

    @Override
    public void setUseAllLatestPacks(boolean bUseLatest) {
        fbUseAllLatestPacks = bUseLatest;
    }

    @Override
    public boolean isUseLatest(String packId) {
        if (fbUseAllLatestPacks)
            return true;
        if (isExcluded(packId))
            return false;
        Set<String> versions = getVersions(packId);
        return versions == null || versions.isEmpty(); // entry exists, but null or empty => use latest
    }

    @Override
    public void setUseLatest(String packId) {
        String familyId = CpPack.familyFromId(packId);
        fPackVersions.put(familyId, null);
    }

    @Override
    public void setFixed(String familyId, Set<String> fixedVersions) {
        fPackVersions.put(familyId, fixedVersions);
    }

    @Override
    public boolean isExcluded(String packId) {
        if (fbUseAllLatestPacks)
            return false;
        String familyId = CpPack.familyFromId(packId);
        return !fPackVersions.containsKey(familyId);
    }

    @Override
    public void setExcluded(String packId, boolean excluded) {
        String familyId = CpPack.familyFromId(packId);
        if (fPackVersions.containsKey(familyId))
            fPackVersions.remove(familyId);
    }

    @Override
    public boolean isFixed(String packId) {
        if (fbUseAllLatestPacks)
            return false;
        Set<String> versions = getVersions(packId);
        if (versions == null || versions.isEmpty())
            return false;

        String version = CpPack.versionFromId(packId);
        if (version.isEmpty())
            return true; // family Id is supplied => check for latest is wanted
        return versions.contains(version);
    }

    @Override
    public void setFixed(String packId, boolean fixed) {
        String version = CpPack.versionFromId(packId);
        if (version.isEmpty()) {
            if (!fixed) {
                setUseLatest(packId);
                return;
            }
            String id = getLatestPackId(packId);
            if (id == null)
                return;
            version = CpPack.versionFromId(id);
            if (version.isEmpty())
                return;
        }

        Set<String> versions = getVersions(packId);
        if (fixed) {
            if (versions == null) {
                versions = new HashSet<>();
                fPackVersions.put(CpPack.familyFromId(packId), versions);
            }
            versions.add(version);
        } else if (versions != null) {
            versions.remove(version);
        }
    }

    @Override
    public Set<String> getVersions(final String packId) {
        if (fPackVersions.isEmpty())
            return null; // we need to return null, not empty collection to indicate that the collection
                         // needs to be created!
        String familyId = CpPack.familyFromId(packId);
        return fPackVersions.get(familyId);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;

        if (!(other instanceof ICpPackFilter))
            return false;

        ICpPackFilter filter = (ICpPackFilter) other;
        if (filter.isUseAllLatestPacks() != fbUseAllLatestPacks)
            return false;
        if (fbUseAllLatestPacks)
            return true; // only this flag makes sense to compare if enabled

        if (fPackVersions.size() != filter.getFilterdPackVersions().size())
            return false;

        for (Entry<String, Set<String>> e : fPackVersions.entrySet()) {
            Set<String> versions = e.getValue();
            Set<String> otherVersions = filter.getVersions(e.getKey());
            if (versions != null && otherVersions != null) {
                if (!versions.equals(otherVersions))
                    return false;
            } else if (versions != otherVersions) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        // just to make Find Bugs happy, we do rely on the Object.hashCode()
        return super.hashCode();
    }

}
