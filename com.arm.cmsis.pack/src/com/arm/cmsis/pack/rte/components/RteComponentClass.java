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

package com.arm.cmsis.pack.rte.components;

import java.util.Collection;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpComponent;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpTaxonomy;
import com.arm.cmsis.pack.enums.EComponentAttribute;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.info.ICpComponentInfo;
import com.arm.cmsis.pack.rte.RteConstants;
import com.arm.cmsis.pack.rte.dependencies.IRteDependency;
import com.arm.cmsis.pack.rte.dependencies.RteDependency;

/**
 * Class represents Cclass component hierarchy level, contains collection of
 * bundles
 */
public class RteComponentClass extends RteComponentItem implements IRteComponentClass {

    /**
     * @param parent
     */
    public RteComponentClass(IRteComponentItem parent, String name) {
        super(parent, name);
        fComponentAttribute = EComponentAttribute.CBUNDLE;
    }

    @Override
    public IRteComponentClass getParentClass() {
        return this;
    }

    @Override
    public void addComponent(ICpComponent cpComponent, int flags) {
        String bundleName = cpComponent.getBundleName();
        ICpComponentInfo ci = null;
        if (cpComponent instanceof ICpComponentInfo) {
            ci = (ICpComponentInfo) cpComponent;
        }
        // ensure childItem
        IRteComponentItem bundleItem = getChild(bundleName);
        if (bundleItem == null && hasChildren() && ci != null) {
            // try to search existing bundles
            bundleItem = findBundle(ci);
            if (bundleItem != null) {
                bundleName = bundleItem.getName();
            } else {
                // there are some bundles, but not what is needed
                ci.setEvaluationResult(EEvaluationResult.MISSING_BUNDLE);
            }
        }
        if (bundleItem == null) {
            bundleItem = new RteComponentBundle(this, bundleName);
            addChild(bundleItem);
        }

        bundleItem.addComponent(cpComponent, flags);

        if (ci != null) {
            setActiveChild(bundleName);
        } else {
            ICpItem bundle = cpComponent.getParent(CmsisConstants.BUNDLE_TAG);
            if (bundle != null && bundle.isDefaultVariant()) {
                setActiveChild(bundleName);
            }
        }
    }

    @Override
    public void addCpItem(ICpItem cpItem) {
        if (cpItem instanceof ICpTaxonomy) {
            String cgroup = cpItem.getAttribute(CmsisConstants.CGROUP);
            if (cgroup == null || cgroup.isEmpty()) {
                if (getTaxonomy() == null) {
                    fTaxonomy = cpItem;
                }
                return;
            }
        }
        super.addCpItem(cpItem);
    }

    @Override
    public Collection<String> getVariantStrings() {
        return getKeys();
    }

    @Override
    public String getActiveVariant() {
        return getActiveChildName();
    }

    @Override
    public void setActiveVariant(String variant) {
        // store selected components to select them in a new bundle
        Collection<IRteComponent> components = getSelectedComponents(null); // the collection will be allocated

        boolean changed = setActiveChild(variant);
        if (!changed || components == null || components.isEmpty())
            return;

        IRteComponentItem activeBundle = getActiveChild();
        // try to select similar components in the new bundle
        for (IRteComponent rteComponent : components) {
            ICpComponent c = rteComponent.getActiveCpComponent();
            if (c == null)
                continue;
            IRteComponent toSelect = findMatchingComponent(c, activeBundle);

            if (toSelect != null) {
                int nsel = rteComponent.getSelectedCount();
                toSelect.setSelected(nsel);
            }
        }
    }

    /**
     * Tries to find matching component for the bundle
     *
     * @param c ICpCompomnent or ICpComponentInstance
     * @return matching IRteComponent if found or null
     */
    private IRteComponent findMatchingComponent(ICpComponent c, IRteComponentItem bundle) {
        IRteDependency dep = new RteDependency(c, RteConstants.COMPONENT_IGNORE_ALL);
        EEvaluationResult res = bundle.findComponents(dep);
        if (res == EEvaluationResult.SELECTABLE) {
            return dep.getBestMatch();
        }
        return null;
    }

    /**
     * Tries to find matching bundle
     *
     * @param cpComponent
     * @return
     */
    private IRteComponentItem findBundle(ICpComponent cpComponent) {
        if (cpComponent == null || !hasChildren())
            return null;
        IRteComponentItem activeBundle = getActiveChild();
        IRteComponent rteComponent = findMatchingComponent(cpComponent, activeBundle);
        if (rteComponent != null)
            return activeBundle;

        for (IRteComponentItem bundle : children()) {
            if (bundle == activeBundle)
                continue;
            rteComponent = findMatchingComponent(cpComponent, activeBundle);
            if (rteComponent != null)
                return bundle;
        }

        return null;
    }

}
