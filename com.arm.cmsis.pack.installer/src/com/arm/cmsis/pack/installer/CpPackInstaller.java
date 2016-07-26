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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;

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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpEnvironmentProvider;
import com.arm.cmsis.pack.ICpPackInstaller;
import com.arm.cmsis.pack.ICpPackManager;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpPack;
import com.arm.cmsis.pack.data.ICpExample;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPack.PackState;
import com.arm.cmsis.pack.data.ICpPackCollection;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.events.RtePackJobResult;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.installer.console.ConsoleStream;
import com.arm.cmsis.pack.installer.jobs.CpPackInstallJob;
import com.arm.cmsis.pack.installer.jobs.CpPackJob;
import com.arm.cmsis.pack.installer.jobs.CpPackRemoveJob;
import com.arm.cmsis.pack.installer.jobs.CpPackUnpackJob;
import com.arm.cmsis.pack.installer.jobs.LicenseDialog;
import com.arm.cmsis.pack.installer.utils.PackInstallerUtils;
import com.arm.cmsis.pack.installer.utils.RepositoryRefreshingUtils;
import com.arm.cmsis.pack.repository.CpRepositoryList;
import com.arm.cmsis.pack.repository.ICpRepository;
import com.arm.cmsis.pack.utils.Utils;
import com.arm.cmsis.pack.utils.VersionComparator;

/**
 * Default implementation of {@link ICpPackInstaller}
 */
public class CpPackInstaller extends PlatformObject implements ICpPackInstaller {

	private CpPackJob fJob;
	private CpRepositoryList fRepos;
	private IProgressMonitor fMonitor;
	private Map<String, CpPackJob> fJobQueue;

	/**
	 * A group of jobs, only emit RTE events when all the jobs in this queue are
	 * finished.
	 */
	private Set<String> fGroupJobQueue;

	private final static int TIME_OUT = 10000;
	int wait;
	boolean licAgreed;

	private PackWatchThread thread;
	boolean reload;

	ICpPackManager fPackManager;

	Map<ConsoleColor, Color> fColorMap = new HashMap<>();

	public CpPackInstaller() {
		fPackManager = CpPlugIn.getPackManager();
		fJobQueue = Collections.synchronizedMap(new HashMap<>());
		fGroupJobQueue = Collections.synchronizedSet(new HashSet<>());
		initColorMap();
	}

	private void initColorMap() {
		fColorMap.put(ConsoleColor.INFO, new Color(null, 0, 0, 0));
		fColorMap.put(ConsoleColor.ERROR, new Color(null, 255, 0, 0));
		fColorMap.put(ConsoleColor.WARNING, new Color(null, 0, 0, 255));
	}

	class PackWatchThread extends Thread {

		private long lastModified = 0;

