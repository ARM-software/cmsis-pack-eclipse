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

package com.arm.cmsis.pack.installer.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackInstaller;
import com.arm.cmsis.pack.installer.ui.Messages;
import com.arm.cmsis.pack.utils.Utils;

/**
 * Handler of importing pack from a .pack or .zip file
 */
public class ImportPacksHandler extends AbstractHandler {
	
	private static final String[] FILTER_NAMES = {
		      "Pack Files (*.pack)", //$NON-NLS-1$
		      "Zip Files (*.zip)"}; //$NON-NLS-1$
	
	private static final String[] FILTER_EXTS = {"*.pack", "*.zip"}; //$NON-NLS-1$ //$NON-NLS-2$


	public ImportPacksHandler() {
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ICpPackInstaller packInstaller = CpPlugIn.getPackManager().getPackInstaller();
		if(packInstaller == null)
			return null;
		IWorkbenchWindow  window = HandlerUtil.getActiveWorkbenchWindow(event);
		FileDialog dialog = new FileDialog(window.getShell(), SWT.MULTI);
		dialog.setText(Messages.ImportPacksHandler_DialogText); 
		dialog.setFilterNames(FILTER_NAMES);
		dialog.setFilterExtensions(FILTER_EXTS);
		dialog.setFilterPath("C:/"); //$NON-NLS-1$
		if (dialog.open() != null) {
			String[] files = dialog.getFileNames();
			for (String file : files) {
				String fullFileName = Utils.addTrailingSlash(dialog.getFilterPath()) + file;
				packInstaller.importPack(fullFileName);
			}
		}
		
		return null;
	}

}
