/*******************************************************************************
* Copyright (c) 2021 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* Eclipse Project - generation from template
* ARM Ltd and ARM Germany GmbH - application-specific implementation
*******************************************************************************/
package com.arm.cmsis.pack.project;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class CpProjectPlugIn extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "com.arm.cmsis.pack.project"; //$NON-NLS-1$

    // The shared instance
    private static CpProjectPlugIn plugin;
    private RteProjectManager rteProjectManager = null;

    /**
     * The constructor
     */
    public CpProjectPlugIn() {
        rteProjectManager = new RteProjectManager();
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        rteProjectManager.destroy();
        rteProjectManager = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static CpProjectPlugIn getDefault() {
        return plugin;
    }

    public static synchronized RteProjectManager getRteProjectManager() {
        return getDefault().rteProjectManager;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in relative
     * path
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

}
