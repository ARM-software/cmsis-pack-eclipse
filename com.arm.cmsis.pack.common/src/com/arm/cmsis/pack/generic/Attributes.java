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

package com.arm.cmsis.pack.generic;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.utils.WildCards;

/**
 * Default implementation of IAttributes interface
 */
public class Attributes implements IAttributes {

    protected Map<String, String> fAttributes = null;

    /**
     * Default constructor
     */
    public Attributes() {
    }

    /**
     * Copy constructor
     *
     * @param copyFrom
     */
    public Attributes(final IAttributes copyFrom) {
        setAttributes(copyFrom.getAttributesAsMap());
    }

    /**
     * Constructor out of map
     *
     * @param attributes attributes key-value map to set
     */
    public Attributes(Map<String, String> attributes) {
        setAttributes(attributes);
    }

    @Override
    public synchronized void clear() {
        if (fAttributes != null) {
            fAttributes.clear();
        }
    }

    @Override
    public synchronized boolean hasAttribute(String key) {
        return fAttributes != null && fAttributes.containsKey(key);
    }

    @Override
    public synchronized String getAttribute(String key) {
        if (fAttributes != null) {
            return fAttributes.get(key);
        }
        return null;
    }

    @Override
    public synchronized String getAttribute(String key, String defaultValue) {
        String value = getAttribute(key);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }

    @Override
    public int getAttributeAsInt(String key, int nDefault) {
        String value = getAttribute(key);
        if (value != null && !value.isEmpty()) {
            try {
                return Integer.decode(value);
            } catch (NumberFormatException e) {
                // do nothing, return default
            }
        }
        return nDefault;
    }

    @Override
    public long getAttributeAsLong(String key, long nDefault) {
        String value = getAttribute(key);
        return IAttributes.stringToLong(value, nDefault);
    }

