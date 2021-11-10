/**
 * 
 */
package com.arm.cmsis.pack.refclient;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;

import com.arm.cmsis.pack.data.ICpExample;
import com.arm.cmsis.pack.project.importer.CpEclipseExampleImporter;

/**
 * Custom example importer
 *
 */
public class RefClientEclipseExampleImporter extends CpEclipseExampleImporter {

    public RefClientEclipseExampleImporter() {
    }

    @Override
    protected IProject confirmCopyExample(ICpExample example, File destFile, IProject project) {

        if (!RefClientEnvironmentProvider.isGnuarmeclipseToolchainInstalled()) {
            String message = "Required GNU ARM C/C++ Cross Toolchain is not installed.\nCopy the example anyway?";
            boolean res = MessageDialog.openQuestion(null, "Required Toolchain not Installed", message);
            if (!res) {
                return null;
            }
        }

        return super.confirmCopyExample(example, destFile, project);
    }

}
