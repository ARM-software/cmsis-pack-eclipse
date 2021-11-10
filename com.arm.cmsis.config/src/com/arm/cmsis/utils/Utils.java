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

package com.arm.cmsis.utils;

/**
 * Utility class for configuration wizard
 */
public class Utils {

    /**
     * Modify the value, return = op(value, modifier). <br>
     * E.g. if op='+', value = 10, modifier = 5, then return = 10 + 5 = 15
     *
     * @param value    the value to be modified
     * @param op       the operation, now there are only '+', '-', '*' and '/'
     * @param modifier the modifier
     * @param max      the value's maximum
     * @param min      the value's minimum
     * @return The modified value
     */
    public static long modifyValue(long value, char op, long modifier, long max, long min) {
        long realValue = value;
        if (realValue > max) {
            realValue = max;
        }
        if (realValue < min) {
            realValue = min;
        }
        switch (op) {
        case '+':
            realValue += modifier;
            break;
        case '-':
            realValue -= modifier;
            break;
        case '*':
            realValue *= modifier;
            break;
        case '/':
            realValue /= modifier;
            break;
        default:
            break;
        }
        return realValue;
    }

    /**
     * Modify the value Reversely, return = (~op)(value, modifier). <br>
     * E.g. if op='+', value = 10, modifier = 5, then return = 10 - 5 = 5
     *
     * @param value    the value to be modified
     * @param op       the operation, now there are only '+', '-', '*' and '/'
     * @param modifier the modifier
     * @param max      the modified value's maximum
     * @param min      the modified value's minimum
     * @return The reversely modified value, or max/min if the reversely modified
     *         value is greater/smaller than the max/min
     */
    public static long modifyValueR(long value, char op, long modifier, long max, long min) {
        long realValue = value;
        switch (op) {
        case '+':
            realValue -= modifier;
            break;
        case '-':
            realValue += modifier;
            break;
        case '*':
            realValue /= modifier;
            break;
        case '/':
            realValue *= modifier;
            break;
        default:
            break;
        }
        if (realValue > max) {
            realValue = max;
        }
        if (realValue < min) {
            realValue = min;
        }
        return realValue;
    }

}
