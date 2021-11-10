package com.arm.cmsis.pack.data;

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

/**
 * Interface for ICpItem-based tree root element (ICpPack, ICpConfigurationInfo,
 * etc.) that gets loaded from a file or refers to an item to load.
 */
public interface ICpRootItem extends ICpItem {

    /**
     * Set absolute file name
     *
     * @param fileName absolute file name to set
     */
    void setFileName(String fileName);

    /**
     * Returns absolute file name of the item
     *
     * @return absolute file name of the item
     */
    String getFileName();

    /**
     * Returns directory path of the item
     *
     * @param keepSlash flag if to keep or remove trailing slash
     * @return absolute directory path of the item
     */
    String getDir(boolean keepSlash);

}
