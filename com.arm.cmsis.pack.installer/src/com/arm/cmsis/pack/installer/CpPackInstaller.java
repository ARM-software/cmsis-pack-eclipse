/*******************************************************************************
 * Copyright (c) 2015 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Eclipse Project - generation from template
 * Liviu Ionescu - initial implementation
 * ARM Ltd and ARM Germany GmbH - application-specific implementation
 *******************************************************************************/

package com.arm.cmsis.pack.installer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpEnvironmentProvider;
import com.arm.cmsis.pack.ICpPackInstaller;
import com.arm.cmsis.pack.ICpPackManager;
import com.arm.cmsis.pack.ICpRepoServiceProvider;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpPack;
import com.arm.cmsis.pack.data.ICpExample;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPack.PackState;
import com.arm.cmsis.pack.data.ICpPackCollection;
import com.arm.cmsis.pack.data.ICpPackFamily;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.generic.RunnableWithIntResult;
import com.arm.cmsis.pack.installer.jobs.CpPackImportFolderJob;
import com.arm.cmsis.pack.installer.jobs.CpPackImportJob;
import com.arm.cmsis.pack.installer.jobs.CpPackInstallJob;
import com.arm.cmsis.pack.installer.jobs.CpPackJob;
import com.arm.cmsis.pack.installer.jobs.CpPackRemoveJob;
import com.arm.cmsis.pack.installer.jobs.CpPackUnpackJob;
import com.arm.cmsis.pack.preferences.CpPreferenceInitializer;
import com.arm.cmsis.pack.repository.CpRepositoryList;
import com.arm.cmsis.pack.repository.ICpRepository;
import com.arm.cmsis.pack.repository.RtePackJobResult;
import com.arm.cmsis.pack.rte.RteModelUtils;
import com.arm.cmsis.pack.utils.Utils;
import com.arm.cmsis.pack.utils.VersionComparator;

/**
 * Default implementation of {@link ICpPackInstaller}
 */
public class CpPackInstaller extends PlatformObject implements ICpPackInstaller {

	protected IProgressMonitor fMonitor;
	protected Map<String, CpPackJob> fJobQueue; // job ID -> pack job

	/**
	 * Pack ID -> Collection of required packs' IDs installing
	 */
	protected Map<String, Collection<String>> fResolvingPacks;

	public final static int TIME_OUT = 10000;

	protected ICpRepoServiceProvider fRepoServiceProvider;

	public CpPackInstaller() {
		fJobQueue = Collections.synchronizedMap(new HashMap<>());
		fResolvingPacks = Collections.synchronizedMap(new HashMap<>());
		fRepoServiceProvider = new CpRepoServiceProvider();
	}

	@Override
	public void installPack(String packId) {
		installPack(packId, true);
	}

	@Override
	public void installPack(String packId, boolean installRequiredPacks) {
		final ICpPackCollection allPacks = CpPlugIn.getPackManager().getPacks();
		if (allPacks == null) {
			popupInstallError(Messages.CpPackInstaller_OpenPackManagerToUpdatePacks);
			return;
		}
		ICpPack pack = allPacks.getPack(packId);
		if (pack != null) {
			if (pack.getPackState() == PackState.INSTALLED) {
				return;
			} else if (pack.getPackState() == PackState.DOWNLOADED) {
				unpackPack(pack, installRequiredPacks);
			} else {
				installPack(pack.getPackId(), pack.getUrl(), installRequiredPacks);
			}
		} else {
			String familyId = CpPack.familyFromId(packId);
			ICpPack latestPack = allPacks.getPack(familyId);
			if (latestPack == null) {
				printInConsole(NLS.bind(Messages.CpPackInstaller_PackFamilyNotFound, familyId),
						ConsoleType.ERROR);
				return;
			}
			if (CpPack.isPackFamilyId(packId)) {
				packId += '.' + latestPack.getVersion();
			}
			installPack(packId, latestPack.getUrl(), installRequiredPacks);
		}
	}

	@Override
	public void installPack(IAttributes packAttributes) {
		String packId = RteModelUtils.constructEffectivePackId(packAttributes);
		installPack(packId, true);
	}

