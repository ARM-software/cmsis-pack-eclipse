/*******************************************************************************
 * Copyright (c) 2022 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/

package com.arm.cmsis.pack.installer.ui.views;

import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackManager;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.installer.ui.IHelpContextIds;
import com.arm.cmsis.pack.installer.ui.Messages;
import com.arm.cmsis.pack.item.CmsisMapItem;
import com.arm.cmsis.pack.item.ICmsisMapItem;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;
import com.arm.cmsis.pack.ui.tree.AdvisedCellLabelProvider;
import com.arm.cmsis.pack.ui.tree.DeviceViewContentProvider;
import com.arm.cmsis.pack.ui.tree.DevicesViewColumnAdvisor;
import com.arm.cmsis.pack.ui.tree.DevicesViewLabelProvider;
import com.arm.cmsis.pack.ui.tree.TreeColumnComparator;

/**
 * Default implementation of the devices view in pack manager
 */
public class DevicesView extends PackInstallerView {

    public static final String ID = "com.arm.cmsis.pack.installer.ui.views.DevicesView"; //$NON-NLS-1$

    public DevicesView() {
    }

    @Override
    public boolean isFilterSource() {
        return true;
    }

    @Override
    protected String getHelpContextId() {
        return IHelpContextIds.DEVICES_VIEW;
    }

    @Override
    public void createTreeColumns() {
        fTree.setInitialText(Messages.DevicesView_SearchDevice);

        TreeViewerColumn column0 = new TreeViewerColumn(fViewer, SWT.LEFT);
        column0.getColumn().setText(CmsisConstants.DEVICE_TITLE);
        column0.getColumn().setWidth(200);
        column0.setLabelProvider(new DevicesViewLabelProvider());

        TreeViewerColumn column1 = new TreeViewerColumn(fViewer, SWT.LEFT);
        column1.getColumn().setText(CmsisConstants.SUMMARY_TITLE);
        column1.getColumn().setWidth(300);
        DevicesViewColumnAdvisor columnAdvisor = new DevicesViewColumnAdvisor(fViewer);
        column1.setLabelProvider(new AdvisedCellLabelProvider(columnAdvisor, COLURL));

        fViewer.setContentProvider(new DeviceViewContentProvider());
        fViewer.setComparator(new TreeColumnComparator(fViewer, columnAdvisor, 0));
        fViewer.setAutoExpandLevel(2);
    }

    @Override
    protected void refresh() {
        if (CpPlugIn.getDefault() == null) {
            return;
        }
        ICpPackManager packManager = CpPlugIn.getPackManager();
        if (packManager != null) {
            ICmsisMapItem<IRteDeviceItem> root = new CmsisMapItem<>();
            IRteDeviceItem allDevices = packManager.getDevices();
            root.addChild(allDevices);
            if (!fViewer.getControl().isDisposed()) {
                fViewer.setInput(root);
            }
        } else {
            if (!fViewer.getControl().isDisposed()) {
                fViewer.setInput(null);
            }
        }
    }
}
