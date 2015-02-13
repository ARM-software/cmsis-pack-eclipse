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
 * RTE event: component selection change, active bundle change, etc.
 */
public class RteEvent {

	// some predefined topics
	public static final String CONFIGURATION_ACTIVATED = "com.arm.comsis.pack.rte.config.activated";
	public static final String CONFIGURATION_MODIFIED = "com.arm.comsis.pack.rte.config.modified";
	public static final String CONFIGURATION_APPLIED  = "com.arm.comsis.pack.rte.config.applied";

	public static final String COMPONENT_SHOW = "com.arm.comsis.pack.rte.component.show";
	
	public static final String PACK_ALL_LOADED = "com.arm.comsis.pack.allloaded";
	
	protected String topic = null;
	protected Object data  = null;
	
	
	/**
	 * Constructor with topic only
	 * @param topic event topic
	 */
	public RteEvent(final String topic) {
		this.topic = topic;
	}

	/**
	 * Constructor with topic and data
	 * @param topic event topic
	 * @param data event data
	 */
	public RteEvent(final String topic, final Object data) {
		this.topic = topic;
		this.data = data;
	}
	
	/**
	 * Returns event topic
	 * @return event topic
	 */
	public String getTopic() {
		return topic;
	}
	
	/**
	 * Returns event data
	 * @return event data
	 */
	public Object getData() {
		return data;
	}
	
}
