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

package com.arm.cmsis.pack.installer;

import java.io.File;
import java.io.FileNotFoundException;
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
import java.util.Base64;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.xml.sax.SAXException;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpRepoServiceProvider;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.parser.CpPidxParser;
import com.arm.cmsis.pack.preferences.CpPreferenceInitializer;
import com.arm.cmsis.pack.utils.Utils;

/**
 * Default implementation of providing repository service like get pdsc files
 * and pack files from the Internet
 */
public class CpRepoServiceProvider implements ICpRepoServiceProvider {

    private static final int TIME_OUT = 10000;

    public CpRepoServiceProvider() {
    }

    @Override
    public int readIndexFile(String indexUrl, List<String[]> pdscList)
            throws ParserConfigurationException, SAXException, IOException {
        return readIndexFile(indexUrl, pdscList, new NullProgressMonitor());
    }

    @Override
    public int readIndexFile(String indexUrl, List<String[]> pdscList, IProgressMonitor monitor)
            throws ParserConfigurationException, SAXException, IOException {

        IPath webFolder = new Path(CpPlugIn.getPackManager().getCmsisPackWebDir());
        String destFileName = webFolder.append(CmsisConstants.REPO_KEIL_PINDEX_FILE).toOSString();
        File pidxFile = downloadFile(indexUrl, destFileName, monitor);
        if (pidxFile == null || monitor.isCanceled()) {
            return -1;
        }
        return CpPidxParser.parsePidx(pidxFile.toString(), pdscList); // parse file

    }

    @Override
    public File getPdscFile(String pdscUrl, String pdscName, String destFileName, IProgressMonitor monitor)
            throws IOException {
        String sourceUrl = Utils.addTrailingSlash(pdscUrl) + pdscName;
        File destFile = downloadFile(sourceUrl, destFileName, monitor);
        if (destFile != null)
            destFile.setReadOnly();
        return destFile;
    }

    @Override
    public File getPackFile(String packUrl, String destFileName, IProgressMonitor monitor) throws IOException {

        IPath downloadDir = new Path(CpPlugIn.getPackManager().getCmsisPackDownloadDir());
        if (!downloadDir.toFile().exists()) {
            downloadDir.toFile().mkdir();
        }
        String downloadFile = downloadDir.append(destFileName).toOSString();

        return downloadFile(packUrl, downloadFile, monitor);
    }

    protected File downloadFile(String packUrl, String destFileName, IProgressMonitor monitor) throws IOException {
        SubMonitor progress = SubMonitor.convert(monitor, 1);
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
                HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
                int responseCode = httpURLConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    break;
                } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    httpURLConnection.disconnect();
                    throw new FileNotFoundException(); // we do not supply a message here, it is processed by caller
                } else if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                        || responseCode == HttpURLConnection.HTTP_MOVED_PERM
                        || responseCode == HttpURLConnection.HTTP_SEE_OTHER) {
                    String newUrl = connection.getHeaderField(CmsisConstants.REPO_LOCATION);
                    url = new URL(newUrl);
                    continue;
                }
            }
            break;
        }

        if (connection == null) {
            return null; // should not happen
        }

        /***************** Start downloading *****************/
        int totalWork = connection.getContentLength();
        if (totalWork == -1) {
            totalWork = IProgressMonitor.UNKNOWN;
        }

        progress = SubMonitor.convert(progress.newChild(90), totalWork);
        progress.subTask(NLS.bind(Messages.CpPackInstallJob_DownloadingFrom, destFileName, packUrl));

        File downloadFile = new File(destFileName);
        File downloadFileTmp = File.createTempFile(Utils.extractFileName(destFileName), ".tmp"); //$NON-NLS-1$

        InputStream input = null;
        OutputStream output = null;
        try {
            input = connection.getInputStream();
            output = new FileOutputStream(downloadFileTmp);

            byte[] buf = new byte[4096]; // 4096 is a common NTFS block size
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

    protected URLConnection getConnection(URL url) throws IOException {
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
            String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes()); //$NON-NLS-1$
            connection.setRequestProperty("Authorization", basicAuth); //$NON-NLS-1$
        }
        return connection;
    }

}
