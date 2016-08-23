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
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Collection;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackInstaller;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPack.PackState;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.installer.Messages;
import com.arm.cmsis.pack.installer.utils.PackInstallerUtils;
import com.arm.cmsis.pack.utils.Utils;

/**
 * Install Job
 */
public class CpPackInstallJob extends CpPackJob {

	private String fPackId;
	private String fPackDestFile;
	String fPackUrl;
	boolean wait;


	/**
	 * Constructor for install job
	 * @param name
	 * @param packInstaller
	 * @param packId
	 * @param url
	 */
	public CpPackInstallJob(String name, ICpPackInstaller packInstaller, String packId, String url) {
		super(name, packInstaller, packId);
		fPackId = packId;
		fPackDestFile = fPackId + CmsisConstants.EXT_PACK;
		fPackUrl = Utils.addTrailingSlash(url) + fPackDestFile;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		progress.beginTask(NLS.bind(Messages.CpPackInstallJob_InstallingPack, fPackId), 100);

		boolean tryAgain = true;
		while (tryAgain) {
			try {
				File downloadFile = fPackInstaller.getRepoServiceProvider().getPackFile(fPackUrl, fPackDestFile, progress.newChild(90));
				if (downloadFile != null) {
					ICpPack pack = unzipAndParse(downloadFile, progress.newChild(10));

					if (pack != null) {
						fResult.setPack(pack);
						fResult.setSuccess(true);
					} else {
						downloadFile.delete();
					}
				}
				tryAgain = false;

			} catch (MalformedURLException e) {
				fResult.setErrorString(Messages.CpPackInstallJob_MalformedURL + fPackUrl);
				return Status.CANCEL_STATUS;
			} catch (SocketTimeoutException | UnknownHostException e) {
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						wait = MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
								Messages.CpPackInstaller_Timout,
								NLS.bind(Messages.CpPackInstaller_TimeoutMessage, fPackUrl));
					}
				});
				if (!wait) {
					fResult.setErrorString(NLS.bind(Messages.CpPackInstallJob_TimeoutConsoleMessage, fPackId, fPackUrl));
					return Status.CANCEL_STATUS;
				}
				continue;
			} catch (IOException e) {
				fResult.setErrorString(NLS.bind(Messages.CpPackInstallJob_FileNotFound, fPackUrl));
				return Status.CANCEL_STATUS;
			} catch (Exception e) {
				fResult.setErrorString(e.getMessage());
				return Status.CANCEL_STATUS;
			} finally {
				fPackInstaller.jobFinished(fPackId, RteEvent.PACK_INSTALL_JOB_FINISHED, fResult);
			}
		}

		return Status.OK_STATUS;
	}

	private ICpPack unzipAndParse(File downloadFile, IProgressMonitor monitor) throws IOException {

		monitor.subTask(Messages.CpPackInstallJob_UnzippingAndParsing + fPackDestFile);

		String relativePath = PackInstallerUtils.getPackRelativeInstallDir(fPackId);
		IPath destPath = new Path(CpPlugIn.getPackManager().getCmsisPackRootDirectory()).append(relativePath);

		boolean unzipSuccess = fPackInstaller.unzip(downloadFile, destPath, monitor);

		if (monitor.isCanceled() || !unzipSuccess) {
			fResult.setErrorString(Messages.CpPackJob_CancelledByUser);
			Utils.deleteFolderRecursive(destPath.toFile());
			return null;
		}

		if (unzipSuccess) {
			Collection<String> pdscFileNames = Utils.findPdscFiles(destPath.toFile(), null, 0);
			if (pdscFileNames.isEmpty()) {
				fResult.setErrorString(Messages.CpPackInstallJob_CannotFindPdscFile);
			} else {
				String pdscFileName = pdscFileNames.iterator().next();
				IPath downloadDir = new Path(PackInstallerUtils.getPacksDownloadDir());
				String destFileName = downloadDir.append(fPackId + CmsisConstants.EXT_PDSC).toOSString();
				// Copy the pdscFileName to .Download folder
				Utils.copy(new File(pdscFileName), new File(destFileName));

				ICpPack pack = (ICpPack) CpPlugIn.getPackManager().getParser().parseFile(pdscFileName);
				pack.setPackState(PackState.INSTALLED);
				return pack;
			}
		}

		monitor.done();

		return null;

	}

}
