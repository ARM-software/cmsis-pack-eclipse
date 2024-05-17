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

package com.arm.cmsis.pack.parser;

import com.arm.cmsis.pack.data.CpItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpItemFactory;
import com.arm.cmsis.pack.error.ICmsisErrorCollection;

/**
 * An abstract format free parser interface
 */
public interface ICpParser extends ICpItemFactory, ICmsisErrorCollection {

    /**
     * Initializes the parser
     *
     * @return true if successful
     */
    boolean init();

    /**
     * Clears internal data, error strings and resets builder
     */
    void clear();

    /**
     * Factory method to create ICpItem-derived root instance
     *
     * @param tag tag for the item
     * @return created ICpItem
     */
    default ICpItem createRootItem(String tag) {
        return new CpItem(null, tag);
    }

    /**
     * Parses supplied file
     *
     * @param filePath path of file to parse
     * @return root ICpItem object
     */
    ICpItem parseFile(String filePath);

    /**
     * Parses supplied string in appropriate format
     *
     * @param input string to parse
     * @return root ICpItem object
     */
    ICpItem parseString(String input);

    /**
     * Generates text out of ICpItem and saves it to a file in the appropriate
     * format
     *
     * @param root     ICpItem to save
     * @param filePath path to file where the generated output should be saved
     * @return true if successful
     */
    boolean writeToFile(ICpItem root, String filePath);

    /**
     * Generates text out of ICpItem and returns it as a string
     *
     * @param root ICpItem to write
     * @return generate text if successful, null otherwise
     */
    String writeToString(ICpItem root);

}