	@Override
	public Collection<String> installRequiredPacks(ICpPack pack) {
		if (pack == null || pack.getPackState() != PackState.INSTALLED) {
			return null;
		}
		Collection<? extends ICpItem> requiredPacks = pack.getRequiredPacks();
		if (requiredPacks == null || requiredPacks.isEmpty()) {
			return null;
		}
		ICpPackManager pm = CpPlugIn.getPackManager();
		Collection<String> reqPacks = new HashSet<>();
		for (ICpItem requiredPack : requiredPacks) {
			ICpPackCollection installedPacks = pm.getInstalledPacks();
			if (installedPacks != null && installedPacks.getPack(requiredPack.attributes()) != null) {
				continue;
			}

			// check releases' versions
			String vendor = requiredPack.getVendor();
			String name = requiredPack.getName();
			String versionRange = requiredPack.getVersion();
			String familyId = vendor + '.' + name;

			ICpPack latestPack = CpPlugIn.getPackManager().getPacks().getPack(familyId);
			if (latestPack == null) {
				printInConsole(NLS.bind(Messages.CpPackInstaller_RequiredPackFamilyNotExist, familyId), ConsoleType.WARNING);
				continue;
			}
			Collection<? extends ICpItem> releases = latestPack.getReleases();
			if (releases == null) {
				printInConsole(NLS.bind(Messages.CpPackInstaller_NoVersionOfPackFamilyIsFound, familyId), ConsoleType.WARNING);
				continue;
			}
			boolean compatibleVersionFound = false;
			for (ICpItem release : releases) {
				String version = release.getAttribute(CmsisConstants.VERSION);
				if (VersionComparator.matchVersionRange(version, versionRange)) {
					boolean addedToJobQueue;
					String packId = familyId + '.' + VersionComparator.removeMetadata(version);
					ICpPack downloadedPack = CpPlugIn.getPackManager().getPacks().getPack(packId);
					if (downloadedPack != null && downloadedPack.getPackState() == PackState.DOWNLOADED) {
						addedToJobQueue = unpackPack(downloadedPack, true);
					} else {
						addedToJobQueue = installPack(packId, latestPack.getUrl(), true);
					}
					if (addedToJobQueue) {
						reqPacks.add(packId);
					}
					compatibleVersionFound = true;
					break;
				}
			}
			if (!compatibleVersionFound) {
				printInConsole(NLS.bind(Messages.CpPackInstaller_NoCompatibleVersionIsFound, familyId, versionRange), ConsoleType.WARNING);
				continue;
			}
		}

		if (!reqPacks.isEmpty()) {
			StringBuilder output = new StringBuilder(NLS.bind(Messages.CpPackInstaller_InstallingRequiredPacks, pack.getId()));
			for (String reqPack : reqPacks) {
				output.append(reqPack + ", "); //$NON-NLS-1$
			}
			output.delete(output.length()-2, output.length());
			printInConsole(output.toString(), ConsoleType.INFO);
		}

		return reqPacks;
	}

	/**
	 * Install pack with pack id and download url
	 *
	 * @param packId full pack id with version
	 * @param url URL of this pack family
	 * @param installRequiredPacks True if the required packs should also be installed
	 * @return True if the job is added to the job queue
	 */
	private boolean installPack(String packId, String url, boolean installRequiredPacks) {
		if (fJobQueue.containsKey(packId)) {
			return false;
		}
		CpPackJob job = new CpPackInstallJob(NLS.bind(Messages.CpPackInstaller_InstallingPack, packId),
				this, packId, url, installRequiredPacks);
		job.setUser(true);
		fJobQueue.put(packId, job);
		job.schedule();
		return true;
	}

	protected void popupInstallError(String errorMessage) {
		Display.getDefault().asyncExec(() -> {
			MessageDialog.openError(null, Messages.CpPackInstaller_NoPacksFound,
					errorMessage);
		});
	}

	@Override
	public void importPack(String filePath) {
		String packId = Utils.extractBaseFileName(filePath);
		if (fJobQueue.containsKey(packId)) {
			return;
		}
		CpPackJob job = new CpPackImportJob(NLS.bind(Messages.CpPackInstaller_ImportingPack, packId),
				this, packId, filePath);
		job.setUser(true);
		fJobQueue.put(packId, job);
		job.schedule();
	}

