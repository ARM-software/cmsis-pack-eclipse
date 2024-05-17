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
package com.arm.cmsis.zone.ui.wizards;

import java.util.Map;

import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.enums.EMemoryPrivilege;
import com.arm.cmsis.pack.enums.EMemorySecurity;
import com.arm.cmsis.pack.generic.ITreeObject;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.tree.TreeObjectContentProvider;
import com.arm.cmsis.pack.utils.FullDeviceName;
import com.arm.cmsis.zone.data.ICpProcessorUnit;
import com.arm.cmsis.zone.data.ICpZone;
import com.arm.cmsis.zone.ui.Messages;
import com.arm.cmsis.zone.ui.editors.CmsisZoneController;
import com.arm.cmsis.zone.widgets.CmsisZoneColumnAdvisor;

public class CmsisZoneWizardPage extends CmsisZoneAbstractWizardPage<ICpZone> {
    private Label lblZoneName;
    private Text textName;
    private Label lblSecurity;
    private Combo comboSecurity;
    private Label lblPrivilege;
    private Combo comboPrivilege;
    private Label lblDevice;
    private Combo comboDevice;
    private Label lblInfo;
    private Text textInfo;
    private Label lblClass;
    private Text textClass;
    private Label lblAssignments;
    private Tree treeAssignments;
    private TreeViewer fTreeViewer;

    private EMemorySecurity fSecurity = EMemorySecurity.NOT_SPECIFIED;
    private EMemoryPrivilege fPrivilege = EMemoryPrivilege.NOT_SPECIFIED;

    /**
     * @wbp.parser.constructor
     */
    public CmsisZoneWizardPage() {
        super(Messages.CmsisZoneWizardPage_CmsisZone);
    }

