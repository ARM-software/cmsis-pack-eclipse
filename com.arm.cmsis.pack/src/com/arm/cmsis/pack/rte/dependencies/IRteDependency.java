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

import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.rte.RteConstants;
import com.arm.cmsis.pack.rte.components.IRteComponent;
import com.arm.cmsis.pack.rte.components.IRteComponentItem;

/**
 * Interface for dependency evaluation and resolving
 */
public interface IRteDependency extends IRteDependencyItem {

    /**
     * Checks if this dependency is resolved
     *
     * @return true if resolved
     */
    boolean isResolved();

    /**
     * Returns dependency evaluation result for specific component candidate
     *
     * @return dependency evaluation result if component found, otherwise
     *         EEvaluationResult.UNDEFINED
     */
    EEvaluationResult getEvaluationResult(IRteComponent component);

    /**
     * Returns list of collected components which are candidates to resolve
     * dependencies
     *
     * @return list of collected candidates to resolve dependencies
     */
    Collection<IRteComponent> getComponents();

    /**
     * Returns component that best matches dependency
     *
     * @return list of collected candidates to resolve dependencies
     */
    IRteComponent getBestMatch();

    /**
     * Adds component to the internal list of candidate components
     *
     * @param component that is a candidate to fulfill dependency
     * @param result    result of the evaluation showing to which extent the
     *                  component fulfills the dependency
     */
    void addComponent(IRteComponent component, EEvaluationResult result);

    /**
     * Adds component hierarchy item that stopped dependency evaluation
     *
     * @param item   a component hierarchy at which evaluation has stopped
     * @param result reason why evaluation has stopped
     */
    void addStopItem(IRteComponentItem item, EEvaluationResult result);

    /**
     * Returns RTE flags to resolve dependency
     *
     * @return resolve flags
     * @see RteConstants
     */
    int getFlags();

}
