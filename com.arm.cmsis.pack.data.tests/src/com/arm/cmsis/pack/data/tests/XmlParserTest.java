/*******************************************************************************
* Copyright (c) 2023 ARM Ltd. and others
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

package com.arm.cmsis.pack.data.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;

import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.parser.CpXmlParser;

/**
 * Test cases for CpXmlParser
 */
public class XmlParserTest {

    private static CpXmlParser parser = new CpXmlParser();
    private static ICpItem root;
    private static ICpItem firstChild;
    private static ICpItem secondChild;
    private static ICpItem thirdChild;

    @BeforeClass
    public static void readFile() {
        String fileContent = ""; //$NON-NLS-1$
        try (InputStream inputStream = XmlParserTest.class.getClassLoader().getResourceAsStream("SimpleInputXml.xml"); //$NON-NLS-1$
                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) { // $NON-NLS-1$
            fileContent = in.lines().collect(Collectors.joining());
        } catch (Exception e) {
            fail("Failed to read XML file : " + e.getMessage()); //$NON-NLS-1$
            e.printStackTrace();
        }

        try {
            root = parser.parseXmlString(fileContent);
            firstChild = root.getFirstChild();
            secondChild = root.getChildren().stream().skip(1).findFirst().orElse(null);
            thirdChild = root.getFirstChild("compilers"); //$NON-NLS-1$
        } catch (Exception e) {
            fail("Parser failed to parse file content: " + fileContent + ", threw exception with message " //$NON-NLS-1$
                    + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testRoot() {
        assertNotEquals(null, root);

        assertEquals(null, root.attributes().getAttributesAsMap());
        assertEquals("mytests", root.getTag()); //$NON-NLS-1$
        assertEquals(3, root.getChildCount());

    }

    @Test
    public void testFirstChild() {
        assertNotEquals(null, firstChild);

        assertEquals(1, firstChild.attributes().getAttributesAsMap().size());
        assertEquals("t1", firstChild.getAttribute("id")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(3, firstChild.getChildCount());
        assertEquals(2, firstChild.getGrandChildren("authors").size()); //$NON-NLS-1$
        assertEquals("XmlTest", firstChild.getFirstChildText("name")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testSecondChild() {
        assertNotEquals(null, secondChild);

        assertEquals(1, secondChild.attributes().getAttributesAsMap().size());
        assertEquals("t2", secondChild.getAttribute("id")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("YmlTest", secondChild.getFirstChildText("name")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(1, secondChild.attributes().getAttributesAsMap().size());
        assertEquals(1, secondChild.getChildCount());
    }

    @Test
    public void testThirdChild() {
        assertNotEquals(null, thirdChild);

        assertEquals(null, thirdChild.attributes().getAttributesAsMap());
        assertEquals(1, thirdChild.getChildCount());

        ICpItem grandChild = thirdChild.getFirstChild();

        assertEquals(0, grandChild.getChildCount());
        assertEquals("AC6", grandChild.getName()); //$NON-NLS-1$
        assertEquals("6.20.0", grandChild.getVersion()); //$NON-NLS-1$
    }
}
