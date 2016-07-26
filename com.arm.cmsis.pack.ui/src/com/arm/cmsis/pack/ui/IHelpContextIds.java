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
