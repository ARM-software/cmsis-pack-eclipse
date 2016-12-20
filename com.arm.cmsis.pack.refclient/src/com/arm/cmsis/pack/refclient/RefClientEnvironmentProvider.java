/*******************************************************************************
* Copyright (c) 2016 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.refclient;

import com.arm.cmsis.pack.CpEnvironmentProvider;
import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.build.gnuarmeclipse.GnuarmeclipseToolChainAdapter;
import com.arm.cmsis.pack.build.settings.RteToolChainAdapterFactory;
import com.arm.cmsis.pack.data.ICpExample;

/**
 *  A sample environment provider 
 */
public class RefClientEnvironmentProvider extends CpEnvironmentProvider {


	static public final String REF_CLIENT = "RefClient"; //$NON-NLS-1$
	static private int gnuarmeclipseInstalled = -1; // not initialized
	
	public RefClientEnvironmentProvider() {
	}

	@Override
	public String getName() {
		return REF_CLIENT;
	}
	
	static public boolean isGnuarmeclipseToolchainInstalled() {
		if(gnuarmeclipseInstalled < 0) {
			String prefix = GnuarmeclipseToolChainAdapter.GNUARMECLIPSE_TOOLCHAIN_PREFIX;
			gnuarmeclipseInstalled = RteToolChainAdapterFactory.isToolchainInstalled(prefix) ? 1 : 0;
		}
		return gnuarmeclipseInstalled > 0;
	}

	@Override
	public void init() {
		// install custom pack installer
		RefClientPackInstaller packInstaller = new RefClientPackInstaller();
		CpPlugIn.getDefault().setPackInstaller(packInstaller);
	}

	@Override
	public boolean isExampleSupported(ICpExample example) {
		// return true to see all examples, even those that are not supported
		return super.isExampleSupported(example);
	}

	
}
