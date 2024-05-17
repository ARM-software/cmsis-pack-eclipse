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
package com.arm.cmsis.pack.project.ui.handlers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.compare.BufferedContent;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.IEditableContent;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.arm.cmsis.pack.data.ICpFile;
import com.arm.cmsis.pack.enums.EFileCategory;
import com.arm.cmsis.pack.enums.EFileRole;
import com.arm.cmsis.pack.info.ICpFileInfo;
import com.arm.cmsis.pack.project.CpProjectPlugIn;
import com.arm.cmsis.pack.project.IRteProject;
import com.arm.cmsis.pack.project.Messages;
import com.arm.cmsis.pack.project.RteProjectStorage;
import com.arm.cmsis.pack.project.utils.ProjectUtils;
import com.arm.cmsis.pack.utils.Utils;

public class MergeConfigFileHandler extends AbstractHandler implements IElementUpdater {

    class CompareConfigFileInput extends CompareEditorInput {

        private DiffNode fRoot;
        private CompareItem fLeft;
        private CompareItem fRight;

        private class CompareItem extends BufferedContent implements IEditableContent, ITypedElement {

            protected IStorage fContent;

            public CompareItem(IStorage storage) {
                fContent = storage;
            }

            @Override
            public boolean isEditable() {
                return true;
            }

            @Override
            public ITypedElement replace(ITypedElement dest, ITypedElement src) {
                return dest;
            }

            @Override
            public String getName() {
                return Utils.extractBaseFileName(fContent.getName());
            }

            @Override
            public Image getImage() {
                return null;
            }

            @Override
            public String getType() {
                return Utils.extractFileExtension(fContent.getName());
            }

            @Override
            protected InputStream createStream() throws CoreException {
                return fContent.getContents();
            }

        }

        /**
         * @param configuration
         */
        public CompareConfigFileInput(IFile currentFile, IFileState previousFile, String currentVersion,
                String previousVersion) {
            super(new CompareConfiguration());
            setTitle("Compare " + currentFile.getLocation().toOSString() + " previous and current revision"); //$NON-NLS-1$ //$NON-NLS-2$
            getCompareConfiguration().setLeftEditable(true);
            getCompareConfiguration().setRightEditable(false);
            getCompareConfiguration().setLeftLabel("Local: " + currentFile.getName() + " (" + currentVersion + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            getCompareConfiguration().setRightLabel("Previous version (" + previousVersion + ")"); //$NON-NLS-1$ //$NON-NLS-2$
            fLeft = new CompareItem(currentFile);
            fRight = new CompareItem(previousFile);
        }

        @Override
        protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            fRoot = new DiffNode(fLeft, fRight);
            return fRoot;
        }

        @Override
        public void saveChanges(IProgressMonitor pm) throws CoreException {
            super.saveChanges(pm);
            IStorage resource = fLeft.fContent;
            if (resource instanceof IFile) {
                byte[] bytes = fLeft.getContent();
                ByteArrayInputStream is = new ByteArrayInputStream(bytes);
                IFile file = (IFile) resource;
                try {
                    if (file.exists()) {
                        file.setContents(is, true, true, pm);
                    } else {
                        file.create(is, true, pm);
                    }
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }

    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (!(selection instanceof IStructuredSelection)) {
            return null;
        }
        Object obj = ((IStructuredSelection) selection).getFirstElement();
        IFile file = ProjectUtils.getRteFileResource(obj);
        String dstFile = file.getProjectRelativePath().toString();

        ICpFileInfo fi = ProjectUtils.getCpFileInfo(file);
        ICpFile f = fi.getFile();
        String srcFile = f.getAbsolutePath(f.getName());

        EFileRole role = fi.getRole();
        if (role == EFileRole.CONFIG || role == EFileRole.COPY) {
            int index = -1;
            EFileCategory cat = fi.getCategory();
            if (cat.isHeader() || cat.isSource()) {
                String baseSrc = Utils.extractBaseFileName(srcFile);
                String baseDst = Utils.extractBaseFileName(dstFile);
                int len = baseSrc.length() + 1;
                if (baseDst.length() > len) {
                    String instance = baseDst.substring(len);
                    try {
                        index = Integer.decode(instance);
                    } catch (NumberFormatException e) {
                        // do nothing, use -1
                    }
                }
            }
            try {
                int bCopied = ProjectUtils.copyFile(file.getProject(), srcFile, dstFile, index, null, true);
                if (bCopied == 1) {
                    String previousVersion = fi.getVersion();

                    // do the version update and save it in the .cproject file
                    fi.setVersion(f.getVersion());
                    IRteProject rteProject = CpProjectPlugIn.getRteProjectManager().getRteProject(file.getProject());
                    RteProjectStorage projectStorage = rteProject.getProjectStorage();
                    projectStorage.setConfigFileVersion(dstFile, f.getVersion());
                    projectStorage.save(CoreModel.getDefault().getProjectDescription(file.getProject()));
                    rteProject.save();

                    if (role == EFileRole.CONFIG) {
                        IFileState fileState = file.getHistory(null)[0];
                        CompareConfigFileInput compareInput = new CompareConfigFileInput(file, fileState,
                                fi.getVersion(), previousVersion);
                        CompareUI.openCompareEditorOnPage(compareInput,
                                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage());
                    }
                }
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
        ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        ISelection selection = selectionService.getSelection("org.eclipse.ui.navigator.ProjectExplorer"); //$NON-NLS-1$
        if (selection instanceof IStructuredSelection) {
            Object obj = ((IStructuredSelection) selection).getFirstElement();
            IFile file = ProjectUtils.getRteFileResource(obj);
            ICpFileInfo fi = ProjectUtils.getCpFileInfo(file);
            if (fi == null || fi.getFile() == null) {
                return;
            }
            int versionDiff = fi.getVersionDiff();
            String versionText = " (" + fi.getVersion() + " -> " + fi.getFile().getVersion() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            if (versionDiff < 0 || versionDiff > 2) {
                element.setText(Messages.MergeConfigFileHandler_Merge + file.getName() + versionText);
            }
        }
    }

}
