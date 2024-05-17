/*******************************************************************************
* Copyright (c) 2023 ARM Ltd. and others
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

package com.arm.cmsis.pack.project.wizards;

import java.io.File;
import java.util.Collection;

import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpRootItem;
import com.arm.cmsis.pack.generic.ITreeObject;
import com.arm.cmsis.pack.project.Messages;
import com.arm.cmsis.pack.project.csolution.CsolutionProjectImporter;
import com.arm.cmsis.pack.ui.tree.AdvisedCellLabelProvider;
import com.arm.cmsis.pack.ui.tree.AdvisedEditingSupport;
import com.arm.cmsis.pack.ui.tree.ColumnAdvisor;
import com.arm.cmsis.pack.ui.tree.TreeObjectContentProvider;

/**
 *
 */
public class CsolutionImportWizardPage extends WizardPage {

    private Composite container;
    private CsolutionProjectImporter projectImporter;
    final static private String[] allowedFileExtensions = new String[] {
            CmsisConstants.ASTERISK + CmsisConstants.EXT_CSOLUTION_YML };
    public TreeViewer treeViewer;
    private ICpRootItem target;

    protected CsolutionImportWizardPage(CsolutionProjectImporter projectImporter, String pageName, String pageTitle) {
        super(pageName);
        this.projectImporter = projectImporter;
        setTitle(pageTitle);
        setDescription(Messages.CsolutionProjectImporter_Description);
        this.setPageComplete(false);
    }

    @Override
    public void createControl(Composite parent) {
        container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout());
        container.setLayoutData(new GridData(GridData.FILL_BOTH));
        setControl(container);

        Label selectedFile = new Label(container, SWT.None);
        selectedFile.setText(Messages.CsolutionProjectImporter_SelectFile);

