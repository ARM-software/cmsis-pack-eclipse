/*******************************************************************************
 * Copyright (c) 2021 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Eclipse Project - generation from template
 * ARM Ltd and ARM Germany GmbH - application-specific implementation
 *******************************************************************************/

package com.arm.cmsis.zone.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.ui.widgets.MemoryPermissionsControl;
import com.arm.cmsis.pack.utils.Utils;
import com.arm.cmsis.zone.data.ICpRootZone;
import com.arm.cmsis.zone.data.ICpZoneCreator;
import com.arm.cmsis.zone.ui.Messages;
import com.arm.cmsis.zone.ui.editors.CmsisZoneController;

public class CmsisZoneSetupComposite extends Composite {

    Group grpZone = null;
    Label lblZoneName;
    Label lblZoneSecurity;
    Label lblZonePrivilege;
    Label lblZoneResourceFile;
    Label lblZoneInfo;

    Group grpCreator;
    Label lblCreatorTool;
    Label lblCreatorAzone;
    Label lblCreatorRzone;

    Group grpMode;
    private Button rbModeProject;
    private Button rbModeMPU;

    Group grpOptions;
    private Button btnShowRAM;
    private Button btnShowROM;
    private Button btnShowPeripherals;

    private Button btnSelectRAM;
    private Button btnSelectROM;
    private Button btnSelectPeripherals;

    private CmsisZoneController fModelController = null;

    private List<Button> fOptionButtons = new ArrayList<>();

    SelectionAdapter optionsSelectionAdapter;

