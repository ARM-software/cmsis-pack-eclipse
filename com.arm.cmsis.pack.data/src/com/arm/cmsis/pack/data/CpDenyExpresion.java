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

package com.arm.cmsis.pack.data;

import com.arm.cmsis.pack.enums.EEvaluationResult;

/**
 *
 */
public class CpDenyExpresion extends CpExpression {

    /**
     * @param parent
     * @param tag
     */
    public CpDenyExpresion(ICpItem parent, String tag) {
        super(parent, tag);
    }

    @Override
    public EEvaluationResult evaluate(ICpConditionContext context) {
        EEvaluationResult result = super.evaluate(context);
        if (getExpressionDomain() == ICpExpression.COMPONENT_EXPRESSION) {
            return result; // already denied
        }

        if (context.isDependencyContext()) {
            switch (result) {
            case UNDEFINED:
            case ERROR:
                return result;
            case FULFILLED:
                return EEvaluationResult.INCOMPATIBLE;
            case INCOMPATIBLE:
            case CONFLICT:
            case INACTIVE:
            case INCOMPATIBLE_API:
            case INCOMPATIBLE_BUNDLE:
            case INCOMPATIBLE_VARIANT:
            case INCOMPATIBLE_VENDOR:
            case INCOMPATIBLE_VERSION:
            case INSTALLED:
            case MISSING:
            case MISSING_API:
            case MISSING_BUNDLE:
            case MISSING_GPDSC:
            case MISSING_VARIANT:
            case MISSING_VENDOR:
            case MISSING_VERSION:
            case SELECTABLE:
            case UNAVAILABLE:
            case UNAVAILABLE_PACK:
                return EEvaluationResult.FULFILLED;

            case IGNORED:
            case FAILED:
            default:
                return EEvaluationResult.IGNORED;
            }
        } else {
            switch (result) {
            case FULFILLED:
                return EEvaluationResult.FAILED;
            case FAILED:
                return EEvaluationResult.FULFILLED;
            case ERROR:
            case UNDEFINED:
                return result;
            case IGNORED:
            default:
                return EEvaluationResult.IGNORED;
            }
        }
    }
}
