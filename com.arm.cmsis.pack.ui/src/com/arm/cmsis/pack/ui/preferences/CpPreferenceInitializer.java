/*******************************************************************************
* Copyright (c) 2014 ARM Ltd.
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/

package com.arm.cmsis.pack.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.ui.CpPlugInUI;

/**
 * Initializes CMSIS pack and RTE preferences 
 */
public class CpPreferenceInitializer extends AbstractPreferenceInitializer {
	/**
	 * Default constructor 
	 */
	public CpPreferenceInitializer() {
	}

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = CpPlugInUI.getDefault().getPreferenceStore();
		store.setDefault(CpPlugInUI.CMSIS_PACK_ROOT_PREFERENCE, IAttributes.EMPTY_STRING);
	}

}
