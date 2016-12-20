/*******************************************************************************
 * Copyright (c) 2016 ARM Ltd. and others
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
import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackInstaller;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpPack;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPack.PackState;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.installer.Messages;
import com.arm.cmsis.pack.parser.ICpXmlParser;
import com.arm.cmsis.pack.utils.Utils;

/**
 * The Pack Unpacking Job. This job unzip the .pack file in the
 * .Download folder to the corresponding folder.
 */
public class CpPackUnpackJob extends CpPackJob {

	/** File path of the .pack file in .Download folder*/
	protected IPath fSourceFilePath;

	/** Destination path of the unpacked file*/
	protected IPath fDestPath;

	protected boolean fInstallRequiredPacks;
	int returnCode;

	/**
	 * Constructor for unpacking a .pack or .zip file
	 * @param name the job's name
	 * @param installer the pack installer
	 * @param packId Pack ID
	 * @param sourceFilePath the .pack file's source file path
	 * @param installRequiredPacks Set to true if required packs must also be installed
	 */
	public CpPackUnpackJob(String name, ICpPackInstaller installer, String packId, boolean installRequiredPacks) {
		super(name, installer, packId);
		fSourceFilePath = createDownloadFolder().append(packId + CmsisConstants.EXT_PACK);
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

		File sourceFile = fSourceFilePath.toFile();
		monitor.setTaskName(Messages.CpPackUnpackJob_Unpacking + sourceFile.toString());

		if (!sourceFile.exists()) {
			fResult.setErrorString(sourceFile.toString() + Messages.CpPackUnpackJob_SourceFileCannotBeFound);
			return true;
		}

		if (fDestPath.toFile().exists()) {
			final String messageString = NLS.bind(Messages.CpPackUnpackJob_PathAlreadyExists, fDestPath.toOSString());
			Display.getDefault().syncExec(() -> {
				final MessageDialog dialog = new MessageDialog(Display.getDefault().getActiveShell(),
						Messages.CpPackUnpackJob_OverwriteQuery, null, messageString, MessageDialog.QUESTION,
						new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL,
								IDialogConstants.CANCEL_LABEL }, 0);
				returnCode = dialog.open();
			});

			if (returnCode == IDialogConstants.OK_ID) {
				Utils.deleteFolderRecursive(fDestPath.toFile());
			} else {
				fResult.setErrorString(Messages.CpPackJob_CancelledByUser);
				return false;
			}
		}

		try {
			if (!fPackInstaller.unzip(sourceFile, fDestPath, progress.newChild(95))) {
				fResult.setErrorString(Messages.CpPackJob_CancelledByUser);
				Utils.deleteFolderRecursive(fDestPath.toFile());
				return false;
			}

			Collection<String> pdscFiles = new LinkedList<>();
			Utils.findPdscFiles(fDestPath.toFile(), pdscFiles, 1);
			if (pdscFiles.isEmpty()) {
				Utils.deleteFolderRecursive(fDestPath.toFile());
				fResult.setErrorString(Messages.CpPackUnpackJob_PdscFileNotFoundInFolder
						+ fDestPath.toOSString());
				return true;
			}

			String pdscFile = pdscFiles.iterator().next();
			ICpXmlParser parser = CpPlugIn.getPackManager().getParser();
			ICpPack pack = (ICpPack) parser.parseFile(pdscFile);
			if (pack != null) {
				pack.setPackState(PackState.INSTALLED);
				fResult.setPack(pack);
				fResult.setSuccess(true);
				IPath downloadPath = new Path(CpPlugIn.getPackManager().getCmsisPackDownloadDir());
				Utils.copy(new File(pdscFile), downloadPath.append(pack.getId() + CmsisConstants.EXT_PDSC).toFile());
				return true;
			}
			Utils.deleteFolderRecursive(fDestPath.toFile());
			StringBuilder sb = new StringBuilder(Messages.CpPackUnpackJob_FailToParsePdscFile + pdscFile);
			for (String es : parser.getErrorStrings()) {
				sb.append(System.lineSeparator());
				sb.append(es);
			}
			fResult.setErrorString(sb.toString());
			return true;
		} catch (IOException e) {
			fResult.setErrorString(Messages.CpPackUnpackJob_FailedToUnzipFile + sourceFile.toString());
			Utils.deleteFolderRecursive(fDestPath.toFile());
			return true;
		}
	}

	protected IPath createDownloadFolder() {
		IPath downloadDir = new Path(CpPlugIn.getPackManager().getCmsisPackDownloadDir());
		if (!downloadDir.toFile().exists()) {
			downloadDir.toFile().mkdir();
		}
		return downloadDir;
	}
}
