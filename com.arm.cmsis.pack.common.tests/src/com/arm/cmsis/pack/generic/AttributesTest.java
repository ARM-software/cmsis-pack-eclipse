/*******************************************************************************
 * Copyright (c) 2021 ARM Ltd and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/

package com.arm.cmsis.pack.generic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;

public class AttributesTest {
    final static Map<String, String> fTestAttributeMap = new HashMap<>();

    @Before
    public void init() {
        fTestAttributeMap.put("key1", "attribute1");
        fTestAttributeMap.put("key2", "attribute2");
        fTestAttributeMap.put("key3", "attribute3");
    }

    @Test
    public void testGetAttribute() {
        Attributes attributes = new Attributes();
        attributes.setAttribute("nullAttribute", null);
        String value = attributes.getAttribute("nullAttribute", "defaultValue");
        assertEquals("Attributes are equal", "defaultValue", value);
    }

    @Test
    public void testGetAttributeAsInt() {
        Attributes attributes = new Attributes();
        attributes.setAttribute("intAttribute", 42);
        int value = attributes.getAttributeAsInt("intAttribute", 0);
        assertEquals("Attributes are equal", 42, value);

        value = attributes.getAttributeAsInt("nonExitentAttribute", -1);
        assertEquals("Attributes are equal", -1, value);

        attributes.setAttribute("illegalIntAttribute", "illegalInt");
        value = attributes.getAttributeAsInt("illegalIntAttribute", -2);
        assertEquals("Attributes are equal", -2, value);
    }

    @Test
    public void testGetAttributeAsLong() {
        Attributes attributes = new Attributes();
        attributes.setAttribute("longAttribute", "42L");
        long value = attributes.getAttributeAsLong("longAttribute", 42L);
        assertEquals("Attributes are equal", 42L, value);
    }

    @Test
    public void testGetAttributeAsBoolean() {
        Attributes attributes = new Attributes();
        attributes.setAttribute("nullAttribute", null);
        boolean value = attributes.getAttributeAsBoolean("nullAttribute", true);
        assertEquals("Attributes are equal", true, value);

        attributes.setAttribute("booleanAttribute", true);
        value = attributes.getAttributeAsBoolean("booleanAttribute", true);
        assertEquals("Attributes are equal", true, value);
    }

    @Test
    public void testSetAttribute() {
        Attributes attributes = new Attributes();
        attributes.setAttribute(null, "attributeWithNullAsKey");
        Map<String, String> testAttributeMap = attributes.getAttributesAsMap();
        assertNull(testAttributeMap);

        attributes.setAttribute("keyAttribute1", null);
        TreeMap<String, String> treeMap = (TreeMap<String, String>) attributes.getAttributesAsMap();
        assertNotNull(treeMap);

        attributes.setAttribute("keyAttribute1", "attribute1");
        int size = attributes.getAttributesAsMap().size();
        assertEquals("Adding 1 attribute to the map", 1, size);
    }

    @Test
    public void testSetAttributeAsBoolean() {
        Attributes attributes = new Attributes();
        attributes.setAttribute("booleanAttribute", true);
        int size = attributes.getAttributesAsMap().size();
        assertEquals("Adding 1 booleanAttribute to the map", 1, size);
    }

    @Test
    public void testSetAttributeAsInt() {
        Attributes attributes = new Attributes();
        attributes.setAttribute("intAttribute", 1);
        int size = attributes.getAttributesAsMap().size();
        assertEquals("Adding 1 intAttribute to the map", 1, size);
    }

    @Test
    public void testaddAttributes() {
        Attributes attributes = new Attributes();
        Map<String, String> attributesMap = new HashMap<>();
        attributesMap.put("key1", "attribute1");
        attributesMap.put("key2", "attribute2");
        attributesMap.put("key3", "attribute3");
        attributes.addAttributes(attributesMap);
        int size = attributes.getAttributesAsMap().size();
        assertEquals("Adding 1 intAttribute to the map", 3, size);

        // Test a no empty map
        attributesMap.put("key4", "attribute4");
        attributesMap.put("key5", "attribute5");
        attributesMap.put("key6", "attribute6");

        attributes.addAttributes(attributesMap);
        size = attributes.getAttributesAsMap().size();
        assertEquals("Adding 3 more attributes to the map", 6, size);
    }

    @Test
    public void testMergeAttributes() {
        // Nothing to merge
        IAttributes attributesToMerge = new Attributes();
        Attributes attributes = new Attributes();
        attributes.mergeAttributes(attributesToMerge);

        // Merge attributes
        attributes.setAttribute("key4", "attribute4");
        attributesToMerge.addAttributes(fTestAttributeMap);
        attributes.mergeAttributes(attributesToMerge);
        int size = attributes.getAttributesAsMap().size();
        assertEquals("Merging 3 attributes to the map", 4, size);
    }

    @Test
    public void testMatchAttributes() {
        Attributes attributes = new Attributes();
        IAttributes attributesToMatch = new Attributes();
        String prefix = "k";
        boolean bCommon = true;
        boolean isMatched = attributes.matchAttributes(attributesToMatch, prefix, bCommon);
        // Nothing to match -> case 1: fAttributes = null or empty
        assertTrue(isMatched);

        // Nothing to match -> case 2: prefix is different from keys
        attributes.addAttributes(fTestAttributeMap);
        IAttributes noEmptyAttributesToMatch = new Attributes();
        prefix = "x";
        Map<String, String> attributesMap = new HashMap<>();
        attributesMap.put("key1", "attribute1");
        attributesMap.put("key2", "attribute2");
        attributesMap.put("key3", "attribute3");
        noEmptyAttributesToMatch.addAttributes(attributesMap);
        isMatched = attributes.matchAttributes(noEmptyAttributesToMatch, prefix, bCommon);
        assertTrue(isMatched);

        // Match some attributes
        prefix = "k";
        isMatched = attributes.matchAttributes(noEmptyAttributesToMatch, prefix, bCommon);
        assertTrue(isMatched);

        // Do not match some attributes case 1: one value of the attributes is different
        // from one in fAttributes
        attributesMap.clear();
        attributesMap.put("key1", "attribute1");
        attributesMap.put("key2", "attr2");
        attributesMap.put("key3", "attribute3");
        noEmptyAttributesToMatch.addAttributes(attributesMap);
        isMatched = attributes.matchAttributes(noEmptyAttributesToMatch, prefix, bCommon);
        assertFalse(isMatched);

    }

    @Test
    public void testSplitString() {
        String attributesString = null;
        // Nothing to split
        Map<String, String> splittedAttributesMap = Attributes.splitString(attributesString);
        assertNull(splittedAttributesMap);

        // Split a string of attributes
        attributesString = "key1=\"attribute1\", key2=\"attribute2\", key3=\"attribute3\"";
        splittedAttributesMap = Attributes.splitString(attributesString);
        IAttributes splittedAttributes = new Attributes(splittedAttributesMap);
        assertEquals("Map of strings are equal", attributesString, splittedAttributes.toString());
    }

}
