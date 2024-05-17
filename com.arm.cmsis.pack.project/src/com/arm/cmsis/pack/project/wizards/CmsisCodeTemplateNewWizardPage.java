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

package com.arm.cmsis.pack.project.wizards;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpCodeTemplate;
import com.arm.cmsis.pack.project.CpProjectPlugIn;
import com.arm.cmsis.pack.project.IHelpContextIds;
import com.arm.cmsis.pack.project.IRteProject;
import com.arm.cmsis.pack.project.Messages;
import com.arm.cmsis.pack.project.utils.ProjectUtils;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.utils.Utils;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name with the extension that
 * matches the code template's.
 */
public class CmsisCodeTemplateNewWizardPage extends WizardPage {
    private Text projectText;
    private Button projectBrowse;

    private Text containerText;
    private Button containerBrowse;

    private ISelection selection;
    private TreeViewer fViewer;
    private boolean fbInitialized = false;
    Text fileText;

    ICpCodeTemplate selectedCodeTemplate;

    ICpCodeTemplate getCpCodeTemplate(Object obj) {
        if (obj instanceof ICpCodeTemplate) {
            return (ICpCodeTemplate) obj;
        }
        return null;
    }

    class CodeTemplateContentProvider implements ITreeContentProvider {

        @Override
        public void dispose() {
            // does nothing
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // does nothing
        }

        @Override
        public Object[] getElements(Object inputElement) {
            return getChildren(inputElement);
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            ICpCodeTemplate codeTemplate = getCpCodeTemplate(parentElement);
            if (codeTemplate != null) {
                return codeTemplate.getChildArray();
            }
            return null;
        }

        @Override
        public Object getParent(Object element) {
            ICpCodeTemplate codeTemplate = getCpCodeTemplate(element);
            if (codeTemplate != null) {
                return codeTemplate.getParent();
            }
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            return getChildren(element) != null && getChildren(element).length > 0;
        }

    }

    class ComponentColumnLabelProvider extends ColumnLabelProvider {
        @Override
        public Image getImage(Object element) {
            return CpPlugInUI.getImage(CpPlugInUI.ICON_RTE);
        }

        @Override
        public String getText(Object element) {
            ICpCodeTemplate codeTemplate = getCpCodeTemplate(element);
            if (codeTemplate != null) {
                return codeTemplate.getComponentName();
            }
            return CmsisConstants.EMPTY_STRING;
        }
    }

    class NameColumnLabelProvider extends ColumnLabelProvider {
        @Override
        public Image getImage(Object element) {
            return null;
        }

        @Override
        public String getText(Object element) {
            ICpCodeTemplate codeTemplate = getCpCodeTemplate(element);
            if (codeTemplate != null) {
                return codeTemplate.getSelectionName();
            }
            return CmsisConstants.EMPTY_STRING;
        }
    }

    /**
     * Constructor for SampleNewWizardPage.
     *
     * @param pageName
     */
    public CmsisCodeTemplateNewWizardPage(ISelection selection) {
        super(Messages.CmsisCodeTemplate_WizardPage);
        setTitle(Messages.CmsisCodeTemplate_Title);
        setDescription(Messages.CmsisCodeTemplate_Description);
        this.selection = selection;
    }

    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 3;
        // layout.verticalSpacing = 5;