		@Override
		public void run() {
			final java.nio.file.Path dirPath = FileSystems.getDefault()
					.getPath(CpPlugIn.getPackManager().getCmsisPackRootDirectory());
			final File file = new File(
					Utils.addTrailingSlash(CpPlugIn.getPackManager().getCmsisPackRootDirectory())
					+ CmsisConstants.PACK_IDX);
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try (final WatchService watcher = FileSystems.getDefault().newWatchService()) {
				dirPath.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
				while (true) {
					final WatchKey watckKey = watcher.take();
					List<WatchEvent<?>> events = watckKey.pollEvents();
					for (WatchEvent<?> event : events) {
						if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
							continue;
						}
						// we only register "ENTRY_MODIFY" so the context is
						// always a Path.
						final java.nio.file.Path changed = (java.nio.file.Path) event.context();
						if (changed.toString().equals(CmsisConstants.PACK_IDX)
								// remove glitch in file change event
								&& Math.abs(file.lastModified() - lastModified) > 3) {
							lastModified = file.lastModified();

							synchronized (CpPackInstaller.this) {
								if (!isBusy()) {
									Display.getDefault().syncExec(new Runnable() {
										@Override
										public void run() {
											reload = MessageDialog.openQuestion(null,
													Messages.CpPackInstaller_ReloadPacksAndManagerTitle,
													Messages.CpPackInstaller_ReloadPacksAndManagerMessage);
										}
									});
									if (reload) {
										CpPlugIn.getPackManager().reload();
									}
								} else { // Pack Installer is waiting to proceed
									CpPackInstaller.this.notify();
								}
								break;
							}
						}
					}
					// reset the key
					boolean valid = watckKey.reset();
					if (!valid) {
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void startPackWatchThread() {
		if (thread != null && thread.isAlive()) {
			thread.interrupt();
		}

		thread = new PackWatchThread();
		thread.start();
	}

	@Override
	public void stopPackWatchThread() {
		if (thread != null && thread.isAlive()) {
			thread.interrupt();
		}
	}

	@Override
	public void installPack(String packId) {
		ICpPackCollection allPacks = CpPlugIn.getPackManager().getPacks();
		if (allPacks == null) {
			popupInstallError(Messages.CpPackInstaller_OpenPackManagerToUpdatePacks);
			return;
		}
		ICpPack pack = allPacks.getPack(packId);
		if (pack != null) {
			if (pack.getPackState() == PackState.INSTALLED) {
				return;
			} else if (pack.getPackState() == PackState.DOWNLOADED) {
				unpackPack(pack);
			} else {
				installPack(pack.getPackId(), pack.getUrl());
			}
		} else {
			String familyId = CpPack.familyFromId(packId);
			Collection<? extends ICpItem> packs = allPacks.getPacksByPackFamilyId(familyId);
			if (packs == null) {
				printInConsole(NLS.bind(Messages.CpPackInstaller_PackFamilyNotFound, packId),
						ConsoleColor.ERROR);
				return;
			}
			ICpItem latestPack = packs.iterator().next();
			if (CpPack.isPackFamilyId(packId)) {
				packId += '.' + latestPack.getVersion();
			}
			installPack(packId, latestPack.getUrl());
		}
	}

	@Override
	public void installPack(IAttributes packAttributes) {
		String packId = CpPack.constructPackId(packAttributes);
		fGroupJobQueue.add(packId);
		installPack(packId);
	}

	/**
	 * Install pack with pack id and download url
	 *
	 * @param packId
	 *            the full pack id with version
	 * @param url
	 *            The URL of this pack family
	 */
	private void installPack(String packId, String url) {
		if (fJobQueue.containsKey(packId)) {
			return;
		}
		fJob = new CpPackInstallJob(NLS.bind(Messages.CpPackInstaller_InstallingPack, packId), this, packId, url);
		fJob.setUser(true);
		fJobQueue.put(packId, fJob);
		fJob.schedule();
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
		fJob = new CpPackUnpackJob(NLS.bind(Messages.CpPackInstaller_ImportingPack, packId), this, filePath);
		fJob.setUser(true);
		fJobQueue.put(packId, fJob);
		fJob.schedule();
	}

	/**
	 * Unpack the pack that stays in the .Download folder
	 *
	 * @param pack
	 */
	private void unpackPack(ICpPack pack) {
		if (fJobQueue.containsKey(pack.getId())) {
			return;
		}
		fJob = new CpPackUnpackJob(NLS.bind(Messages.CpPackInstaller_UnpackingPack, pack.getId()), this, pack);
		fJob.setUser(true);
		fJobQueue.put(pack.getId(), fJob);
		fJob.schedule();
	}

	@Override
	public void removePack(ICpPack pack, boolean delete) {
		if (fJobQueue.containsKey(pack.getId())) {
			return;
		}
		String jobName = NLS.bind(Messages.CpPackInstaller_RemovingPack, pack.getId());
		if (delete) {
			jobName = NLS.bind(Messages.CpPackInstaller_DeletingPack, pack.getId());
		}
		fJob = new CpPackRemoveJob(jobName, this, pack, delete);
		fJob.setUser(true);
		if (pack.getPackState() == PackState.ERROR) {
			fJobQueue.put(pack.getTag(), fJob);
		} else {
			fJobQueue.put(pack.getId(), fJob);
		}
		fJob.schedule();
	}

	@Override
	public boolean unzip(File archiveFile, IPath destPath, IProgressMonitor monitor) throws IOException {
		SubMonitor progress = SubMonitor.convert(monitor, PackInstallerUtils.getFilesCount(archiveFile));

		if (destPath.toFile().exists()) {
			Utils.deleteFolderRecursive(destPath.toFile());
		}

		File tempFolder = File.createTempFile("temp", null); //$NON-NLS-1$
		tempFolder.delete();
		tempFolder.mkdir();
		IPath tempDestPath = new Path(tempFolder.getAbsolutePath());

		boolean unzipFinished = false;
		try {
			unzipFinished = unzipTemp(archiveFile, tempDestPath, progress);
		} catch (BadLocationException e) {
		}
		if (!unzipFinished) {
			Utils.deleteFolderRecursive(tempDestPath.toFile());
			return false;
		}
		Utils.copyDirectory(tempDestPath.toFile(), destPath.toFile());
		Utils.deleteFolderRecursive(tempDestPath.toFile());
		return true;
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

		PackInstallerUtils.clearReadOnly(project.getLocation().toFile(),
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
	public void jobFinished(String packId, String jobTopic, Object jobData) {
		touchPackIdx();
		try {
			synchronized (CpPackInstaller.this) {
				wait(1000);
			}
		} catch (InterruptedException e) {
		}
		CpPackJob job = fJobQueue.remove(packId);
		String jobName = job != null ? job.getName() : Messages.CpPackInstaller_Processing;
		boolean isGroupJob = fGroupJobQueue.remove(packId);

		String output = new SimpleDateFormat("HH:mm:ss").format(new Date()); //$NON-NLS-1$
		output += ": " + jobName; //$NON-NLS-1$
		RtePackJobResult result = (RtePackJobResult) jobData;
		ConsoleColor messageColor;
		if (result != null && result.isSuccess()) {
			messageColor = ConsoleColor.INFO;
			output += Messages.CpPackInstaller_Completed;
			if (!isGroupJob) {
				CpPlugIn.getDefault().emitRteEvent(jobTopic, jobData);
				// For importing jobs, refresh the pack manager perspective
				if (Messages.CpPackInstaller_ImportingPack.equals(jobName)) {
					CpPlugIn.getPackManager().reload();
				}
			} else if (fGroupJobQueue.isEmpty()) {
				CpPlugIn.getPackManager().reload();
			}
		} else {
			messageColor = ConsoleColor.ERROR;
			output += Messages.CpPackInstaller_WasNotSuccessful;
			if (result != null) {
				output += result.getErrorString();
			}
		}
		printInConsole(output, messageColor);
	}

	private void touchPackIdx() {
		java.nio.file.Path path = FileSystems.getDefault().getPath(
				CpPlugIn.getPackManager().getCmsisPackRootDirectory(), CmsisConstants.PACK_IDX);
		try {
			Files.setLastModifiedTime(path, FileTime.fromMillis(System.currentTimeMillis()));
		} catch (IOException e) {
		}
	}

	@Override
	public boolean isBusy() {
		return !fJobQueue.isEmpty();
	}

	@Override
	public boolean isProcessing(String packId) {
		return fJobQueue.containsKey(packId);
	}

	@Override
	public boolean isProcessing(IAttributes packAttributes) {
		String packId = CpPack.constructPackId(packAttributes);
		return isProcessing(packId);
	}

	@Override
	public void reset() {
		for (Job job : fJobQueue.values()) {
			job.cancel();
		}
		fJobQueue.clear();
	}

	@Override
	public void printInConsole(String message, ConsoleColor color) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				MessageConsoleStream stream = ConsoleStream.getConsoleOut(color);
				stream.setColor(fColorMap.get(color));
				stream.println(message);
			}
		});
	}

	/***************** Here begins the Update Packs part *****************/
	private void updatePacks() {
		fPackManager = CpPlugIn.getPackManager();
		fRepos = fPackManager.getCpRepositoryList();
		long beginTime = System.currentTimeMillis();

		try {
			// String[] { url, name, version }
			List<String[]> list = new LinkedList<String[]>();

			List<ICpRepository> reposList = fRepos.getList();
			for (ICpRepository repo : reposList) {

				if (fMonitor.isCanceled()) {
					break;
				}

				String type = repo.getType();
				String indexUrl = repo.getUrl();
				if (CmsisConstants.REPO_PACK_TYPE.equals(type)) {

					// collect all pdsc references in this site
					int count = readCmsisIndex(indexUrl, list);
					if (count > 0) {
						list.add(0,
								new String[]{Utils.extractPath(indexUrl, true),
										Utils.extractFileName(indexUrl),
										CmsisConstants.EMPTY_STRING});
					}

				} else {
					printInConsole(NLS.bind(Messages.CpPackInstaller_RepoTypeNotSupported, type),
							ConsoleColor.WARNING);
				}
			}

			// Set total number of work units to the number of pdsc files
			fMonitor.beginTask(Messages.CpPackInstaller_RefreshAllPacks, list.size() + 3);

			// Read all .pdsc files and collect summary
			aggregateCmsis(list);

			fMonitor.worked(1); // Should reach 100% now

		} catch (Exception e) {
			printInConsole(e.toString(), ConsoleColor.ERROR);
		}

		if (fMonitor.isCanceled()) {
			printInConsole(Messages.CpPackInstaller_JobCancelled, ConsoleColor.WARNING);
		} else {

			fPackManager.reload();

			long endTime = System.currentTimeMillis();
			long duration = endTime - beginTime;
			if (duration == 0) {
				duration = 1;
			}
		}

	}

	/**
	 * Reads the .index file
	 *
	 * @param indexUrl
	 *            the url of .index file
	 * @param pdscList
	 *            a list of pdsc files
	 */
	private int readCmsisIndex(String indexUrl, List<String[]> pdscList) {

		printInConsole(NLS.bind(Messages.CpPackInstaller_Parsing, indexUrl), ConsoleColor.INFO);

		try {

			int count = RepositoryRefreshingUtils.readIndex(indexUrl, pdscList);

			return count;

		} catch (FileNotFoundException e) {
			printInConsole(Messages.CpPackInstaller_FileNotFound + e.getMessage(),
					ConsoleColor.ERROR);
		} catch (Exception e) {
			printInConsole(e.toString(), ConsoleColor.ERROR);
		}

		return 0;
	}

	/**
	 * collect CMSIS packs from the web
	 *
	 * @param repo
	 *            the repository map
	 */
	private void aggregateCmsis(List<String[]> list) {

		// repo keys: { "type", "url", "list" }

		// String[] { url, name, version }
		for (int i = 0; i < list.size(); i++) {
			String[] pdsc = list.get(i);

			if (fMonitor.isCanceled()) {
				break;
			}

			// Make url always end in '/'
			final String pdscUrl = Utils.addTrailingSlash(pdsc[0]);
			final String pdscName = pdsc[1];
			final String pdscVersion = pdsc[2];
			final String packFamilyId = Utils.extractBaseFileName(pdscName);

			fMonitor.subTask(NLS.bind(Messages.CpPackInstaller_Updating, pdscName, pdscUrl));

			// if this is not .idx file and we have already higher version, then
			// skip
			if (pdscName.endsWith(CmsisConstants.EXT_PDSC)
					&& skipUpdate(pdscUrl, packFamilyId, pdscVersion)) {
				fMonitor.worked(1);
				continue;
			}

			File destFileTmp = null;
			try {

				URL sourceUrl = new URL(pdscUrl + pdscName);

				IPath webFolder = new Path(PackInstallerUtils.getPacksWebDir());
				if (!webFolder.toFile().exists()) {
					webFolder.toFile().mkdir();
				}
				String destFileName = webFolder.append(pdscName).toOSString();
				String destFileNameTmp = destFileName + CmsisConstants.EXT_TEMP;
				destFileTmp = new File(destFileNameTmp);

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
						if (fMonitor.isCanceled()) {
							finished = false;
							break;
						}
					}
					output.close();
					if (input != null) {
						input.close();
					}
					if (finished) {
						File destFile = new File(destFileName);
						Utils.copy(destFileTmp, destFile);
						destFile.setReadOnly();
					}
					destFileTmp.delete();
					if (connection instanceof HttpURLConnection) {
						((HttpURLConnection) connection).disconnect();
					}
				}

			} catch (FileNotFoundException e) {
				printInConsole(NLS.bind(Messages.CpPackInstaller_ErrorWhileRefreshingCheckFolder,
						e.getMessage()), ConsoleColor.ERROR);
			} catch (SocketTimeoutException | UnknownHostException e) {
				wait = timeoutQuestion(pdscUrl);
				if (wait == 0) { // Yes
					i--;
					continue;
				} else if (wait == 1) { // No
					printInConsole(NLS.bind(Messages.CpPackInstaller_TimeoutConsoleMessage,
							pdscName, pdscUrl), ConsoleColor.WARNING);
					if (destFileTmp != null) {
						destFileTmp.delete();
					}
				} else { // Cancel
					fMonitor.setCanceled(true);
					break;
				}
			} catch (Exception e) {
				printInConsole(NLS.bind(Messages.CpPackInstaller_ErrorWhileRefreshingIgnored,
						e.getMessage()), ConsoleColor.WARNING);
			}

			// One more unit completed
			fMonitor.worked(1);
		}

	}

