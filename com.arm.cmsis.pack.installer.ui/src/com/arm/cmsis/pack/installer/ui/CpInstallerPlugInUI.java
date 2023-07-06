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

package com.arm.cmsis.pack.installer.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpEnvironmentProvider;
import com.arm.cmsis.pack.ICpPackInstaller;
import com.arm.cmsis.pack.ICpPackManager;
import com.arm.cmsis.pack.ICpPackManager.LoadMode;
import com.arm.cmsis.pack.installer.ui.perspectives.PackManagerPerspective;
import com.arm.cmsis.pack.preferences.CpPreferenceInitializer;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.console.RteConsole;
import com.arm.cmsis.pack.utils.Utils;

/**
 * The activator class controls the plug-in life cycle
 */
public class CpInstallerPlugInUI extends AbstractUIPlugin implements IWorkbenchListener, IPerspectiveListener {

    // The plug-in ID
    public static final String PLUGIN_ID = "com.arm.cmsis.pack.installer.ui"; //$NON-NLS-1$

    // The shared instance
    private static CpInstallerPlugInUI plugin;

    static PackInstallerViewController viewController = null;

    /**
     * The constructor
     */
    public CpInstallerPlugInUI() {
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        ICpEnvironmentProvider envProvider = CpPlugIn.getEnvironmentProvider();
        if (envProvider != null) {
            viewController = envProvider.getAdapter(PackInstallerViewController.class);
        }
        if (viewController == null) {
            viewController = new PackInstallerViewController();
        }

        CpPlugIn.addRteListener(viewController);
        RteConsole.openGlobalConsole();

        IWorkbench wb = PlatformUI.getWorkbench();
        if (wb != null) {
            wb.addWorkbenchListener(this);
            if (wb.getActiveWorkbenchWindow() != null) {
                wb.getActiveWorkbenchWindow().addPerspectiveListener(this);
            }
        }
        // load all packs if needed
        ICpPackManager pm = CpPlugIn.getPackManager();
        if (pm == null) {
            return;
        }
        boolean bReload = pm.getLoadMode() != LoadMode.ALL;
        pm.setLoadMode(LoadMode.ALL);
        boolean bCheckForUpdates = pm.initPackRoot();
        if (!bCheckForUpdates) {
            String now = Utils.getCurrentDate();
            bCheckForUpdates = CpPreferenceInitializer.getAutoUpdateFlag()
                    && !now.equals(CpPreferenceInitializer.getLastUpdateTime());
        }
        if (bCheckForUpdates) {
            pm.setCheckForUpdates(bCheckForUpdates);
        }
        CpPlugInUI.scheduleCheckForPackUpdates();
        if (bReload) {
            pm.reload();
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        CpPlugIn.removeRteListener(viewController);
        viewController.clear();

        IWorkbench wb = PlatformUI.getWorkbench();
        if (wb != null) {
            wb.removeWorkbenchListener(this);
            if (wb.getActiveWorkbenchWindow() != null) {
                wb.getActiveWorkbenchWindow().removePerspectiveListener(this);
            }
        }
        super.stop(context);
    }

    public static PackInstallerViewController getViewController() {
        return viewController;
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static CpInstallerPlugInUI getDefault() {
        return plugin;
    }

    @Override
    public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
        if (PackManagerPerspective.ID.equals(page.getPerspective().getId())) {
            page.setEditorAreaVisible(false);
        }
    }

    @Override
    public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
        // does nothing
    }

    @Override
    public boolean preShutdown(IWorkbench workbench, boolean forced) {
        if (CpPlugIn.getPackManager() == null || forced) {
            return true;
        }
        ICpPackInstaller packInstaller = CpPlugIn.getPackManager().getPackInstaller();
        if (packInstaller != null && packInstaller.isBusy()) {
            boolean exit = MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
                    Messages.CpInstallerPlugInUI_ExitEclipse, Messages.CpInstallerPlugInUI_ExitEclipseMessage);
            if (exit) {
                packInstaller.reset();
                try {
                    Thread.sleep(500); // wait for the cancel
                } catch (InterruptedException e) {
                    // ignore the exception
                }
            }
            return exit;
        }
        return true;
    }

    @Override
    public void postShutdown(IWorkbench workbench) {
        // does nothing
    }

}