	@Override
	public void importFolderPacks(String rootPath) {
		List<String> files = new LinkedList<String>();;
		Utils.findPdscFiles(new File(rootPath), files, 256);
		String jobId = files.stream().map(filename -> CpPlugIn.getPackManager().readPack(filename))
				.filter(pack -> pack != null).map(pack -> pack.getId())
				.collect(Collectors.joining(",")); //$NON-NLS-1$
		CpPackJob job = new CpPackImportFolderJob(
				NLS.bind(Messages.CpPackInstaller_ImportingFolderPacks, rootPath), this, jobId,
				rootPath);
		job.setUser(true);
		fJobQueue.put(jobId, job);
		job.schedule();
	}

	/**
	 * Unpack the pack that stays in the .Download folder
	 * @param pack The pack to unzip
	 * @param installRequiredPacks true if the required packs should also be installed
	 * @return True if the job is added to the job queue
	 */
	private boolean unpackPack(ICpPack pack, boolean installRequiredPacks) {
		String id = pack.getId();
		if (fJobQueue.containsKey(id)) {
			return false;
		}
		CpPackJob job = new CpPackUnpackJob(NLS.bind(Messages.CpPackInstaller_UnpackingPack, pack.getId()),
				this, id, installRequiredPacks);
		job.setUser(true);
		fJobQueue.put(id, job);
		job.schedule();
		return true;
	}

	@Override
	public void removePack(ICpPack pack, boolean delete) {
		String id = pack.getId();
		if (fJobQueue.containsKey(id)) {
			return;
		}
		String jobName = NLS.bind(Messages.CpPackInstaller_RemovingPack, pack.getId());
		if (delete) {
			jobName = NLS.bind(Messages.CpPackInstaller_DeletingPack, pack.getId());
		}
		CpPackJob job = new CpPackRemoveJob(jobName, this, pack, delete);
		job.setUser(true);
		if (pack.getPackState() == PackState.ERROR) {
			fJobQueue.put(id, job);
		} else {
			fJobQueue.put(id, job);
		}
		job.schedule();
	}

	@Override
	public boolean unzip(File archiveFile, IPath destPath, IProgressMonitor monitor) throws IOException {
		SubMonitor progress = SubMonitor.convert(monitor, Utils.getFilesCount(archiveFile));

		if (destPath.toFile().exists()) {
			Utils.deleteFolderRecursive(destPath.toFile());
		}
		ZipInputStream zipInput = new ZipInputStream(new FileInputStream(archiveFile));
		ZipEntry zipEntry = zipInput.getNextEntry();

		int countBytes = 0;
		boolean result = true;
		for (; zipEntry != null; zipEntry = zipInput.getNextEntry()) {
			if (progress.isCanceled()) {
				result = false;
				break;
			}
			if (zipEntry.isDirectory())
				continue;
			String fileName = zipEntry.getName();
			IPath path = destPath.append(fileName);
			File outFile = new File(path.toOSString());
			if (!outFile.getParentFile().exists()) {
				outFile.getParentFile().mkdirs();
			}
			OutputStream output = new FileOutputStream(outFile);
			byte[] buf = new byte[4096]; // 4096 is a common NTFS block size
			int bytesRead;
			while ((bytesRead = zipInput.read(buf)) > 0) {
				output.write(buf, 0, bytesRead);
				countBytes += bytesRead;
			}
			output.close();
			outFile.setReadOnly();
			progress.worked(1);
		}
		zipInput.closeEntry();
		zipInput.close();
		if(countBytes  == 0) { // something went wrong, empty archive? 
			throw new IOException(); // caller adds message
		}
		return result;
	}

