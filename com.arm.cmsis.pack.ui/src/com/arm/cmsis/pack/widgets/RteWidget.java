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
package com.arm.cmsis.pack.widgets;

import com.arm.cmsis.pack.events.IRteConfigurationProxy;
import com.arm.cmsis.pack.events.IRteEventListener;
import com.arm.cmsis.pack.rte.IRteConfiguration;

public abstract class RteWidget implements IRteEventListener {
	public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0]; 

	protected IRteConfigurationProxy fConfiguration = null;		// contains RteComponents
	
	/**
	 * Set a RTE configuration
	 * @param configuration
	 */
	public void setConfiguration(IRteConfigurationProxy configuration) {
		if(fConfiguration != null)
			configuration.removeRteEventListener(this);
		fConfiguration = configuration;
		if(fConfiguration != null)
			configuration.addRteEventListener(this);
	}
	
	/**
	 * @return RTE configuration
	 */
	public IRteConfiguration getConfiguration() {
		return fConfiguration;
	}
	
	/**
	 * Refresh UI without changing configuration 
	 */
	public abstract void refresh();
	
	/**
	 * refresh UI after having changed configuration 
	 */
	public abstract void update();
	
	
	
}
