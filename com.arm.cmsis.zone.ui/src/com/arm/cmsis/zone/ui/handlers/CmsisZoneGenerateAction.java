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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.arm.cmsis.pack.parser.ICpXmlParser;
import com.arm.cmsis.zone.parser.CpZoneParser;
import com.arm.cmsis.zone.ui.Messages;
import com.arm.cmsis.zone.ui.editors.CmsisZoneEditor;

/**
 * Action to call generator
 */
public class CmsisZoneGenerateAction extends Action {

    protected ICpXmlParser fParser = null;

    /**
     * Default constructor
     */
    public CmsisZoneGenerateAction() {
        super(Messages.CmsisZoneGenerateAction_Generate, AS_PUSH_BUTTON);
        setToolTipText(Messages.CmsisZoneGenerateAction_GenerateCodeFromFreeMarkerTemplates);
        setImageDescriptor(
                PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(IDE.SharedImages.IMG_OBJS_TASK_TSK));
    }

    @Override
    public void run() {
        IProgressMonitor monitor = null;

        IEditorPart editor = getActiveEditor();
        if (!(editor instanceof CmsisZoneEditor))
            return;
        IEditorInput input = editor.getEditorInput();
        if (!(input instanceof IFileEditorInput))
            return;
        CmsisZoneEditor ze = (CmsisZoneEditor) editor;
        ze.doSave(monitor);

        IFileEditorInput fileEditorInput = (IFileEditorInput) input;
        IFile pZoneFile = fileEditorInput.getFile();
        ze.getModelController().generate(pZoneFile, monitor);
    }

    protected ICpXmlParser getParser() {
        if (fParser == null) {
            fParser = new CpZoneParser();
        }
        return fParser;
    }

    protected IEditorPart getActiveEditor() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
    }

    protected IFile saveZoneFile(IProgressMonitor monitor) {
        IEditorPart editor = getActiveEditor();
        if (!(editor instanceof CmsisZoneEditor))
            return null;
        IEditorInput input = editor.getEditorInput();
        if (!(input instanceof IFileEditorInput))
            return null;
        CmsisZoneEditor ze = (CmsisZoneEditor) editor;
        ze.doSave(monitor);

        IFileEditorInput fileEditorInput = (IFileEditorInput) input;
        IFile pZoneFile = fileEditorInput.getFile();
        return pZoneFile;
    }
}
