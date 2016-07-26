/*******************************************************************************
* Copyright (c) 2016 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.ui.tree;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ColorConstants {

	public final static Color COLOR_BUTTON_TOP = new Color(Display.getCurrent(), new RGB(211, 215, 227));
	public final static Color COLOR_BUTTON_BOTTOM= new Color(Display.getCurrent(), new RGB(144, 153, 170));

	public final static Color COLOR_SPINNER_TOP = new Color(Display.getCurrent(), new RGB(211, 215, 227));
	public final static Color COLOR_SPINNER_BUTTON = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);

	public final static Color COLOR_BORDER = new Color(Display.getCurrent(), new RGB(150,150,150));

	public final static int ARC_WIDTH_HEIGHT = 8;
}