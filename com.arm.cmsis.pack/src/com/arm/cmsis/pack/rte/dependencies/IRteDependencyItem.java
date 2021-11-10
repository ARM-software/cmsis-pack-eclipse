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
import com.arm.cmsis.pack.enums.IEvaluationResult;
import com.arm.cmsis.pack.item.ICmsisItem;
import com.arm.cmsis.pack.rte.components.IRteComponentItem;

/**
 * Base interface for object constructing dependency tree
 */
public interface IRteDependencyItem extends ICmsisItem, IEvaluationResult {

    /**
     * Returns associated component item if any
     *
     * @return associated component item
     */
    IRteComponentItem getComponentItem();

    /**
     * Returns associated ICpItem that is:
     * <ul>
     * <li>a source of dependency (an ICpExpresiion or an ICpApi)
     * <li>or an ICpComponent corresponding to associated IRteComponentItem
     * </ul>
     *
     * @return ICpItem that is source of dependency or underlying ICpCompoent
     */
    ICpItem getCpItem();

    /**
     * Checks if this item is a master item that defines severity and icon
     *
     * @return true if this item is a master of its children
     */
    boolean isMaster();

    /**
     * Checks if this item is evaluated in negative context: it denies the matching
     * components
     *
     * @return if the dependency is a deny one
     */
    boolean isDeny();

    @Override
    Collection<? extends IRteDependencyItem> getChildren();

}