	@Override
	public IProject copyExample(ICpExample example) {
		if (example == null) {
			return null;
		}
		ICpEnvironmentProvider envProvider = CpPlugIn.getEnvironmentProvider();
		if (envProvider == null) {
			return null;
		}

		String loadPath = example.getAbsoluteLoadPath(envProvider.getName());
		if (loadPath == null || loadPath.isEmpty()) {
			return null;
		}

		IPath examplePath = new Path(loadPath);
		// default implementation assumes that the example is an Eclipse project
		IProjectDescription projDesc = getProjectDescription(examplePath);
		if (projDesc == null) {
			popupCopyError(NLS.bind(Messages.CpPackInstaller_ErrorWhileReadingProjectDescriptionFile,
					examplePath));
			return null;
		}
		String projectName = projDesc.getName();
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProject project = workspace.getRoot().getProject(projectName);
		File destFile = ResourcesPlugin.getWorkspace().getRoot().getLocation()
				.append(project.getName()).toFile();

		if (!confirmCopyExample(example, destFile, project)) {
			return null;
		}

		if (project.exists()) {
			try {
				project.delete(true, true, new NullProgressMonitor());
			} catch (CoreException e) {
				popupCopyError(NLS.bind(Messages.CpPackInstaller_ErrorWhileOverwritingExistingProject,
						project.getName()));
				return null;
			}
		} else if (destFile.exists()) {
			Utils.deleteFolderRecursive(destFile);
		}

		CpPlugIn.getDefault().emitRteEvent(RteEvent.PRE_IMPORT, null);
		File importSource = new File(projDesc.getLocationURI());
		FileSystemStructureProvider structureProvider = FileSystemStructureProvider.INSTANCE;

		ImportOperation operation = new ImportOperation(project.getFullPath(), importSource,
				structureProvider, new OverwriteQuery(null),
				structureProvider.getChildren(importSource));
		operation.setContext(null);
		operation.setOverwriteResources(true); // need to overwrite
		operation.setCreateContainerStructure(false);
		try {
			operation.run(new NullProgressMonitor());
		} catch (InvocationTargetException | InterruptedException e) {
			popupCopyError(NLS.bind(Messages.CpPackInstaller_FailedImportFilesFromFolder,
					examplePath.removeLastSegments(1)));
			return null;
		}

		Utils.clearReadOnly(project.getLocation().toFile(),
				CmsisConstants.EMPTY_STRING);
		CpPlugIn.getDefault().emitRteEvent(RteEvent.POST_IMPORT, project);
		return project;
	}

	protected boolean confirmCopyExample(ICpExample example, File destFile, IProject project) {
		CopyExampleDialog copyDialog = new CopyExampleDialog(null, example.getName(),
				example.getPackId(), destFile.toString(), project.getName(),
				project.exists() || destFile.exists());

		return copyDialog.open() == Window.OK;
	}

	protected void popupCopyError(String errorMessage) {
		Display.getDefault().asyncExec(() -> {
			MessageDialog.openError(null, Messages.CpPackInstaller_ErrorWhileCopyingExample,
					errorMessage);
		});
	}

	private IProjectDescription getProjectDescription(IPath path) {
		try {
			return ResourcesPlugin.getWorkspace().loadProjectDescription(path);
		} catch (CoreException e) {
			return null;
		}
	}

	@Override
	public void updatePacks(IProgressMonitor monitor) {
		fMonitor = monitor;
		updatePacks();
	}

	@Override
	synchronized public void jobFinished(String jobId, String jobTopic, RtePackJobResult jobResult) {
		CpPackJob job = fJobQueue.remove(jobId);
		String jobName = job != null ? job.getName() : Messages.CpPackInstaller_Processing;

		// job is finished, update the resolving status (fResolvingPacks)
		updateResolvingPacks(jobId);

		String output = new SimpleDateFormat("HH:mm:ss").format(new Date()); //$NON-NLS-1$
		output += ": " + jobName; //$NON-NLS-1$
		if (jobResult != null && jobResult.isSuccess()) {
			output += Messages.CpPackInstaller_Completed;
			printInConsole(output, ConsoleType.OUTPUT);

			if(job != null && job.installRequiredPacks()) {
				Collection<String> reqPacks = installRequiredPacks(jobResult.getPack());
				if (reqPacks != null && !reqPacks.isEmpty()) {
					fResolvingPacks.put(jobId, reqPacks);
				}
			}
			CpPlugIn.getDefault().emitRteEvent(jobTopic, jobResult);
		} else {
			output += Messages.CpPackInstaller_WasNotSuccessful;
			if (jobResult != null) {
				output += jobResult.getErrorString();
			}
			printInConsole(output, ConsoleType.ERROR);
			// anyway notify the recipients that the job has completed
			CpPlugIn.getDefault().emitRteEvent(jobTopic, jobResult);
		}
	}

	@Override
	public ICpRepoServiceProvider getRepoServiceProvider() {
		return fRepoServiceProvider;
	}

	@Override
	public void setRepoServiceProvider(ICpRepoServiceProvider repoServiceProvider) {
		fRepoServiceProvider = repoServiceProvider;
	}

