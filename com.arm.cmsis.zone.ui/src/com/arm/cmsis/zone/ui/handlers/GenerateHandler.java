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
package com.arm.cmsis.zone.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.arm.cmsis.zone.ui.Messages;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 *
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class GenerateHandler extends AbstractHandler {
    /**
     * The constructor.
     */
    public GenerateHandler() {
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        MessageDialog.openInformation(window.getShell(), Messages.GenerateHandler_CmsisZoneUI,
                Messages.GenerateHandler_HelloEclipseWorld);
        return null;
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled();
    }

    @Override
    public boolean isHandled() {
        return super.isHandled();
    }

    @Override
    protected void setBaseEnabled(boolean state) {
        super.setBaseEnabled(state);
    }

    @Override
    public void setEnabled(Object evaluationContext) {
        super.setEnabled(evaluationContext);
    }

}
