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

package com.arm.cmsis.pack.ui.preferences;

/**
 * Constant definitions for plug-in preferences
 *
 */
public class CpUIPreferenceConstants {

    public static final String CONSOLE_PREFIX = "com.arm.cmsis.pack.console."; //$NON-NLS-1$
    public static final String CONSOLE_COLOR_PREFIX = CONSOLE_PREFIX + "color."; //$NON-NLS-1$
    public static final String CONSOLE_OPEN_ON_OUT = CONSOLE_PREFIX + "OpenOnOut"; //$NON-NLS-1$
    public static final String CONSOLE_PRINT_IN_CDT = CONSOLE_PREFIX + "PrintInCdtConsole"; //$NON-NLS-1$
    public static final String CONSOLE_OUT_COLOR = CONSOLE_COLOR_PREFIX + "Out"; //$NON-NLS-1$
    public static final String CONSOLE_INFO_COLOR = CONSOLE_COLOR_PREFIX + "Info"; //$NON-NLS-1$
    public static final String CONSOLE_WARNING_COLOR = CONSOLE_COLOR_PREFIX + "Warning"; //$NON-NLS-1$
    public static final String CONSOLE_ERROR_COLOR = CONSOLE_COLOR_PREFIX + "Error"; //$NON-NLS-1$
    public static final String CONSOLE_BG_COLOR = CONSOLE_PREFIX + "BgColor"; //$NON-NLS-1$
}
