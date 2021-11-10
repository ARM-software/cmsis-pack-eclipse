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

package com.arm.cmsis.pack.refclient;

import java.io.File;

import com.arm.cmsis.pack.CpEnvironmentProvider;
import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpExampleImporter;
import com.arm.cmsis.pack.ICpPackRootProvider;
import com.arm.cmsis.pack.build.gnuarmeclipse.GnuarmeclipseToolChainAdapter;
import com.arm.cmsis.pack.build.settings.RteToolChainAdapterFactory;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpExample;

/**
 * A sample environment provider
 */
public class RefClientEnvironmentProvider extends CpEnvironmentProvider implements ICpPackRootProvider {

    static public final String REF_CLIENT = "RefClient"; //$NON-NLS-1$
    static private int gnuarmeclipseInstalled = -1; // not initialized

    public RefClientEnvironmentProvider() {
    }

    @Override
    public String getName() {
        return REF_CLIENT;
    }

    static public boolean isGnuarmeclipseToolchainInstalled() {
        if (gnuarmeclipseInstalled < 0) {
            String prefix = GnuarmeclipseToolChainAdapter.GNUARMECLIPSE_TOOLCHAIN_PREFIX;
            gnuarmeclipseInstalled = RteToolChainAdapterFactory.isToolchainInstalled(prefix) ? 1 : 0;
        }
        return gnuarmeclipseInstalled > 0;
    }

    @Override
    public void init() {
        // initialize default example importer
        fExampleImporter = new RefClientEclipseExampleImporter();
        // install custom pack installer
        RefClientPackInstaller packInstaller = new RefClientPackInstaller();
        CpPlugIn.getDefault().setPackInstaller(packInstaller);
    }

    @Override
    public boolean isExampleSupported(ICpExample example) {
        // return true; // uncomment to see all examples, even those that are not
        // supported
        return super.isExampleSupported(example);
    }

    @Override
    public ICpPackRootProvider getCmsisRootProvider() {
        return this;
    }

    @Override
    public String getPackRoot() {
        String dir = System.getProperty("user.home"); //$NON-NLS-1$
        if (dir == null || dir.isEmpty())
            return CmsisConstants.EMPTY_STRING;

        return dir + File.separator + "com.arm.cmsis.pack.refclient" + File.separator + "PACK"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public boolean isUserEditable() {
        return true;
    }

    @Override
    public void setDefaultImporter(ICpExampleImporter exampleImporter) {
        if (getDefaultImporter() == null)
            super.setDefaultImporter(exampleImporter);
    }

}
