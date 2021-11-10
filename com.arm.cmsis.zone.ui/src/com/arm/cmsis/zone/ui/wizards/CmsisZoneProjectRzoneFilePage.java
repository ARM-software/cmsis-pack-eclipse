package com.arm.cmsis.zone.ui.wizards;

import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.zone.project.CmsisZoneProjectCreator;
import com.arm.cmsis.zone.ui.Messages;

/**
 * Page to select resource source
 */
public class CmsisZoneProjectRzoneFilePage extends WizardPage {

    protected FileFieldEditor fileEditor = null; // File field editor widget to search a .rzone file
    protected Composite fileEditorComposite = null; // Container for field editor widget
    private static final String[] cmsisZoneFileExtensions = new String[] { '*' + CmsisConstants.DOT_RZONE }; // Set a
                                                                                                             // filter
                                                                                                             // for
                                                                                                             // .rzone
                                                                                                             // files in
                                                                                                             // the file
                                                                                                             // field
                                                                                                             // fileEditor
                                                                                                             // widget
    private Group resourceSourceArea = null;
    protected CmsisZoneProjectCreator cmsisZoneProjectCreator = null;
    protected boolean bDeviceSelected = true;

    /**
     * Constructor
     */
    public CmsisZoneProjectRzoneFilePage(CmsisZoneProjectCreator cmsisZoneProjectCreator, String pageName) {
        super(pageName);
        setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_CMSIS_ZONE_48));
        this.cmsisZoneProjectCreator = cmsisZoneProjectCreator;
    }

    @Override
    public void createControl(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Create a group with "Resource source" title
        resourceSourceArea = new Group(composite, SWT.NONE);
        resourceSourceArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        resourceSourceArea.setText(Messages.CmsisZoneProjectRzoneFilePage_ResourceFile);
        resourceSourceArea.setLayout(new GridLayout(1, false));

        // Add space between group's name and 'Select Device' radio button
        Composite groupSpace = new Composite(resourceSourceArea, SWT.NONE);
        GridData groupSpaceGridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        groupSpaceGridData.heightHint = 15;
        groupSpace.setLayoutData(groupSpaceGridData);

        // Create 'Select Device' radio button
        Button btnSelectDevice = new Button(resourceSourceArea, SWT.RADIO);
        btnSelectDevice.setText(Messages.CmsisZoneProjectRzoneFilePage_DeviceSelection);
        btnSelectDevice.setSelection(true);
        btnSelectDevice.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                setDeviceSelected(true);
            }
        });

        // Add space between radio buttons
        Composite radioButtonsSpace = new Composite(resourceSourceArea, SWT.NONE);
        GridData radioButtonsSpaceGridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        radioButtonsSpaceGridData.heightHint = 6;
        radioButtonsSpace.setLayoutData(radioButtonsSpaceGridData);

        // Create '.rzone' file
        Button btnSelectRzoneFile = new Button(resourceSourceArea, SWT.RADIO);
        btnSelectRzoneFile.setText(Messages.CmsisZoneProjectRzoneFilePage_ResourceFileUsage);
        btnSelectRzoneFile.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                setDeviceSelected(false);
            }
        });

        // Create container for the file field fileEditor widget
        fileEditorComposite = new Composite(resourceSourceArea, SWT.NONE);
        fileEditorComposite.setLayout(new FillLayout());
        fileEditorComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

        // Create a file field fileEditor widget to select a .rzone file
        fileEditor = new FileFieldEditor("fileSelect", CmsisConstants.EMPTY_STRING, fileEditorComposite); //$NON-NLS-1$
        fileEditor.getTextControl(fileEditorComposite).addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                rZoneFileModified();
            }
        });
        fileEditor.setFileExtensions(cmsisZoneFileExtensions);
        fileEditor.setEnabled(false, fileEditorComposite);

        setControl(composite);
        checkPageComplete();
    }

    protected void setDeviceSelected(boolean bSelected) {
        bDeviceSelected = bSelected;
        fileEditor.setEnabled(!bDeviceSelected, fileEditorComposite);
        cmsisZoneProjectCreator.setDeviceSelected(bDeviceSelected);
        checkPageComplete();
    }

    /**
     * 'rzone' file's name is changed : selected or edited
     */
    protected void rZoneFileModified() {
        if (fileEditor == null)
            return;
        String rZoneFile = fileEditor.getStringValue();

        // Set .rzone file to CmsisZoneManager
        cmsisZoneProjectCreator.setRzoneFile(rZoneFile);
        checkPageComplete();
    }

    /**
     * Validates wizard page every time when user generates an event
     */
    void checkPageComplete() {
        boolean valid = validatePage();
        setPageComplete(valid);
    }

    /**
     * Validates wizard's main page
     * 
     * @return boolean true is page is valid
     */
    protected boolean validatePage() {
        if (bDeviceSelected)
            return true;

        if (cmsisZoneProjectCreator == null)
            return false;

        String rZoneFile = cmsisZoneProjectCreator.getRzoneFile();
        if (rZoneFile == null || rZoneFile.isEmpty()) {
            setErrorMessage(null);
            return false;
        }

        String msg = cmsisZoneProjectCreator.validate();
        if (!msg.isEmpty()) {
            setErrorMessage(msg);
            return false;
        }
        setErrorMessage(null);

        return true;
    }

    /**
     * Checks if device is selected
     * 
     * @return true if device is selected
     */
    public boolean isDeviceSelected() {
        return bDeviceSelected;
    }

    @Override
    public boolean canFlipToNextPage() {
        if (bDeviceSelected) {
            return super.canFlipToNextPage();
        }
        return false;
    }

}
