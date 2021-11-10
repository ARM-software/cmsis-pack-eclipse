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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackInstaller;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpPack;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPack.PackState;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.installer.Messages;
import com.arm.cmsis.pack.installer.utils.PackInstallerUtils;
import com.arm.cmsis.pack.utils.Utils;

/**
 * The Pack Removing Job. This job deletes the pack's folder and deletes the
 * .pack file in the .Download folder if necessary.
 */
public class CpPackRemoveJob extends CpPackJob {

    protected ICpPack fPack;
    protected boolean fDelete;

    /**
     * @param name      The Job's name
     * @param installer The pack installer
     * @param pack      the pack to remove
     * @param delete    true if the .pack file in the .Download folder should also
     *                  be deleted
     */
    public CpPackRemoveJob(String name, ICpPackInstaller installer, ICpPack pack, boolean delete) {
        super(name, installer, pack.getId());
        fPack = pack;
        fDelete = delete;
    }

    private IStatus deleteErrorPack(IProgressMonitor monitor) {
        if (fPack == null) {
            return Status.OK_STATUS;
        }

        fJobId = fPack.getFileName();
        String path = fPack.getFileName();
        path = Utils.extractPath(path, false);

        if (path.lastIndexOf(CmsisConstants.DOT_DOWNLOAD) != -1 || path.lastIndexOf(CmsisConstants.DOT_LOCAL) != -1) {
            IPath filePath = new Path(fPack.getFileName());
            File file = filePath.toFile();
            if (file.exists()) {
                file.delete();
            }
        } else {
            IPath installedDir = new Path(fPack.getFileName());
            SubMonitor progress = SubMonitor.convert(monitor, Utils.countFiles(installedDir.toFile()));
            progress.setTaskName(NLS.bind(Messages.CpPackRemoveJob_DeletingFilesFromFolder, installedDir.toOSString()));
            PackInstallerUtils.deleteFolderRecursiveWithProgress(installedDir.toFile(), progress);
            progress.done();
        }

        fResult.setPack(fPack);
        fResult.setSuccess(true);
        fPackInstaller.jobFinished(fJobId, RteEvent.PACK_DELETE_JOB_FINISHED, fResult);

        return Status.OK_STATUS;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        if (fPack == null) {
            return Status.OK_STATUS;
        }

        // handle 'Delete' and 'Delete All' items from ERRORS group
        if (fDelete && fPack.getPackState() == PackState.ERROR) {
            return deleteErrorPack(monitor);
        }

        IPath installedDir;
        if (fPack.getPackState() == PackState.ERROR) {
            fJobId = fPack.getTag();
            installedDir = new Path(fPack.getFileName());
        } else {
            fJobId = fPack.getId();
            installedDir = new Path(fPack.getInstallDir(CpPlugIn.getPackManager().getCmsisPackRootDirectory()));
        }
        monitor.setTaskName(Messages.CpPackRemoveJob_RemovingPack + fJobId);

        IPath downloadPackFilePath = new Path(CpPlugIn.getPackManager().getCmsisPackDownloadDir())
                .append(fJobId + CmsisConstants.EXT_PACK);
        File downloadPackFile = downloadPackFilePath.toFile();
        IPath downloadPdscFilePath = new Path(CpPlugIn.getPackManager().getCmsisPackDownloadDir())
                .append(fJobId + CmsisConstants.EXT_PDSC);
        File downloadPdscFile = downloadPdscFilePath.toFile();

        SubMonitor progress = SubMonitor.convert(monitor, Utils.countFiles(installedDir.toFile()));
        progress.setTaskName(NLS.bind(Messages.CpPackRemoveJob_DeletingFilesFromFolder, installedDir.toOSString()));
        PackInstallerUtils.deleteFolderRecursiveWithProgress(installedDir.toFile(), progress);

        String familyId = CpPack.familyFromId(fJobId);
        IPath localPdscFilePath = new Path(CpPlugIn.getPackManager().getCmsisPackLocalDir())
                .append(familyId + CmsisConstants.EXT_PDSC);
        File localPdscFile = localPdscFilePath.toFile();

        boolean deleted = false;
        if (fDelete) {
            if (downloadPdscFile.exists()) {
                deleted = downloadPdscFile.setWritable(true, false) && downloadPdscFile.delete();
            }
            if (deleted && downloadPackFile.exists())
                deleted = downloadPackFile.setWritable(true, false) && downloadPackFile.delete();
            // delete local pdsc if no downloaded packs exist for this family id
            if (localPdscFile.exists()) {
                ICpItem family = fPack.getParent();
                if (family == null || family.getChildCount() == 1) // only one
                    localPdscFile.delete();
            }
        }
        fResult.setPack(fPack);

        // If the deleted pack is not an error pack, change the pack state
        if (fPack.getPackState() != PackState.ERROR) {
            ICpPack newPack = null;
            if (downloadPackFile.exists() && downloadPdscFile.exists()) {
                fPack.setPackState(PackState.DOWNLOADED);
                fPack.setFileName(downloadPdscFile.getAbsolutePath());
                fResult.setNewPack(fPack);
            } else {
                fPack.setPackState(PackState.AVAILABLE);
                newPack = PackInstallerUtils.loadLatestPack(fPack);
                fResult.setNewPack(newPack);
            }
        }

        fResult.setSuccess(true);

        progress.done();

        if (deleted) {
            fPackInstaller.jobFinished(fJobId, RteEvent.PACK_DELETE_JOB_FINISHED, fResult);
        } else {
            fPackInstaller.jobFinished(fJobId, RteEvent.PACK_REMOVE_JOB_FINISHED, fResult);
        }

        return Status.OK_STATUS;
    }

}
