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
import java.util.Map;

import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.rte.components.IRteComponent;
import com.arm.cmsis.pack.rte.components.IRteComponentItem;

/**
 * Class to wrap a dependency with multiple choice
 */
public class RteDependencyWrapper extends RteDependency {

    protected IRteDependency fOriginalDependency;

    // list of component items that stop the search
    protected Map<IRteComponentItem, IRteDependencyItem> fStopItems = null;

    public RteDependencyWrapper(IRteDependency dep) {
        super(dep.getCpItem(), dep.getFlags());
        fOriginalDependency = dep;
        setEvaluationResult(dep.getEvaluationResult());
    }

    @Override
    public IRteComponentItem getComponentItem() {
        return fOriginalDependency.getComponentItem();
    }

    @Override
    public Collection<IRteComponent> getComponents() {
        return fOriginalDependency.getComponents();
    }

    @Override
    public EEvaluationResult getEvaluationResult(IRteComponent component) {
        return fOriginalDependency.getEvaluationResult(component);
    }

    @Override
    public IRteComponent getBestMatch() {
        return null; // multiple choice
    }

    @Override
    public void addComponent(IRteComponent component, EEvaluationResult result) {
        fOriginalDependency.addComponent(component, result);
    }

    @Override
    public void addStopItem(IRteComponentItem item, EEvaluationResult result) {
        fOriginalDependency.addStopItem(item, result);
    }

    @Override
    public Collection<? extends IRteDependencyItem> getChildren() {
        return fOriginalDependency.getChildren();
    }
}
