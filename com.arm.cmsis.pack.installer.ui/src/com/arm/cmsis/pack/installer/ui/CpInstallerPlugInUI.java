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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.services.IEvaluationService;
import org.osgi.framework.BundleContext;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackInstaller;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.installer.ui.perspectives.PackManagerPerspective;

/**
 * The activator class controls the plug-in life cycle
 */
public class CpInstallerPlugInUI extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.arm.cmsis.pack.installer.ui"; //$NON-NLS-1$

	// The shared instance
	private static CpInstallerPlugInUI plugin;

	private IWorkbenchListener workbenchListener = null;

	private IPerspectiveListener perspectiveListener = null;

	static List<ViewPart> viewParts;

	static boolean isOnline;

	volatile static Timer timer = null;
	boolean timerStarted = false;

	/**
	 * The constructor
	 */
	public CpInstallerPlugInUI() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		viewParts = new LinkedList<>();
		registerPerspectiveListener();
		registerWorkbenchListener();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		if (timer != null) {
			timer.cancel();
		}
		IWorkbench wb = PlatformUI.getWorkbench();
		if(wb != null) {
			if(workbenchListener != null) {
				wb.removeWorkbenchListener(workbenchListener);
			}
			if(wb.getActiveWorkbenchWindow() != null && perspectiveListener != null) {
				wb.getActiveWorkbenchWindow().removePerspectiveListener(perspectiveListener);
			}
		}
		workbenchListener = null;
		perspectiveListener = null;
		viewParts.clear();
		viewParts = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static CpInstallerPlugInUI getDefault() {
		return plugin;
	}

	private void registerWorkbenchListener() {
		IWorkbench wb = PlatformUI.getWorkbench();
		if(wb == null) {
			return;
		}
		final ICpPackInstaller packInstaller = CpPlugIn.getPackManager().getPackInstaller();
		workbenchListener = new IWorkbenchListener() {
			@Override
			public boolean preShutdown(IWorkbench workbench, boolean forced) {
				if (packInstaller.isBusy()) {
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
				// does nothing
			}
		};
		wb.addWorkbenchListener(workbenchListener);
	}

	private void registerPerspectiveListener() {
		IWorkbench wb = PlatformUI.getWorkbench();
		if(wb == null || wb.getActiveWorkbenchWindow() == null) {
			return;
		}

		perspectiveListener = new IPerspectiveListener() {
			@Override
			public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
				if ("viewShow".equals(changeId)) { //$NON-NLS-1$
					setTimerTask(page, perspective);
				} else {	// viewHide
					if(timer != null) {
						timer.cancel();
						timer = null;
					}
				}
			}
			@Override
			public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
				setTimerTask(page, perspective);
			}
		};
		wb.getActiveWorkbenchWindow().addPerspectiveListener(perspectiveListener);
	}

	void setTimerTask(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		if (PackManagerPerspective.ID.equals(page.getPerspective().getId())) {
			page.setEditorAreaVisible(false);
			if (!timerStarted) {
				startUpdatingStatusLine();
				timerStarted = true;
			}
		} else {
			if(timer != null) {
				timer.cancel();
				timer = null;
			}
			timerStarted = false;
		}
	}

	public static boolean isOnline() {
		return isOnline;
	}

	void startUpdatingStatusLine() {
		if (timer == null) {
			timer = new Timer();
		} else if (timerStarted) {
			return;
		}
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				Socket socket = new Socket();
				InetSocketAddress addr = new InetSocketAddress(CmsisConstants.REPO_KEILWEB, 80);
				try {
					socket.connect(addr, 2000);
					if (!socket.isClosed() && socket.isConnected()) {
						updateStatusLineAndButton(true);
					}
				} catch (Exception e) {
					updateStatusLineAndButton(false);
				} finally {
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}, 0, 5000);
	}

	synchronized public static void registerViewPart(ViewPart viewPart) {
		viewParts.add(viewPart);
	}

	void updateStatusLineAndButton(boolean online) {
		final boolean connectionStateChanged = isOnline != online;
		if (!connectionStateChanged) {
			return;
		}
		isOnline = online;
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				IEvaluationService evaludationService = (IEvaluationService) PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getService(IEvaluationService.class);
				evaludationService.requestEvaluation("com.arm.cmsis.pack.installer.ui.onlineTest"); //$NON-NLS-1$

				Iterator<ViewPart> iter = viewParts.iterator();
				while (iter.hasNext()) {
					ViewPart viewPart = iter.next();
					IStatusLineManager mgr = viewPart.getViewSite().getActionBars().getStatusLineManager();
					if (online) {
						mgr.setErrorMessage(null);
						mgr.setMessage(Messages.CpInstallerPlugInUI_ONLINE);
					} else {
						mgr.setErrorMessage(Messages.CpInstallerPlugInUI_OFFLINE);
					}
					Viewer viewer = (Viewer) viewPart.getAdapter(Viewer.class);
					if (connectionStateChanged && viewer != null && !viewer.getControl().isDisposed()) {
						viewer.refresh();
					}
				}
			}
		});
	}

}
