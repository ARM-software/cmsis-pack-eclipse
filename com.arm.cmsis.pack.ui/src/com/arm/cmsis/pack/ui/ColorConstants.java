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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ColorConstants {

    public static final Color COLOR_BUTTON_TOP = new Color(Display.getCurrent(), new RGB(211, 215, 227));
    public static final Color COLOR_BUTTON_BOTTOM = new Color(Display.getCurrent(), new RGB(144, 153, 170));

    public static final Color COLOR_SPINNER_TOP = new Color(Display.getCurrent(), new RGB(211, 215, 227));
    public static final Color COLOR_SPINNER_BUTTON = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);

    public static final Color COLOR_BORDER = new Color(Display.getCurrent(), new RGB(150, 150, 150));

    public static final Color COLOR_SUFFICS_BUTTON_TOP = new Color(Display.getCurrent(), new RGB(210, 210, 210));
    public static final Color COLOR_SUFFICS_BUTTON_BOTTOM = new Color(Display.getCurrent(), new RGB(160, 160, 160));

    public static final Color COLOR_LINK_FOREGROUND = Display.getCurrent().getSystemColor(SWT.COLOR_LINK_FOREGROUND);
    public static final Color COLOR_WIDGET_BORDER = Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BORDER);

    public static final Color GREEN = new Color(Display.getCurrent(), CpPlugInUI.GREEN);
    public static final Color YELLOW = new Color(Display.getCurrent(), CpPlugInUI.YELLOW);
    public static final Color RED = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
    public static final Color GRAY = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
    public static final Color BLACK = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
    public static final Color DARK_GRAY = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);

    public static final Color PALE_RED = new Color(Display.getCurrent(), new RGB(242, 161, 161));

    public static final int ARC_WIDTH_HEIGHT = 8;
}