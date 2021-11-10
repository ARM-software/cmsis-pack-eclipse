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

import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.item.CmsisItem;
import com.arm.cmsis.pack.rte.components.IRteComponentItem;

/**
 *
 */
public class RteDependencyItem extends CmsisItem implements IRteDependencyItem {

    protected EEvaluationResult fResult = EEvaluationResult.UNDEFINED;
    protected IRteComponentItem fComponentItem = null;

    /**
     * Default constructor
     */
    public RteDependencyItem() {
    }

    public RteDependencyItem(EEvaluationResult result) {
        setEvaluationResult(result);
    }

    /**
     * Constructor
     *
     * @param component IRteComponent candidate component
     */
    public RteDependencyItem(IRteComponentItem componentItem) {
        fComponentItem = componentItem;
    }

    /**
     * Constructor
     *
     * @param component IRteComponent candidate component
     */
    public RteDependencyItem(IRteComponentItem componentItem, EEvaluationResult result) {
        fComponentItem = componentItem;
        setEvaluationResult(result);
    }

    @Override
    public boolean isDeny() {
        // Default returns false
        return false;
    }

    @Override
    public EEvaluationResult getEvaluationResult() {
        return fResult;
    }

    @Override
    public void setEvaluationResult(EEvaluationResult result) {
        fResult = result;
    }

    @Override
    public Collection<? extends IRteDependencyItem> getChildren() {
        return null;
    }

    @Override
    public IRteComponentItem getComponentItem() {
        return fComponentItem;
    }

    @Override
    public ICpItem getCpItem() {
        IRteComponentItem componentItem = getComponentItem();
        if (componentItem != null) {
            return componentItem.getActiveCpItem();
        }
        return null;
    }

    @Override
    public String getName() {
        ICpItem cpItem = getCpItem();
        if (cpItem != null)
            return cpItem.getName();
        if (fComponentItem != null)
            return fComponentItem.getName();
        return super.getName();
    }

    @Override
    public String getDescription() {
        ICpItem cpItem = getCpItem();
        if (cpItem != null)
            return cpItem.getDescription();
        return super.getDescription();
    }

    @Override
    public String getUrl() {
        ICpItem cpItem = getCpItem();
        if (cpItem != null)
            return cpItem.getUrl();
        return super.getUrl();
    }

    @Override
    public boolean isMaster() {
        return hasChildren();
    }

}
