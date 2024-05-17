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

import java.util.ArrayList;
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

/**
 * Class that represents component taxonomy level that can be selected.<br>
 * Contain collection of component variant items.
 */
public class RteComponent extends RteComponentItem implements IRteComponent {

    protected int fnSelected = 0; // number of selected instances
    protected boolean fbBootStrap = false; // flag indicating that at least one of the component variants is a bootstrap
                                           // one
    protected boolean fbGenerated = false; // one of the component variants is generated

    public RteComponent(IRteComponentItem parent, String name) {
        super(parent, name);
        fComponentAttribute = EComponentAttribute.CVARIANT;
    }

    @Override
    public boolean isSelected() {
        return fnSelected > 0;
    }

    @Override
    public int getSelectedCount() {
        return fnSelected;
    }

    @Override
    public boolean isGenerated() {
        return fbGenerated;
    }

    @Override
    public boolean isBootStrap() {
        return fbBootStrap;
    }

    @Override
    public boolean isUseLatestVersion() {
        if (hasBundle()) {
            return getParentBundle().isUseLatestVersion();
        }
        return super.isUseLatestVersion();
    }

    @Override
    public String getEffectiveName() {
        String name = super.getEffectiveName();
        if (name.isEmpty()) {
            IRteComponentGroup g = getParentGroup();
            if (g != null) {
                name = g.getName();
            }
        }
        return name;
    }

    @Override
    public boolean setSelected(int count) {
        if (fnSelected == count) {
            return false;
        }
        fnSelected = count;
        return true;
    }

    @Override
    public int getMaxInstanceCount() {
        ICpComponent c = getActiveCpComponent();
        if (c != null) {
            return c.getMaxInstances();
        }
        return 0;
    }

    @Override
    public void addComponent(ICpComponent cpComponent, int flags) {
        if (cpComponent == null)
            return;
        if (cpComponent.isApi()) {
            return;
        }

        boolean generated = cpComponent.isGenerated();

        if (cpComponent instanceof ICpComponentInfo) {
            addComponentInfo((ICpComponentInfo) cpComponent, flags);
            return;
        }

        if (cpComponent.isBootStrap()) {
            // bootstrap component comes from a regular pack
            fbBootStrap = true;
        }

        // add variant, vendor and version items
        String variant = cpComponent.getAttribute(CmsisConstants.CVARIANT);
        if (isGenerated()) {
            if (variant.equals(getActiveVariant()))
                return; // do not insert a bootstrap component itself
        }

        IRteComponentItem variantItem = getChild(variant);
        if (variantItem == null) {
            variantItem = new RteComponentVariant(this, variant);
            addChild(variantItem);
        }

        // first try to get supplied vendor
        String vendor = cpComponent.getVendor();
        IRteComponentItem vendorItem = variantItem.getChild(vendor);
        if (vendorItem == null) {
            vendorItem = new RteComponentVendor(variantItem, cpComponent.getVendor());
            variantItem.addChild(vendorItem);
        }

        String version = cpComponent.getVersion();
        IRteComponentItem versionItem = vendorItem.getChild(version);
        if (versionItem == null) {
            versionItem = new RteComponentVersion(vendorItem, cpComponent.getVersion());
            vendorItem.addChild(versionItem);
        }

        // set the generated flag now
        if (generated) {
            fbGenerated = generated;
            setSelected(1);
        }

        versionItem.addComponent(cpComponent, flags);
        if (generated || (!isGenerated() && cpComponent.isDefaultVariant())) {
            setActiveChild(variant);
        }
    }

