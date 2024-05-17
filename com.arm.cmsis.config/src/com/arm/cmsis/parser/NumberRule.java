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
 * QNX Software Systems - initial API and implementation
 * Wind River Systems, Inc. - bug fixes
 * ARM Ltd and ARM Germany GmbH - application-specific implementation
 *******************************************************************************/
package com.arm.cmsis.parser;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * Recognizes positive integer numbers.
 */
public class NumberRule implements IRule {
    private IToken token;

    public NumberRule(IToken token) {
        super();
        this.token = token;
    }

    @Override
    public IToken evaluate(ICharacterScanner scanner) {
        scanner.unread();
        int prevCh = scanner.read();
        int startCh = scanner.read();
        int ch;
        int unreadCount = 1;

        if (isNumberStart(startCh)) {
            ch = startCh;
            if ((prevCh >= 'A' && prevCh <= 'Z') || (prevCh >= 'a' && prevCh <= 'z') || prevCh == '_') {
                while (isValidIdentifierChar((char) ch)) {
                    ch = scanner.read();
                }
                return Token.UNDEFINED;
            }
            if (startCh == '-') {
                ch = scanner.read();
                ++unreadCount;
            }
            if (ch == '0') {
                int xCh = scanner.read();
                if (xCh == 'x' || xCh == 'X' || xCh == 'b' || xCh == 'B') {
                    // hex/bin number
                    do {
                        ch = scanner.read();
                    } while (isHexNumberPart((char) ch));
                    scanner.unread();
                    if (!isValidIdentifierChar((char) ch)) {
                        return token;
                    }
                    return Token.UNDEFINED;
                }
                scanner.unread();
                // assert ch == '0';
            } else if (ch == '.') {
                ch = scanner.read();
                ++unreadCount;
            }
            if (Character.isDigit((char) ch)) {
                // need at least one digit
                do {
                    ch = scanner.read();
                } // while (Character.isDigit((char)ch));
                while (isHexNumberPart((char) ch));

                if (!isNumberSuffix((char) ch)) {
                    scanner.unread();
                }
                return token;
            }
        }
        do {
            scanner.unread();
        } while (--unreadCount > 0);
        return Token.UNDEFINED;
    }

    /**
     * Checks if start of number.
     *
     * @param ch
     * @return true if it is number start.
     */
    private boolean isNumberStart(int ch) {
        return ch == '-' || Character.isDigit((char) ch);
    }

    /**
     * Checks if part of hex number;
     *
     * @param ch
     * @return true if it is part of a hex number
     */
    private boolean isHexNumberPart(int ch) {
        return Character.isDigit((char) ch) || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F');
    }

    private boolean isNumberSuffix(char ch) {
        return ch == 'b' || ch == 'B' || ch == 'h' || ch == 'H' || ch == 'd' || ch == 'D' || ch == 'o' || ch == 'O'
                || ch == 'q' || ch == 'Q';
    }

    private boolean isValidIdentifierChar(char ch) {
        return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9') || ch == '_';
    }
}