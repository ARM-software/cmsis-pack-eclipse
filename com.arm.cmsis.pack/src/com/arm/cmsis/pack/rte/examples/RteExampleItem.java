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

package com.arm.cmsis.pack.rte.examples;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpEnvironmentProvider;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpExample;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.item.CmsisMapItem;
import com.arm.cmsis.pack.utils.AlnumComparator;
import com.arm.cmsis.pack.utils.VersionComparator;

/**
 * Default implementation of IRteExampleItem
 */
public class RteExampleItem extends CmsisMapItem<IRteExampleItem> implements IRteExampleItem {

    protected Map<String, ICpExample> fExamples = null;
    protected boolean fRoot;
    protected Boolean fSupported = null;
    protected Boolean fImport = null;
    protected String fEnvironment = null;
    protected String fLoadPath = null;
    protected String fProjectFolder = null;
    protected ICpPack fPack = null; // example's pack

    /**
     * Default constructor, used for the root node
     */
    public RteExampleItem() {
        fName = "All Examples"; //$NON-NLS-1$
        fRoot = true;
        fSupported = true;
    }

    /**
     * Constructor with name and parent
     *
     * @param name   the name
     * @param parent the parent
     */
    public RteExampleItem(String name, IRteExampleItem parent) {
        super(parent);
        fName = name;
        fRoot = false;
    }

    @Override
    protected Map<String, IRteExampleItem> createMap() {
        // create TreeMap with Alpha-Numeric case-insensitive ascending sorting
        return new TreeMap<String, IRteExampleItem>(new AlnumComparator(false, false));
    }

    public static IRteExampleItem createTree(Collection<ICpPack> packs) {
        IRteExampleItem root = new RteExampleItem();
        if (packs == null || packs.isEmpty()) {
            return root;
        }
        for (ICpPack pack : packs) {
            root.addExamples(pack);
        }
        return root;
    }

    @Override
    public void addExample(ICpExample item) {
        if (item == null) {
            return;
        }

        if (fRoot) {
            addExampleItem(item, item.getId());
        } else {
            ICpPack pack = item.getPack();
            String packId = pack.getId();
            if (fExamples == null) {
                fExamples = new TreeMap<String, ICpExample>(new VersionComparator());
            }

            ICpExample example = fExamples.get(packId);
            if (example == null ||
            // new item's pack is installed/downloaded and the one in the tree is not
                    (item.getPack().getPackState().ordinal() < example.getPack().getPackState().ordinal())) {
                fExamples.put(packId, item);
            }
        }
    }

    protected void addExampleItem(ICpExample item, final String itemName) {
        IRteExampleItem ei = getChild(itemName);
        if (ei == null) {
            ei = new RteExampleItem(itemName, this);
            addChild(ei);
        }
        ei.addExample(item);
    }

    @Override
    public void removeExample(ICpExample item) {
        if (item == null) {
            return;
        }

        if (fRoot) {
            IRteExampleItem e = getChild(item.getId());
            if (e == null) {
                return;
            }
            e.removeExample(item);
        } else {
            if (fExamples == null) {
                return;
            }

            String packId = item.getPackId();
            fExamples.remove(packId);

            if (fExamples.size() == 0) {
                getParent().removeChild(this);
                setParent(null);
            }
            return;
        }
    }

    @Override
    public void removeExamples(ICpPack pack) {
        if (pack == null) {
            return;
        }
        Collection<? extends ICpItem> examples = pack.getGrandChildren(CmsisConstants.EXAMPLES_TAG);
        if (examples != null) {
            for (ICpItem item : examples) {
                if (!(item instanceof ICpExample)) {
                    continue;
                }
                ICpExample currentExample = (ICpExample) item;
                removeExample(currentExample);
            }
        }
    }

    @Override
    public ICpExample getExample() {
        if (fExamples != null && !fExamples.isEmpty()) {
            // Return the latest INSTALLED pack's example
            for (ICpExample example : fExamples.values()) {
                if (example.getPack().getPackState().isInstalledOrLocal()) {
                    return example;
                }
            }
            // Otherwise return the latest pack's board
            return fExamples.values().iterator().next();
        }
        return null;
    }

    @Override
    public Collection<ICpExample> getExamples() {
        if (fExamples != null) {
            return fExamples.values();
        }
        return null;
    }

    @Override
    public void addExamples(ICpPack pack) {
        if (pack == null) {
            return;
        }
        Collection<? extends ICpItem> examples = pack.getGrandChildren(CmsisConstants.EXAMPLES_TAG);
        if (examples == null) {
            return;
        }
        ICpEnvironmentProvider envProvider = CpPlugIn.getEnvironmentProvider();

        for (ICpItem item : examples) {
            if (!(item instanceof ICpExample)) {
                continue;
            }
            ICpExample example = (ICpExample) item;
            if (envProvider == null || !envProvider.isExampleSupported(example))
                continue;

            addExample(example);
        }
    }

    @Override
    public ICpPack getPack() {
        if (fPack == null) {
            ICpExample example = getExample();
            if (example != null)
                fPack = example.getPack();
        }
        return fPack;
    }

    @Override
    public boolean isSupported() {
        if (fSupported == null) {
            ICpEnvironmentProvider envProvider = CpPlugIn.getEnvironmentProvider();
            fSupported = envProvider.isExampleSupported(getExample());
        }
        return fSupported;
    }

    @Override
    public String getEnvironment() {
        if (fEnvironment == null && isSupported()) {
            ICpEnvironmentProvider envProvider = CpPlugIn.getEnvironmentProvider();
            fEnvironment = envProvider.getEnvironment(getExample());
        }
        return fEnvironment;
    }

    @Override
    public String getLoadPath() {
        if (fLoadPath == null && isSupported()) {
            ICpEnvironmentProvider envProvider = CpPlugIn.getEnvironmentProvider();
            fLoadPath = envProvider.getAbsoluteLoadPath(getExample());
        }
        return fLoadPath;
    }

    @Override
    public String getProjectFolder() {
        if (fProjectFolder == null && isSupported()) {
            ICpEnvironmentProvider envProvider = CpPlugIn.getEnvironmentProvider();
            fProjectFolder = envProvider.getProjectFolder(getExample());
        }
        return fProjectFolder;
    }

    @Override
    public boolean isToImport() {
        if (fImport == null && isSupported()) {
            String loadPath = getLoadPath();
            fImport = loadPath != null && !loadPath.endsWith(CmsisConstants.DOT_PROJECT);
        }
        return fImport;
    }

}
