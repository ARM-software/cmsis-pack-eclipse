/*******************************************************************************
* Copyright (c) 2024 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
* SPDX-License-Identifier: EPL-2.0
* 
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.parser.yml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import com.amihaiemil.eoyaml.Node;
import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.amihaiemil.eoyaml.YamlNode;
import com.amihaiemil.eoyaml.YamlSequence;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpRootItem;
import com.arm.cmsis.pack.enums.ESeverity;
import com.arm.cmsis.pack.error.CmsisError;
import com.arm.cmsis.pack.parser.CpParser;
import com.arm.cmsis.pack.parser.CpYmlParserError;

public class CpYmlParser extends CpParser {

    private StringBuilder builder = new StringBuilder(); // builds output string

    private YamlMapping rootYmlMapping = null;

    @Override
    public ICpItem parseFile(String filePath) {
        clear();
        this.file = filePath;
        init();
        try {
            this.rootYmlMapping = Yaml.createYamlInput(new File(filePath)).readYamlMapping();
            parseMapping(rootItem, rootYmlMapping);
        } catch (IOException e) {
            CmsisError err = new CpYmlParserError(file, CpYmlParserError.Y401, ESeverity.Error, "Error parsing file", //$NON-NLS-1$
                    e);
            addError(err);
            rootItem = null;
            e.printStackTrace();
        }
        return rootItem;
    }

    @Override
    public ICpItem parseString(String input) {
        clear();
        init();
        try {
            this.rootYmlMapping = Yaml.createYamlInput(input).readYamlMapping();
            parseMapping(rootItem, rootYmlMapping);
        } catch (IOException e) {
            CmsisError err = new CpYmlParserError(file, CpYmlParserError.Y402, ESeverity.Error, "Error parsing string", //$NON-NLS-1$
                    e);
            addError(err);
            e.printStackTrace();
        }
        return rootItem;
    }

    @Override
    public boolean writeToFile(ICpItem root, String file) {
        clear();
        String ymlOutput = writeToString(root);
        File outputFile = new File(file);
        this.file = file;

        try (FileWriter fw = new FileWriter(outputFile);) {
            fw.write(ymlOutput);
            if (root instanceof ICpRootItem) {
                ((ICpRootItem) root).setFileName(file);
            }
        } catch (IOException e) {
            CmsisError err = new CpYmlParserError(file, CpYmlParserError.Y403, ESeverity.Error, "Error writing to file", //$NON-NLS-1$
                    e);
            addError(err);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public String writeToString(ICpItem root) {
        clear();
        writeItem(rootItem, 0);
        return builder.toString();
    }

    @Override
    public boolean init() {
        rootItem = createRootItem(CmsisConstants.EMPTY_STRING);
        return true;
    }

    @Override
    public void clear() {
        builder.delete(0, builder.length());
        this.file = null;
    }

    /**
     * Traverses passed YamlMapping and populates ICpItem parent
     * 
     * @param parent ICpItem used to represent the parent item
     * @param map    YamlMapping to parse
     */
    private void parseMapping(ICpItem parent, YamlMapping map) {
        for (YamlNode key : map.keys()) {
            YamlNode valueNode = map.value(key);
            if (valueNode.type() == Node.SCALAR) {
                parent.setAttribute(key.asScalar().value(), valueNode.asScalar().value());
            } else if (valueNode.type() == Node.SEQUENCE) {
                ICpItem child = createItem(parent, key.asScalar().value());
                parent.addChild(child);
                parseSequence(child, valueNode.asSequence());
            } else if (valueNode.type() == Node.MAPPING) {
                ICpItem child = createItem(parent, key.asScalar().value());
                parent.addChild(child);
                parseMapping(child, valueNode.asMapping());
            }
        }
    }

    /**
     * Traverses passed YamlSequence and populates ICpItem parent
     * 
     * @param parent   ICpItem used to represent the parent item
     * @param sequence YamlSequence to parse
     */
    protected void parseSequence(ICpItem parent, YamlSequence sequence) {
        for (YamlNode node : sequence) {
            ICpItem child = createItem(parent, CmsisConstants.MINUS);
            parent.addChild(child);
            if (node.type() == Node.SCALAR) {
                child.setText(node.asScalar().value());
            } else if (node.type() == Node.SEQUENCE) {
                parseSequence(child, node.asSequence());
            } else if (node.type() == Node.MAPPING) {
                parseMapping(child, node.asMapping());
            }
        }
    }

    /**
     * Traverses passed ICpItem item and populates string builder
     * 
     * @param item   ICpItem item to traverse
     * @param indent specify starting indent
     */
    private void writeItem(ICpItem item, int indent) {
        if (item == null) {
            return;
        }

        if (!item.equals(rootItem)) {
            if (!item.getTag().equals(CmsisConstants.MINUS)) {
                builder.append(CmsisConstants.SPACE.repeat(indent));
                builder.append(item.getTag() + CmsisConstants.COLON); // $NON-NLS-1$
                if (!item.getText().isEmpty()) {
                    builder.append(CmsisConstants.SPACE + item.getText());
                }
            } else {
                builder.append(CmsisConstants.SPACE.repeat(indent));
                builder.append(item.getTag() + CmsisConstants.SPACE);
                builder.append(item.getText());
            }

            boolean isSequence = item.getTag().equals(CmsisConstants.MINUS) && item.getText().isEmpty();
            if (!isSequence) {
                builder.append("\n"); //$NON-NLS-1$
            }
            boolean isFirstElement = true;
            Map<String, String> attributesMap = item.attributes().getAttributesAsMap();
            if (attributesMap != null) {
                for (String key : item.attributes().getAttributesAsMap().keySet()) {
                    if (isSequence && isFirstElement) {
                        isFirstElement = false;
                    } else {
                        builder.append(CmsisConstants.SPACE.repeat(indent + 2));
                    }
                    builder.append(key);
                    builder.append(CmsisConstants.COLON + CmsisConstants.SPACE);
                    builder.append(item.attributes().getAttribute(key));
                    builder.append("\n"); //$NON-NLS-1$
                }
            }
        }

        for (ICpItem child : item.getChildren()) {
            writeItem(child, item.equals(rootItem) ? 0 : indent + 2);
        }
    }
}
