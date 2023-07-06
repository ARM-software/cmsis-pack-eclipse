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

package com.arm.cmsis.pack.rte.dependencies;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.arm.cmsis.pack.CpStrings;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.rte.components.IRteComponent;
import com.arm.cmsis.pack.rte.components.IRteComponentItem;

/**
 * Default implementation of IRteDependency interface
 */
public class RteDependency extends RteDependencyItem implements IRteDependency {

    protected ICpItem fCpItem = null; // component attributes to search for
    protected int fFlags = 0; // RTE flags
    // collection to store candidates to resolve dependency
    protected Map<IRteComponent, IRteDependencyItem> fComponentEntries = new LinkedHashMap<IRteComponent, IRteDependencyItem>();

    // list of component items that stop the search
    protected Map<IRteComponentItem, IRteDependencyItem> fStopItems = null;

    public RteDependency(ICpItem item) {
        fCpItem = item;
    }

    public RteDependency(ICpItem item, int flags) {
        this(item);
        fFlags = flags;
    }

    @Override
    public int getFlags() {
        return fFlags;
    }

    @Override
    public boolean isMaster() {
        return true;
    }

    @Override
    public boolean isResolved() {
        if (fResult == EEvaluationResult.IGNORED)
            return true;
        if (fResult == EEvaluationResult.FULFILLED)
            return true;
        if (fResult == EEvaluationResult.INCOMPATIBLE)
            return false;
        return false;
    }

    @Override
    public Collection<IRteComponent> getComponents() {
        return fComponentEntries.keySet();
    }

    @Override
    public ICpItem getCpItem() {
        return fCpItem;
    }

    @Override
    public EEvaluationResult getEvaluationResult(IRteComponent component) {
        IRteDependencyItem entry = fComponentEntries.get(component);
        if (entry != null) {
            EEvaluationResult result = entry.getEvaluationResult();
            return result;
        }
        return EEvaluationResult.UNDEFINED;
    }

    @Override
    public IRteComponent getBestMatch() {
        // TODO add bundle and variant calculations
        IRteComponent bestComponent = null;
        // EEvaluationResult bestResult = EEvaluationResult.MISSING;
        for (Entry<IRteComponent, IRteDependencyItem> e : fComponentEntries.entrySet()) {
            IRteComponent c = e.getKey();
            EEvaluationResult r = e.getValue().getEvaluationResult();
            if (r == EEvaluationResult.FULFILLED) {
                return c;
            } else if (r == EEvaluationResult.SELECTABLE) {
                if (bestComponent == null)
                    bestComponent = c;
                else
                    return null;
            }
        }
        return bestComponent;
    }

    @Override
    public void addComponent(IRteComponent component, EEvaluationResult result) {
        IRteDependencyItem de = new RteDependencyItem(component, result);
        fComponentEntries.put(component, de);
        if (fResult.ordinal() < result.ordinal())
            fResult = result;
    }

    @Override
    public void addStopItem(IRteComponentItem item, EEvaluationResult result) {
        if (fStopItems == null)
            fStopItems = new LinkedHashMap<IRteComponentItem, IRteDependencyItem>();

        fStopItems.put(item, new RteDependencyItem(item, result));
        if (fResult.ordinal() < result.ordinal())
            fResult = result;
    }

    @Override
    public Collection<? extends IRteDependencyItem> getChildren() {
        return fComponentEntries.values();
    }

    @Override
    public String getDescription() {
        EEvaluationResult res = getEvaluationResult();
        switch (res) {
        case CONFLICT:
            return CpStrings.RteDependency_Conflict;
        case INCOMPATIBLE_API:
            return CpStrings.RteDependency_SelectCompatibleAPI;
        case INCOMPATIBLE:
        case INCOMPATIBLE_BUNDLE:
        case INCOMPATIBLE_VARIANT:
        case INCOMPATIBLE_VENDOR:
        case INCOMPATIBLE_VERSION:
            return CpStrings.RteDependency_SelectCompatibleComponent;
        case INSTALLED:
            return CpStrings.RteDependency_UpdatePackVariantOrBundleSelection;
        case MISSING:
            return CpStrings.RteMissingComponentResult_NoComponentFoundMatchingDeviceCompiler;
        case MISSING_API:
            return CpStrings.RteDependency_MissingAPI;
        case MISSING_API_VERSION:
            return CpStrings.RteDependency_MissingAPIVersion;
        case MISSING_BUNDLE:
            return CpStrings.RteDependency_MissingBundle;
        case MISSING_VARIANT:
            return CpStrings.RteDependency_MissingVariant;
        case MISSING_VENDOR:
            return CpStrings.RteDependency_MissingVendor;
        case MISSING_VERSION:
            return CpStrings.RteDependency_MissingVersion;
        case SELECTABLE:
            return CpStrings.RteDependency_SelectComponentFromList;
        case UNAVAILABLE:
            return CpStrings.RteDependency_ComponentNotAvailable;
        case UNAVAILABLE_PACK:
            return CpStrings.RteDependency_PackNotSelected;

        case FULFILLED:
        case UNDEFINED:
        case ERROR:
        case FAILED:
        case IGNORED:
        case INACTIVE:
        default:
            break;
        }
        return super.getDescription();
    }
}