        Label label = new Label(container, SWT.NULL);
        label.setText(Messages.CmsisCodeTemplateNewWizardPage_Project);
        projectText = new Text(container, SWT.BORDER | SWT.SINGLE);
        projectText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        projectText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                projectChanged();
            }
        });
        projectBrowse = new Button(container, SWT.PUSH);
        projectBrowse.setText(Messages.CmsisCodeTemplate_Browse);
        projectBrowse.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleProjectBrowse();
            }
        });

        fViewer = new TreeViewer(container, SWT.FULL_SELECTION | SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
        Tree tree = fViewer.getTree();
        GridData gd_tree = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
        gd_tree.heightHint = 300;
        tree.setLayoutData(gd_tree);
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);
        tree.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Object obj = e.item.getData();
                if (obj instanceof ICpCodeTemplate) {
                    selectedCodeTemplate = (ICpCodeTemplate) obj;
                    String[] codeTemplates = selectedCodeTemplate.getCodeTemplates();
                    if (codeTemplates.length == 0) {
                        fileText.setEditable(false);
                        fileText.setText(CmsisConstants.EMPTY_STRING);
                        return;
                    }
                    fileText.setEditable(true);
                    StringBuilder sb = new StringBuilder(Utils.extractFileName(codeTemplates[0]));
                    for (int i = 1; i < codeTemplates.length; i++) {
                        fileText.setEditable(false);
                        sb.append(' ');
                        sb.append(Utils.extractFileName(codeTemplates[i]));
                    }
                    fileText.setText(sb.toString());
                }
            }
        });

        TreeViewerColumn column0 = new TreeViewerColumn(fViewer, SWT.LEFT);
        column0.getColumn().setText(Messages.CmsisCodeTemplate_Component);
        column0.getColumn().setWidth(200);
        column0.setLabelProvider(new ComponentColumnLabelProvider());

        TreeViewerColumn column1 = new TreeViewerColumn(fViewer, SWT.LEFT);
        column1.getColumn().setText(Messages.CmsisCodeTemplate_Name);
        column1.getColumn().setWidth(320);
        column1.setLabelProvider(new NameColumnLabelProvider());

        fViewer.setContentProvider(new CodeTemplateContentProvider());

        IRteProject rteProject = getRteProject();
        if (rteProject != null) {
            ICpCodeTemplate codeTemplate = rteProject.getRteConfiguration().getCmsisCodeTemplate();
            fViewer.setInput(codeTemplate);
            fViewer.getControl().setFocus();
        }

        label = new Label(container, SWT.NULL);
        label.setText(Messages.CmsisCodeTemplate_Location);
        containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
        containerText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        containerText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                dialogChanged();
            }
        });
        containerBrowse = new Button(container, SWT.PUSH);
        containerBrowse.setText(Messages.CmsisCodeTemplate_Browse);
        containerBrowse.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleContainerBrowse();
            }
        });

        label = new Label(container, SWT.NULL);
        label.setText(Messages.CmsisCodeTemplate_FileName);
        fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
        fileText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fileText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                dialogChanged();
            }
        });

        initialize();
        fbInitialized = true;
        projectChanged();
        setControl(container);
        new Label(container, SWT.NONE);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent.getParent(), IHelpContextIds.CODE_TEMPLATE_WIZARD);

    }

    /**
     * Tests if the current workbench selection is a suitable container to use.
     */
    private void initialize() {
        if (selection != null && selection.isEmpty() == false && selection instanceof IStructuredSelection) {
            IStructuredSelection ssel = (IStructuredSelection) selection;
            if (ssel.size() > 1) {
                return;
            }
            Object obj = ssel.getFirstElement();
            IContainer container = null;
            if (obj instanceof IResource) {
                if (obj instanceof IContainer) {
                    container = (IContainer) obj;
                } else {
                    container = ((IResource) obj).getParent();
                }
            } else if (obj instanceof IAdaptable) {
                IAdaptable adaptable = (IAdaptable) obj;
                IResource res = adaptable.getAdapter(IResource.class);
                if (res == null) {
                    return;
                }
                if (res instanceof IContainer) {
                    container = (IContainer) res;
                } else {
                    container = res.getParent();
                }
            }
            if (container != null) {
                projectText.setText(container.getProject().getName());
                projectText.setEditable(false);
                projectBrowse.setEnabled(false);
                containerText.setText(container.getFullPath().toString());
            }
        }
        if (getProjectName().isEmpty()) {
            containerText.setEditable(false);
            containerBrowse.setEnabled(false);
            fileText.setEditable(false);
        }
    }

    private IRteProject getRteProject() {
        if (selection != null && selection.isEmpty() == false && selection instanceof IStructuredSelection) {
            IStructuredSelection ssel = (IStructuredSelection) selection;
            if (ssel.size() > 1) {
                return null;
            }
            Object obj = ssel.getFirstElement();
            IResource res = ProjectUtils.getResource(obj);
            if (res != null) {
                return CpProjectPlugIn.getRteProjectManager().getRteProject(res.getProject());
            }
        }
        return null;
    }

    /**
     * Open the browser to select project
     */
    void handleProjectBrowse() {
        Set<IRteProject> rteProjects = new HashSet<IRteProject>();
        for (IRteProject p : CpProjectPlugIn.getRteProjectManager().getRteProjects()) {
            if (p.getProject().isOpen()) {
                rteProjects.add(p);
            }
        }
        ProjectSelectionDialog dialog = new ProjectSelectionDialog(getShell(), rteProjects);
        if (dialog.open() == Window.OK) {
            Object[] result = dialog.getResult();
            if (result != null && result.length == 1) {
                projectText.setText(((IRteProject) result[0]).getName());
            }
        }
    }

    /**
     * Uses the standard container selection dialog to choose the new value for the
     * container field.
     */
    void handleContainerBrowse() {
        ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(),
                ResourcesPlugin.getWorkspace().getRoot().getProject(getProjectName()), false,
                Messages.CmsisCodeTemplate_SelectFolder);
        dialog.showClosedProjects(false);
        if (dialog.open() == Window.OK) {
            Object[] result = dialog.getResult();
            if (result.length == 1) {
                containerText.setText(((Path) result[0]).toString());
            }
        }
    }

    void projectChanged() {
        if (!fbInitialized)
            return;
        containerText.setEditable(false);
        containerBrowse.setEnabled(false);
        if (getProjectName().isEmpty()) {
            fViewer.setInput(null);
            updateStatus(Messages.CmsisCodeTemplateNewWizardPage_ProjectMustBeSpecified);
            return;
        }
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(getProjectName());
        if (!project.exists() || (project.getType() & IResource.PROJECT) == 0) {
            fViewer.setInput(null);
            updateStatus(Messages.CmsisCodeTemplateNewWizardPage_ProjectMustExist);
            return;
        }
        if (!refreshComponents(project)) {
            fViewer.setInput(null);
            updateStatus(Messages.CmsisCodeTemplate_RefreshRTEProject);
            return;
        }
        containerText.setEditable(true);
        containerBrowse.setEnabled(true);
        dialogChanged();
    }

    /**
     * Ensures that location and file are set.
     */
    void dialogChanged() {
        if (!fbInitialized)
            return;

        // Check if we have any available template
        if (fViewer.getTree().getItemCount() == 0) {
            updateStatus(Messages.CmsisCodeTemplate_NoTemplates);
            return;
        }

        IResource container = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(getContainerName()));
        String fileName = getFileName();

        if (getContainerName().isEmpty()) {
            updateStatus(Messages.CmsisCodeTemplate_FileContainerNotSpecified);
            return;
        }
        if (container == null || (container.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0) {
            updateStatus(Messages.CmsisCodeTemplate_FileContainerNotExist);
            return;
        }
        String[] segments = container.getFullPath().segments();
        if (!segments[0].equals(getProjectName())) {
            updateStatus(Messages.CmsisCodeTemplateNewWizardPage_LocationNotUnderProject);
            return;
        }
        if (!container.isAccessible()) {
            updateStatus(Messages.CmsisCodeTemplate_ProjectNotWritable);
            return;
        }

        segments = container.getProjectRelativePath().segments();
        if (segments.length > 0 && segments[0].equals(CmsisConstants.RTE)) {
            updateStatus(Messages.CmsisCodeTemplate_FileUnderRTEFolder);
            return;
        }

        if (fileName.length() == 0) {
            updateStatus(Messages.CmsisCodeTemplate_FileNameNotSpecified);
            return;
        }

        if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
            updateStatus(Messages.CmsisCodeTemplate_FileNameNotValid);
            return;
        }
        int dotLoc = fileName.lastIndexOf('.');
        if (dotLoc == -1) {
            updateStatus(Messages.CmsisCodeTemplate_FileExtensionNotConsistent);
            return;
        }
        String ext = fileName.substring(dotLoc + 1);
        if (!extIsValid(ext)) {
            updateStatus(Messages.CmsisCodeTemplate_FileExtensionNotConsistent);
            return;
        }

        updateStatus(null);
    }

    private boolean extIsValid(String ext) {
        if (selectedCodeTemplate == null) {
            return false;
        }
        if (selectedCodeTemplate.getCodeTemplates().length != 1) {
            return true;
        }
        String templateExt = Utils.extractFileExtension(selectedCodeTemplate.getCodeTemplates()[0]);
        String fileExt = Utils.extractFileExtension(fileText.getText());
        if (templateExt.equals(fileExt)) {
            return true;
        }
        return false;
    }

    private void updateStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }

    /**
     * @param resource
     * @return true if the fViewer has updated its input, false otherwise
     */
    protected boolean refreshComponents(IProject project) {
        IRteProject rteProject = CpProjectPlugIn.getRteProjectManager().getRteProject(project);
        if (rteProject != null) {
            ICpCodeTemplate codeTemplate = rteProject.getRteConfiguration().getCmsisCodeTemplate();
            fViewer.setInput(codeTemplate);
            fViewer.getControl().setFocus();
            return true;
        }
        return false;
    }

    public String getProjectName() {
        return projectText.getText();
    }

    public String getContainerName() {
        return containerText.getText();
    }

    public String getFileName() {
        return fileText.getText();
    }

    public String[] getCodeTemplateFileNames() {
        String[] fileNames = new String[selectedCodeTemplate.getCodeTemplates().length];
        String[] templates = selectedCodeTemplate.getCodeTemplates();
        for (int i = 0; i < fileNames.length; i++) {
            fileNames[i] = selectedCodeTemplate.getAbsolutePath(templates[i]);
        }
        return fileNames;
    }
}