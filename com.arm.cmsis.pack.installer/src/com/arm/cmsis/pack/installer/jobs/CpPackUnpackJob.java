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
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPack.PackState;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.installer.Messages;
import com.arm.cmsis.pack.installer.utils.PackInstallerUtils;
import com.arm.cmsis.pack.parser.ICpXmlParser;
import com.arm.cmsis.pack.utils.Utils;

/**
 * The Pack Unpacking Job. This job unzip the .pack file in the
 * .Download folder to the corresponding folder.
 */
public class CpPackUnpackJob extends CpPackJob {

	private ICpPack fPack;
	private String fPackId;
	private String fSourceFilePath;
	private IPath fDestPath;
	int returnCode;

	/**
	 * Constructor for unpacking a .pack file from the .Download folder
	 * @param name the job's name
	 * @param installer the pack installer
	 * @param pack the pack to unpack
	 */
	public CpPackUnpackJob(String name, ICpPackInstaller installer, ICpPack pack) {
		super(name, installer, pack.getId());
		fPack = pack;
		fPackId = fPack.getId();
		fSourceFilePath = new Path(PackInstallerUtils.getPacksDownloadDir())
				.append(fPackId + CmsisConstants.EXT_PACK).toOSString();
		String relativeDir = PackInstallerUtils.getPackRelativeInstallDir(fPackId);
		fDestPath = new Path(CpPlugIn.getPackManager().getCmsisPackRootDirectory())
				.append(relativeDir);
	}

	/**
	 * Constructor for unpacking a user-defined .pack or .zip file
	 * @param name the job's name
	 * @param installer the pack installer
	 * @param sourceFilePath the .pack file's source file path
	 */
	public CpPackUnpackJob(String name, ICpPackInstaller installer, String sourceFilePath) {
		super(name, installer, Utils.extractBaseFileName(sourceFilePath));
		fPackId = Utils.extractBaseFileName(sourceFilePath);
		fSourceFilePath = sourceFilePath;
		String baseFileName = Utils.extractBaseFileName(fSourceFilePath);
		String relativeDir = PackInstallerUtils.getPackRelativeInstallDir(baseFileName);
		fDestPath = new Path(CpPlugIn.getPackManager().getCmsisPackRootDirectory())
				.append(relativeDir);
		fResult.setSourceFilePath(sourceFilePath);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		IStatus status = Status.CANCEL_STATUS;

		if (myRun(monitor)) {
			status = Status.OK_STATUS;
		}

		monitor.done();

		fPackInstaller.jobFinished(fPackId, RteEvent.PACK_UNPACK_JOB_FINISHED, fResult);

		return status;
	}

	private boolean myRun(IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, 100);

		File sourceFile = new File(fSourceFilePath);
		monitor.setTaskName(Messages.CpPackUnpackJob_Unpacking + sourceFile.toString());

		if (fDestPath.toFile().exists()) {
			final String messageString = NLS.bind(Messages.CpPackUnpackJob_PathAlreadyExists, fDestPath.toOSString());
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					final MessageDialog dialog = new MessageDialog(Display.getDefault().getActiveShell(),
							Messages.CpPackUnpackJob_OverwriteQuery, null, messageString, MessageDialog.QUESTION,
							new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL,
									IDialogConstants.CANCEL_LABEL }, 0);
					returnCode = dialog.open();
				}
			});

			if (returnCode == IDialogConstants.OK_ID) {
				Utils.deleteFolderRecursive(fDestPath.toFile());
			} else {
				fResult.setSuccess(false);
				fResult.setErrorString(Messages.CpPackJob_CancelledByUser);
				return false;
			}
		}

		if (!sourceFile.exists()) {
			fResult.setSuccess(false);
			fResult.setErrorString(sourceFile.toString() + Messages.CpPackUnpackJob_SourceFileCannotBeFound);
			return true;
		}

		try {
			if (!fPackInstaller.unzip(sourceFile, fDestPath, progress.newChild(95))) {
				fResult.setSuccess(false);
				fResult.setErrorString(Messages.CpPackJob_CancelledByUser);
				Utils.deleteFolderRecursive(fDestPath.toFile());
				return false;
			}
			if (fPack != null) {	// unpack job
				fPack.setPackState(PackState.INSTALLED);
				fPack.setFileName(fDestPath.append(fPack.getPackFamilyId() + CmsisConstants.EXT_PDSC).toString());
				fResult.setPack(fPack);
				fResult.setSuccess(true);
				return true;
			}
			Collection<String> files = new LinkedList<>();
			Utils.findPdscFiles(fDestPath.toFile(), files, 1);
			if (files.isEmpty()) {
				Utils.deleteFolderRecursive(fDestPath.toFile());
				fResult.setSuccess(false);
				fResult.setErrorString(Messages.CpPackUnpackJob_PdscFileNotFoundInFolder
						+ fDestPath.toOSString());
				return true;
			}

			String file = files.iterator().next();
			ICpXmlParser parser = CpPlugIn.getPackManager().getParser();
			fPack = (ICpPack) parser.parseFile(file);
			if (fPack != null) {
				ICpItem urlItem = fPack.getFirstChild(CmsisConstants.URL);
				if (urlItem == null || !Utils.isValidURL(urlItem.getText())) {
					fPack.setPackState(PackState.GENERATED);
				} else {
					fPack.setPackState(PackState.INSTALLED);
				}
				fResult.setPack(fPack);
				fResult.setSuccess(true);
				return true;
			}
			Utils.deleteFolderRecursive(fDestPath.toFile());
			StringBuilder sb = new StringBuilder(Messages.CpPackUnpackJob_FailToParsePdscFile + file);
			for (String es : parser.getErrorStrings()) {
				sb.append(System.lineSeparator());
				sb.append(es);
			}
			fResult.setErrorString(sb.toString());
			fResult.setSuccess(false);
			return true;
		} catch (IOException e) {
			fResult.setSuccess(false);
			fResult.setErrorString(Messages.CpPackUnpackJob_FailedToUnzipFile + sourceFile.toString());
			Utils.deleteFolderRecursive(fDestPath.toFile());
			return true;
		}
	}

}
