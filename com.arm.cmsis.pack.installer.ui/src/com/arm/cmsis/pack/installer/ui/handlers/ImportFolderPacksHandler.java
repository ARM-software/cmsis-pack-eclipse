/*******************************************************************************
 * Copyright (c) 2021 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Eclipse Project - generation from template
 * ARM Ltd and ARM Germany GmbH - application-specific implementation
 *******************************************************************************/

package com.arm.cmsis.pack.installer.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackInstaller;
import com.arm.cmsis.pack.installer.ui.Messages;

/**
 * Handler of importing packs from a folder
 */
public class ImportFolderPacksHandler extends AbstractHandler {

    static String lastImportFolder;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ICpPackInstaller packInstaller = CpPlugIn.getPackManager().getPackInstaller();
        if (packInstaller == null) {
            return null;
        }
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
        DirectoryDialog dialog = new DirectoryDialog(window.getShell());
        dialog.setText(Messages.ImportFolderPacksHandler_Title);
        dialog.setMessage(Messages.ImportFolderPacksHandler_Message);
        if (lastImportFolder != null) {
            dialog.setFilterPath(lastImportFolder);
        }
        String rootPath = dialog.open();
        if (rootPath != null && !rootPath.isEmpty()) {
            lastImportFolder = rootPath;
            packInstaller.importFolderPacks(rootPath);
        }

        return null;
    }

}
