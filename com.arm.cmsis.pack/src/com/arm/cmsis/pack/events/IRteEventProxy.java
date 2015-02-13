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

/**
 * Interface that provides communication way from RTE model to clients (e.g. GUI).
 * <br>
 * The implementation is responsible for translating RTE model notifications into events and deliver them to clients.
 * <br>
 * RTE model itself can register only one proxy that in turn should maintain its own listener list, or use other methods
 * to delivering the events to clients.  
 */
public interface IRteEventProxy {

	/**
	 * Processes the RteEvent and deliver it to clients 
	 * @param event RteEvent to process
	 */
	void processRteEvent(RteEvent event);
	
}
