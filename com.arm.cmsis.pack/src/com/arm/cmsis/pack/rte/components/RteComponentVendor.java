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

package com.arm.cmsis.pack.rte.components;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.arm.cmsis.pack.CpStrings;
import com.arm.cmsis.pack.enums.EComponentAttribute;
import com.arm.cmsis.pack.utils.VersionComparator;

/**
 * Class represent Cvendor hierarchy level, contains collection of variants.
 * Direct child for bundles and components
 */
public class RteComponentVendor extends RteComponentItem {

    /**
     * @param parent
     */
    public RteComponentVendor(IRteComponentItem parent, String name) {
        super(parent, name);
        fComponentAttribute = EComponentAttribute.CVERSION;
    }

    @Override
    public Map<String, IRteComponentItem> createMap() {
        // versions are sorted in descending order
        return new TreeMap<String, IRteComponentItem>(new VersionComparator());
    }

    @Override
    public Collection<String> getVersionStrings() {
        IRteComponent component = getParentComponent();
        if (component != null && component.hasBundle()) {
            return null;
        }
        return getKeys();
    }

    @Override
    public String getDefaultChildName() {
        return CpStrings.RteComponentVersionLatest;
    }

    @Override
    public String getActiveVersion() {
        return getActiveChildName();
    }

    @Override
    public void setActiveVersion(String version) {
        setActiveChild(version);
    }

    @Override
    public boolean isUseLatestVersion() {
        return isActiveChildDefault();
    }

}
