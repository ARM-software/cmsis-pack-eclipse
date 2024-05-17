/*******************************************************************************
* Copyright (c) 2024 ARM Ltd. and others
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

package com.arm.cmsis.pack.parser.yml.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import org.junit.BeforeClass;
import org.junit.Test;

import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.parser.yml.CpYmlParser;

/*
 * Test cases for CpYmlParser
 */

public class YmlParserTest {

    private static CpYmlParser parser = new CpYmlParser();
    private static final String input = 
            "nested:\n  one: 1\n  two: 2\n  three:\n    s_one: 3.1\n    s_two: 3.2\n  fourth-node:\n    - 4.1\n    - 4.2.a: a\n      4.2.b: b\n"; //$NON-NLS-1$
    private static ICpItem root;
    private static ICpItem child;
    private static ICpItem firstGrandChild;
    private static ICpItem secondGrandChild;

    @BeforeClass
    public static void parseInput() {
        try {
            root = parser.parseString(input);
            child = root.getFirstChild();
            firstGrandChild = child.getFirstChild("three"); //$NON-NLS-1$
            secondGrandChild = child.getFirstChild("fourth-node"); //$NON-NLS-1$
        } catch (Exception e) {
            fail("Parser failed to parse file content: " + input + ", threw exception with message " //$NON-NLS-1$ //$NON-NLS-2$
                    + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testRoot() {
        assertNotEquals(null, root);

        assertEquals(1, root.getChildCount());
        assertEquals(child, root.getFirstChild("nested")); //$NON-NLS-1$
        assertEquals(null, root.attributes().getAttributesAsMap());
    }

    @Test
    public void testChild() {
        assertNotEquals(null, child);

        assertEquals(2, child.getChildCount());
        assertEquals(firstGrandChild, child.getFirstChild("three")); //$NON-NLS-1$
        assertEquals(secondGrandChild, child.getFirstChild("fourth-node")); //$NON-NLS-1$
        assertEquals(2, child.attributes().getAttributesAsMap().size());
        assertEquals("1", child.getAttribute("one")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("2", child.getAttribute("two")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testFirstGrandChild() {
        assertNotEquals(null, firstGrandChild);

        assertEquals(0, firstGrandChild.getChildCount());
        assertEquals(2, firstGrandChild.attributes().getAttributesAsMap().size());
        assertEquals("3.1", firstGrandChild.getAttribute("s_one")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("3.2", firstGrandChild.getAttribute("s_two")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testSecondGrandChild() {
        assertNotEquals(null, secondGrandChild);

        assertEquals(2, secondGrandChild.getChildCount());
        assertEquals(null, secondGrandChild.attributes().getAttributesAsMap());

        ICpItem sequenceChild1 = secondGrandChild.getFirstChild();
        ICpItem sequenceChild2 = secondGrandChild.getChildren().stream().skip(1).findFirst().orElse(null);

        assertEquals("-", sequenceChild1.getTag()); //$NON-NLS-1$
        assertEquals("4.1", sequenceChild1.getText()); //$NON-NLS-1$
        assertEquals("-", sequenceChild2.getTag()); //$NON-NLS-1$
        assertEquals(2, sequenceChild2.attributes().getAttributesAsMap().size());
        assertEquals("a", sequenceChild2.getAttribute("4.2.a")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("b", sequenceChild2.getAttribute("4.2.b")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testOutput() {
        assertEquals(input, parser.writeToString(root));
    }

    @Test
    public void testParseFile() {
        File temporaryFile = null;
        String filename = null;
        try {
            temporaryFile = File.createTempFile("temp", ".yml"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = temporaryFile.getCanonicalPath().toString();
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            writer.write(input);
            writer.close();
        } catch (Exception e) {
            fail("Failed to create a temporary file for testParseFile"); //$NON-NLS-1$
            return;
        }
        try {
            ICpItem item = parser.parseFile(filename);
            assertNotEquals(null, item);
            String output = parser.writeToString(item);
            assertEquals(input, output);
            temporaryFile.deleteOnExit();
        } catch (Exception e) {
            fail("Failed to execute parseFile correctly"); //$NON-NLS-1$
            e.printStackTrace();
        }
    }
}