    /**
     * Create the composite.
     *
     * @param parent
     * @param style
     */
    public CmsisZoneSetupComposite(Composite parent) {
        super(parent, SWT.BORDER);
        setLayout(new GridLayout(2, false));

        optionsSelectionAdapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button btn = (Button) e.getSource();
                String type = (String) btn.getData(CmsisConstants.TYPE);
                String key = (String) btn.getData(CmsisConstants.KEY);
                boolean value = btn.getSelection();
                optionModified(type, key, value);
            }
        };

        grpZone = new Group(this, SWT.NONE);
        grpZone.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        grpZone.setText(Messages.CmsisZoneSetupComposite_Zone);
        grpZone.setLayout(new GridLayout(2, false));

        Label lblName = new Label(grpZone, SWT.NONE);
        lblName.setText(Messages.CmsisZoneSetupComposite_Name);

        lblZoneName = new Label(grpZone, SWT.NONE);
        lblZoneName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblSecurity = new Label(grpZone, SWT.NONE);
        lblSecurity.setText(Messages.CmsisZoneSetupComposite_Security);

        lblZoneSecurity = new Label(grpZone, SWT.NONE);
        lblZoneSecurity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblPrivulege = new Label(grpZone, SWT.NONE);
        lblPrivulege.setText(Messages.CmsisZoneSetupComposite_Privilege);

        lblZonePrivilege = new Label(grpZone, SWT.NONE);
        lblZonePrivilege.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblRzone = new Label(grpZone, SWT.NONE);
        lblRzone.setText(Messages.CmsisZoneSetupComposite_ResourceFile);

        lblZoneResourceFile = new Label(grpZone, SWT.NONE);
        lblZoneResourceFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblInfo = new Label(grpZone, SWT.NONE);
        lblInfo.setText(Messages.CmsisZoneSetupComposite_Info);

        lblZoneInfo = new Label(grpZone, SWT.NONE);
        lblZoneInfo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        grpCreator = new Group(this, SWT.NONE);
        grpCreator.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        grpCreator.setText(Messages.CmsisZoneSetupComposite_Creator);
        grpCreator.setLayout(new GridLayout(2, false));

        Label lblTool = new Label(grpCreator, SWT.NONE);
        lblTool.setText(Messages.CmsisZoneSetupComposite_Tool);

        lblCreatorTool = new Label(grpCreator, SWT.NONE);
        lblCreatorTool.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblRzoneC = new Label(grpCreator, SWT.NONE);
        lblRzoneC.setText(Messages.CmsisZoneSetupComposite_InputResources);
        lblCreatorRzone = new Label(grpCreator, SWT.NONE);
        lblCreatorRzone.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblAzoneC = new Label(grpCreator, SWT.NONE);
        lblAzoneC.setText(Messages.CmsisZoneSetupComposite_InputAssignments);
        lblCreatorAzone = new Label(grpCreator, SWT.NONE);
        lblCreatorAzone.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        grpMode = new Group(this, SWT.NONE);
        grpMode.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        grpMode.setText(Messages.CmsisZoneSetupComposite_Mode);
        grpMode.setLayout(new GridLayout(2, false));

        rbModeProject = new Button(grpMode, SWT.RADIO);
        rbModeProject.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
        rbModeProject.setText(Messages.CmsisZoneSetupComposite_Project);
        Label lblModeProject = new Label(grpMode, SWT.NONE);
        lblModeProject.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
        lblModeProject.setText(Messages.CmsisZoneSetupComposite_ResourcesAllocation);

        rbModeMPU = new Button(grpMode, SWT.RADIO);
        rbModeMPU.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
        rbModeMPU.setText(Messages.CmsisZoneSetupComposite_Execution);
        rbModeMPU.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                modeModified();
            }
        });
        Label lblModeMPU = new Label(grpMode, SWT.NONE);
        lblModeMPU.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
        lblModeMPU.setText(Messages.CmsisZoneSetupComposite_ResourcesAssignation);

        grpOptions = new Group(this, SWT.NONE);
        grpOptions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        grpOptions.setText(Messages.CmsisZoneSetupComposite_Options);
        grpOptions.setLayout(new GridLayout(3, false));

        Label lblRam = new Label(grpOptions, SWT.NONE);
        lblRam.setText(Messages.CmsisZoneSetupComposite_RAM);

        btnShowRAM = new Button(grpOptions, SWT.CHECK);
        btnShowRAM.setText(CmsisConstants.SHOW);
        addOptionButton(btnShowRAM, CmsisConstants.RAM, CmsisConstants.SHOW);

        btnSelectRAM = new Button(grpOptions, SWT.CHECK);
        btnSelectRAM.setText(CmsisConstants.SELECT);
        addOptionButton(btnSelectRAM, CmsisConstants.RAM, CmsisConstants.SELECT);

        Label lblRom = new Label(grpOptions, SWT.NONE);
        lblRom.setText(Messages.CmsisZoneSetupComposite_ROM);

        btnShowROM = new Button(grpOptions, SWT.CHECK);
        btnShowROM.setText(CmsisConstants.SHOW);
        addOptionButton(btnShowROM, CmsisConstants.ROM, CmsisConstants.SHOW);

        btnSelectROM = new Button(grpOptions, SWT.CHECK);
        btnSelectROM.setText(CmsisConstants.SELECT);
        addOptionButton(btnSelectROM, CmsisConstants.ROM, CmsisConstants.SELECT);

        Label lblPeripherals = new Label(grpOptions, SWT.NONE);
        lblPeripherals.setText(Messages.CmsisZoneSetupComposite_Peripherals);
        btnShowPeripherals = new Button(grpOptions, SWT.CHECK);
        btnShowPeripherals.setText(CmsisConstants.SHOW);
        addOptionButton(btnShowPeripherals, CmsisConstants.PERIPHERAL, CmsisConstants.SHOW);

        btnSelectPeripherals = new Button(grpOptions, SWT.CHECK);
        btnSelectPeripherals.setText(CmsisConstants.SELECT);
        addOptionButton(btnSelectPeripherals, CmsisConstants.PERIPHERAL, CmsisConstants.SELECT);

    }

    private void addOptionButton(Button btn, String type, String key) {
        btn.setData(CmsisConstants.TYPE, type);
        btn.setData(CmsisConstants.KEY, key);
        btn.addSelectionListener(optionsSelectionAdapter);
        fOptionButtons.add(btn);
    }

    protected void optionModified(String type, String key, boolean value) {
        if (fModelController == null) {
            return;
        }
        fModelController.setZoneOption(type, key, value);

    }

    protected void modeModified() {
        if (fModelController == null)
            return;

        String mode = rbModeMPU.isEnabled() && rbModeMPU.getSelection() ? CmsisConstants.MPU : CmsisConstants.PROJECT;
        fModelController.setZoneMode(mode);
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    public void setModelController(CmsisZoneController modelController) {
        fModelController = modelController;
    }

    public void refresh() {
        if (fModelController == null)
            return;
        if (grpZone == null)
            return; // not initialized yet

        ICpRootZone rootZone = fModelController.getRootZone();
        if (rootZone == null) {
            grpOptions.setEnabled(false);
            rbModeProject.setSelection(true);
            return;
        }
        grpOptions.setEnabled(true);

        String rzone = rootZone.getResourceFileName();
        String zoneName = Utils.extractBaseFileName(rzone);
        lblZoneName.setText(zoneName);

        String security = MemoryPermissionsControl.getDispalyString(rootZone.getSecurity());
        lblZoneSecurity.setText(security);

        String privilege = MemoryPermissionsControl.getDispalyString(rootZone.getPrivilege());
        lblZonePrivilege.setText(privilege);

        lblZoneResourceFile.setText(Utils.extractFileName(rzone));

        // Disable (grey out) MPU selection for devices that don't have MPU
        boolean bHasMpu = rootZone.hasMPU();
        rbModeMPU.setEnabled(bHasMpu);

        boolean bMPU = bHasMpu && rootZone.isZoneModeMPU();
        rbModeMPU.setSelection(bMPU);
        rbModeProject.setSelection(!bMPU);

        for (Button btn : fOptionButtons) {
            String type = (String) btn.getData(CmsisConstants.TYPE);
            String key = (String) btn.getData(CmsisConstants.KEY);
            boolean value = rootZone.getZoneOption(type, key);
            btn.setSelection(value);
        }
        ICpZoneCreator creator = rootZone.getZoneCreator();

        if (creator != null) {
            lblZoneInfo.setText(creator.getInfo());
            lblCreatorTool.setText(creator.getTool());
            lblCreatorRzone.setText(creator.getAttribute(CmsisConstants.RZONE));
            lblCreatorAzone.setText(creator.getAttribute(CmsisConstants.AZONE));
        }

    }
}
