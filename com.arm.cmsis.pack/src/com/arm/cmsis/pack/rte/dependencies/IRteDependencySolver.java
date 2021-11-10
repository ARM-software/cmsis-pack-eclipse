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

import com.arm.cmsis.pack.data.ICpConditionContext;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.rte.components.IRteComponentItem;

/**
 *
 */
public interface IRteDependencySolver extends ICpConditionContext {

    /**
     * Evaluates dependencies for selected components
     *
     * @return worst dependency evaluation result
     */
    EEvaluationResult evaluateDependencies();

    /**
     * Tries to resolve component dependencies
     *
     * @return evaluation result after dependency resolving
     */
    EEvaluationResult resolveDependencies();

    /**
     * Returns dependency item for given component item (bundle, group or component)
     *
     * @param component IRteComponentItem for which to get result
     * @return dependency result or null if component item has no unresolved
     *         dependencies
     */
    IRteDependencyItem getDependencyItem(IRteComponentItem componentItem);

    /**
     * Returns dependency evaluation result for given item (class, group or
     * component)
     *
     * @param item IRteComponentItem for which to get result
     * @return condition result or IGNORED if item has no result
     */
    EEvaluationResult getEvaluationResult(IRteComponentItem item);

    /**
     * Returns collection of dependency results (items and dependencies)
     *
     * @return collection of dependency results
     */
    Collection<? extends IRteDependencyItem> getDependencyItems();

}
