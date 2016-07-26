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
package com.arm.cmsis.pack.installer;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.arm.cmsis.pack.installer.console.ConsoleStream;

public class CpInstallerPlugIn implements BundleActivator {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.arm.cmsis.pack.installer"; //$NON-NLS-1$

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		CpInstallerPlugIn.context = bundleContext;
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		CpInstallerPlugIn.context = null;
		ConsoleStream.dispose();
	}

}
