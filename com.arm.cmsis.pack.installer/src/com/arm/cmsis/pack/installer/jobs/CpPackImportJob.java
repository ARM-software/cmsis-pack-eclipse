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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackInstaller;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpPack;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.installer.Messages;
import com.arm.cmsis.pack.parser.PdscParser;
import com.arm.cmsis.pack.utils.Utils;

/**
 * Import a .pack or .zip file to the Pack Manager
 */
public class CpPackImportJob extends CpPackUnpackJob {

	protected String fImportSourceFile;

	/**
	 * @param name Job name
	 * @param installer Pack Installer
	 * @param packId Pack ID
	 * @param importSourceFile File path of the .pack/.zip file to be imported
	 */
	public CpPackImportJob(String name, ICpPackInstaller installer, String packId,
			String importSourceFile) {
		super(name, installer, packId, true);
		fImportSourceFile = importSourceFile;
	}

	@Override
	protected boolean copyToDownloadFolder(IProgressMonitor monitor) {
		try {
			parsePdscFile();
			Utils.copy(new File(fImportSourceFile), fDownloadedPackPath.toFile());
			return true;
		} catch (IOException e) {
			fResult.setErrorString(e.getMessage());
			return false;
		}
	}

	// Extracts pdsc file from archive to memory and parses it 
	private void parsePdscFile() throws IOException {
		ZipInputStream zipInput;
		zipInput = new ZipInputStream(new FileInputStream(fImportSourceFile));
		ZipEntry zipEntry = zipInput.getNextEntry();
		String pdscContent = null;
		String pdscFileName = null;
		try{
			for (; zipEntry != null; zipEntry = zipInput.getNextEntry()) {
				if (zipEntry.isDirectory())
					continue;
				String fileName = zipEntry.getName();
				if (!fileName.endsWith(CmsisConstants.EXT_PDSC))
					continue;
				pdscFileName = fileName;
				OutputStream output = new ByteArrayOutputStream();
				byte[] buf = new byte[4096]; // 4096 is a common NTFS block size
				int bytesRead;
				while ((bytesRead = zipInput.read(buf)) > 0) {
					output.write(buf, 0, bytesRead);
				}
				output.close();
				pdscContent = output.toString();
			}
		} finally {
			zipInput.closeEntry();
			zipInput.close();
		}
		if(pdscContent == null) {
			String msg = Messages.CpPackUnpackJob_PdscFileNotFoundInPack + fImportSourceFile; 
			throw new IOException(msg);
		}
		PdscParser parser = new PdscParser();
		ICpPack pack = (ICpPack) parser.parseXmlString(pdscContent);
		if (pack == null) {
			String msg = Messages.CpPackUnpackJob_FailToParsePdscFile + pdscFileName +"\n"; //$NON-NLS-1$
			msg += String.join("\n", parser.getErrorStrings()); //$NON-NLS-1$
			throw new IOException(msg);
		}
		// adjust pack id, download file name and destination path
		String packId = pack.getId();
		String relativeDir = CpPack.getPackRelativeInstallDir(packId);
		fDestPath = new Path(CpPlugIn.getPackManager().getCmsisPackRootDirectory()).append(relativeDir);
		fDownloadedPackPath = createDownloadFolder().append(packId + CmsisConstants.EXT_PACK); 
	}
}
