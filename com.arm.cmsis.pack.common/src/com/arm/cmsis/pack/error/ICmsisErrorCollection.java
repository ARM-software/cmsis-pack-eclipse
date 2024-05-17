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

import java.util.Collection;

import com.arm.cmsis.pack.enums.ESeverity;

/**
 * Interface describing collection of CMSIS error messages: errors, warnings,
 * information
 */
public interface ICmsisErrorCollection extends ICmsisConsoleStrategy {

    /**
     * Clears the collection, removes all errors and messages from it
     */
    void clearErrors();

    /**
     * Removes errors whose ID matches supplied pattern
     *
     * @param pattern String to match to, can contain wild cards
     */
    void clearErrors(final String pattern);

    /**
     * Removes errors whose Severity matches supplied one
     *
     * @param severity ESeverity to match to
     */
    void clearErrors(ESeverity severity);

    /**
     * Adds an error to collection
     *
     * @param e CmsisError
     */
    void addError(CmsisError e);

    /**
     * Adds errors from supplied collection
     *
     * @param errors collection of CmsisError objects
     */
    void addErrors(Collection<CmsisError> errors);

    /**
     * Adds errors from supplied ICmsisErrorCollection object
     *
     * @param errors ICmsisErrorCollection
     */
    default void addErrors(ICmsisErrorCollection errors) {
        if (errors != null)
            addErrors(errors.getErrors());
    }

    /**
     * Returns collection of errors
     *
     * @return collection CmsisError objects
     */
    Collection<CmsisError> getErrors();

    /**
     * Returns the first errors
     *
     * @return CmsisError or null if collection is empty
     */
    CmsisError getFirstError();

    /**
     * Returns the error with supplied ID
     *
     * @param id error ID
     * @return CmsisError or null if nor error eith given ID is found
     */
    CmsisError getError(final String id);

    /**
     * Checks if collection contain an error with ID matching supplied pattern
     *
     * @param pattern error ID patter (may include wild cards)
     * @return true if collection contains an error matching pattern
     */
    boolean hasError(final String pattern);

    /**
     * Returns collection of error strings
     *
     * @return error strings obtained from errors
     */
    Collection<String> getErrorStrings();

    /**
     * Returns the first error string
     *
     * @return error string or empty
     */
    String getFirstErrorString();

    /**
     * Checks if collection contains at least one severe error
     *
     * @return true if collection contains at least one severe error
     */
    boolean hasSevereErrors();

    /**
     * Returns number of errors in the collection
     *
     * @return number of errors
     */
    int getSevereErrorCount();

    /**
     * Returns number of warnings in the collection
     *
     * @return number of warnings
     */
    int getWarningCount();

    /**
     * Checks if collection contains at least one severe error
     *
     * @return true if collection contains at least one severe error
     */
    boolean hasWarning();

    /**
     * Returns highest severity of the errors in the collection
     *
     * @return ESeverity
     */
    ESeverity getSeverity();

}
