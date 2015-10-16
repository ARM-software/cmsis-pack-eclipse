/*******************************************************************************
* Copyright (c) 2015 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.events;

/**
 * RTE event: component selection change, active bundle change, etc.
 */
public class RteEvent {

	// some predefined topics
	public static final String CONFIGURATION_MODIFIED  = "com.arm.comsis.pack.rte.config.modified"; //$NON-NLS-1$
	public static final String CONFIGURATION_COMMITED  = "com.arm.comsis.pack.rte.config.commited"; //$NON-NLS-1$

	public static final String FILTER_MODIFIED = "com.arm.comsis.pack.rte.filter.modified"; //$NON-NLS-1$
	
	public static final String COMPONENT_SELECTION_MODIFIED = "com.arm.comsis.pack.rte.component.modified"; //$NON-NLS-1$
	public static final String COMPONENT_SHOW = "com.arm.comsis.pack.rte.component.show"; //$NON-NLS-1$
	
	public static final String PACKS_RELOADED = "com.arm.comsis.pack.reloaded"; //$NON-NLS-1$

	public static final String DEVICE_TRIGGER_SELECT  = "com.arm.comsis.pack.rte.device.select"; //$NON-NLS-1$

	public static final String PROJECT_ADDED  	= "com.arm.comsis.pack.rte.project.added"; //$NON-NLS-1$
	public static final String PROJECT_REMOVED  = "com.arm.comsis.pack.rte.project.removed"; //$NON-NLS-1$
	public static final String PROJECT_UPDATED  = "com.arm.comsis.pack.rte.project.updated"; //$NON-NLS-1$

	
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
