/*******************************************************************************
 * Copyright (c) 2021 ARM Ltd. and others
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
    public static final String CONFIGURATION_MODIFIED = "com.arm.cmsis.pack.rte.config.modified"; //$NON-NLS-1$
    public static final String CONFIGURATION_COMMITED = "com.arm.cmsis.pack.rte.config.commited"; //$NON-NLS-1$

    public static final String FILTER_MODIFIED = "com.arm.cmsis.pack.rte.filter.modified"; //$NON-NLS-1$

    public static final String COMPONENT_SELECTION_MODIFIED = "com.arm.cmsis.pack.rte.component.modified"; //$NON-NLS-1$
    public static final String COMPONENT_SHOW = "com.arm.cmsis.pack.rte.component.show"; //$NON-NLS-1$
    public static final String PACK_FAMILY_SHOW = "com.arm.cmsis.pack.rte.pack.show"; //$NON-NLS-1$

    public static final String PACKS_UPDATE_PENDING = "com.arm.cmsis.pack.update.pending"; //$NON-NLS-1$
    public static final String PACKS_RELOADED = "com.arm.cmsis.pack.reloaded"; //$NON-NLS-1$ - all packs reloaded
    public static final String PACKS_UPDATED = "com.arm.cmsis.pack.updated"; //$NON-NLS-1$ - some packs changed (added,
                                                                             // removed)
    public static final String PACK_JOB = "com.arm.cmsis.pack.job"; //$NON-NLS-1$ - prefix for job finished
    public static final String PACK_JOB_RELOAD = PACK_JOB + ".reload"; //$NON-NLS-1$ // job finished and requests
                                                                       // reload
    public static final String PACK_INSTALL_JOB_FINISHED = PACK_JOB + ".installed"; //$NON-NLS-1$
    public static final String PACK_IMPORT_FOLDER_JOB_FINISHED = PACK_JOB + ".imported"; //$NON-NLS-1$
    public static final String PACK_REMOVE_JOB_FINISHED = PACK_JOB + ".removed"; //$NON-NLS-1$
    public static final String PACK_DELETE_JOB_FINISHED = PACK_JOB + ".deleted"; //$NON-NLS-1$

    public static final String PACK_UPDATE_JOB_STARTED = PACK_JOB + ".update.started"; //$NON-NLS-1$
    public static final String PACK_UPDATE_JOB_FINISHED = PACK_JOB + ".update.finished"; //$NON-NLS-1$

    public static final String PRINT = "com.arm.cmsis.pack.print"; //$NON-NLS-1$
    public static final String PRINT_OUTPUT = PRINT + ".output"; //$NON-NLS-1$
    public static final String PRINT_INFO = PRINT + ".info"; //$NON-NLS-1$
    public static final String PRINT_WARNING = PRINT + ".warning"; //$NON-NLS-1$
    public static final String PRINT_ERROR = PRINT + ".error"; //$NON-NLS-1$

    public static final String PACK_OLNLINE_STATE_CHANGED = "com.arm.cmsis.pack.online"; //$NON-NLS-1$

    public static final String DEVICE_TRIGGER_SELECT = "com.arm.cmsis.pack.rte.device.select"; //$NON-NLS-1$

    public static final String PROJECT_ADDED = "com.arm.cmsis.pack.rte.project.added"; //$NON-NLS-1$
    public static final String PROJECT_REMOVED = "com.arm.cmsis.pack.rte.project.removed"; //$NON-NLS-1$
    public static final String PROJECT_UPDATED = "com.arm.cmsis.pack.rte.project.updated"; //$NON-NLS-1$

    public static final String IMPORT_PROJECT = "com.arm.cmsis.pack.rte.project.import_project"; //$NON-NLS-1$
    public static final String IMPORT_EXAMPLE = "com.arm.cmsis.pack.rte.project.import_example"; //$NON-NLS-1$

    public static final String PRE_IMPORT = "com.arm.cmsis.pack.rte.project.pre_import"; //$NON-NLS-1$
    public static final String POST_IMPORT = "com.arm.cmsis.pack.rte.project.post_import"; //$NON-NLS-1$

    // gpdsc file with given name is changed : created, deleted, modified
    public static final String GPDSC_CHANGED = "com.arm.cmsis.pack.gpdsc.changed"; //$NON-NLS-1$
    public static final String GPDSC_LAUNCH_ERROR = "com.arm.cmsis.pack.gpdsc.launch.error"; //$NON-NLS-1$

    protected String topic = null;
    protected Object data = null;

    /**
     * Constructor with topic only
     *
     * @param topic event topic
     */
    public RteEvent(final String topic) {
        this.topic = topic;
    }

    /**
     * Constructor with topic and data
     *
     * @param topic event topic
     * @param data  event data
     */
    public RteEvent(final String topic, final Object data) {
        this.topic = topic;
        this.data = data;
    }

    /**
     * Returns event topic
     *
     * @return event topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * Returns event data
     *
     * @return event data
     */
    public Object getData() {
        return data;
    }

}
