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

package com.arm.cmsis.pack.project;

/**
 * Interface for help context IDs in this plug-in
 */
public interface IHelpContextIds {
    public static final String PREFIX = CpProjectPlugIn.PLUGIN_ID + '.';

    public static final String CMSIS_PROJECT_WIZARD = PREFIX + "cmsis_project_wizard"; //$NON-NLS-1$
    public static final String CMSIS_DEVICE_SELECT_WIZARD = PREFIX + "cmsis_device_select_wizard"; //$NON-NLS-1$
    public static final String CODE_TEMPLATE_WIZARD = PREFIX + "code_template"; //$NON-NLS-1$
    public static final String RTE_PROPERTY_PAGE = PREFIX + "rte_property_page"; //$NON-NLS-1$
}
