/*******************************************************************************
 * Copyright (c) 2023 Analog Devices, Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Analog Devices - command line pack installer application
 *******************************************************************************/

package com.arm.cmsis.pack.installer;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackInstaller;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPackCollection;
import com.arm.cmsis.pack.events.IRteEventListener;
import com.arm.cmsis.pack.events.RteEvent;

/**
 * Command line pack installer
 *
 */
public class ClPackInstaller implements IApplication, IRteEventListener {
	
	/** true if using the command line installer */
	private static boolean isHeadless = false;
	
	/** return code for errors */
	private static final int EXIT_ERROR = 0x1;
	
	/** true if an error has occurred while using the installer */
	private boolean errorOccurred = false;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object start(IApplicationContext context) throws Exception {
		// suppress error dialogs
		System.setProperty(IApplicationContext.EXIT_DATA_PROPERTY, "");
		isHeadless = true;
		CpPlugIn.getDefault().addListener(this);
		
		ICpPackInstaller packInstaller = CpPlugIn.getPackManager().getPackInstaller();
		if(packInstaller == null)
			return -1;

		boolean showUsage = false;
		String[] args = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		if (args != null) {
			for (int i=0; i<args.length; i++) {
				String arg = args[i];
				if (arg.equals("-importPack")) {
					if (i+1 < args.length) {
						String packFile = args[i+1];
						CpPlugIn.getDefault().emitRteEvent(RteEvent.PRINT_INFO, NLS.bind(Messages.CpPackInstaller_ImportingPack, packFile));
						packInstaller.importPack(packFile);
						i++;
					}
					else {
						showUsage = true;
					}
				}
				else if (arg.equals("-installPack")) {
					if (i+1 < args.length) {
						String packId = args[i+1];
						// retrieve the packs from the web so we can install one of them
						CpPlugIn.getDefault().emitRteEvent(RteEvent.PRINT_INFO, Messages.ClPackInstaller_RetrievingPacks);
						packInstaller.updatePacks(new NullProgressMonitor());
						CpPlugIn.getDefault().emitRteEvent(RteEvent.PRINT_INFO, NLS.bind(Messages.CpPackInstaller_InstallingPack, packId));
						packInstaller.installPack(packId);
						i++;
					}
					else {
						showUsage = true;
					}
				}
				else if (arg.equals("-deletePack")) {
					if (i+1 < args.length) {
						String packId = args[i+1];
						CpPlugIn.getDefault().emitRteEvent(RteEvent.PRINT_INFO, NLS.bind(Messages.CpPackInstaller_DeletingPack, packId));
						ICpPackCollection packs = CpPlugIn.getPackManager().getInstalledPacks();
						ICpPack pack = null;
						if (packs != null) {
							pack = packs.getPack(packId);
							if (pack != null) {
								packInstaller.removePack(pack, true);
							}
						}
						if (packs == null || pack == null) {
							CpPlugIn.getDefault().emitRteEvent(RteEvent.PRINT_ERROR, NLS.bind(Messages.ClPackInstaller_FailToDeletePack, packId));
						}
						i++;
					}
					else {
						showUsage = true;
					}
				}
				else if (arg.equals("-help")) {
					showUsage = true;
					break;
				}
				else {
					CpPlugIn.getDefault().emitRteEvent(RteEvent.PRINT_ERROR, NLS.bind(Messages.ClPackInstaller_InvalidArg, arg));
					showUsage = true;
					break;
				}
			}
			
			if (showUsage) {
				printUsage();
			}
		}
		
		// wait for all jobs to finish
		while (packInstaller.isBusy()) {
			// flush the display to ensure all dialogs are shown
			if (!Display.getDefault().readAndDispatch()) {
				Thread.sleep(100);
			}
		}
		
		if (errorOccurred) {
			return EXIT_ERROR;
		}
		
		return EXIT_OK;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stop() {
		CpPlugIn.getDefault().removeListener(this);
	}
	
	/**
	 * Print the usage to the console
	 */
	public static void printUsage() {
		CpPlugIn.getDefault().emitRteEvent(RteEvent.PRINT_INFO, Messages.ClPackInstaller_Usage); 
	}

	/**
	 * Returns true if using the command line installer (no UI)
	 * 
	 * @return true if using the command line installer, otherwise false
	 */
	public static boolean isHeadless() {
		return isHeadless;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handle(RteEvent event) {
		switch(event.getTopic()) {
		case RteEvent.PRINT_OUTPUT:
		case RteEvent.PRINT_INFO:
		case RteEvent.PRINT_WARNING:
			System.out.println((String)event.getData());
			break;
		case RteEvent.PRINT_ERROR:
			errorOccurred = true;
			System.out.println((String)event.getData());
			break;
		}
	}
}
