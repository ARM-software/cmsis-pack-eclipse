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

package com.arm.cmsis.pack.data;

/**
 * The CMSIS code template
 */
public interface ICpCodeTemplate extends ICpItem {

    /**
     * Returns the component name
     *
     * @return component name
     */
    String getComponentName();

    /**
     * Return's the selection name
     *
     * @return selection name
     */
    String getSelectionName();

    /**
     * Some code template item contains multiple templates
     *
     * @return code templates
     */
    String[] getCodeTemplates();

    /**
     * @param filePath the file's path to this code template
     */
    void addCodeTemplate(String filePath);
}
