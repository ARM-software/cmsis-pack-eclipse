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

/**
 * Interface describing a condition expression
 */
public interface ICpExpression extends ICpItem {

    static final char ACCEPT_EXPRESSION = 'a';
    static final char DENY_EXPRESSION = 'd';
    static final char REQUIRE_EXPRESSION = 'r';

    static final char BOARD_EXPRESSION = 'B';
    static final char DEVICE_EXPRESSION = 'D';
    static final char HW_EXPRESSION = 'H';
    static final char TOOLCHAIN_EXPRESSION = 'T';
    static final char COMPONENT_EXPRESSION = 'C';
    static final char REFERENCE_EXPRESSION = 'R';
    static final char ERROR_EXPRESSION = 'E';
    static final char UNKNOWN_EXPRESSION = 'U';

    /**
     * Returns expression domain:
     * <dl>
     * <dt>'D'
     * <dd>Device
     * <dt>'C'
     * <dd>Component
     * <dt>'T'
     * <dd>Toolchain
     * <dt>'R'
     * <dd>Reference to condition
     * <dt>'E'
     * <dd>Error (mixed attribute types or missing attributes)
     * <dt>'U'
     * <dd>Unknown
     * </dl>
     * <p>
     * Note: <code>char</code> type is used instead of an enum for performance
     * reason
     * </p>
     *
     * @return the expression domain
     */
    public char getExpressionDomain();

    /**
     * Returns expression type:
     * <dl>
     * <dt>'a'
     * <dd>accept
     * <dt>'d'
     * <dd>deny
     * <dt>'r'
     * <dd>require
     * </dl>
     * <p>
     * Note: <code>char</code> type is used instead of an enum for performance
     * reason
     * </p>
     *
     * @return expression type
     */
    public char getExpressionType();

}