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

package com.arm.cmsis.pack.rte.components;

import java.util.Map;

import com.arm.cmsis.pack.data.ICpComponent;

/**
 * Interface for Cgroup level of component hierarchy. Extends IRteComponentItem
 * and adds methods to handle ICpApi
 *
 * @see IRteComponentItem
 */
public interface IRteComponentGroup extends IRteComponentItem {

    /**
     * Returns API collection as map sorted by version (from latest to oldest )
     *
     * @return API map
     */
    Map<String, ICpComponent> getApis();

    /**
     * Returns api of specified version
     *
     * @return ICpApi object if found or null
     */
    ICpComponent getApi(String version);

    /**
     * Returns version of active API
     *
     * @return active API version or null if no active API available
     */
    String getActiveApiVersion();

    /**
     * Sets active API version
     *
     * @param version to set active
     * @return if active version has changed
     */
    boolean setActiveApi(String version);

}
