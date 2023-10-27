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

package com.arm.cmsis.pack.error;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.enums.ESeverity;
import com.arm.cmsis.pack.utils.WildCards;

/**
 *
 */
public class CmsisErrorCollection extends CmsisConsoleStrategy implements ICmsisErrorCollection {

    // errors for current file
    protected List<CmsisError> fErrors = null;

    @Override
    public void clearErrors() {
        fErrors = null;
    }

    @Override
    public void clearErrors(String pattern) {
        if (CmsisConstants.ASTERISK.equals(pattern)) {
            clearErrors();
            return;
        }

        for (Iterator<CmsisError> it = getErrors().iterator(); it.hasNext();) {
            CmsisError e = it.next();
            String id = e.getId();
            if (WildCards.match(id, pattern)) {
                it.remove();
            }
        }
    }

    @Override
    public void clearErrors(ESeverity severity) {
        for (Iterator<CmsisError> it = getErrors().iterator(); it.hasNext();) {
            CmsisError e = it.next();
            if (e.getSeverity().equals(severity)) {
                it.remove();
            }
        }
    }

    @Override
    public void addError(CmsisError e) {
        if (e == null)
            return;
        if (fErrors == null)
            fErrors = new ArrayList<>();
        fErrors.add(e);
        ICmsisConsole console = getCmsisConsole();
        if (console != null) {
            console.outputError(e);
        }
    }

    @Override
    public void addErrors(Collection<CmsisError> errors) {
        if (errors == null || errors.isEmpty())
            return;
        for (CmsisError e : errors) {
            addError(e);
        }
    }

    @Override
    public Collection<CmsisError> getErrors() {
        if (fErrors != null)
            return fErrors;
        return Collections.emptyList();
    }

    @Override
    public CmsisError getFirstError() {
        if (!getErrors().isEmpty()) {
            return getErrors().iterator().next();
        }
        return null;
    }

    @Override
    public Collection<String> getErrorStrings() {
        List<String> errorStrings = new ArrayList<>();
        for (ICmsisError e : getErrors()) {
            errorStrings.add(e.toString());
        }
        return errorStrings;
    }

    @Override
    public String getFirstErrorString() {
        CmsisError e = getFirstError();
        if (e == null)
            return CmsisConstants.EMPTY_STRING;
        return e.toString();
    }

    @Override
    public int getSevereErrorCount() {
        int nErrors = 0;
        for (ICmsisError e : getErrors()) {
            if (e.isSevere())
                nErrors++;
        }
        return nErrors;
    }

    @Override
    public boolean hasSevereErrors() {
        for (CmsisError e : getErrors()) {
            if (e.isSevere())
                return true;
        }
        return false;
    }

    @Override
    public boolean hasError(String pattern) {
        if (fErrors == null || pattern == null)
            return false;

        for (CmsisError e : getErrors()) {
            if (WildCards.match(pattern, e.getId()))
                return true;
        }
        return false;
    }

    @Override
    public CmsisError getError(String id) {
        if (fErrors == null || id == null)
            return null;

        for (CmsisError e : getErrors()) {
            if (id.equals(e.getId()))
                return e;
        }
        return null;
    }

    @Override
    public boolean hasWarning() {
        for (CmsisError e : getErrors()) {
            if (e.isWarning())
                return true;
        }
        return false;
    }

    @Override
    public int getWarningCount() {
        int nWarnings = 0;
        for (ICmsisError e : getErrors()) {
            if (e.isWarning())
                nWarnings++;
        }
        return nWarnings;
    }

    @Override
    public ESeverity getSeverity() {
        ESeverity severity = ESeverity.None;
        for (ICmsisError e : getErrors()) {
            ESeverity s = e.getSeverity();
            if (s.ordinal() > severity.ordinal()) {
                severity = s;
            }
            if (severity == ESeverity.FatalError)
                break;
        }
        return severity;
    }

}
