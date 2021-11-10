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
package com.arm.cmsis.pack.project.utils;

import java.util.LinkedHashMap;
import java.util.Map;

import com.arm.cmsis.pack.common.CmsisConstants;

public final class CPreprocessorDefineOptionsParser {
    public static final String delimiters = " ,;"; //$NON-NLS-1$

    public static enum State {
        NAME, SEARCH_VALUE, STRING, VALUE, SPACE
    }

    protected Map<String, String> definePairs = new LinkedHashMap<>();
    protected String name = CmsisConstants.EMPTY_STRING;
    protected String value = CmsisConstants.EMPTY_STRING;

    /**
     * Parses a string of defines
     *
     * @param defines string of defines
     * @return map (define's name, value) with parsed defines
     */
    public Map<String, String> parseDefines(String defines) {
        definePairs.clear();
        State currentState = State.NAME;
        defines = defines.trim();

        for (int i = 0; i < defines.length(); i++) {
            char ch = defines.charAt(i);

            switch (currentState) {
            case NAME:
                if (ch == '=') {
                    currentState = State.SEARCH_VALUE;
                    break;
                }
                if (delimiters.indexOf(ch) >= 0) {
                    currentState = State.SPACE;
                } else {
                    name += ch;
                    if (i == defines.length() - 1) {
                        insertPair();
                    }
                    currentState = State.NAME;
                    break;
                }

            case SPACE:
                if (delimiters.indexOf(ch) >= 0) {
                    currentState = State.SPACE;
                    break;
                }
                if (ch != '=') {
                    insertPair();
                    name += ch;
                    currentState = State.NAME;
                    break;
                }
                currentState = State.SEARCH_VALUE;
                break;

            case SEARCH_VALUE:
                if (ch == ' ') {
                    currentState = State.SEARCH_VALUE;
                    break;
                } else if (ch == '"') {
                    currentState = State.STRING;
                    break;
                } else {
                    currentState = State.VALUE;
                }

            case VALUE:
                if (delimiters.indexOf(ch) >= 0) {
                    insertPair();
                    currentState = State.NAME;
                    break;
                }
                value += ch;
                if (i == defines.length() - 1) {
                    insertPair();
                    currentState = State.NAME;
                    break;
                }
                currentState = State.VALUE;
                break;

            case STRING:
                if (ch != '"') {
                    value += ch;
                    currentState = State.STRING;
                    break;
                } else if (ch == '"') {
                    insertPair();
                    currentState = State.NAME;
                } else if (delimiters.indexOf(ch) >= 0) {
                    insertPair();
                    currentState = State.NAME;
                    break;
                }
            }
        }
        return definePairs;
    }

    /**
     * Inserts pair (define's name, value)
     */
    protected void insertPair() {
        // close value with quotations
        if (!value.isEmpty())
            value = "\"" + value + "\""; //$NON-NLS-1$ //$NON-NLS-2$

        // Insert pair
        if (!name.isEmpty())
            definePairs.put(name, value);

        // Clean variables
        name = CmsisConstants.EMPTY_STRING;
        value = CmsisConstants.EMPTY_STRING;
    }

}
