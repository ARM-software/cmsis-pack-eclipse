/*******************************************************************************
* Copyright (c) 2021 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
* Contributors:
* Eclipse Project - generation from template
* ARM Ltd and ARM Germany GmbH - application-specific implementation
*******************************************************************************/

package com.arm.cmsis.pack;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import com.arm.cmsis.pack.events.IRteEventListener;
import com.arm.cmsis.pack.events.IRteEventProxy;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.events.RteEventProxy;
import com.arm.cmsis.pack.preferences.CpPreferenceInitializer;
import com.arm.cmsis.pack.utils.DeviceVendor;

/**
 * The activator class controls the plug-in life cycle
 */
public class CpPlugIn extends Plugin implements IRteEventProxy {

    // The plug-in ID
    public static final String PLUGIN_ID = "com.arm.cmsis.pack"; //$NON-NLS-1$
    // Preference ID

    private static BundleContext context;
    private static CpPlugIn plugin;

    private IRteEventProxy rteEventProxy = new RteEventProxy();
    private ICpPackManager thePackManager = null;
    private ICpEnvironmentProvider theEnvironmentProvider = null;
    private ICpPackInstaller thePackInstaller = null;

    public CpPlugIn() {
        super();
    }

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        plugin = this;
        CpPlugIn.context = bundleContext;

        DeviceVendor.fillMaps(); // the maps can later be updated by ICpEnvironmentProvider

        // initialize environment provider first to let it change pack manager or/and
        // installer
        initEnvironmentProvider();

        if (thePackManager == null) {
            thePackManager = new CpPackManager();
        }
        thePackManager.setRteEventProxy(this);

        if (thePackInstaller == null) {
            thePackInstaller = CpPackInstallerFactory.getInstance().getExtender();
        }

        if (thePackManager.getPackInstaller() == null) {
            thePackManager.setPackInstaller(thePackInstaller);
        }

        String packRoot = CpPreferenceInitializer.getPackRoot();
        thePackManager.initParser(null);
        thePackManager.setCmsisPackRootDirectory(packRoot); // will load packs and issue corresponding event
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        CpPlugIn.context = null;
        DeviceVendor.clear();
        CpPackInstallerFactory.destroy();
        CpEnvironmentProviderFactory.destroy();
        CpPreferenceInitializer.destroy();
        plugin = null;
        thePackManager = null;
        theEnvironmentProvider = null;
        thePackInstaller = null;
        rteEventProxy.removeAllListeners();
        rteEventProxy = null;
        super.stop(bundleContext);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static CpPlugIn getDefault() {
        return plugin;
    }

    static BundleContext getContext() {
        return context;
    }

    /**
     * Explicitly sets the environment provider
     *
     * @param provider ICpEnvironmentProvider
     */
    public void setEnvironmentProvider(ICpEnvironmentProvider provider) {
        if (provider == theEnvironmentProvider) {
            return;
        }
        rteEventProxy.removeListener(theEnvironmentProvider);
        theEnvironmentProvider = provider;
        rteEventProxy.addListener(theEnvironmentProvider);
    }

    /**
     * Returns the environment provider
     *
     * @return ICpEnvironmentProvider
     */
    public static ICpEnvironmentProvider getEnvironmentProvider() {
        return plugin != null ? plugin.theEnvironmentProvider : new CpEnvironmentProvider();
    }

    /**
     * Initializes an environment provider
     */
    private void initEnvironmentProvider() {
        if (theEnvironmentProvider == null) {
            theEnvironmentProvider = CpEnvironmentProviderFactory.getInstance().getExtender();
            if (theEnvironmentProvider == null) {
                // create default one
                theEnvironmentProvider = new CpEnvironmentProvider();
            }
        }
        theEnvironmentProvider.setRteEventProxy(rteEventProxy);
        rteEventProxy.addListener(theEnvironmentProvider);
        theEnvironmentProvider.init();
    }

    /**
     * Returns the pack manager
     *
     * @return ICpPackManager
     */
    public static ICpPackManager getPackManager() {
        return plugin != null ? plugin.thePackManager : null;
    }

    /**
     * Explicitly sets the pack manager
     *
     * @param pm ICpPackManager
     */
    public void setPackManager(ICpPackManager pm) {
        thePackManager = pm;
    }

    /**
     * Explicitly sets the pack installer and assigns it to the pack manager
     *
     * @param pm ICpPackManager
     */
    public void setPackInstaller(ICpPackInstaller pi) {
        thePackInstaller = pi;
        if (thePackManager != null) {
            thePackManager.setPackInstaller(thePackInstaller);
        }
    }

    /**
     * Adds an IRteEventListener to the internal listener list
     *
     * @param listener IRteEventListener
     */
    public static void addRteListener(IRteEventListener listener) {
        if (plugin != null) {
            plugin.addListener(listener);
        }
    }

    /**
     * Removes an IRteEventListener from the internal listener list
     *
     * @param listener
     */
    public static void removeRteListener(IRteEventListener listener) {
        if (plugin != null) {
            plugin.removeListener(listener);
        }
    }

    @Override
    public IRteEventProxy getRteEventProxy() {
        return rteEventProxy;
    }

    @Override
    public void addListener(IRteEventListener listener) {
        rteEventProxy.addListener(listener);
    }

    @Override
    public void removeListener(IRteEventListener listener) {
        rteEventProxy.removeListener(listener);
    }

    @Override
    public void removeAllListeners() {
        rteEventProxy.removeAllListeners();
    }

    @Override
    public void notifyListeners(RteEvent event) {
        rteEventProxy.notifyListeners(event);
    }

}
