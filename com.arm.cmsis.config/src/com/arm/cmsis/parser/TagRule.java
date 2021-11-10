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

package com.arm.cmsis.parser;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.SingleLineRule;

/**
 * Rule for detecting tag
 */
public class TagRule extends SingleLineRule {

    public TagRule(IToken token) {
        super("<", ">", token); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    protected boolean sequenceDetected(ICharacterScanner scanner, char[] sequence, boolean eofAllowed) {
        if (sequence[0] == '<') {
            int c1 = scanner.read();
            if (c1 == '<' || c1 == '>') {
                // << xyz...", ignore
                return false;
            }
            scanner.unread();
        }
        return super.sequenceDetected(scanner, sequence, eofAllowed);
    }

    @Override
    protected boolean endSequenceDetected(ICharacterScanner scanner) {
        boolean detected = super.endSequenceDetected(scanner);
        scanner.unread();
        int c = scanner.read();
        if (c == '\r' || c == '\n') {
            return false;
        }
        return detected;
    }
}
