package com.arm.cmsis.pack.installer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;

import com.arm.cmsis.pack.ICpRepoServiceProvider;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.installer.utils.PackInstallerUtils;
import com.arm.cmsis.pack.installer.utils.RepositoryRefreshingUtils;
import com.arm.cmsis.pack.utils.Utils;

public class CpRepoServiceProvider implements ICpRepoServiceProvider {

	private final static int TIME_OUT = 10000;

	public CpRepoServiceProvider() {
	}

	@Override
	public int readIndexFile(String indexUrl, List<String[]> pdscList) throws Exception {
		URL url = new URL(indexUrl);
		return RepositoryRefreshingUtils.readIndex(url.openStream(), pdscList);
	}

	@Override
	public File getPdscFile(String pdscUrl, String pdscName, String destFileName, IProgressMonitor monitor) throws Exception {
		File destFile = null;

		URL sourceUrl = new URL(pdscUrl + pdscName);

		String destFileNameTmp = destFileName + CmsisConstants.EXT_TEMP;
		File destFileTmp = new File(destFileNameTmp);

		URLConnection connection = null;
		while (true) {
			connection = sourceUrl.openConnection();
			if (connection == null) {
				break;
			}
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
					sourceUrl = new URL(newUrl);
				} else {
					break;
				}
			}
		}

		if (connection != null) {
			if (destFileTmp.exists()) {
				destFileTmp.delete();
			}

			InputStream input = connection.getInputStream();
			OutputStream output = new FileOutputStream(destFileTmp);
			boolean finished = true;
			byte[] buf = new byte[1024];
			int bytesRead;
			while ((bytesRead = input.read(buf)) > 0) {
				output.write(buf, 0, bytesRead);
				// Check if the cancel button is pressed
				if (monitor.isCanceled()) {
					finished = false;
					break;
				}
			}
			output.close();
			if (input != null) {
				input.close();
			}
			if (finished) {
				destFile = new File(destFileName);
				Utils.copy(destFileTmp, destFile);
				destFile.setReadOnly();
			}
			destFileTmp.delete();
			if (connection instanceof HttpURLConnection) {
				((HttpURLConnection) connection).disconnect();
			}
		}
		return destFile;
	}

	@Override
	public File getPackFile(String packUrl, String destFileName, IProgressMonitor monitor) throws Exception {
		SubMonitor progress = SubMonitor.convert(monitor, 100);

		/***************** Establish connection *****************/
		URLConnection connection = null;
		progress.subTask(Messages.CpPackInstallJob_ConnectingTo + packUrl);
		URL url = new URL(packUrl);
		while (true) {
			progress.worked(10);
			if (progress.isCanceled()) {
				return null;
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
		
		if(connection == null) {
			return null; // should not happen
		}

		/***************** Start downloading *****************/
		int totalWork = connection.getContentLength();
		if (totalWork == -1) {
			totalWork = IProgressMonitor.UNKNOWN;
		}

		progress = SubMonitor.convert(progress.newChild(90), totalWork);
		progress.subTask(NLS.bind(Messages.CpPackInstallJob_DownloadingFrom, destFileName, packUrl));

		IPath downloadDir = new Path(PackInstallerUtils.getPacksDownloadDir());
		if (!downloadDir.toFile().exists()) {
			downloadDir.toFile().mkdir();
		}
		File downloadFile = downloadDir.append(destFileName).toFile();
		File downloadFileTmp = downloadDir.append(destFileName + CmsisConstants.EXT_TEMP).toFile();

		InputStream input = null;
		OutputStream output = null;
		try {
			input = connection.getInputStream();

			if (downloadFileTmp.exists()) {
				downloadFileTmp.delete();
			}
			output = new FileOutputStream(downloadFileTmp);

			byte[] buf = new byte[1024];
			int bytesRead;
			while ((bytesRead = input.read(buf)) > 0) {
				output.write(buf, 0, bytesRead);
				progress.worked(bytesRead);
				// Check if the cancel button is pressed
				if (progress.isCanceled()) {
					throw new Exception(Messages.CpPackJob_CancelledByUser);
				}
			}
			Utils.copy(downloadFileTmp, downloadFile);
			return downloadFile;
		} finally {
			if (input != null) {
				input.close();
			}
			if (output != null) {
				output.close();
			}
			downloadFileTmp.delete();
			if (connection instanceof HttpURLConnection) {
				((HttpURLConnection) connection).disconnect();
			}
		}
	}

}