	@Override
	public synchronized boolean isBusy() {
		return !fJobQueue.isEmpty();
	}

	@Override
	public synchronized boolean isProcessing(String packId) {
		
		if(!isBusy())
			return false;
		if(fJobQueue.containsKey(packId) || fResolvingPacks.containsKey(packId))
			return true;
		return false;
	}

	@Override
	public synchronized boolean isProcessing(IAttributes packAttributes) {
		String packId = RteModelUtils.constructEffectivePackId(packAttributes);
		return isProcessing(packId);
	}

	@Override
	public synchronized void reset() {
		for (Job job : fJobQueue.values()) {
			job.cancel();
		}
		fJobQueue.clear();
		fResolvingPacks.clear();
	}

	@Override
	public void printInConsole(String message, ConsoleType type) {
		switch (type) {
		case OUTPUT:
			CpPlugIn.getDefault().emitRteEvent(RteEvent.PRINT_OUTPUT, message);
			break;
		case INFO:
			CpPlugIn.getDefault().emitRteEvent(RteEvent.PRINT_INFO, message);
			break;
		case WARNING:
			CpPlugIn.getDefault().emitRteEvent(RteEvent.PRINT_WARNING, message);
			break;
		case ERROR:
			CpPlugIn.getDefault().emitRteEvent(RteEvent.PRINT_ERROR, message);
			break;
		default :
			break;
		}
	}

	/***************** Here begins the Update Packs part *****************/
	protected void updatePacks() {
		if (CpPlugIn.getPackManager().getCmsisPackRootDirectory() == null
				|| CpPlugIn.getPackManager().getCmsisPackRootDirectory().isEmpty()) {
			// if this is not an automatic update, print the message on the console
			if (!(fMonitor instanceof NullProgressMonitor)) {
				printInConsole(Messages.CpPackInstaller_SetCmsisPackRootFolderAndTryAgain,
						ConsoleType.ERROR);
			}
			return;
		}

		boolean success = true;

		CpRepositoryList repos = CpPlugIn.getPackManager().getCpRepositoryList();

		// String[] { url, name, version }
		List<String[]> indexList = new LinkedList<String[]>();
		try {
			boolean needsUpdate = false;
			List<ICpRepository> reposList = repos.getList();
			for (ICpRepository repo : reposList) {

				if (fMonitor.isCanceled()) {
					break;
				}

				String type = repo.getType();
				String indexUrl = repo.getUrl();
				if (CmsisConstants.REPO_PACK_TYPE.equals(type)) {

					// collect all pdsc references in this site
					int count = readCmsisIndex(indexUrl, indexList);
					if (count > 0) {
						indexList.add(0, new String[]{Utils.extractPath(indexUrl, true),
								Utils.extractFileName(indexUrl),
								CmsisConstants.EMPTY_STRING});
						needsUpdate = true;
					} else if (count == -1) { // this index file is not correctly downloaded/parsed
						success = false;
					}
				} else {
					printInConsole(NLS.bind(Messages.CpPackInstaller_RepoTypeNotSupported, type),
							ConsoleType.WARNING);
				}
			}
			
			// Set total number of work units to the number of pdsc files
			fMonitor.beginTask(Messages.CpPackInstaller_RefreshAllPacks, indexList.size() + 7);

			// Read all .pdsc files and collect summary if index.pidx's time stamp changes
			if (needsUpdate) {
				aggregateCmsis(indexList);
			}

			fMonitor.worked(1); // Should reach 100% now

		} catch (Exception e) {
			printInConsole(e.toString(), ConsoleType.ERROR);
		}
		
		if (fMonitor.isCanceled()) {
			printInConsole(Messages.CpPackInstaller_JobCancelled, ConsoleType.WARNING);
		} else if (success) {
			updateWebAndLocalFolders(indexList);
			if (!fMonitor.isCanceled()) {
				CpPreferenceInitializer.updateLastUpdateTime(true);
			}
		}
		printInConsole(Messages.CpPackInstaller_PackUpdatesCompleted, ConsoleType.INFO);

		CpPlugIn.getPackManager().reload();
	}

