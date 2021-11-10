package com.arm.cmsis.zone.ui.wizards;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ResourceListSelectionDialog;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.utils.Utils;
import com.arm.cmsis.zone.ui.Messages;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (azone).
 */

public class CmsisZoneAzoneFileWizardPage extends WizardPage {
    private Text rZoneFileText;
    private Text aZoneFileText;
    private ISelection selection;
    private static final String[] rzoneFileExtensions = new String[] { "*" + CmsisConstants.DOT_RZONE }; //$NON-NLS-1$
    private String azoneFile;
    private String rzoneFile;
    private Label lblresourceFilePath;
    private Label lblassignmentPath;
    private Composite compositeButtons;

    /**
     * Constructor for SampleNewWizardPage.
     * 
     * @param pageName
     */
    public CmsisZoneAzoneFileWizardPage(ISelection selection) {
        super(Messages.CmsisZoneAzoneFileWizardPage_WizardTitle);
        setTitle(Messages.CmsisZoneAzoneFileWizardPage_WizardTitle);
        setDescription(Messages.CmsisZoneAzoneFileWizardPage_WizardDescription);
        setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_CMSIS_ZONE_48));
        this.selection = selection;
    }

    /**
     * @see IDialogPage#createControl(Composite)
     */
    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 2;
        layout.verticalSpacing = 9;
        lblresourceFilePath = new Label(container, SWT.NULL);
        lblresourceFilePath.setText(Messages.CmsisZoneAzoneFileWizardPage_ResourceFilePath);

        rZoneFileText = new Text(container, SWT.BORDER | SWT.SINGLE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        rZoneFileText.setLayoutData(gd);
        rZoneFileText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                validate();
            }
        });
        new Label(container, SWT.NONE);

        compositeButtons = new Composite(container, SWT.ARROW_RIGHT);
        compositeButtons.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        GridLayout layoutCompositeButtons = new GridLayout();
        layoutCompositeButtons.numColumns = 2;
        compositeButtons.setLayout(layoutCompositeButtons);
        layoutCompositeButtons.verticalSpacing = 9;

        // Buttons
        Button button = new Button(compositeButtons, SWT.PUSH);
        button.setText(Messages.CmsisZoneAzoneFileWizardPage_Workspace);
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleWorkspaceButton();
            }
        });

        Button buttonFileSystem = new Button(compositeButtons, SWT.PUSH);
        buttonFileSystem.setText(Messages.CmsisZoneAzoneFileWizardPage_FileSystem);
        buttonFileSystem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleFileSystemButton();
            }
        });

        lblassignmentPath = new Label(container, SWT.NULL);
        lblassignmentPath.setText(Messages.CmsisZoneAzoneFileWizardPage_AssignmentFileName);

        aZoneFileText = new Text(container, SWT.BORDER | SWT.SINGLE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        aZoneFileText.setLayoutData(gd);
        aZoneFileText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                validate();
            }
        });
        initialize();
        setControl(container);
    }

    private void initialize() {
        setAzoneFile(getRzoneFileFromSelection());
    }

    private void setAzoneFile(String rzone) {
        if (rzone == null) {
            validate();
            return;
        }

        // Validate rzone path
        if (rzone.indexOf(CmsisConstants.SLASH) == 1) {
            rzone = rzone.substring(1, rzone.length());
            rzone = getWorkspacePath() + rzone;
        }

        IFile rzoneIFile = CpPlugInUI.getFileForLocation(rzone);
        if (rzoneIFile != null) {
            rzoneFile = getWorkspacePath() + rzoneIFile.getFullPath().toPortableString();
            rzone = CmsisConstants.WORKSPACE_LOC + Utils.extractFileName(rzone);

        } else {// File does not belong to any project in workspace (external file)
            rzoneFile = rzone;
        }

        // Initialize resource file field
        rZoneFileText.setText(rzone);

        // Initialize assignment file field
        aZoneFileText.setText(Utils.extractFileName(Utils.changeFileExtension(rzone, CmsisConstants.AZONE)));

        // Save azone file name from aZoneFileText
        azoneFile = aZoneFileText.getText();
    }

    /**
     * Gets workspace path
     * 
     * @return String with workspace path
     */
    public String getWorkspacePath() {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        if (root == null)
            return null;
        return root.getLocation().toString();
    }

    private String getRzoneFileFromSelection() {
        if (!(selection instanceof IStructuredSelection) || selection.isEmpty()) {
            return null;
        }
        IStructuredSelection ssel = (IStructuredSelection) selection;
        if (ssel.size() > 1)
            return null;
        Object obj = ssel.getFirstElement();
        if (!(obj instanceof IResource)) {
            return null;
        }

        IResource res = (IResource) obj;
        int type = res.getType();
        IContainer container = null;
        switch (type) {
        case IResource.FILE:
            if (CmsisConstants.RZONE.equals(res.getFileExtension())) {
                return res.getLocation().toOSString();
            }
            container = res.getParent();
            break;
        case IResource.FOLDER:
        case IResource.PROJECT:
            container = (IContainer) res;
            break;
        default:
            return null;
        }
        IResource[] children;
        try {
            children = container.members();
            if (children == null || children.length == 0) {
                return null;
            }
        } catch (CoreException e) {
            e.printStackTrace();
            return null;
        }

        for (IResource r : children) {
            if (r.getType() == IResource.FILE && CmsisConstants.RZONE.equals(r.getFileExtension())) {
                return r.getLocation().toOSString();
            }
        }
        return null;
    }

    class RzoneSelectionDialof extends ResourceListSelectionDialog {
        IResource fInitialResource = null;

        public RzoneSelectionDialof(Shell parentShell, IContainer container, int typeMask, IResource initialResource) {
            super(parentShell, container, typeMask);
            fInitialResource = initialResource;
        }

        @Override
        protected String adjustPattern() {
            String text = super.adjustPattern();
            if (text.isEmpty())
                return CmsisConstants.ASTERISK;
            return text;
        }

        @Override
        protected boolean select(IResource res) {
            if (res == null) {
                return false;
            }
            return res.toString().endsWith(CmsisConstants.DOT_RZONE);
        }

        @Override
        public void create() {
            super.create();
            if (fInitialResource != null) {
                List<IResource> l = Collections.singletonList(fInitialResource);
                setInitialElementSelections(l);
            }
            refresh(true);
        }
    }

    /**
     * Uses the standard container selection dialog to choose the new value for the
     * container field.
     */
    private void handleWorkspaceButton() {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IFile iFile = CpPlugInUI.getFileForLocation(rzoneFile);
        RzoneSelectionDialof dlg = new RzoneSelectionDialof(getShell(), root, IResource.FILE, iFile);
        dlg.setTitle(Messages.CmsisZoneAzoneFileWizardPage_SelectResourceFile);
        if (dlg.open() == Window.OK) {
            Object[] result = dlg.getResult();
            if (result.length == 1) {
                String file = (result[0]).toString();
                setAzoneFile(file);
            }
        }
    }

    private void handleFileSystemButton() {
        FileDialog dialog = new FileDialog(getShell());
        dialog.setFilterExtensions(rzoneFileExtensions);
        dialog.setFileName(rzoneFile);
        String file = dialog.open();
        setAzoneFile(file);
    }

    public String validate() {
        String result = CmsisConstants.EMPTY_STRING;

        // Save azone file name from aZoneFileText
        azoneFile = aZoneFileText.getText();

        // Validate rzone file
        String validateRzoneFile = validateRzoneFile();
        if (!validateRzoneFile.isEmpty()) {
            updateStatus(validateRzoneFile);
            return validateRzoneFile;
        }

        // Validate file azone file
        String validateAzoneFile = validateAzoneFile();
        if (!validateAzoneFile.isEmpty()) {
            updateStatus(validateAzoneFile);
            return validateAzoneFile;
        }

        updateStatus(null);
        return result;
    }

    private String validateRzoneFile() {
        String result = CmsisConstants.EMPTY_STRING;
        String rzone = getRzoneFile();

        if (rzone == null || rzone.isEmpty()) {
            return result = Messages.CmsisZoneAzoneFileWizardPage_SpecificationResourceFile;

        }
        if (rzone != null) {
            File file = new File(rzone);
            if (!file.exists()) {
                return result = Messages.CmsisZoneAzoneFileWizardPage_ExistenceRzoneFile;
            }
        }
        return result;
    }

    private String validateAzoneFile() {
        String result = CmsisConstants.EMPTY_STRING;
        String azone = getAzoneFile();

        // Get azone extension and add it if missing
        if (!azone.isEmpty()) {
            String azoneFileExt = Utils.extractFileExtension(azone);
            if (azoneFileExt == null) { // Add azone extension
                azoneFile = azone + CmsisConstants.DOT_AZONE;
            }
        }

        if (azone.length() == 0) {
            return result = Messages.CmsisZoneAzoneFileWizardPage_SpecificationAssignmentFile;

        }
        if (azone.replace('\\', '/').indexOf('/', 1) > 0) {
            return result = Messages.CmsisZoneAzoneFileWizardPage_SpecificationAssignmentFileName;

        }
        int dotLoc = azone.lastIndexOf('.');
        if (dotLoc != -1) {
            String ext = azone.substring(dotLoc + 1);
            if (!ext.equalsIgnoreCase(CmsisConstants.AZONE)) {
                return result = Messages.CmsisZoneAzoneFileWizardPage_SpecificationAzoneFileExtension;
            }
        }

        // Build azone file path
        String rzone = getRzoneFile();
        IPath rzoneFilePath = new Path(rzone);
        IPath azoneFilePath = rzoneFilePath.removeLastSegments(1);
        String azoneFileName = azoneFilePath.toPortableString() + CmsisConstants.SLASH + azoneFile;

        // Check if file exists
        File file = new File(azoneFileName);
        if (file.exists()) {
            return result = Messages.CmsisZoneAzoneFileWizardPage_File + azoneFile
                    + Messages.CmsisZoneAzoneFileWizardPage_AlreadyExists;
        }

        return result;
    }

    private void updateStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }

    public String getRzoneFile() {
        return rzoneFile;

    }

    public String getAzoneFile() {
        return azoneFile;
    }
}