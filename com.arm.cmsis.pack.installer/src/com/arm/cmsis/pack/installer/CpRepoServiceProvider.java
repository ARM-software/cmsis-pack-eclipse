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

package com.arm.cmsis.pack.installer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.xml.sax.SAXException;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpRepoServiceProvider;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.installer.utils.RepositoryRefreshingUtils;
import com.arm.cmsis.pack.preferences.CpPreferenceInitializer;
import com.arm.cmsis.pack.utils.Utils;

/**
 * Default implementation of providing repository service like get pdsc files and pack files
 * from the Internet
 */
public class CpRepoServiceProvider implements ICpRepoServiceProvider {

	private final static int TIME_OUT = 10000;

	public CpRepoServiceProvider() {
	}

	@Override
	public int readIndexFile(String indexUrl, List<String[]> pdscList)
			throws ParserConfigurationException, SAXException, IOException {
		URL url = new URL(indexUrl);
		URLConnection connection = getConnection(url);
		return RepositoryRefreshingUtils.readIndex(connection.getInputStream(), pdscList);
	}

	@Override
	public File getPdscFile(String pdscUrl, String pdscName, String destFileName, IProgressMonitor monitor) throws IOException {
		File destFile = null;

		URL sourceUrl = new URL(pdscUrl + pdscName);

		File destFileTmp = File.createTempFile("temp-pdsc", ".tmp"); //$NON-NLS-1$ //$NON-NLS-2$

		URLConnection connection = null;
		while (true) {
			connection = getConnection(sourceUrl);
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
			if (destFileTmp.exists()) {
				destFileTmp.delete();
			}
			if (connection instanceof HttpURLConnection) {
				((HttpURLConnection) connection).disconnect();
			}
		}
		return destFile;
	}

	@Override
	public File getPackFile(String packUrl, String destFileName, IProgressMonitor monitor) throws IOException {
		SubMonitor progress = SubMonitor.convert(monitor, 100);

		/***************** Establish connection *****************/
		URLConnection connection = null;
		progress.subTask(Messages.CpPackInstallJob_ConnectingTo + packUrl);
		URL url = new URL(packUrl);
		while (true) {
			progress.worked(10);
			if (progress.isCanceled()) {
				throw new InterruptedIOException(Messages.CpPackJob_CancelledByUser);
			}
			connection = getConnection(url);
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

		IPath downloadDir = new Path(CpPlugIn.getPackManager().getCmsisPackDownloadDir());
		if (!downloadDir.toFile().exists()) {
			downloadDir.toFile().mkdir();
		}
		File downloadFile = downloadDir.append(destFileName).toFile();
		File downloadFileTmp = File.createTempFile("temp-pack", ".tmp"); //$NON-NLS-1$ //$NON-NLS-2$

		InputStream input = null;
		OutputStream output = null;
		try {
			input = connection.getInputStream();
			output = new FileOutputStream(downloadFileTmp);

			byte[] buf = new byte[1024];
			int bytesRead;
			while ((bytesRead = input.read(buf)) > 0) {
				output.write(buf, 0, bytesRead);
				progress.worked(bytesRead);
				// Check if the cancel button is pressed
				if (progress.isCanceled()) {
					throw new InterruptedIOException(Messages.CpPackJob_CancelledByUser);
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
			if (downloadFileTmp.exists()) {
				downloadFileTmp.delete();
			}
			if (connection instanceof HttpURLConnection) {
				((HttpURLConnection) connection).disconnect();
			}
		}
	}

	private URLConnection getConnection(URL url) throws IOException {
		URLConnection connection = null;
		int proxyMode = CpPreferenceInitializer.getProxyMode();
		if (proxyMode == 0) { // No Proxy
			connection = url.openConnection();
		} else {
			String addr = CpPreferenceInitializer.getProxyAddress();
			int port = CpPreferenceInitializer.getProxyPort();
			SocketAddress socket = new InetSocketAddress(addr, port);
			Proxy proxy;
			if (proxyMode == 1) { // HTTP Proxy
				proxy = new Proxy(Proxy.Type.HTTP, socket);
			} else if (proxyMode == 2) { // Socket Proxy
				proxy = new Proxy(Proxy.Type.SOCKS, socket);
			} else {
				proxy = Proxy.NO_PROXY;
			}
			connection = url.openConnection(proxy);

			String username = CpPreferenceInitializer.getProxyUsername();
			String password = CpPreferenceInitializer.getProxyPassword();
			String userpass = username + ':' + password;
			String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(userpass.getBytes()); //$NON-NLS-1$
			connection.setRequestProperty("Authorization", basicAuth); //$NON-NLS-1$
		}
		return connection;
	}

}
