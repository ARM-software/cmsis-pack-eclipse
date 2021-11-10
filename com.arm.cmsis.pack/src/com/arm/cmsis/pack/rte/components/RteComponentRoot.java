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

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpComponent;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.enums.EComponentAttribute;

/**
 * Class represents root of component hierarchy Contains collection of groups
 */
public class RteComponentRoot extends RteComponentItem {
    /**
     * Default root constructors
     */
    public RteComponentRoot() {
        this(CmsisConstants.EMPTY_STRING);
    }

    public RteComponentRoot(String name) {
        super(null, "root"); //$NON-NLS-1$
        fComponentAttribute = EComponentAttribute.CCLASS;
        fbExclusive = false;
        fName = name;
    }

    @Override
    public void addComponent(ICpComponent cpComponent, int flags) {
        String className = cpComponent.getAttribute(CmsisConstants.CCLASS);
        if (className == null || className.isEmpty())
            return;
        // ensure childItem
        IRteComponentItem classItem = getChild(className);

        if (classItem == null) {
            classItem = new RteComponentClass(this, className);
            addChild(classItem);
        }
        classItem.addComponent(cpComponent, flags);
    }

    @Override
    public void addCpItem(ICpItem cpItem) {
        String className = cpItem.getAttribute(CmsisConstants.CCLASS);
        if (className == null || className.isEmpty())
            return;
        // check if class exists
        IRteComponentItem classItem = getChild(className);
        if (classItem == null) {
            return; // no class => no add
        }
        classItem.addCpItem(cpItem);
    }
}
