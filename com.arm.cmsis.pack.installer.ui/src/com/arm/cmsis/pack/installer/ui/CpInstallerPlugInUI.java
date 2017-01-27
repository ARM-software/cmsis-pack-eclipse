/*******************************************************************************
 * Copyright (c) 2015 ARM Ltd. and others
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

import java.lang.reflect.InvocationTargetException;
import java.util.Timer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
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
import com.arm.cmsis.pack.installer.ui.perspectives.PackManagerPerspective;
import com.arm.cmsis.pack.preferences.CpPreferenceInitializer;
import com.arm.cmsis.pack.ui.console.RteConsole;
import com.arm.cmsis.pack.utils.Utils;

/**
 * The activator class controls the plug-in life cycle
 */
public class CpInstallerPlugInUI extends AbstractUIPlugin implements IWorkbenchListener, IPerspectiveListener  {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.arm.cmsis.pack.installer.ui"; //$NON-NLS-1$

	// The shared instance
	private static CpInstallerPlugInUI plugin;

	static PackInstallerViewController viewController = null;

	volatile static Timer timer = null;
	boolean timerStarted = false;
	private boolean checkForUpdates = true;

	/**
	 * The constructor
	 */
	public CpInstallerPlugInUI() {
	}

	@SuppressWarnings("cast")
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		ICpEnvironmentProvider envProvider = CpPlugIn.getEnvironmentProvider();
		if(envProvider != null) {
			viewController = (PackInstallerViewController)envProvider.getAdapter(PackInstallerViewController.class);
		}
		if(viewController == null) {
			viewController = new PackInstallerViewController();
		}

		CpPlugIn.addRteListener(viewController);
		RteConsole.openPackManagerConsole();

		IWorkbench wb = PlatformUI.getWorkbench();
		if(wb != null) {
			wb.addWorkbenchListener(this);
			if(wb.getActiveWorkbenchWindow() != null) {
				wb.getActiveWorkbenchWindow().addPerspectiveListener(this);
			}
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		if (timer != null) {
			timer.cancel();
		}
		CpPlugIn.removeRteListener(viewController);
		viewController.clear();

		IWorkbench wb = PlatformUI.getWorkbench();
		if(wb != null ) {
			wb.removeWorkbenchListener(this);
			if(wb.getActiveWorkbenchWindow() != null) {
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

	/**
	 * start the automatic tasks such as checking online-status and check-for-updates
	 * @param page
	 * @param perspective
	 */
	void startAutomaticTasks(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		if (PackManagerPerspective.ID.equals(page.getPerspective().getId())) {
			page.setEditorAreaVisible(false);
			String now = Utils.getCurrentDate();
			if (checkForUpdates && CpPreferenceInitializer.getAutoUpdateFlag() && !now.equals(CpPreferenceInitializer.getLastUpdateTime())) {
				checkForUpdates = false; // check only once
				startCheckForUpdates(); // this will update the time
			}
		} else {
			if(timer != null) {
				timer.cancel();
				timer = null;
			}
			timerStarted = false;
		}
	}

	void startCheckForUpdates() {
		if (CpPlugIn.getPackManager() != null && CpPlugIn.getPackManager().getPackInstaller() != null) {
			if (PlatformUI.getWorkbench() != null &&
					PlatformUI.getWorkbench().getProgressService() != null) {
				Display.getDefault().asyncExec(() -> {
					try {
						PlatformUI.getWorkbench().getProgressService().run(true, true, new IRunnableWithProgress() {
							@Override
							public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
								CpPlugIn.getPackManager().getPackInstaller().updatePacks(monitor);
							}
						});
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				});
			} else {
				new Thread(() -> {
					CpPlugIn.getPackManager().getPackInstaller().updatePacks(new NullProgressMonitor());
				}).start();
			}
		}
	}

	@Override
	public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		startAutomaticTasks(page, perspective);
	}

	@Override
	public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
		if ("viewShow".equals(changeId)) { //$NON-NLS-1$
			startAutomaticTasks(page, perspective);
		} else {	// viewHide
			if(timer != null) {
				timer.cancel();
				timer = null;
			}
		}
	}

	@Override
	public boolean preShutdown(IWorkbench workbench, boolean forced) {
		if(CpPlugIn.getPackManager() == null || forced) {
			return true;
		}
		ICpPackInstaller packInstaller = CpPlugIn.getPackManager().getPackInstaller();
		if (packInstaller != null && packInstaller.isBusy()) {
			boolean exit = MessageDialog.openQuestion(
					Display.getDefault().getActiveShell(),
					Messages.CpInstallerPlugInUI_ExitEclipse,
					Messages.CpInstallerPlugInUI_ExitEclipseMessage);
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
		//does nothing
	}

}
