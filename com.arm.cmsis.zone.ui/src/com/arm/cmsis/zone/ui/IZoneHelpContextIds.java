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

package com.arm.cmsis.zone.ui;

/**
 * Interface for help context IDs in this plug-in
 */
public interface IZoneHelpContextIds {
    public static final String PREFIX = CpZonePluginUI.PLUGIN_ID + "."; //$NON-NLS-1$

    public static final String RESOURCE_PAGE = PREFIX + "resource_page"; //$NON-NLS-1$
    public static final String MEMORY_MAP_PAGE = PREFIX + "memory_map_page"; //$NON-NLS-1$
    public static final String ZONE_PAGE = PREFIX + "zone_page"; //$NON-NLS-1$
    public static final String ZONE_MAP_PAGE = PREFIX + "zone_map_page"; //$NON-NLS-1$
    public static final String SETUP_PAGE = PREFIX + "setup_page"; //$NON-NLS-1$

}
