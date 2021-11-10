/*******************************************************************************
 * Copyright (c) 2021  ARM Ltd, ARM Germany GmbH and others.
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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackInstaller;

/**
 * Handler of checking for updates
 */
public class UpdatePacksHandler extends AbstractHandler {

    public UpdatePacksHandler() {
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        final ICpPackInstaller packInstaller = CpPlugIn.getPackManager().getPackInstaller();
        if (packInstaller == null) {
            return null;
        }
        packInstaller.updatePacksAsync();
        return null;
    }
}