	/**
	 * Updates .Web and .Local folders
	 * Remove .pdsc files that are in .Web folder but not listed in index.pidx file
	 * Remove .pdsc files that are in .Local folder but listed in index.pidx file
	 * Update .pdsc files in .Local folder if needed
	 * @param indexList a list of .pdsc files in index.pidx, each item is { url, name, version }
	 */
	protected void updateWebAndLocalFolders(List<String[]> indexList) {
		ICpPackManager manager = CpPlugIn.getPackManager();
		File webFile = new File(CpPlugIn.getPackManager().getCmsisPackWebDir());
		if (!webFile.exists()) {
			return;
		}
		// put the .pdsc files listed in index.pidx in a set
		Set<String> indexPdscFiles = new HashSet<>();
		for (String[] entry : indexList) {
			indexPdscFiles.add( entry[1]);
		}
		// clean .Web folder
		Collection<String> webPdscFiles = Utils.findPdscFiles(webFile, null, 0);
		for (String pdscFile : webPdscFiles) {
			String relName = Utils.extractFileName(pdscFile);
			if (!indexPdscFiles.contains(relName)) {
				new File(pdscFile).delete();
			}
		}
		// clean .Local folder
		IPath localFolder = new Path(CpPlugIn.getPackManager().getCmsisPackLocalDir());
		Collection<String> localPdscFiles = Utils.findPdscFiles(localFolder.toFile(), null, 0);
		for (String pdscFile : localPdscFiles) {
			String packId = Utils.extractBaseFileName(pdscFile);
			String familyId = CpPack.familyFromId(packId);
			String pdscName = familyId + CmsisConstants.EXT_PDSC;
			if (indexPdscFiles.contains(pdscName)) {
				new File(pdscFile).delete();
			}
		}

		// update .Local folder
		Map<String, ICpPackFamily> families = manager.getPacks().getFamilies();
		for (Entry<String, ICpPackFamily> entry : families.entrySet()) {
			if(fMonitor.isCanceled()) {
				return;
			}
			ICpPackFamily family = entry.getValue(); 
			ICpPack latestPack = family.getPack();
			if(latestPack.isDeprecated())
				continue;
			String familyId = entry.getKey(); 
			final String pdscName = familyId + CmsisConstants.EXT_PDSC;
			if (indexPdscFiles.contains(pdscName)) {
				continue;
			}
			final String pdscUrl = latestPack.getUrl();
			final String destFileName = localFolder.append(pdscName).toOSString();
			downloadPdscFile(pdscUrl, pdscName, destFileName);
		}			
	}
	
	/**
	 * Reads the .index file
	 *
	 * @param indexUrl url of .index file
	 * @param pdscList list of pdsc files
	 * @return number of pdsc files that needs to be updated. -1 if exception occurs.
	 */
	protected int readCmsisIndex(String indexUrl, List<String[]> pdscList) {

		printInConsole(NLS.bind(Messages.CpPackInstaller_Parsing, indexUrl), ConsoleType.INFO);

		try {
			return fRepoServiceProvider.readIndexFile(indexUrl, pdscList);
		} catch (FileNotFoundException e) {
			printInConsole(Messages.CpPackInstaller_FileNotFound + e.getMessage(),
					ConsoleType.ERROR);
		} catch (UnknownHostException e) {
			printInConsole(NLS.bind(Messages.CpPackInstallJob_UnknownHostException, e.getMessage()), ConsoleType.ERROR);
		} catch (Exception e) {
			printInConsole(e.toString(), ConsoleType.ERROR);
		}

		return -1;
	}
	

	/**
	 * Collects pack info from index file
	 * @param list List of packs, each entry is like {url, name, version}
	 */
	protected void aggregateCmsis(List<String[]> list) {

		IPath webFolder = new Path(CpPlugIn.getPackManager().getCmsisPackWebDir());

		// repo keys: { "type", "url", "list" }

		// String[] { url, name, version }
		for (int i = 0; i < list.size(); i++) {
			String[] pdsc = list.get(i);

			if (fMonitor.isCanceled()) {
				break;
			}

			// Make url always ends in '/'
			final String pdscUrl = Utils.addTrailingSlash(pdsc[0]);
			final String pdscName = pdsc[1];
			final String pdscVersion = pdsc[2];
			final String packFamilyId = Utils.extractBaseFileName(pdscName);

			fMonitor.subTask(NLS.bind(Messages.CpPackInstaller_Updating, pdscName, pdscUrl));

			String destFileName = webFolder.append(pdscName).toOSString();

			// if this is not .idx file and we have already higher version, skip
			if (pdscName.endsWith(CmsisConstants.EXT_PDSC)
					&& new File(destFileName).exists()
					&& skipUpdate(pdscUrl, packFamilyId, pdscVersion)) {
				fMonitor.worked(1);
				continue;
			}
			downloadPdscFile(pdscUrl, pdscName, destFileName);
		}
	}
	
