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
package com.arm.cmsis.pack.project.template;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackInstaller;
import com.arm.cmsis.pack.ICpPackManager;
import com.arm.cmsis.pack.info.ICpBoardInfo;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.project.Messages;

/**
 * @author edriouk
 *
 */
public class InstallDeviceAndBoardPacks extends ProcessRunner {

    @Override
    public void process(TemplateCore template, ProcessArgument[] args, String processId, IProgressMonitor monitor)
            throws ProcessFailureException {
        String projectName = args[0].getSimpleValue();

        String msg = Messages.CreateRteProject_ErrorCreatingRteProject;
        ICpPackManager packManager = CpPlugIn.getPackManager();
        ICpPackInstaller packInstaller = packManager != null ? packManager.getPackInstaller() : null;
        if (packInstaller == null) {
            msg += Messages.CmsisHeadlessBuilder_CmsisPackManagerNotAvailable;
            msg += projectName;
            throw new ProcessFailureException(getProcessMessage(processId, IStatus.ERROR, msg));
        }
        String cmsisPackRoot = packManager.getCmsisPackRootDirectory();
        if (cmsisPackRoot == null || cmsisPackRoot.isEmpty()) {
            msg += Messages.CmsisHeadlessBuilder_NoCmsisPackRoot;
            throw new ProcessFailureException(getProcessMessage(processId, IStatus.ERROR, msg));
        }

        // Get device and board infos to create obtain packs
        ICpDeviceInfo deviceInfo = RteProjectTemplate.getSelectedDeviceInfo();
        Set<String> requiredPacks = new HashSet<>();
        if (deviceInfo != null) {
            requiredPacks.add(deviceInfo.getPackId());
        }
        ICpBoardInfo boardInfo = RteProjectTemplate.getSelectedBoardInfo();
        if (boardInfo != null) {
            requiredPacks.add(boardInfo.getPackId());
        }
        packInstaller.installPacks(requiredPacks);
    }
}
