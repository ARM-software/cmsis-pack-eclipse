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

package com.arm.cmsis.pack.events;

import com.arm.cmsis.pack.rte.IRteConfiguration;

/**
 * Combined interface for configuration-to-client bidirectional communication 
 */
public interface IRteConfigurationProxy extends IRteConfiguration, IRteEventProxy{

	/**
	 * Adds RTE event listener to internal list 
	 * @param listener listener to add
	 */
	void addRteEventListener(IRteEventListener listener);

	
	/**
	 * Removes  RTE event listener from internal list 
	 * @param listener listener to remove
	 */
	void removeRteEventListener(IRteEventListener listener);

	
	/**
	 * Sends RTE event to registered listeners 
	 * @param event an event to send
	 */
	void fireRteEvent(RteEvent event); 
}
