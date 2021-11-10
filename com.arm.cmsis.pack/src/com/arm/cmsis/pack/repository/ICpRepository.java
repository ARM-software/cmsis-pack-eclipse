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

package com.arm.cmsis.pack.repository;

/**
 * The common interface for CMSIS-Packs Repository
 */
public interface ICpRepository {

    /**
     * Get the type of the repository's packs
     *
     * @return type of the repository's packs
     */
    String getType();

    /**
     * Get the name of the repository
     *
     * @return name of the repository
     */
    String getName();

    /**
     * Get the URL of the repository
     *
     * @return URL of the repository
     */
    String getUrl();

    /**
     * Get the number of attributes this repository contains. By default it is 3:
     * type, name and url
     *
     * @return number of attributes this repository contains
     */
    int getAttrCount();

}
