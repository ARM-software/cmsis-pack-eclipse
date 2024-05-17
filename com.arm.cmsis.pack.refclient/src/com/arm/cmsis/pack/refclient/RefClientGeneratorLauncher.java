/*******************************************************************************
* Copyright (c) 2021 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.refclient;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * Sample Eclipse generator
 */
public class RefClientGeneratorLauncher {

    static public void launch(String gpdsc) {

        Display.getDefault().asyncExec(() -> doLaunchGenerator(gpdsc));

    }

    static public void launchGenerated(String gpdsc, String workDir) {

        Display.getDefault().asyncExec(() -> doLaunchGenerator(gpdsc, workDir));

    }

    private static void doLaunchGenerator(String gpdsc) {
        IWorkbench wb = PlatformUI.getWorkbench();
        if (wb == null)
            return;
        IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
        if (window == null)
            return;
        IWorkbenchPage page = window.getActivePage();
        if (page == null)
            return;
        File fileToOpen = new File(gpdsc);
        if (fileToOpen.exists() && fileToOpen.isFile()) {
            IFileStore fileStore = EFS.getLocalFileSystem().getStore(fileToOpen.toURI());
            try {
                IDE.openEditorOnFileStore(page, fileStore);
            } catch (PartInitException e) {
                // Put your exception handler here if you wish to
            }
        } else {
            // Do something if the file does not exist
        }
    }

    private static void doLaunchGenerator(String gpdsc, String workDir) {
        doLaunchGenerator(gpdsc);
    }

}