	/**
	 * Download the pdsc file with url and name
	 * 
	 * @param pdscUrl URL of the pdsc file
	 * @param pdscName pdsc file name
	 * @param destFileName destination file name
	 * @return true if this pdsc file is successfully downloaded, false otherwise
	 *         False if this pdsc file needs to be downloaded again.
	 */
	private boolean downloadPdscFile(String pdscUrl, String pdscName, String destFileName) {
		
		while(true) { // while for timeout
			if(fMonitor.isCanceled())
				return false;
			try {
				fRepoServiceProvider.getPdscFile(pdscUrl, pdscName, destFileName, fMonitor);
			} catch (FileNotFoundException e) {
				String url = pdscUrl + pdscName;
				printInConsole(NLS.bind(Messages.CpPackInstallJob_FileNotFound, url), ConsoleType.ERROR);
				return false;
			} catch (UnknownHostException e) {
				printInConsole(NLS.bind(Messages.CpPackInstallJob_UnknownHostException, e.getMessage()), ConsoleType.ERROR);
				return false;
			} catch (SocketTimeoutException e) {
				int wait = timeoutQuestion(pdscUrl);
				if (wait == 0) { // Yes
					return false;
				} else if (wait == 1) { // No
					printInConsole(NLS.bind(Messages.CpPackInstaller_TimeoutConsoleMessage,
							pdscName, pdscUrl), ConsoleType.WARNING);
					continue;
				} else { // Cancel
					fMonitor.setCanceled(true);
					return false;
				}
			} catch (InterruptedIOException e) {
				printInConsole(e.getMessage(), ConsoleType.ERROR);
				return false;
			} catch (IOException e) {
				printInConsole(NLS.bind(Messages.CpPackInstaller_ErrorWhileRefreshingIgnored,
						e.getMessage()), ConsoleType.WARNING);
				return false;
			}
			break;
		}
		// One more unit completed
		fMonitor.worked(1);
		return true;
	}

	/***************** Here begins utility function part *****************/
	protected int timeoutQuestion(String pdscUrl) {
		RunnableWithIntResult runnable = new RunnableWithIntResult() {
			@Override
			public void run() {
				MessageDialog dialog = new MessageDialog(null, Messages.CpPackInstaller_Timout, null,
						NLS.bind(Messages.CpPackInstaller_TimeoutMessage, pdscUrl, TIME_OUT / 1000),
						MessageDialog.QUESTION_WITH_CANCEL, new String[]{IDialogConstants.YES_LABEL,
								IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL},
						0);
				result = dialog.open();
			}
			@Override
			public Integer getResult() {
				return result;
			}
		};
		Display.getDefault().syncExec(runnable);
		return runnable.getResult();
	}

	/**
	 * Determine if this .pdsc file should be skipped when updating from the web
	 *
	 * @param pdscUrl pdsc file's url
	 * @param packFamilyId pack family id
	 * @param pdscVersion pdsc file's version
	 * @return true if should be skipped for updating
	 */
	protected boolean skipUpdate(String pdscUrl, String packFamilyId, String pdscVersion) {
		if (CpPlugIn.getPackManager().getPacks() == null) {
			return false;
		}
		ICpPackFamily family = CpPlugIn.getPackManager().getPacks().getFamily(packFamilyId);

		if (family == null) {
			return false;
		}
		ICpPack latestPpack = family.getPack();
		if (VersionComparator.versionCompare(latestPpack.getVersion(), pdscVersion) >= 0) {
			return true;
		}
		return false;
	}

	/**
	 * Update the resolving pack's status
	 * @param packId Pack ID of the pack that just finished installation (maybe unsuccessful)
	 */
	protected void updateResolvingPacks(String packId) {
		Iterator<Entry<String, Collection<String>>> iter = fResolvingPacks.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, Collection<String>> entry = iter.next();
			entry.getValue().remove(packId);
			if (entry.getValue().isEmpty()) { // all required packs are installed
				iter.remove();
			}
		}
	}

}
