/*******************************************************************************
 * Copyright (c) 2014 Liviu Ionescu.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Liviu Ionescu - initial implementation.
 *     ARM Ltd and ARM Germany GmbH - application-specific implementation
 *******************************************************************************/

package com.arm.cmsis.pack.installer.ui.handlers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.handlers.HandlerUtil;

import com.arm.cmsis.pack.CpPlugIn;

/**
 * Handler of checking for updates
 */
public class UpdatePacksHandler extends AbstractHandler {

	public UpdatePacksHandler() {
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IRunnableContext context = HandlerUtil.getActiveWorkbenchWindow(event)
				.getWorkbench().getProgressService();
		try {
			context.run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					CpPlugIn.getPackManager().getPackInstaller().updatePacks(monitor);
				}
			});

		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

}
