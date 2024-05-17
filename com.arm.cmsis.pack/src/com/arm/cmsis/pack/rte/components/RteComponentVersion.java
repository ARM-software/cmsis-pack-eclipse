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

import java.util.LinkedHashSet;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpComponent;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.info.ICpComponentInfo;
import com.arm.cmsis.pack.rte.dependencies.IRteDependency;

/**
 * Class represent Cversion hierarchy level (the end-leaf), contains references
 * to ICpComponents.
 */
public class RteComponentVersion extends RteComponentItem {

    protected LinkedHashSet<ICpComponent> fComponents = new LinkedHashSet<ICpComponent>();
    protected ICpComponentInfo fComponentInfo = null;

    public RteComponentVersion(IRteComponentItem parent, String name) {
        super(parent, name);
    }

    @Override
    public void destroy() {
        super.destroy();
        fComponents.clear();
        fComponentInfo = null;
    }

    @Override
    public boolean purge() {
        if (!isSelected()) {
            fComponentInfo = null;
            if (fComponents.isEmpty()) {
                destroy();
                return true;
            }
        }
        return false;
    }

    @Override
    public EEvaluationResult findComponents(IRteDependency dependency) {
        if (getEntityCount() > 1)
            return EEvaluationResult.INSTALLED;
        return EEvaluationResult.SELECTABLE;
    }

    @Override
    public void addComponent(ICpComponent cpComponent, int flags) {
        if (cpComponent instanceof ICpComponentInfo) {
            if (cpComponent != fComponentInfo) {
                fComponentInfo = (ICpComponentInfo) cpComponent;
                fComponentInfo.setComponent(getFirstCpComponent());
            }
        } else if (!fComponents.contains(cpComponent)) {
            fComponents.add(cpComponent);
        }
    }

    @Override
    public ICpItem getCpItem() {
        return getActiveCpComponent();
    }

    @Override
    public ICpComponent getActiveCpComponent() {
        ICpComponent cpComponent = getFirstCpComponent();
        if (cpComponent != null)
            return cpComponent;
        return fComponentInfo;
    }

    @Override
    public ICpComponentInfo getActiveCpComponentInfo() {
        return fComponentInfo;
    }

    protected ICpComponent getFirstCpComponent() {
        if (!fComponents.isEmpty())
            return fComponents.iterator().next();
        return null;
    }

    @Override
    public ICpComponent getApi() {
        IRteComponentGroup group = getParentGroup();
        if (group != null) {
            ICpItem cpItem = getCpItem();
            if (cpItem != null && cpItem.hasAttribute(CmsisConstants.CAPIVERSION))
                return group.getApi(cpItem.getAttribute(CmsisConstants.CAPIVERSION)); // certain API version version
            return group.getApi(); // active API version
        }
        return null;
    }

    protected int getEntityCount() {
        return fComponents.size();
    }
}
