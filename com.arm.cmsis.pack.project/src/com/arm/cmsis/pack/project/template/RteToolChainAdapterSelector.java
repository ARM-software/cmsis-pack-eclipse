/*******************************************************************************
* Copyright (c) 2021 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/
package com.arm.cmsis.pack.project.template;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.arm.cmsis.pack.build.settings.RteToolChainAdapterFactory;
import com.arm.cmsis.pack.build.settings.RteToolChainAdapterInfo;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.project.Messages;
import com.arm.cmsis.pack.ui.IStatusMessageListener;
import com.arm.cmsis.pack.ui.StatusMessageListerenList;

public class RteToolChainAdapterSelector extends Group {
    private Combo comboAdapter = null;
    private Combo comboFamily = null;
    private Label lblDescription = null;
    private IToolChain toolChain = null;
    String selectedFamily = null;

    private boolean bUpdatingControls = false;

    private Collection<RteToolChainAdapterInfo> adapters = null;

    private StatusMessageListerenList listeners = new StatusMessageListerenList();

    private static final String[] families = new String[] { "ARMCC", "GCC", "G++", "IAR", "GHS", "Tasking", "Cosmic" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
    private Label lblToolchainName;

    /**
     * Create the composite.
     *
     * @param parent
     * @param style
     */
    public RteToolChainAdapterSelector(Composite parent, int style) {
        super(parent, style);
        setText(Messages.RteTooolChainAdapterSelector_ToolchainAdapter);
        setLayout(new GridLayout(3, false));

        Label lblToolchainLabel = new Label(this, SWT.NONE);
        lblToolchainLabel.setText(Messages.RteTooolChainAdapterSelector_Toolchain);

        lblToolchainName = new Label(this, SWT.NONE);
        lblToolchainName.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        lblToolchainName.setText(""); //$NON-NLS-1$

        Label lbAdapter = new Label(this, SWT.NONE);
        lbAdapter.setToolTipText(Messages.RteTooolChainAdapterSelector_ToolChainAdapterIsReponsibleFor);
        lbAdapter.setText(Messages.RteTooolChainAdapterSelector_Adapter);

        comboAdapter = new Combo(this, SWT.READ_ONLY);
        comboAdapter.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateControls();
            }
        });
        comboAdapter.setToolTipText(Messages.RteTooolChainAdapterSelector_ToolChainAdapterIsReponsibleFor);
        comboAdapter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        Label lblFamily = new Label(this, SWT.NONE);
        lblFamily.setToolTipText(Messages.RteTooolChainAdapterSelector_FamilyToolTipAndDescription);
        lblFamily.setText(Messages.RteTooolChainAdapterSelector_Family);

        comboFamily = new Combo(this, SWT.READ_ONLY);
        comboFamily.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                selectedFamily = getTcompiler();
                if (selectedFamily == null || selectedFamily.isEmpty())
                    updateStatus(Messages.RteTooolChainAdapterSelector_SelectFamily);
                else
                    updateStatus(null);

            }
        });
        comboFamily.setToolTipText(Messages.RteTooolChainAdapterSelector_FamilyToolTipAndDescription);
        comboFamily.setItems(families);
        comboFamily.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        comboFamily.setToolTipText(Messages.RteTooolChainAdapterSelector_FamilyToolTipAndDescription);

        Label lblTcompiler = new Label(this, SWT.NONE);
        lblTcompiler.setText(Messages.RteTooolChainAdapterSelector_PassedviaTCompiler);

        lblDescription = new Label(this, SWT.WRAP);
        GridData gd_lblDescription = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
        gd_lblDescription.widthHint = 370;
        gd_lblDescription.heightHint = 252;
        lblDescription.setLayoutData(gd_lblDescription);

    }

    public void setToolChain(IToolChain toolchain) {
        this.toolChain = toolchain;
        init();
    }

    void updateStatus(String message) {
        // notify listeners
        listeners.notifyListeners(message);
    }

    public void addListener(IStatusMessageListener listener) {
        listeners.addListener(listener);
    }

    public void removeListener(IStatusMessageListener listener) {
        listeners.removeListener(listener);
    }

    public void init() {
        bUpdatingControls = true;
        comboAdapter.removeAll();
        RteToolChainAdapterFactory factory = RteToolChainAdapterFactory.getInstance();
        String toolChainName = CmsisConstants.EMPTY_STRING;

        if (toolChain != null) {
            adapters = factory.getAdapterInfos(toolChain);
            toolChainName = toolChain.getName();
            if (adapters.isEmpty()) {
                adapters = factory.getAdapterInfos();
                toolChainName += " (" + toolChain.getId() + ")"; //$NON-NLS-1$//$NON-NLS-2$
            }
        }

        lblToolchainName.setText(toolChainName);

        for (RteToolChainAdapterInfo info : adapters) {
            comboAdapter.add(info.getName());
        }
        if (!adapters.isEmpty())
            comboAdapter.select(0); // best adapter is always on top

        bUpdatingControls = false;
        updateControls();
    }

    protected void updateControls() {
        if (bUpdatingControls)
            return;
        bUpdatingControls = true;
        String description = null;
        String family = null;
        RteToolChainAdapterInfo info = getSelectedAdapterInfo();
        if (info != null) {
            description = info.getDescription();
            family = info.getFamily();
            if (family == null || family.isEmpty()) {
                comboFamily.setEnabled(true);
                family = discoverFamily();
            } else {
                comboFamily.setEnabled(false);
            }
            updateStatus(null);
        } else {
            description = Messages.RteTooolChainAdapterSelector_NoToolchainAdapterAvailable;
            updateStatus(description);
        }
        if (family == null)
            family = selectedFamily;
        selectedFamily = family;

        int familyIndex = -1;
        if (selectedFamily != null)
            familyIndex = comboFamily.indexOf(selectedFamily);

        if (familyIndex >= 0) {
            comboFamily.select(familyIndex);
        } else {
            updateStatus(Messages.RteTooolChainAdapterSelector_SelectFamily);
        }

        lblDescription.setText(description);
        bUpdatingControls = false;
    }

    private String discoverFamily() {
        if (toolChain != null) {
            String tsId = toolChain.getId().toLowerCase();
            if (tsId.indexOf("gcc") >= 0 || tsId.indexOf("g++") >= 0 || tsId.indexOf("gnu") >= 0) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                return "GCC"; //$NON-NLS-1$
            else if (tsId.indexOf("arm") >= 0) //$NON-NLS-1$
                return "ARM"; //$NON-NLS-1$
        }
        return null;
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    public RteToolChainAdapterInfo getSelectedAdapterInfo() {
        int index = comboAdapter.getSelectionIndex();
        if (index < 0)
            return null;

        Iterator<RteToolChainAdapterInfo> iterator = adapters.iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            RteToolChainAdapterInfo info = iterator.next();
            if (i == index)
                return info;
        }
        return null;
    }

    public String getAdapterId() {
        RteToolChainAdapterInfo info = getSelectedAdapterInfo();
        if (info != null)
            return info.getId();
        return null;
    }

    public String getTcompiler() {
        int index = comboFamily.getSelectionIndex();
        if (index >= 0 && index < families.length)
            return (families[index]);
        return null;
    }

}
