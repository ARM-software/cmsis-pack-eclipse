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

package com.arm.cmsis.pack.events;

import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.generic.ICommitable;

/**
 * Interface represents a controller to edit underlying configuration.</br>
 */
public interface IRteController extends ICommitable, IRteEventProxy {

    /**
     * Returns model-specific data info
     *
     * @return model-specific data info
     */
    ICpItem getDataInfo();

    /**
     * Sets model-specific data info
     *
     * @param info model-specific data info
     */
    void setDataInfo(ICpItem info);

    /**
     * Updates model-specific data info
     */
    void updateDataInfo();

    /**
     * Opens an URL in a browser or associated system editor
     *
     * @param url URL to open
     * @return null if successfully opened, otherwise reason why operation failed
     */
    String openUrl(String url);

}
