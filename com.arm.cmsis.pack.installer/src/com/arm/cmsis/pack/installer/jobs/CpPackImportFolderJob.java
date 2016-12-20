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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPackCollection;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.installer.Messages;
import com.arm.cmsis.pack.parser.ICpXmlParser;
import com.arm.cmsis.pack.parser.PdscParser;
import com.arm.cmsis.pack.utils.Utils;

/**
 * Job of importing packs from a folder
 */
public class CpPackImportFolderJob extends CpPackJob {

	protected final String fRootPath;
	protected final Set<String> fPackFolders;

	public CpPackImportFolderJob(String name, ICpPackInstaller packInstaller, String jobId,
			String rootPath) {
		super(name, packInstaller, jobId);
		fRootPath = rootPath;
		fPackFolders = new HashSet<>();
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		File rootFile = new File(fRootPath);
		if (!rootFile.exists()) {
			fResult.setSuccess(false);
			fResult.setErrorString(NLS.bind(Messages.CpPackImportFolderJob_FolderNotExist, fRootPath));
			fPackInstaller.jobFinished(CmsisConstants.EMPTY_STRING, RteEvent.PACK_IMPORT_FOLDER_JOB_FINISHED, fResult);
			return Status.OK_STATUS;
		}
		SubMonitor progress = SubMonitor.convert(monitor, Utils.countFiles(rootFile));
		progress.setTaskName(NLS.bind(Messages.CpPackImportFolderJob_ImportingPacksFrom, fRootPath));

		try {
			if (importFolderPacks(rootFile, new HashSet<String>(), progress)) {
				fResult.setSuccess(true);
			}
		} catch (IOException e) {
			fResult.setSuccess(false);
			fResult.setErrorString(e.getMessage());
		}

		fPackInstaller.jobFinished(fJobId, RteEvent.PACK_IMPORT_FOLDER_JOB_FINISHED, fResult);
		return Status.OK_STATUS;
	}

	protected boolean importFolderPacks(File parentFolder, Set<String> copiedFolder, IProgressMonitor progress) throws IOException {
		for (File folder : parentFolder.listFiles()) {
			if (progress.isCanceled()) {
				fPackFolders.stream().forEach(path -> Utils.deleteFolderRecursive(new File(path)));
				fResult.setSuccess(false);
				fResult.setErrorString(Messages.CpPackJob_CancelledByUser);
				return false;
			}
			if (folder.isDirectory()) {
				if (!importFolderPacks(folder, copiedFolder, progress)) { // first import child folder packs
					return false;
				}
			}
		}
		List<String> files = new LinkedList<String>();
		Utils.findPdscFiles(parentFolder, files, 0);
		if (files.isEmpty()) { // this is a normal folder
			return true;
		}
		ICpXmlParser parser = new PdscParser();
		ICpPack pack = (ICpPack) parser.parseFile(files.get(0));
		if (pack == null) {
			fResult.setSuccess(false);
			fResult.setErrorString(String.join("\n", parser.getErrorStrings())); //$NON-NLS-1$
			return false;
		}
		ICpPackCollection installedPacks = CpPlugIn.getPackManager().getInstalledPacks();
		if (installedPacks != null && installedPacks.getPack(pack.getId()) != null) { // pack already installed
			copiedFolder.add(parentFolder.getAbsolutePath());
			return true;
		}
		progress.subTask(NLS.bind(Messages.CpPackImportFolderJob_ImportingPack, pack.getId()));
		String relativeDir = CpPack.getPackRelativeInstallDir(pack.getId());
		IPath dstFolder = new Path(CpPlugIn.getPackManager().getCmsisPackRootDirectory()).append(relativeDir);
		for (File file : parentFolder.listFiles()) {
			if (file.isFile()) {
				Utils.copy(file, dstFolder.append(file.getName()).toFile());
				progress.worked(1);
			} else if (file.isDirectory() && !copiedFolder.contains(file.getAbsolutePath())) {
				Utils.copyDirectoryWithProgress(file, dstFolder.append(file.getName()).toFile(), copiedFolder, progress);
			}
		}
		fPackFolders.add(dstFolder.toOSString());
		copiedFolder.add(parentFolder.getAbsolutePath()); // this folder has been copied, will not enter any subfolders

		return true;
	}

}
