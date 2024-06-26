/*******************************************************************************
 * Copyright (c) 2021 ARM Ltd and others.
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

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.enums.EEvaluationResult;

/**
 *
 */
public class CpExpression extends CpItem implements ICpExpression {

    protected char fExpressionDomain = 0; // 'D' Device),
                                          // 'C' Component),
                                          // 'T' Toolchain),
                                          // 'R' Reference to condition
                                          // 'E' Error (mixed attribute types or missing attributes)
                                          // 'U' Unknown

    /**
     * Hierarchical constructor
     *
     * @param parent parent ICpItem
     * @param tag    item's tag
     */
    public CpExpression(ICpItem parent, String tag) {
        super(parent, tag);
    }

    @Override
    public char getExpressionType() {
        return getTag().charAt(0);
    }

    @Override
    public String constructId() {
        return getTag() + " " + attributes().toString(); //$NON-NLS-1$
    }

    @Override
    public char getExpressionDomain() {
        if (fExpressionDomain == 0) {
            int nTypes = 0;
            boolean bError = false;

            if (hasCondition()) {
                nTypes++;
                fExpressionDomain = REFERENCE_EXPRESSION;
            }
            if (attributes().containsAttribute("C*")) { //$NON-NLS-1$
                nTypes++;
                fExpressionDomain = COMPONENT_EXPRESSION;
                if (!attributes().hasAttribute(CmsisConstants.CCLASS)
                        || !attributes().hasAttribute(CmsisConstants.CGROUP)) {
                    bError = true;
                }
            }
            if (attributes().containsAttribute("D*") || attributes().hasAttribute(CmsisConstants.PNAME)) { //$NON-NLS-1$
                nTypes++;
                fExpressionDomain = DEVICE_EXPRESSION;
            }

            if (attributes().containsAttribute("B*")) {//$NON-NLS-1$
                nTypes++;
                fExpressionDomain = BOARD_EXPRESSION;
            }

            if (attributes().containsAttribute("T*")) {//$NON-NLS-1$
                nTypes++;
                fExpressionDomain = TOOLCHAIN_EXPRESSION;
            }

            if (attributes().containsAttribute("H*")) {//$NON-NLS-1$
                nTypes++;
                fExpressionDomain = HW_EXPRESSION;
            }

            if (bError || nTypes > 1) {
                fExpressionDomain = ERROR_EXPRESSION;
            } else if (fExpressionDomain == 0) {
                fExpressionDomain = UNKNOWN_EXPRESSION;
            }
        }
        return fExpressionDomain;
    }

    @Override
    public EEvaluationResult evaluate(ICpConditionContext context) {
        return context.evaluateExpression(this);
    }

    @Override
    public String getName() {
        return getId();
    }

    @Override
    public int hashCode() {
        if (getExpressionDomain() != REFERENCE_EXPRESSION) {
            return getId().hashCode();
        }
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj))
            return true;
        if (obj instanceof ICpExpression) {
            ICpExpression expr = (ICpExpression) obj;
            if (expr.getId().equals(getId()))
                return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return getId();
    }

    @Override
    public boolean isDeviceDependent() {
        char domain = getExpressionDomain();
        if (domain == DEVICE_EXPRESSION) {
            return hasAttribute(CmsisConstants.DNAME);
        } else if (domain == REFERENCE_EXPRESSION) {
            return super.isDeviceDependent();
        }
        return false;
    }

    @Override
    public boolean isBoardDependent() {
        char domain = getExpressionDomain();
        if (domain == BOARD_EXPRESSION) {
            return hasAttribute(CmsisConstants.BNAME);
        } else if (domain == REFERENCE_EXPRESSION) {
            return super.isBoardDependent();
        }
        return false;
    }

}