        Group fileSelectionArea = new Group(container, SWT.NONE);
        fileSelectionArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));

        // Create a file field editor widget to select a correct YAML files
        FileFieldEditor fileEditor = new FileFieldEditor("fileSelect", //$NON-NLS-1$
                CmsisConstants.EMPTY_STRING, fileSelectionArea);
        WizardPage currentPage = this;
        fileEditor.getTextControl(fileSelectionArea).addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                String path = fileEditor.getStringValue();
                String errorStatus = isValidCsolutionFile(path);
                if (errorStatus == null) {
                    String res = projectImporter.setSourceFileFromCsolutionYmlFile(path);
                    setErrorMessage(res);

                    // triggers reading of file using its ContentProvider
                    treeViewer.refresh();
                    treeViewer.expandAll();
                    emitModifyEvent();

                    boolean valid = validatePage();
                    currentPage.setPageComplete(valid);
                } else {
                    setErrorMessage(errorStatus);
                }
            }
        });
        fileEditor.setFileExtensions(allowedFileExtensions);

        // Target selector
        // Create tree Viewer widget
        treeViewer = new TreeViewer(fileSelectionArea, SWT.FULL_SELECTION | SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
        Tree tree = treeViewer.getTree();
        tree.setLinesVisible(true);
        tree.setHeaderVisible(true);
        GridData gd_tree = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
        gd_tree.heightHint = 100;
        tree.setLayoutData(gd_tree);

        CsolutionTargetColumnAdvisor columnAdvisor = new CsolutionTargetColumnAdvisor(treeViewer);
        // Create column1: target name
        TreeViewerColumn treeViewerColumn = new TreeViewerColumn(treeViewer, SWT.LEFT);
        TreeColumn trclmnTargetName = treeViewerColumn.getColumn();
        trclmnTargetName.setWidth(200);
        trclmnTargetName.setText(Messages.CsolutionProjectImport_CbuildName);
        treeViewerColumn.setLabelProvider(new AdvisedCellLabelProvider(columnAdvisor, 0));
        treeViewerColumn.setEditingSupport(new AdvisedEditingSupport(treeViewer, columnAdvisor, 0));

        // Create column2: target' details
        TreeViewerColumn treeViewerColumn_1 = new TreeViewerColumn(treeViewer, SWT.LEFT);
        TreeColumn trclmnNewColumn = treeViewerColumn_1.getColumn();
        trclmnNewColumn.setWidth(256);
        trclmnNewColumn.setText(Messages.CsolutionProjectImport_CbuildDetails);
        treeViewerColumn_1.setLabelProvider(new AdvisedCellLabelProvider(columnAdvisor, 1));

        // Set the content provider used by this TreeViewer
        treeViewer.setContentProvider(new CbuildIdxContentProvider());
        treeViewer.setInput(projectImporter);
        treeViewer.expandAll();
        if (fileEditor != null) {
            fileEditor.setFocus();
        }
    }

    public class CsolutionTargetColumnAdvisor extends ColumnAdvisor {

        public CsolutionTargetColumnAdvisor(ColumnViewer columnViewer) {
            super(columnViewer);
        }

        @Override
        public String getString(Object obj, int columnIndex) {
            CsolutionProjectImporter project = getProjectImporter(obj);
            if (project != null) {
                switch (columnIndex) {
                case 0:
                    return project.getSourceProjectFile();
                default:
                    return null;
                }
            }
            target = getRootItem(obj);

            if (target == null)
                return null;
            switch (columnIndex) {
            case 0:
                return target.getName();
            case 1:
                return target.getInfo();
            default:
                break;
            }
            return null;
        }

        @Override
        public CellControlType getCellControlType(Object obj, int columnIndex) {
            if (columnIndex == 0) {
                if (getRootItem(obj) != null)
                    return CellControlType.INPLACE_CHECK;
            }
            return CellControlType.TEXT;
        }

        @Override
        public boolean getCheck(Object obj, int columnIndex) {
            if (columnIndex != 0)
                return false;
            target = getRootItem(obj);
            if (target == null)
                return false;

            return target.isValid() && target.getAttributeAsBoolean(CmsisConstants.IS_SELECTED, true);
        }

        @Override
        public boolean isEnabled(Object obj, int columnIndex) {

            return isValid(obj, columnIndex);
        }

        @Override
        public boolean isValid(Object obj, int columnIndex) {
            if (columnIndex != 0)
                return true;
            target = getRootItem(obj);

            if (target != null) {
                return target.isValid();
            }
            return true;
        }

        @Override
        public void setCheck(Object element, int columnIndex, boolean newVal) {
            target = getRootItem(element);
            if (target == null || !target.isValid())
                return;

            target.setAttribute(CmsisConstants.IS_SELECTED, newVal);
            getTreeViewer().refresh(element);

            emitModifyEvent();
        }

        @Override
        public boolean canEdit(Object obj, int columnIndex) {
            if (columnIndex != 0)
                return false;
            target = getRootItem(obj);
            if (target == null)
                return false;
            return target.isValid();
        }
    }

    /**
     * Class to manage cbuild index files
     */
    class CbuildIdxContentProvider extends TreeObjectContentProvider {

        @Override
        public Object[] getChildren(Object parentElement) {
            CsolutionProjectImporter importer = getProjectImporter(parentElement);
            if (importer != null && !importer.getSourceProjectFile().isEmpty()) {
                importer.readAndSaveFiles();
                Collection<ICpItem> cbuilds = importer.getCbuildFileRootToPacks().keySet();
                return cbuilds.toArray(new ICpItem[cbuilds.size()]);
            }
            return ITreeObject.EMPTY_OBJECT_ARRAY;
        }

        @Override
        public boolean hasChildren(Object element) {
            return false;
        }

    }

    CsolutionProjectImporter getProjectImporter(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj == projectImporter) {
            return projectImporter;
        }
        return ITreeObject.castTo(obj, CsolutionProjectImporter.class);
    }

    ICpRootItem getRootItem(Object obj) {
        return ITreeObject.castTo(obj, ICpRootItem.class);

    }

    /**
     * Emits modify event every time that user selects data from either target or
     * toolchain combo widget
     */
    public void emitModifyEvent() {
        Event e = new Event();
        e.data = CmsisConstants.EMPTY_STRING;
        e.display = container.getDisplay();
        e.widget = container;
        container.notifyListeners(SWT.Modify, e);
    }

    protected String isValidCsolutionFile(String fileName) {
        String messageStatus = null;
        boolean isFileTypeValid = fileName.endsWith(CmsisConstants.EXT_CSOLUTION_YML);
        if (isFileTypeValid) {
            File f = new File(fileName);
            if (!f.exists()) {
                messageStatus = Messages.CsolutionProjectImport_ErrorCsolutionNotExist;
            }
        } else {
            messageStatus = Messages.CsolutionProjectImport_ErrorNotCsolutionFile;
        }
        return messageStatus;
    }

    protected boolean validatePage() {
        if (projectImporter == null) {
            return false;
        }
        String projectFile = projectImporter.getSourceProjectFile();
        if (projectFile == null || projectFile.isEmpty()) {
            return false;
        }

        String msg = projectImporter.validate();
        if (msg != null) {
            setErrorMessage(msg);
            return false;
        }

        return true;
    }
}
