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

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;

import com.arm.cmsis.pack.ICpPackInstaller;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.installer.CpPackInstaller;
import com.arm.cmsis.pack.installer.Messages;

/**
 * Job of installing a pack from Internet
 */
public class CpPackInstallJob extends CpPackUnpackJob {

    protected String fPackDestFile;
    protected String fPackUrl;
    boolean wait;

    /**
     * Constructor for install pack
     *
     * @param name          name of the job
     * @param packInstaller the pack installer for callback
     * @param packId        pack's ID
     * @param url           pack's download URL
     */
    public CpPackInstallJob(String name, ICpPackInstaller packInstaller, String packId, String url,
            boolean installRequiredPacks) {
        super(name, packInstaller, packId, installRequiredPacks);
        fPackDestFile = packId + CmsisConstants.EXT_PACK;
        fDownloadedPackPath = createDownloadFolder().append(fPackDestFile);
        fPackUrl = url;
    }

    @Override
    protected boolean copyToDownloadFolder(IProgressMonitor monitor) {
        while (true) {
            try {
                File downloadFile = fPackInstaller.getRepoServiceProvider().getPackFile(fPackUrl, fPackDestFile,
                        monitor);
                if (downloadFile != null && downloadFile.exists()) {
                    return true;
                }
                return false;

            } catch (MalformedURLException e) {
                fResult.setErrorString(Messages.CpPackInstallJob_MalformedURL + fPackUrl);
                return false;
            } catch (UnknownHostException e) {
                fResult.setErrorString(NLS.bind(Messages.CpPackInstallJob_UnknownHostException, e.getMessage()));
                return false;
            } catch (SocketTimeoutException e) {
                Display.getDefault()
                        .syncExec(() -> wait = MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
                                Messages.CpPackInstaller_Timout,
                                NLS.bind(Messages.CpPackInstallJob_InstallTimeoutMessage, fPackUrl,
                                        CpPackInstaller.TIME_OUT / 1000)));
                if (!wait) {
                    fResult.setErrorString(NLS.bind(Messages.CpPackInstallJob_TimeoutConsoleMessage, fJobId, fPackUrl));
                    return false;
                }
                continue;
            } catch (InterruptedIOException e) {
                fResult.setErrorString(e.getMessage());
                return false;
            } catch (IOException e) {
                fResult.setErrorString(NLS.bind(Messages.CpPackInstallJob_FileNotFound, fPackUrl));
                return false;
            }
        }
    }

}
