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

package com.arm.cmsis.pack.dsq;

public interface IDsqGeneratorInfo {

    /**
     * Get ID of the DSQ script generator
     *
     * @return ID of the DSQ script generator
     */
    String getID();

    /**
     * Get name of the DSQ script generator
     *
     * @return Name of the DSQ script generator
     */
    String getName();

    /**
     * Get description of the DSQ script generator
     *
     * @return Description of the DSQ script generator
     */
    String getDescription();
}
