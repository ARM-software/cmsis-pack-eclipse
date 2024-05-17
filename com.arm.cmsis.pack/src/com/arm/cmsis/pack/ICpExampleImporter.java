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
package com.arm.cmsis.pack;

import java.util.Collection;

import org.eclipse.core.runtime.IAdaptable;

import com.arm.cmsis.pack.data.ICpExample;

/**
 * Base interface to import examples from CMSIS-Packs
 */
public interface ICpExampleImporter extends IAdaptable {

    /**
     * Imports the example and/or creates an environment-specific project out of it
     *
     * @param example ICpExample to import
     * @return the adaptable object that is can deliver created projects, e.g.
     *         created IProject itself
     * @see {@link #getCreatedProjects()}
     */
    IAdaptable importExample(ICpExample example);

    /**
     * Returns collection of created project name
     *
     * @return collection of project names
     */
    Collection<String> getCreatedProjectNames();
}