    protected void addComponentInfo(ICpComponentInfo ci, int flags) {

        // calculate ignore flags
        boolean versionFixed = (flags & RteConstants.COMPONENT_IGNORE_VERSION) == 0 && ci.isVersionFixed();
        // version is fixed => variant and vendor implicitly fixed too
        boolean vendorFixed = versionFixed || ((flags & RteConstants.COMPONENT_IGNORE_VENDOR) == 0);
        String variant = ci.getAttribute(CmsisConstants.CVARIANT);
        boolean variantFixed = versionFixed
                || ((flags & RteConstants.COMPONENT_IGNORE_VARIANT) == 0 && !variant.isEmpty());

        if (isGenerated()) {
            // in case of a generated component we do not care about bootstrap attributes
            versionFixed = vendorFixed = variantFixed = false;
        }

        // add variant, vendor and version items
        // try to get supplied variant
        IRteComponentItem variantItem = getChild(variant);
        if (isGenerated() || (variantItem == null && !variantFixed)) {
            variantItem = getChild(getActiveVariant());
        }
        if (variantItem == null) {
            if (hasChildren()) {
                ci.setEvaluationResult(EEvaluationResult.MISSING_VARIANT);
            } else {
                ci.setEvaluationResult(EEvaluationResult.MISSING);
            }
            variantItem = new RteComponentVariant(this, variant);
            addChild(variantItem);
        }
        variant = variantItem.getName(); // ensure actual variant string to select active variant

        // try to get supplied vendor
        String vendor = ci.getVendor();
        IRteComponentItem vendorItem = variantItem.getChild(vendor);
        if (isGenerated() || (vendorItem == null && !vendorFixed)) {
            vendorItem = variantItem.getActiveChild();
        }
        if (vendorItem == null) {
            if (variantItem.hasChildren()) {
                // there are some vendors in the collection, but not what is needed
                ci.setEvaluationResult(EEvaluationResult.MISSING_VENDOR);
            } else {
                ci.setEvaluationResult(EEvaluationResult.MISSING);
            }
            vendorItem = new RteComponentVendor(variantItem, vendor);
            variantItem.addChild(vendorItem);
        }

        String version = null;
        if (versionFixed) {
            version = ci.getVersion();
        }
        IRteComponentItem versionItem = vendorItem.getChild(version);
        if (versionItem == null) {
            if (vendorItem.hasChildren()) {
                // there are some versions in the collection, but not what is needed
                ci.setEvaluationResult(EEvaluationResult.MISSING_VERSION);
            } else {
                ci.setEvaluationResult(EEvaluationResult.MISSING);
            }
            versionItem = new RteComponentVersion(vendorItem, ci.getVersion());
            vendorItem.addChild(versionItem);
        }

        versionItem.addComponent(ci, flags);

        setSelected(ci.getInstanceCount());
        setActiveChild(variant);
        variantItem.setActiveChild(vendor);
        vendorItem.setActiveChild(version);
    }

    @Override
    public void addCpItem(ICpItem cpItem) {
        if (cpItem instanceof ICpComponent) {
            addComponent((ICpComponent) cpItem, RteConstants.NONE);
        } else if (cpItem instanceof ICpTaxonomy) {
            String csub = cpItem.getAttribute(CmsisConstants.CGROUP);
            if (csub.equals(getName())) {
                if (getTaxonomy() == null) {
                    fTaxonomy = cpItem;
                }
                return;
            }
        }
    }

    @Override
    public void setActiveComponentInfo(ICpComponentInfo ci) {
        if (ci == null) {
            return;
        }
        addComponent(ci, RteConstants.NONE);
    }

    @Override
    public IRteComponent getParentComponent() {
        return this;
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
        setActiveChild(variant);
    }

    @Override
    public String getActiveVendor() {
        if (hasBundle()) {
            return CmsisConstants.EMPTY_STRING;
        }
        return super.getActiveVendor();
    }

    @Override
    public void setActiveVendor(String vendor) {
        if (hasBundle()) {
            return;
        }
        super.setActiveVendor(vendor);
    }

    @Override
    public String getActiveVersion() {
        return super.getActiveVersion();
    }

    @Override
    public void setActiveVersion(String version) {
        if (hasBundle()) {
            return;
        }
        super.setActiveVersion(version);
    }

    @Override
    public Collection<IRteComponent> getSelectedComponents(Collection<IRteComponent> components) {
        // is we are here => component is active
        if (isSelected()) {
            if (components == null) {
                components = new ArrayList<IRteComponent>();
            }
            components.add(this);
        }
        return components;
    }

    @Override
    public Collection<IRteComponent> getUsedComponents(Collection<IRteComponent> components) {
        // is we are here => component is active
        ICpComponentInfo ci = getActiveCpComponentInfo();
        if (ci != null) {
            if (components == null) {
                components = new ArrayList<IRteComponent>();
            }
            components.add(this);
        }
        return components;
    }

    @Override
    public Collection<IRteComponent> getGeneratorComponents(String generatorId, Collection<IRteComponent> components) {
        // is we are here => component is active
        if (components == null) {
            components = new ArrayList<IRteComponent>();
        }
        if (!isGenerated() && !isBootStrap())
            return components;

        ICpComponent c = getActiveCpComponent();
        if (c == null)
            return components;
        String genId = c.getGeneratorId();
        if (genId != null && genId.equals(generatorId)) {
            components.add(this);
        }
        return components;
    }

    @Override
    public String getGeneratorId() {
        ICpComponent c = getActiveCpComponent();
        if (c != null)
            return c.getGeneratorId();
        return null;
    }

    @Override
    public EEvaluationResult findComponents(IRteDependency dependency) {
        EEvaluationResult result = super.findComponents(dependency);
        if (result == EEvaluationResult.SELECTABLE) {
            if (isSelected()) {
                result = EEvaluationResult.FULFILLED;
            }
        } else if (result.ordinal() >= EEvaluationResult.INSTALLED.ordinal()) {
            if (!isActive()) {
                result = EEvaluationResult.INACTIVE;
            }
        }
        dependency.addComponent(this, result);
        return result;
    }

    @Override
    public int getUseCount() {
        if (isGenerated())
            return fnSelected;
        ICpComponentInfo ci = getActiveCpComponentInfo();
        if (ci != null) {
            return ci.getInstanceCount();
        }
        return 0;
    }
}
