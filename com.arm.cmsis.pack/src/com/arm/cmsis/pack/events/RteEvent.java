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
	public static final String CONFIGURATION_MODIFIED  = "com.arm.cmsis.pack.rte.config.modified"; //$NON-NLS-1$
	public static final String CONFIGURATION_COMMITED  = "com.arm.cmsis.pack.rte.config.commited"; //$NON-NLS-1$

	public static final String FILTER_MODIFIED = "com.arm.cmsis.pack.rte.filter.modified"; //$NON-NLS-1$
	
	public static final String COMPONENT_SELECTION_MODIFIED = "com.arm.cmsis.pack.rte.component.modified"; //$NON-NLS-1$
	public static final String COMPONENT_SHOW = "com.arm.cmsis.pack.rte.component.show"; //$NON-NLS-1$
	
	public static final String PACKS_RELOADED = "com.arm.cmsis.pack.reloaded"; //$NON-NLS-1$
	public static final String PACK_INSTALL_JOB_FINISHED = "com.arm.cmsis.pack.installed"; //$NON-NLS-1$
	public static final String PACK_REMOVE_JOB_FINISHED = "com.arm.cmsis.pack.removed"; //$NON-NLS-1$
	public static final String PACK_DELETE_JOB_FINISHED = "com.arm.cmsis.pack.deleted"; //$NON-NLS-1$
	public static final String PACK_UNPACK_JOB_FINISHED = "com.arm.cmsis.pack.unpacked"; //$NON-NLS-1$
	
	public static final String EXAMPLE_INSTALL_JOB_FINISHED = "com.arm.cmsis.pack.einstalled"; //$NON-NLS-1$
	public static final String EXAMPLE_COPY_JOB_FINISHED = "com.arm.cmsis.pack.copied"; //$NON-NLS-1$

	public static final String DEVICE_TRIGGER_SELECT  = "com.arm.cmsis.pack.rte.device.select"; //$NON-NLS-1$

	public static final String PROJECT_ADDED  	= "com.arm.cmsis.pack.rte.project.added"; //$NON-NLS-1$
	public static final String PROJECT_REMOVED  = "com.arm.cmsis.pack.rte.project.removed"; //$NON-NLS-1$
	public static final String PROJECT_UPDATED  = "com.arm.cmsis.pack.rte.project.updated"; //$NON-NLS-1$

	
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
