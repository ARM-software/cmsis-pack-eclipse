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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
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
	private ICpPack fPack;

	private final IPath fDownloadDir;
	private final File fDownloadFile;
	private final File fDownloadFileTmp;

	private final static int TIME_OUT = 10000;
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

		fDownloadDir = new Path(PackInstallerUtils.getPacksDownloadDir());
		if (!fDownloadDir.toFile().exists()) {
			fDownloadDir.toFile().mkdir();
		}
		fDownloadFile = fDownloadDir.append(fPackDestFile).toFile();
		fDownloadFileTmp = fDownloadDir.append(fPackDestFile + CmsisConstants.EXT_TEMP).toFile();
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		progress.beginTask(NLS.bind(Messages.CpPackInstallJob_InstallingPack, fPackId), 100);

		boolean tryAgain = true;
		while (tryAgain) {
			try {
				URLConnection connection = getUrlConnection(progress.newChild(10));

				boolean downloadSuccess = downloadPack(progress.newChild(80), connection);
				if (downloadSuccess) {
					fPack = unzipAndParse(progress.newChild(10));

					if (fPack != null) {
						fResult.setPack(fPack);
						fResult.setSuccess(true);
					} else {
						fDownloadFile.delete();
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
			} finally {
				fPackInstaller.jobFinished(fPackId, RteEvent.PACK_INSTALL_JOB_FINISHED, fResult);
			}
		}

		return Status.OK_STATUS;
	}

	private URLConnection getUrlConnection(IProgressMonitor monitor) throws MalformedURLException, IOException {
		SubMonitor progress = SubMonitor.convert(monitor, 100);

		URLConnection connection = null;
		progress.subTask(Messages.CpPackInstallJob_ConnectingTo + fPackUrl);
		URL url = new URL(fPackUrl);
		while (true) {
			progress.worked(50);
			if (progress.isCanceled()) {
				cancel();
			}
			connection = url.openConnection();
			connection.setConnectTimeout(TIME_OUT);
			connection.setReadTimeout(TIME_OUT);
			if (connection instanceof HttpURLConnection) {
				int responseCode = ((HttpURLConnection) connection).getResponseCode();
				if (responseCode == HttpURLConnection.HTTP_OK) {
					break;
				} else if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP
						|| responseCode == HttpURLConnection.HTTP_MOVED_PERM
						|| responseCode == HttpURLConnection.HTTP_SEE_OTHER) {
					String newUrl = connection.getHeaderField(CmsisConstants.REPO_LOCATION);
					url = new URL(newUrl);
				} else {
					break;
				}
			}
		}
		progress.setWorkRemaining(0);

		return connection;
	}

	private boolean downloadPack(IProgressMonitor monitor, URLConnection connection) throws IOException {

		int totalWork = connection.getContentLength();
		if (totalWork == -1) {
			totalWork = IProgressMonitor.UNKNOWN;
		}

		SubMonitor progress = SubMonitor.convert(monitor, totalWork);
		progress.subTask(NLS.bind(Messages.CpPackInstallJob_DownloadingFrom, fPackDestFile, fPackUrl));

		InputStream input = null;
		OutputStream output = null;
		try {
			input = connection.getInputStream();

			if (fDownloadFileTmp.exists()) {
				fDownloadFileTmp.delete();
			}
			output = new FileOutputStream(fDownloadFileTmp);

			byte[] buf = new byte[1024];
			int bytesRead;
			while ((bytesRead = input.read(buf)) > 0) {
				output.write(buf, 0, bytesRead);
				progress.worked(bytesRead);
				// Check if the cancel button is pressed
				if (progress.isCanceled()) {
					fResult.setErrorString(Messages.CpPackJob_CancelledByUser);
					return false;
				}
			}
			Utils.copy(fDownloadFileTmp, fDownloadFile);
			return true;
		} catch (IOException e) {
			fResult.setErrorString(NLS.bind(Messages.CpPackInstallJob_FileNotFound, fPackUrl));
			return false;
		} finally {
			if (input != null) {
				input.close();
			}
			if (output != null) {
				output.close();
			}
			fDownloadFileTmp.delete();
			if (connection instanceof HttpURLConnection) {
				((HttpURLConnection) connection).disconnect();
			}
		}
	}

	private ICpPack unzipAndParse(IProgressMonitor monitor) throws IOException {

		SubMonitor progress = SubMonitor.convert(monitor, 100);
		progress.subTask(Messages.CpPackInstallJob_UnzippingAndParsing + fPackDestFile);

		String relativePath = PackInstallerUtils.getPackRelativeInstallDir(fPackId);
		IPath destPath = new Path(CpPlugIn.getPackManager().getCmsisPackRootDirectory()).append(relativePath);

		boolean unzipSuccess = fPackInstaller.unzip(fDownloadFile, destPath, progress.newChild(80));

		if (progress.isCanceled() || !unzipSuccess) {
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
				String destFileName = fDownloadDir.append(fPackId + CmsisConstants.EXT_PDSC).toOSString();
				// Copy the pdscFileName to .Download folder
				Utils.copy(new File(pdscFileName), new File(destFileName));

				fPack = (ICpPack) CpPlugIn.getPackManager().getParser().parseFile(pdscFileName);
				fPack.setPackState(PackState.INSTALLED);
			}
		}

		progress.done();

		return fPack;

	}

}
