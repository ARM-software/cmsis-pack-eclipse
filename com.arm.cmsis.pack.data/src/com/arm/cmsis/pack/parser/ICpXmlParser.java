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

package com.arm.cmsis.pack.parser;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.arm.cmsis.pack.data.ICpItem;

/**
 * Interface to CMSIS-Pack description file (*.pdsc) parser
 */
public interface ICpXmlParser extends ICpParser {

    /**
     * Sets schema file to use by the parser
     *
     * @param xsdFile schema file name to use with absolute path
     */
    void setXsdFile(String xsdFile);

    /**
     * Returns schema file used by parser
     *
     * @return absolute schema file name or null if not set
     */
    String getXsdFile();

    /**
     * Sets XML tags to ignore during parsing
     *
     * @param ignoreTags set of tags to ignore
     */
    void setIgnoreTags(Set<String> ignoreTags);

    /**
     * Sets XML tags to ignore during parsing
     *
     * @param ignoreTags array of tags to ignore
     */
    default void setIgnoreTagsFromArray(String[] ignoreTags) {
        Set<String> tagSet = new HashSet<>();
        if (ignoreTags != null && ignoreTags.length > 0)
            Collections.addAll(tagSet, ignoreTags);
        setIgnoreTags(tagSet);
    }

    /**
     * Sets XML tags to ignore during writing, does the same as setIgnoreTags()
     *
     * @param ignoreTags set of tags to ignore
     * @deprecated
     */
    @Deprecated
    void setWriteIgnoreTags(Set<String> ignoreTags);

    /**
     * Adjusts deprecated attribute values to modern ones
     *
     * @param key   attribute key
     * @param value attribute value
     * @return adjusted attribute value
     */
    String adjustAttributeValue(String key, String value);

    /**
     * Parses supplied string in XML format
     *
     * @param xml XML string to parse
     * @return root ICpItem object
     */
    ICpItem parseXmlString(String xml);

    /**
     * Generates XML text out of ICpItem and saves it to an XML file
     *
     * @param root ICpItem to save
     * @param file file to contain the generated XML
     * @return true if successful
     */
    boolean writeToXmlFile(ICpItem root, String file);

    /**
     * Generates XML text out of ICpItem and returns it as a string
     *
     * @param root ICpItem to write
     * @return generate XML text if successful, null otherwise
     */
    String writeToXmlString(ICpItem root);

}
