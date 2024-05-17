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

package com.arm.cmsis.pack.installer.jobs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackInstaller;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpPack;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPack.PackState;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.generic.RunnableWithIntResult;
import com.arm.cmsis.pack.installer.Messages;
import com.arm.cmsis.pack.parser.ICpXmlParser;
import com.arm.cmsis.pack.parser.PdscParser;
import com.arm.cmsis.pack.utils.Utils;

/**
 * The Pack Unpacking Job. This job unzip the .pack file in the .Download folder
 * to the corresponding folder.
 */
public class CpPackUnpackJob extends CpPackJob {

    /** File path of the .pack file in .Download folder */
    protected IPath fDownloadedPackPath;

    /** Destination path of the unpacked file */
    protected IPath fDestPath;

    /** Source temporary path with consideration of subfolder */
    protected IPath fSrcTempPath;

    protected boolean fInstallRequiredPacks;
    int returnCode;

    /**
     * Constructor for unpacking a .pack or .zip file
     *
     * @param name                 the job's name
     * @param installer            the pack installer
     * @param packId               Pack ID
     * @param sourceFilePath       the .pack file's source file path
     * @param installRequiredPacks Set to true if required packs must also be
     *                             installed
     */
    public CpPackUnpackJob(String name, ICpPackInstaller installer, String packId, boolean installRequiredPacks) {
        super(name, installer, packId);
        fDownloadedPackPath = createDownloadFolder().append(packId + CmsisConstants.EXT_PACK);
        String relativeDir = CpPack.getPackRelativeInstallDir(packId);
        fDestPath = new Path(CpPlugIn.getPackManager().getCmsisPackRootDirectory()).append(relativeDir);
        this.fInstallRequiredPacks = installRequiredPacks;
    }

    @Override
    public boolean installRequiredPacks() {
        return fInstallRequiredPacks;
    }

    /**
     * Copy the .pack file to .Download folder
     *
     * @return true if the operation is successful, otherwise false
     */
    protected boolean copyToDownloadFolder(IProgressMonitor monitor) {
        return true;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        IStatus status = Status.CANCEL_STATUS;
        SubMonitor progress = SubMonitor.convert(monitor, 100);

        if (copyToDownloadFolder(progress.newChild(80))) {
            if (unzip(progress.newChild(20))) {
                status = Status.OK_STATUS;
            }
        }

        monitor.done();

        fPackInstaller.jobFinished(fJobId, RteEvent.PACK_INSTALL_JOB_FINISHED, fResult);

        return status;
    }

    private boolean unzip(IProgressMonitor monitor) {
        SubMonitor progress = SubMonitor.convert(monitor, 100);

        File sourceFile = fDownloadedPackPath.toFile();
        monitor.setTaskName(Messages.CpPackUnpackJob_Unpacking + sourceFile.toString());

        if (!sourceFile.exists()) {
            fResult.setErrorString(sourceFile.toString() + Messages.CpPackUnpackJob_SourceFileCannotBeFound);
            return false;
        }

        if (fDestPath.toFile().exists()) {
            final String messageString = NLS.bind(Messages.CpPackUnpackJob_PathAlreadyExists, fDestPath.toOSString());
            Display.getDefault().syncExec(() -> {
                final MessageDialog dialog = new MessageDialog(Display.getDefault().getActiveShell(),
                        Messages.CpPackUnpackJob_OverwriteQuery, null, messageString, MessageDialog.QUESTION,
                        new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL,
                                IDialogConstants.CANCEL_LABEL },
                        0);
                returnCode = dialog.open();
            });

            if (returnCode == IDialogConstants.OK_ID) {
                Utils.deleteFolderRecursive(fDestPath.toFile());
            } else {
                fResult.setErrorString(Messages.CpPackJob_CancelledByUser);
                return false;
            }
        }

