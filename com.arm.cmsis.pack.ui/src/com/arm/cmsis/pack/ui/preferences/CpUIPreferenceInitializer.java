/*******************************************************************************
* Copyright (c) 2015 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/package com.arm.cmsis.pack.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.RGB;

import com.arm.cmsis.pack.ui.CpPlugInUI;


/**
 * Initializes UI preferences
 *
 */
public class CpUIPreferenceInitializer extends AbstractPreferenceInitializer {

	/** 
	 *  Default constructor
	 */
	public CpUIPreferenceInitializer() {
	}

	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences defaultPreferences = DefaultScope.INSTANCE.getNode(CpPlugInUI.PLUGIN_ID);
		
		defaultPreferences.putBoolean(CpUIPreferenceConstants.CONSOLE_OPEN_ON_OUT, true);

		defaultPreferences.put(CpUIPreferenceConstants.CONSOLE_BG_COLOR, StringConverter.asString(new RGB(255, 255, 255)));
		defaultPreferences.put(CpUIPreferenceConstants.CONSOLE_OUT_COLOR, StringConverter.asString(new RGB(0, 0, 0)));
		defaultPreferences.put(CpUIPreferenceConstants.CONSOLE_ERROR_COLOR, StringConverter.asString(new RGB(255, 0, 0)));
		defaultPreferences.put(CpUIPreferenceConstants.CONSOLE_INFO_COLOR, StringConverter.asString(new RGB(0, 0, 255)));

		
	}

}
