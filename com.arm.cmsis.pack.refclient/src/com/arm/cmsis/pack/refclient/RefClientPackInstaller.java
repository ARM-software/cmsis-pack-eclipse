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

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;

import com.arm.cmsis.pack.data.ICpExample;
import com.arm.cmsis.pack.installer.CpPackInstaller;

/**
 *  Sample custom pack installer
 */
public class RefClientPackInstaller extends CpPackInstaller {


	@Override
	protected boolean confirmCopyExample(ICpExample example, File destFile, IProject project) {
		
		if(!RefClientEnvironmentProvider.isGnuarmeclipseToolchainInstalled()) {
			String message = "Required GNU ARM C/C++ Cross Toolchain is not installed.\nCopy the example anyawy?";
			boolean res = MessageDialog.openQuestion(null, "Required Toolchain not Installed", message);
			if(!res)
				return false;
		}
		
		return super.confirmCopyExample(example, destFile, project);
	}
	
}