    @Override
    public boolean getAttributeAsBoolean(String key, boolean bDefault) {
        String value = getAttribute(key);
        if (value == null || value.isEmpty()) {
            return bDefault;
        }
        return value.equals("1") || value.equalsIgnoreCase("true"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public synchronized void setAttribute(String key, String value) {
        if (key == null) {
            return;
        }
        if (fAttributes == null) {
            fAttributes = new TreeMap<>(); // use natural sorting to get consistent string representation
        }
        if (value != null) {
            fAttributes.put(key, value);
        } else {
            fAttributes.remove(key);
        }
    }

    @Override
    public void setAttribute(String key, boolean value) {
        setAttribute(key, value ? "1" : "0"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public void setAttribute(String key, int value) {
        setAttribute(key, Integer.toString(value));
    }

    @Override
    public synchronized void removeAttribute(String key) {
        if (fAttributes != null) {
            fAttributes.remove(key);
        }
    }

    @Override
    public synchronized void mergeAttribute(String key, String value) {
        if (!hasAttribute(key)) {
            setAttribute(key, value);
        }
    }

    @Override
    public synchronized boolean hasAttributes() {
        return fAttributes != null && !fAttributes.isEmpty();
    }

    @Override
    public synchronized Map<String, String> getAttributesAsMap() {
        return fAttributes;
    }

    @Override
    public synchronized void setAttributes(Map<String, String> attributes) {
        if (attributes == null) {
            fAttributes = null;
        } else {
            // make copy
            fAttributes = new TreeMap<>();
            fAttributes.putAll(attributes);
        }
    }

    @Override
    public synchronized void addAttributes(Map<String, String> attributes) {
        if (attributes != null && !attributes.isEmpty()) {
            // make copy
            if (fAttributes == null) {
                setAttributes(attributes);
            } else {
                fAttributes.putAll(attributes);
            }
        }
    }

    @Override
    public void setAttributes(String attributesString) {
        fAttributes = splitString(attributesString);
    }

    @Override
    public synchronized void setAttributes(IAttributes attributes) {
        if (attributes != null && attributes.hasAttributes()) {
            setAttributes(attributes.getAttributesAsMap());
        } else {
            fAttributes = null;
        }
    }

    @Override
    public synchronized void addAttributes(IAttributes attributes) {
        if (attributes != null && attributes.hasAttributes()) {
            addAttributes(attributes.getAttributesAsMap());
        }
    }

    @Override
    public synchronized void mergeAttributes(final IAttributes attributes) {
        if (attributes == null || !attributes.hasAttributes()) {
            return; // nothing to merge
        }
        mergeAttributes(attributes.getAttributesAsMap());
    }

    @Override
    public synchronized void mergeAttributes(Map<String, String> attributesMap) {
        if (attributesMap == null || attributesMap.isEmpty()) {
            return; // nothing to merge
        }
        if (fAttributes == null) {
            setAttributes(attributesMap);
        }
        for (Entry<String, String> e : attributesMap.entrySet()) {
            mergeAttribute(e.getKey(), e.getValue());
        }
    }

    @Override
    public synchronized void mergeAttributes(final IAttributes attributes, final String prefix) {
        if (attributes == null || !attributes.hasAttributes()) {
            return; // nothing to merge
        }
        Map<String, String> attributesMap = attributes.getAttributesAsMap();
        for (Entry<String, String> e : attributesMap.entrySet()) {
            String key = e.getKey();
            if (key.startsWith(prefix)) {
                mergeAttribute(key, e.getValue());
            }
        }
    }

    @Override
    public synchronized boolean containsAttribute(String pattern) {
        if (fAttributes == null) {
            return false;
        }
        for (String key : fAttributes.keySet()) {
            if (WildCards.match(pattern, key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equalsAttributes(final IAttributes otherAttributes) {
        if (otherAttributes == null)
            return false;
        return equals(otherAttributes);
    }

    @Override
    public boolean matchAttributes(final IAttributes attributes) {
        return matchAttributes(attributes, null, false);
    }

    @Override
    public boolean matchAttributes(final IAttributes attributes, String prefix) {
        return matchAttributes(attributes, prefix, false);
    }

    @Override
    public boolean matchCommonAttributes(final IAttributes attributes) {
        return matchAttributes(attributes, null, true);
    }

    @Override
    public boolean matchCommonAttributes(final IAttributes attributes, String prefix) {
        return matchAttributes(attributes, prefix, true);
    }

    /**
     * Checks if attributes found in this element and supplied map match using wild
     * card match
     *
     * @param attributes to match to
     * @param prefix     if not null or empty - match only attributes with that
     *                   prefix
     * @param bCommon    match mode: true - compare only attributes found in both
     *                   maps, false - match all
     * @return true if matches, false otherwise
     */
    public boolean matchAttributes(final IAttributes attributes, String prefix, boolean bCommon) {
        if (fAttributes == null || fAttributes.isEmpty())
            return true;
        if (attributes == null || !attributes.hasAttributes()) {
            return false;
        }
        for (Entry<String, String> e : fAttributes.entrySet()) {
            String key = e.getKey();
            if (prefix != null && !prefix.isEmpty() && !key.startsWith(prefix)) {
                continue;
            }
            String val = e.getValue();
            String pattern = attributes.getAttribute(key);
            if (pattern == null) {
                if (bCommon) {
                    continue;
                }
                return false;
            }
            if (!matchAttribute(key, val, pattern)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean matchAttribute(String key, String value, String pattern) {
        return WildCards.match(pattern, value);
    }

    @Override
    public boolean containsValue(String value) {
        if (fAttributes == null || fAttributes.isEmpty()) {
            return false;
        }
        return fAttributes.containsValue(value);
    }

    @Override
    public boolean containsValuePattern(String pattern) {
        if (pattern == null || fAttributes == null || fAttributes.isEmpty()) {
            return false;
        }
        for (Entry<String, String> e : fAttributes.entrySet()) {
            String key = e.getKey();
            String val = e.getValue();
            if (matchAttribute(key, val, pattern)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getUrl() {
        return getAttribute(CmsisConstants.URL);
    }

    @Override
    public String getDoc() {
        return getAttribute(CmsisConstants.DOC);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IAttributes) {
            IAttributes other = (IAttributes) obj;
            return other.toString().equals(toString());
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return getAttributesAsString(", "); //$NON-NLS-1$
    }

    /**
     * Splits given string into key-value pairs using "," as delimiter
     *
     * @param attributesString string to split, e.g in format
     *                         <code>key1="value1", key2="vaule2", ...</code>
     * @return Map of key-value String pairs
     */
    public static Map<String, String> splitString(String attributesString) {
        if (attributesString == null || attributesString.isEmpty()) {
            return null;
        }
        String[] pairs = attributesString.split("\\\","); //$NON-NLS-1$
        if (pairs == null || pairs.length == 0) {
            return null;
        }
        Map<String, String> attributes = new TreeMap<>();
        for (String p : pairs) {
            String[] pair = p.split("=\\\""); //$NON-NLS-1$
            if (pair == null || pair.length != 2) {
                continue;
            }
            String key = pair[0].trim();
            String val = pair[1].trim();
            if (val.endsWith("\"")) { //$NON-NLS-1$
                val = val.substring(0, val.length() - 1);
            }
            attributes.put(key, val);
        }

        return attributes;
    }
}