	/***************** Here begins utility function part *****************/
	protected boolean unzipTemp(File archiveFile, IPath destPath, IProgressMonitor progress) throws IOException, BadLocationException {

		boolean result = true;
		boolean containLic = false;
		String licenseFileName = CmsisConstants.EMPTY_STRING;
		File licenseFile = null;
		licAgreed = true;

		ZipInputStream zipInput;
		zipInput = new ZipInputStream(new FileInputStream(archiveFile));
		ZipEntry zipEntry = zipInput.getNextEntry();

		int countBytes = 0;
		while (zipEntry != null) {

			if (progress.isCanceled()) {
				result = false;
				break;
			}

			if (!zipEntry.isDirectory()) {

				String fileName = zipEntry.getName();

				IPath path = destPath.append(fileName);
				File outFile = new File(path.toOSString());
				if (!outFile.getParentFile().exists()) {
					outFile.getParentFile().mkdirs();
				}

				OutputStream output = new FileOutputStream(outFile);

				byte[] buf = new byte[1024];
				int bytesRead;
				while ((bytesRead = zipInput.read(buf)) > 0) {
					output.write(buf, 0, bytesRead);
					countBytes += bytesRead;
				}
				output.close();

				outFile.setReadOnly();

				if (outFile.toString().endsWith(CmsisConstants.EXT_PDSC)) {
					ICpPack pack = (ICpPack) CpPlugIn.getPackManager().getParser().parseFile(outFile.toString());
					if (pack != null && pack.getFirstChild(CmsisConstants.LICENSE_TAG) != null) {
						containLic = true;
						licenseFileName = pack.getFirstChild(CmsisConstants.LICENSE_TAG).getText().replace('\\', '/');
					}
				}

				if (containLic && licenseFileName.equals(fileName)) {
					licenseFile = outFile;
				}

				progress.worked(1);

			}

			zipEntry = zipInput.getNextEntry();
		}

		zipInput.closeEntry();
		zipInput.close();
		if (countBytes <= 0) {
			result = false;
		}

		// if not cancelled by user, contains license file, ask if the user to agree
		if (result && containLic && licenseFile != null) {
			progress.setTaskName(Messages.PackInstallerUtils_PleaseAgreeLicenseAgreement);

			String absolutePath = licenseFile.getAbsolutePath();
			byte[] allBytes = Files.readAllBytes(Paths.get(absolutePath));
			String fileExt = Utils.extractFileExtension(absolutePath);

			String packName = Utils.extractBaseFileName(archiveFile.getName());

			String text;
			if ("rtf".equalsIgnoreCase(fileExt)) { //$NON-NLS-1$
				RTFEditorKit rtfParser = new RTFEditorKit();
				Document document = rtfParser.createDefaultDocument();
				rtfParser.read(new ByteArrayInputStream(allBytes), document, 0);
				text = document.getText(0, document.getLength());
			} else {
				text = new String(allBytes,	Charset.defaultCharset());
			}
			licAgreed = licenseQuestion(packName, text, destPath);
			if (!licAgreed) {
				Utils.deleteFolderRecursive(destPath.toFile());
			}
			result = licAgreed;
		}
		progress.setTaskName(Messages.CpPackInstaller_FinishingOperation);

		return result;
	}