        File tempFolder = null;
        try {
            // unzip pack to a temporary folder
            tempFolder = Files.createTempDirectory(CmsisConstants.CMSIS).toFile();
            IPath tempDestPath = new Path(tempFolder.getAbsolutePath());

            if (!fPackInstaller.unzip(sourceFile, tempDestPath, progress.newChild(95))) {
                fResult.setErrorString(Messages.CpPackJob_CancelledByUser);
                return false;
            }
            // load pack and check license if any
            ICpPack pack = parsePdscFile(tempFolder);
            if (pack == null) {
                return false; // result is already set
            }
            progress.worked(1);
            if (!checkLicense(pack, progress)) {
                return false;
            }
            // copy pack from temporary directory to the destination
            tempFolder = fSrcTempPath.toFile();
            Utils.copyDirectory(tempFolder, fDestPath.toFile());

            // copy pdsc to download directory
            IPath downloadPath = new Path(CpPlugIn.getPackManager().getCmsisPackDownloadDir());
            String pdscFile = pack.getFileName();
            Utils.copy(new File(pdscFile), downloadPath.append(pack.getId() + CmsisConstants.EXT_PDSC).toFile());
            if (isLocalPack(pack)) {
                copyToLocal(pack);
            }

            // convert pack to installed
            pack.setPackState(PackState.INSTALLED);
            pdscFile = fDestPath.append(Utils.extractFileName(pdscFile)).toString();
            pack.setFileName(pdscFile);
            // set successful result
            fResult.setPack(pack);
            fResult.setSuccess(true);
        } catch (IOException e) {
            fResult.setErrorString(Messages.CpPackUnpackJob_FailedToUnzipFile + sourceFile.toString());
            Utils.deleteFolderRecursive(fDestPath.toFile());
            return false;
        } finally {
            if (tempFolder != null)
                Utils.deleteFolderRecursive(tempFolder);
        }
        return true;
    }

    /**
     * Finds and parses pack's pdsc file
     *
     * @param tempFolder
     * @return parsed ICpPack or null if not found or an error occurred
     */
    protected ICpPack parsePdscFile(File tempFolder) {
        // get pdsc file from temporary folder
        Collection<String> pdscFiles = new ArrayList<>();
        Utils.findPdscFiles(tempFolder, pdscFiles, 1);
        if (pdscFiles.isEmpty()) {
            fResult.setErrorString(Messages.CpPackUnpackJob_PdscFileNotFoundInPack + fDownloadedPackPath.toOSString());
            return null;
        }
        if (pdscFiles.size() > 1) {
            fResult.setErrorString(
                    Messages.CpPackUnpackJob_PdscFileMoreThanOneFoundInPack + fDownloadedPackPath.toOSString());
            return null;
        }

        // load pack
        String pdscFile = pdscFiles.iterator().next();
        fSrcTempPath = new Path(Utils.extractPath(pdscFile, true));
        ICpXmlParser parser = new PdscParser();
        ICpPack pack = (ICpPack) parser.parseFile(pdscFile);
        if (pack == null) {
            StringBuilder sb = new StringBuilder(
                    Messages.CpPackUnpackJob_FailToParsePdscFile + Utils.extractFileName(pdscFile));
            for (String es : parser.getErrorStrings()) {
                sb.append(System.lineSeparator());
                sb.append(es);
            }
            fResult.setErrorString(sb.toString());
        }
        return pack;
    }

    /**
     * Checks if pack has license and user accepted it
     *
     * @param pack     ICpPack
     * @param progress
     * @return true if user accepted license or pack has no license
     * @throws IOException
     */
    protected boolean checkLicense(ICpPack pack, IProgressMonitor progress) {
        ICpItem licItem = pack.getFirstChild(CmsisConstants.LICENSE_TAG);
        if (licItem == null)
            return true; // no license item

        String licFile = licItem.getText();
        if (licFile == null || licFile.isEmpty())
            return true; // no license file

        String absolutePath = pack.getAbsolutePath(licFile);
        String packName = pack.getId();
        String text;
        progress.setTaskName(Messages.PackInstallerUtils_PleaseAgreeLicenseAgreement);
        try {
            byte[] allBytes = Files.readAllBytes(Paths.get(absolutePath));
            String fileExt = Utils.extractFileExtension(absolutePath);

            if ("rtf".equalsIgnoreCase(fileExt)) { //$NON-NLS-1$
                RTFEditorKit rtfParser = new RTFEditorKit();
                Document document = rtfParser.createDefaultDocument();
                rtfParser.read(new ByteArrayInputStream(allBytes), document, 0);
                text = document.getText(0, document.getLength());
            } else {
                text = new String(allBytes, Charset.defaultCharset());
            }
        } catch (BadLocationException | IOException e) {
            e.printStackTrace();
            // ignore that, the user should not be affected if license file does not exist
            // or malformed
            return true;
        }
        if (licenseQuestion(packName, text))
            return true;
        return false;
    }

    protected boolean licenseQuestion(String packname, String licenseText) {
        RunnableWithIntResult runnable = new RunnableWithIntResult() {
            @Override
            public void run() {
                LicenseDialog dlg = new LicenseDialog(null, packname, licenseText);
                result = dlg.open();
            }
        };
        Display.getDefault().syncExec(runnable);
        return runnable.getResult() == Window.OK;
    }

    protected IPath createDownloadFolder() {
        IPath downloadDir = new Path(CpPlugIn.getPackManager().getCmsisPackDownloadDir());
        if (!downloadDir.toFile().exists()) {
            downloadDir.toFile().mkdir();
        }
        return downloadDir;
    }
}
