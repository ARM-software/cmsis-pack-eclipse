package com.arm.cmsis.zone.it;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class CmsisZoneTestsActivator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "com.arm.cmsis.zone.it"; //$NON-NLS-1$

    // The shared instance
    private static CmsisZoneTestsActivator plugin;

    /**
     * The constructor
     */
    public CmsisZoneTestsActivator() {
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static CmsisZoneTestsActivator getDefault() {
        return plugin;
    }

}
