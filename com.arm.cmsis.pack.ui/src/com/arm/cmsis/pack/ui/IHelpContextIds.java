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

package com.arm.cmsis.pack.ui;

/**
 * Interface for help context IDs in this plug-in
 */
public interface IHelpContextIds {
    public static final String PREFIX = CpPlugInUI.PLUGIN_ID + "."; //$NON-NLS-1$

    public static final String COMPONENT_PAGE = PREFIX + "component_page"; //$NON-NLS-1$
    public static final String DEVICE_PAGE = PREFIX + "device_page"; //$NON-NLS-1$
    public static final String PACKS_PAGE = PREFIX + "packs_page"; //$NON-NLS-1$

}
