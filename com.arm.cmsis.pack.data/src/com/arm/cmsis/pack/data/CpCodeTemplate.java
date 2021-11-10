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

package com.arm.cmsis.pack.data;

import java.util.Collection;
import java.util.LinkedList;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.info.ICpComponentInfo;
import com.arm.cmsis.pack.info.ICpFileInfo;
import com.arm.cmsis.pack.info.ICpItemInfo;

/**
 * Serves as the default implementation of ICpCodeTemplate
 */
public class CpCodeTemplate extends CpItem implements ICpCodeTemplate {

    Collection<String> fCodeTemplates = new LinkedList<>();
    ICpItemInfo fInfo;

    /**
     * @param parent
     */
    public CpCodeTemplate(ICpItem parent) {
        super(parent);
    }

    /**
     * @param parent
     * @param tag
     * @param info
     */
    public CpCodeTemplate(ICpItem parent, String tag, ICpItemInfo info) {
        super(parent, tag);
        fInfo = info;
    }

    @Override
    public ICpPack getPack() {
        return fInfo == null ? null : fInfo.getPack();
    }

    @Override
    public ICpRootItem getRootItem() {
        return getPack();
    }

    @Override
    public String[] getCodeTemplates() {
        return fCodeTemplates.toArray(new String[fCodeTemplates.size()]);
    }

    @Override
    public void addCodeTemplate(String filePath) {
        fCodeTemplates.add(filePath);
    }

    @Override
    public String getComponentName() {
        if (fInfo instanceof ICpComponentInfo) { // This is the component class node
            return fInfo.getAttribute(CmsisConstants.CCLASS);
        } else if (fInfo instanceof ICpFileInfo) {
            ICpItem componentInfo = fInfo.getParent();
            String componentName = componentInfo.getAttribute(CmsisConstants.CGROUP);
            String sub = componentInfo.getAttribute(CmsisConstants.CSUB);
            if (!sub.isEmpty()) {
                componentName += "." + sub; //$NON-NLS-1$
            }
            return componentName;
        }
        return CmsisConstants.EMPTY_STRING;
    }

    @Override
    public String getSelectionName() {
        if (fInfo instanceof ICpFileInfo) {
            return fInfo.getAttribute(CmsisConstants.SELECT);
        }
        return CmsisConstants.EMPTY_STRING;
    }

}
