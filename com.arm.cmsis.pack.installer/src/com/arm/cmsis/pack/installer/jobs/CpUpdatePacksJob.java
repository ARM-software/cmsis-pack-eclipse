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

package com.arm.cmsis.pack.installer.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.arm.cmsis.pack.ICpPackInstaller;
import com.arm.cmsis.pack.repository.RtePackJobResult;

/**
 * Job to update pack index and available packs
 *
 */
public class CpUpdatePacksJob extends CpPackJob {

    public CpUpdatePacksJob(String name, ICpPackInstaller packInstaller, String jobId) {
        super(name, packInstaller, jobId);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        fPackInstaller.updatePacks(monitor);
        RtePackJobResult jobResult = new RtePackJobResult(null);
        jobResult.setSuccess(true);
        fPackInstaller.jobFinished(fJobId, null, jobResult);
        return Status.OK_STATUS;
    }

}
