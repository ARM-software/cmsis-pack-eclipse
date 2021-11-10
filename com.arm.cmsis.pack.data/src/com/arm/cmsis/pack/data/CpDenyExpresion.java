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
        switch (result) {
        case FULFILLED:
            if (getExpressionDomain() != ICpExpression.COMPONENT_EXPRESSION)
                return EEvaluationResult.INCOMPATIBLE;
            return result;
        case UNDEFINED:
        case ERROR:
        case IGNORED:
        case INCOMPATIBLE:
            return result;
        case CONFLICT:
        case FAILED:
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
        default:
            break;
        }
        return EEvaluationResult.FULFILLED;
    }
}