    /**
     * Create the wizard.
     */
    public CmsisZoneWizardPage(CmsisZoneController controller, ICpZone fExistingItem) {
        super("CMSIS-Zone", controller, fExistingItem); //$NON-NLS-1$
        setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_CMSIS_ZONE_48));
        if (fExistingItem == null) {
            setTitle(Messages.CmsisZoneWizardPage_NewCmsisZone + controller.getZoneLabel());
            setDescription(Messages.CmsisZoneWizardPage_CreateNewCmsis + controller.getZoneLabel());
        } else {
            setTitle(Messages.CmsisZoneWizardPage_Cmsis + controller.getZoneLabel()
                    + Messages.CmsisZoneWizardPage_Properties);
            setDescription(Messages.CmsisZoneWizardPage_EditCmsis + controller.getZoneLabel()
                    + Messages.CmsisZoneWizardPage_PropertiesLowerCase);
        }
    }

    public class ZoneAssignmentsContentProvider extends TreeObjectContentProvider {
        @Override
        public Object[] getElements(Object inputElement) {
            ICpZone zone = getExistingItem();
            if (zone != null) {
                return zone.getZoneAssignments().toArray();
            }
            return ITreeObject.EMPTY_OBJECT_ARRAY;
        }
    }

    public class ZoneAssignmentsCoumnAdvisor extends CmsisZoneColumnAdvisor {
        public ZoneAssignmentsCoumnAdvisor(TreeViewer treeViewer) {
            super(treeViewer);
        }

        @Override
        public String getString(Object obj, int columnIndex) {
            return super.getString(obj, columnIndex);
        }

    }

    /**
     * Create contents of the wizard.
     * 
     * @param parent
     */
    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);

        setControl(container);
        container.setLayout(new GridLayout(2, false));

        lblZoneName = new Label(container, SWT.NONE);
        lblZoneName.setText(Messages.CmsisZoneWizardPage_Name);

        textName = new Text(container, SWT.BORDER);
        textName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        textName.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                nameChanged();
            }
        });

        lblDevice = new Label(container, SWT.NONE);
        lblDevice.setText(Messages.CmsisZoneWizardPage_Device);

        comboDevice = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        comboDevice.addModifyListener((e) -> {
            updateSecurity();
        });
        comboDevice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        lblSecurity = new Label(container, SWT.NONE);
        lblSecurity.setText(Messages.CmsisZoneWizardPage_Security);

        comboSecurity = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        comboSecurity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        comboSecurity.add(Messages.CmsisZoneWizardPage_NotSpecified);
        comboSecurity.add(Messages.CmsisZoneWizardPage_NonSecure);
        comboSecurity.add(Messages.CmsisZoneWizardPage_Secure);

        lblPrivilege = new Label(container, SWT.NONE);
        lblPrivilege.setText(Messages.CmsisZoneWizardPage_Privilege);

        comboPrivilege = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        comboPrivilege.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        comboPrivilege.add(Messages.CmsisZoneWizardPage_NotSpecified);
        comboPrivilege.add(Messages.CmsisZoneWizardPage_Unprivileged);
        comboPrivilege.add(Messages.CmsisZoneWizardPage_Privileged);

        lblInfo = new Label(container, SWT.NONE);
        lblInfo.setText(Messages.CmsisZoneWizardPage_Info);

        textInfo = new Text(container, SWT.BORDER);
        textInfo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        // Class information for execution zones
        lblClass = new Label(container, SWT.NONE);
        lblClass.setText(Messages.CmsisZoneWizardPage_Class);

        textClass = new Text(container, SWT.BORDER);
        textClass.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        lblAssignments = new Label(container, SWT.NONE);
        lblAssignments.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        lblAssignments.setText(Messages.CmsisZoneWizardPage_Assignments);

        treeAssignments = new Tree(container, SWT.BORDER | SWT.FULL_SELECTION);
        treeAssignments.setLinesVisible(true);
        treeAssignments.setHeaderVisible(true);
        GridData gd_treeAssignments = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
        gd_treeAssignments.widthHint = 160;
        treeAssignments.setLayoutData(gd_treeAssignments);

        fTreeViewer = new TreeViewer(treeAssignments);
        ColumnViewerToolTipSupport.enableFor(fTreeViewer);
        fTreeViewer.setContentProvider(new ZoneAssignmentsContentProvider());

        setInitialValues();
    }

    private void setInitialValues() {
        String selectedDevice = null;
        int selectedDeviceIndex = 0;
        // disable assignments for now
        lblAssignments.setVisible(false);
        treeAssignments.setVisible(false);
        ICpZone zone = getExistingItem();

        if (zone != null) {
            selectedDevice = getExistingItem().getFullDeviceName();
            fSecurity = zone.getSecurity();
            fPrivilege = zone.getPrivilege();

            textName.setText(getExistingItem().getName());
            textInfo.setText(getExistingItem().getDescription());
            ZoneAssignmentsCoumnAdvisor adviser = new ZoneAssignmentsCoumnAdvisor(fTreeViewer);
            adviser.createColumns();
            fTreeViewer.setInput(this);
        }

        Map<String, ICpProcessorUnit> processors = fRootZone.getProcessorUnits();
        int i = 0;
        for (ICpProcessorUnit processor : processors.values()) {
            String fullName = processor.getFullDeviceName();
            comboDevice.add(fullName);
            if (fullName.equals(selectedDevice))
                selectedDeviceIndex = i;
            i++;
        }
        comboDevice.select(selectedDeviceIndex);
        updateSecurity();
        updatePrivilege();

        // Update 'Class' info controls
        if (zone != null)
            textClass.setText(zone.getAttribute(CmsisConstants.CLASS));

        if (fRootZone.isZoneModeProject()) { // Radiobutton 'Execution'
            lblClass.setVisible(false);
            textClass.setVisible(false);
        }
    }

    private void updateSecurity() {
        String fullDeviceName = comboDevice.getText();
        String processorName = FullDeviceName.extractProcessoreName(fullDeviceName);

        ICpProcessorUnit processor = fRootZone.getProcessorUnit(processorName);
        String dtz = processor.getAttribute(CmsisConstants.DTZ);
        if (!dtz.equals(CmsisConstants.TZ)) {
            comboSecurity.setEnabled(false);
            comboSecurity.select(0);
            return;
        }
        EMemorySecurity rs = fRootZone.getSecurity();
        EMemorySecurity ms = fSecurity.adjust(rs);
        int securityIndex = 0;
        if (ms == EMemorySecurity.NON_SECURE)
            securityIndex = 1;
        else if (ms == EMemorySecurity.SECURE)
            securityIndex = 2;

        comboSecurity.setEnabled(rs == EMemorySecurity.NOT_SPECIFIED);
        comboSecurity.select(securityIndex);
    }

    private void updatePrivilege() {
        if (fRootZone.isZoneModeProject()) {
            lblPrivilege.setVisible(false);
            comboPrivilege.setVisible(false);
            return;
        }
        EMemoryPrivilege rp = fRootZone.getPrivilege();
        EMemoryPrivilege mp = fPrivilege.adjust(rp);
        int privilegeIndex = 0;
        if (mp == EMemoryPrivilege.UNPRIVILEGED)
            privilegeIndex = 1;
        else if (mp == EMemoryPrivilege.PRIVILEGED)
            privilegeIndex = 2;
        comboPrivilege.setEnabled(rp == EMemoryPrivilege.NOT_SPECIFIED);
        comboPrivilege.select(privilegeIndex);
    }

    void nameChanged() {
        if (fRootZone == null)
            return;

        String name = textName.getText();
        if (name.isEmpty()) {
            updateStatus(Messages.CmsisZoneWizardPage_NameSpecification);
            return;
        }

        ICpZone zone = fRootZone.getZone(name);
        if (zone == null || zone == getExistingItem()) {
            updateStatus(null);
            return;
        }
        updateStatus(Messages.CmsisZoneWizardPage_AzoneNameValidation);
    }

    private void updateStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }

    @Override
    public boolean apply() {
        String name = textName.getText();
        String fullDeviceName = comboDevice.getText();

        String privilege = CmsisConstants.EMPTY_STRING;
        if (!fRootZone.isZoneModeProject()) {
            int privilegeIndex = comboPrivilege.getSelectionIndex();
            if (privilegeIndex == 1)
                privilege = EMemoryPrivilege.u;
            else if (privilegeIndex == 2)
                privilege = EMemoryPrivilege.p;
        }

        String security = CmsisConstants.EMPTY_STRING;
        int securityIndex = comboSecurity.getSelectionIndex();
        if (securityIndex == 1)
            security = EMemorySecurity.n;
        else if (securityIndex == 2)
            security = EMemorySecurity.s;

        String info = textInfo.getText();
        String classInfo = textClass.getText();
        fController.updateZone(getExistingItem(), name, fullDeviceName, security, privilege, info, classInfo);

        return true;
    }
}
