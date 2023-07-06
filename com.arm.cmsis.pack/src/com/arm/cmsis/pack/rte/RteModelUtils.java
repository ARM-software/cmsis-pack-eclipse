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

package com.arm.cmsis.pack.rte;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpPack;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPackCollection;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.info.ICpItemInfo;
import com.arm.cmsis.pack.info.ICpPackInfo;
import com.arm.cmsis.pack.rte.dependencies.IRteDependencyItem;
import com.arm.cmsis.pack.utils.VersionComparator;

/**
 * Utility class to perform operations over IRteModel
 */
public class RteModelUtils {

    /**
     * Private constructor to prevent class instantiation
     */
    private RteModelUtils() {
        // does nothing
    }

    /**
     * Return a collection of missing pack IDs
     *
     * @param model the RTE model
     * @return a collection of missing pack IDs or an empty collection
     */
    public static Collection<String> getMissingPacks(IRteModel model) {
        Set<String> missingPacks = new HashSet<>();
        if (model == null) {
            return missingPacks;
        }
        Collection<? extends IRteDependencyItem> results = model.getDependencyItems();
        for (IRteDependencyItem item : results) {
            ICpItem cpItem = item.getCpItem();
            if (cpItem instanceof ICpItemInfo) {
                ICpItemInfo ci = (ICpItemInfo) cpItem;
                ICpPackInfo pi = ci.getPackInfo();
                if (pi != null) {
                    ICpPack pack = pi.getPack();
                    if (pack == null || !pack.getPackState().isInstalledOrLocal()) {
                        missingPacks.add(constructEffectivePackId(pi.attributes()));
                    }
                }
            }
        }
        return missingPacks;
    }

    /**
     * Constructs an effective pack ID from supplied attributes
     *
     * @param packAttributes
     * @return effective pack ID
     */
    public static String constructEffectivePackId(IAttributes packAttributes) {
        if (packAttributes == null)
            return CmsisConstants.EMPTY_STRING;
        String vendor = packAttributes.getAttribute(CmsisConstants.VENDOR);
        String name = packAttributes.getAttribute(CmsisConstants.NAME);
        String version = VersionComparator.removeMetadata(packAttributes.getAttribute(CmsisConstants.VERSION));
        String packId = vendor + '.' + name;
        if (CmsisConstants.FIXED.equals(packAttributes.getAttribute(CmsisConstants.VERSION_MODE))) { // use fixed
                                                                                                     // version of the
                                                                                                     // pack
            packId += '.' + version;
        } else { // use latest compatible version of the pack
            ICpPackCollection allPacks = CpPlugIn.getPackManager().getPacks();
            if (allPacks == null) {
                return CmsisConstants.EMPTY_STRING;
            }
            String familyId = CpPack.familyFromId(packId);
            Collection<? extends ICpItem> packs = allPacks.getPacksByPackFamilyId(familyId);
            if (packs == null || packs.isEmpty()) {
                return CmsisConstants.EMPTY_STRING;
            }
            ICpItem latestPack = packs.iterator().next();
            String latestVersion = VersionComparator.removeMetadata(latestPack.getVersion());
            int verCmp = VersionComparator.versionCompare(latestVersion, version);
            if (CpPack.isPackFamilyId(packId) && verCmp >= 0) { // compatible
                packId += '.' + latestVersion;
            } else {
                packId += '.' + version;
            }
        }
        return packId;
    }

}
