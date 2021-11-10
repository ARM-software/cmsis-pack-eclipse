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
package com.arm.cmsis.pack.project.importer;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

import com.arm.cmsis.pack.ICpExampleImporter;
import com.arm.cmsis.pack.data.ICpExample;
import com.arm.cmsis.pack.project.Messages;

/**
 * Base class for example importers
 *
 */
public abstract class CpExampleImporter extends PlatformObject implements ICpExampleImporter {

    protected void popupCopyError(String errorMessage) {
        Display.getDefault().asyncExec(() -> {
            MessageDialog.openError(null, Messages.CpEclipseExampleImporter_ErrorWhileCopyingExample, errorMessage);
        });
    }

    protected IProject confirmCopyExample(ICpExample example, File destFile, IProject project) {
        // Open dialog
        CopyExampleDialog copyDialog = new CopyExampleDialog(null, example.getName(), example.getPackId(),
                destFile.toString(), project.getName(), project.exists() || destFile.exists());

        // Get the project name from the dialog and create project
        if (copyDialog.open() == Window.OK) {
            return ResourcesPlugin.getWorkspace().getRoot().getProject(copyDialog.getProjectName());
        } else
            return null;
    }

}
