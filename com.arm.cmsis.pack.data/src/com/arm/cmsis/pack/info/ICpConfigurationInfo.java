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

package com.arm.cmsis.pack.info;

import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPackFilter;
import com.arm.cmsis.pack.data.ICpRootItem;

/**
 * Interface representing root element of instantiated CMSIS-Pack data
 */
public interface ICpConfigurationInfo extends ICpRootItem {

    /**
     * Returns device info stored in the configuration
     *
     * @return ICpDeviceInfo stored in the configuration
     */
    ICpDeviceInfo getDeviceInfo();

    /**
     * Returns board info stored in the configuration
     *
     * @return ICpBoardInfo stored in the configuration
     */
    ICpBoardInfo getBoardInfo();

    /**
     * Sets board info to store in the configuration
     *
     * @param boardInfo ICpBoardInfo to store in the configuration
     */
    void setBoardInfo(ICpBoardInfo boardInfo);

    /**
     * Returns toolchain information as generic IcpItem with "Tcompiler" and
     * "Toutput" attributes
     *
     * @return ICpItem describing toolchain info
     */
    ICpItem getToolChainInfo();

    /**
     * Return item that is parent of components tems
     *
     * @return ICpItem owning ICpComponentInfo items
     */
    ICpItem getComponentsItem();

    /**
     * Return item that is parent of api items
     *
     * @return ICpItem owning ICpComponentInfo items representing APIs
     */
    ICpItem getApisItem();

    /**
     * Returns stored pack filter info if any
     *
     * @return ICpPackFilterInfo
     */
    ICpPackFilterInfo getPackFilterInfo();

    /**
     * Creates pack filter based on information stored in the info
     *
     * @return ICpPackFilter
     */
    ICpPackFilter createPackFilter();

    /**
     * Returns path to the directory where Device Family Pack is installed
     *
     * @return path to DFM installation directory or null if DFP is not installed
     */
    String getDfpPath();
}