	protected int timeoutQuestion(String pdscUrl) {
		Display.getDefault().syncExec(() -> {
			MessageDialog dialog = new MessageDialog(null, Messages.CpPackInstaller_Timout, null,
					NLS.bind(Messages.CpPackInstaller_TimeoutMessage, pdscUrl, TIME_OUT / 1000),
					MessageDialog.QUESTION_WITH_CANCEL, new String[]{IDialogConstants.YES_LABEL,
							IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL},
					0);
			wait = dialog.open();
		});
		return wait;
	}

	protected boolean licenseQuestion(String packname, String licenseText, IPath destPath) {
		Display.getDefault().syncExec(() -> {
			LicenseDialog dlg = new LicenseDialog(null, packname, licenseText);
			licAgreed = dlg.open() == Window.OK;
		});
		return licAgreed;
	}

	/**
	 * Determine if this .pdsc file should be skipped when updating from the web
	 *
	 * @param pdscUrl
	 *            the pdsc file's url
	 * @param packFamilyId
	 *            the pack family id
	 * @param pdscVersion
	 *            .pdsc file's version
	 * @return true if should be skipped for updating
	 */
	private boolean skipUpdate(String pdscUrl, String packFamilyId, String pdscVersion) {
		if (!pdscUrl.contains(CmsisConstants.REPO_KEILWEB)) {
			return false;
		}
		if (fPackManager.getPacks() == null) {
			return false;
		}
		if (fPackManager.getPacks().getPacksByPackFamilyId(packFamilyId) == null) {
			return false;
		}
		for (ICpItem item : fPackManager.getPacks().getPacksByPackFamilyId(packFamilyId)) {
			if (item instanceof ICpPack) {
				ICpPack pack = (ICpPack) item;
				if (new VersionComparator().compare(pack.getVersion(), pdscVersion) <= 0) {
					return true;
				}
			}
		}

		return false;
	}

}
