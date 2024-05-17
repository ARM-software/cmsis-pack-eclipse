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
package com.arm.cmsis.pack.error;

import javax.print.attribute.standard.Severity;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.enums.ESeverity;
import com.arm.cmsis.pack.item.ICmsisItem;

/**
 * Interface describing error messages: errors, warnings, information
 */
public interface ICmsisError {

    /**
     * Returns error ID
     *
     * @return error ID String
     */
    default String getId() {
        return CmsisConstants.EMPTY_STRING;
    }

    /**
     * Returns severity of the error
     *
     * @return {@link Severity}
     */
    default ESeverity getSeverity() {
        return ESeverity.None;
    }

    /**
     * Checks if this error is severe : an error or a fatal error
     *
     * @return true if severe
     */
    default boolean isSevere() {
        return getSeverity() != null && getSeverity().isSevere();
    }

    /**
     * Checks if this error is a warning
     *
     * @return true if warning
     */
    default boolean isWarning() {
        return getSeverity() != null && getSeverity().isWarning();
    }

    /**
     * Checks if this error is an info
     *
     * @return true if info
     */
    default boolean isInfo() {
        return getSeverity() != null && getSeverity().isInfo();
    }

    /**
     * Returns formatted message string: Severity, ID, message
     *
     * @return error String
     */
    String getFormattedMessage();

    /**
     * Returns additional detail string
     *
     * @return additional detail if available or null
     */
    String getDetail();

    /**
     * Sets additional detail
     *
     * @param detail additional detail string
     */
    void setDetail(String detail);

    /**
     * Returns filename associated with error
     *
     * @return filename or null if error is not related to file
     */
    default String getFile() {
        return null;
    }

    /**
     * Returns line number in the file with error
     *
     * @return line number or -1 if no line or file is associated with the error
     */
    default int getLine() {
        return -1;
    }

    /**
     * Returns column number number in the file with error
     *
     * @return column number or -1 if no column r file is associated with the error
     */
    default int getColumn() {
        return -1;
    }

    /**
     * Returns ICmsisItem associated with error
     *
     * @return ICmsisItem object or null if no item associated with error
     */
    default ICmsisItem getItem() {
        return null;
    }

}
