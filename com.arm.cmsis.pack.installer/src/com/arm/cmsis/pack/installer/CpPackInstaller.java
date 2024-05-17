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
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpEnvironmentProvider;
import com.arm.cmsis.pack.ICpPackInstaller;
import com.arm.cmsis.pack.ICpPackManager;
import com.arm.cmsis.pack.ICpPackRootProvider;
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
import com.arm.cmsis.pack.installer.jobs.CpUpdatePacksJob;
import com.arm.cmsis.pack.preferences.CpPreferenceInitializer;
import com.arm.cmsis.pack.repository.RtePackJobResult;
import com.arm.cmsis.pack.rte.RteModelUtils;
import com.arm.cmsis.pack.utils.Utils;
import com.arm.cmsis.pack.utils.VersionComparator;

/**
 * Default implementation of {@link ICpPackInstaller}
 */
public class CpPackInstaller extends PlatformObject implements ICpPackInstaller {

    private IProgressMonitor fMonitor = null;
    protected Map<String, CpPackJob> fJobQueue; // job ID -> pack job
    protected boolean bSuppressMessages = false;
    /**
     * Pack ID -> Collection of required packs' IDs installing
     */
    protected Map<String, Collection<String>> fResolvingPacks;

    public static final int TIME_OUT = 10000;

    protected ICpRepoServiceProvider fRepoServiceProvider;

    /**
     * true if Pack Manager is updating all packs.
     */
    private boolean updatingPacks = false;

    public CpPackInstaller() {
        fJobQueue = Collections.synchronizedMap(new HashMap<>());
        fResolvingPacks = Collections.synchronizedMap(new HashMap<>());
        fRepoServiceProvider = new CpRepoServiceProvider();
    }

    protected IProgressMonitor getMonitor() {
        if (fMonitor == null) {
            fMonitor = new NullProgressMonitor();
        }
        return fMonitor;
    }

    @Override
    public boolean isUpdatingPacks() {
        return updatingPacks;
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

        final ICpPackCollection installedPacks = CpPlugIn.getPackManager().getInstalledPacks();
        if (installedPacks != null && installedPacks.getPack(packId) != null) {
            return; // already installed
        }

        ICpPack pack = allPacks.getPack(packId);
        if (pack == null) {
            String familyId = CpPack.familyFromId(packId);
            pack = allPacks.getPack(familyId); // try to get latest pack and ger revision information out of that
            if (pack == null) {
                printInConsole(NLS.bind(Messages.CpPackInstaller_PackFamilyNotFound, familyId), ConsoleType.ERROR);
                return;
            }
        }
        installPack(pack, packId, installRequiredPacks);
    }

    /**
     * Installs an available pack
     *
     * @param pack ICpPack to install
     */
    private boolean installPack(ICpPack pack, String packId, boolean installRequiredPacks) {
        if (packId.equals(pack.getPackId())) {
            if (pack.getPackState().isInstalledOrLocal()) {
                return true; // already installed
            }
            if (pack.getPackState() == PackState.DOWNLOADED) {
                return unpackPack(pack, installRequiredPacks);
            }
        }
        String version = CpPack.versionFromId(packId);
        if (version.isEmpty()) {
            packId = pack.getPackId();
            version = pack.getVersion();
        }
        String downloadUrl = pack.getDownloadUrl(version);
        if (downloadUrl.endsWith(CmsisConstants.SLASH)) {
            downloadUrl += packId + CmsisConstants.EXT_PACK;
        }
        return installPack(packId, downloadUrl, installRequiredPacks);

    }

    @Override
    public void installPack(IAttributes packAttributes) {
        String packId = RteModelUtils.constructEffectivePackId(packAttributes);
        installPack(packId, true);
    }

    @Override
    public Collection<String> installRequiredPacks(ICpPack pack) {
        Collection<String> reqPacks = new HashSet<>();
        if (pack == null || !pack.getPackState().isInstalledOrLocal()) {
            return reqPacks;
        }
        Collection<? extends ICpItem> requiredPacks = pack.getRequiredPacks();
        if (requiredPacks == null || requiredPacks.isEmpty()) {
            return reqPacks;
        }
        ICpPackManager pm = CpPlugIn.getPackManager();
        ICpPackCollection allPacks = pm.getPacks();
        ICpPackCollection installedPacks = pm.getInstalledPacks();
        for (ICpItem requiredPack : requiredPacks) {
            if (installedPacks != null && installedPacks.getPack(requiredPack.attributes()) != null) {
                continue;
            }

            // check releases' versions
            String vendor = requiredPack.getVendor();
            String name = requiredPack.getName();
            String versionRange = requiredPack.getVersion();
            String familyId = vendor + '.' + name;

            ICpPack latestPack = allPacks.getPack(familyId);
            if (latestPack == null) {
                printInConsole(NLS.bind(Messages.CpPackInstaller_RequiredPackFamilyNotExist, familyId),
                        ConsoleType.WARNING);
                continue;
            }
            Collection<? extends ICpItem> releases = latestPack.getReleases();
            if (releases == null || releases.isEmpty()) {
                printInConsole(NLS.bind(Messages.CpPackInstaller_NoVersionOfPackFamilyIsFound, familyId),
                        ConsoleType.WARNING);
                continue;
            }
            boolean compatibleVersionFound = false;
            for (ICpItem release : releases) {
                String version = release.getAttribute(CmsisConstants.VERSION);
                if (VersionComparator.matchVersionRange(version, versionRange)) {
                    String packId = familyId + '.' + VersionComparator.removeMetadata(version);
                    installPack(packId, true);
                    reqPacks.add(packId);
                    compatibleVersionFound = true;
                    break;
                }
            }
            if (!compatibleVersionFound) {
                printInConsole(NLS.bind(Messages.CpPackInstaller_NoCompatibleVersionIsFound, familyId, versionRange),
                        ConsoleType.WARNING);
            }
        }

        if (!reqPacks.isEmpty()) {
            StringBuilder output = new StringBuilder(
                    NLS.bind(Messages.CpPackInstaller_InstallingRequiredPacks, pack.getId()));
            for (String reqPack : reqPacks) {
                output.append(reqPack + ", "); //$NON-NLS-1$
            }
            output.delete(output.length() - 2, output.length());
            printInConsole(output.toString(), ConsoleType.INFO);
        }

        return reqPacks;
    }

    /**
     * Install pack with pack id and download url
     *
     * @param packId               full pack id with version
     * @param url                  URL of this pack family
     * @param installRequiredPacks True if the required packs should also be
     *                             installed
     * @return True if the job is added to the job queue
     */
    private boolean installPack(String packId, String url, boolean installRequiredPacks) {
        if (fJobQueue.containsKey(packId)) {
            return false;
        }
        CpPackJob job = new CpPackInstallJob(NLS.bind(Messages.CpPackInstaller_InstallingPack, packId), this, packId,
                url, installRequiredPacks);
        job.setUser(true);
        fJobQueue.put(packId, job);
        job.schedule();
        return true;
    }

    protected void popupInstallError(String errorMessage) {
        if (isSuppressMessages())
            return; // do nothing
        Display.getDefault().asyncExec(() -> {
            MessageDialog.openError(null, Messages.CpPackInstaller_NoPacksFound, errorMessage);
        });
    }

    @Override
    public void importPack(String filePath) {
        String packId = Utils.extractBaseFileName(filePath);
        if (fJobQueue.containsKey(packId)) {
            return;
        }
        CpPackJob job = new CpPackImportJob(NLS.bind(Messages.CpPackInstaller_ImportingPack, packId), this, packId,
                filePath);
        job.setUser(true);
        fJobQueue.put(packId, job);
        job.schedule();
    }

    @Override
    public void importFolderPacks(String rootPath) {
        List<String> files = new ArrayList<>();
        Utils.findPdscFiles(new File(rootPath), files, 256);
        String jobId = files.stream().map(filename -> CpPlugIn.getPackManager().readPack(filename))
                .filter(pack -> pack != null).map(pack -> pack.getId()).collect(Collectors.joining(",")); //$NON-NLS-1$
        CpPackJob job = new CpPackImportFolderJob(NLS.bind(Messages.CpPackInstaller_ImportingFolderPacks, rootPath),
                this, jobId, rootPath);
        job.setUser(true);
        fJobQueue.put(jobId, job);
        job.schedule();
    }

    /**
     * Unpack the pack that stays in the .Download folder
     *
     * @param pack                 The pack to unzip
     * @param installRequiredPacks true if the required packs should also be
     *                             installed
     * @return True if the job is added to the job queue
     */
    private boolean unpackPack(ICpPack pack, boolean installRequiredPacks) {
        String id = pack.getId();
        if (fJobQueue.containsKey(id)) {
            return false;
        }
        CpPackJob job = new CpPackUnpackJob(NLS.bind(Messages.CpPackInstaller_UnpackingPack, pack.getId()), this, id,
                installRequiredPacks);
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
            jobName = NLS.bind(Messages.CpPackInstaller_DeletingPack, pack.getTag());
            if (pack.getPackState() == PackState.ERROR) {
                String path = pack.getFileName();
                path = Utils.extractPath(path, false);
                if (path.lastIndexOf(CmsisConstants.DOT_DOWNLOAD) != -1
                        || path.lastIndexOf(CmsisConstants.DOT_LOCAL) != -1) {
                    jobName = NLS.bind(Messages.CpPackInstaller_DeletingFile, pack.getFileName());
                } else {
                    jobName = NLS.bind(Messages.CpPackInstaller_DeletingFolder, path);
                }
                id = pack.getFileName();
            }

        }
        CpPackJob job = new CpPackRemoveJob(jobName, this, pack, delete);
        job.setUser(true);
        fJobQueue.put(id, job);
        job.schedule();
    }

    @Override
    public boolean unzip(File archiveFile, IPath destPath, IProgressMonitor monitor) throws IOException {
        SubMonitor progress = null;
        progress = SubMonitor.convert(monitor, ICpPackInstaller.getFilesCount(archiveFile));

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
            try {
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
            } catch (IOException e) {
                if (e.getMessage().contains("Access is denied")) { //$NON-NLS-1$
                    if (!outFile.exists()) {
                        zipInput.closeEntry();
                        zipInput.close();
                        throw e;
                    }
                }
            }
        }
        zipInput.closeEntry();
        zipInput.close();
        if (countBytes == 0) { // something went wrong, empty archive?
            throw new IOException(); // caller adds message
        }
        return result;
    }

    @Override
    public IProject copyExample(ICpExample example) {

        ICpEnvironmentProvider envProvider = CpPlugIn.getEnvironmentProvider();
        if (envProvider == null) {
            return null;
        }
        IAdaptable copyResult = envProvider.copyExample(example);
        if (copyResult == null) {
            return null;
        }
        return copyResult.getAdapter(IProject.class);
    }

    @Override
    public void updatePacksAsync() {
        if (isProcessing(CmsisConstants.REPO_KEIL_PINDEX_FILE)) {
            return;
        }
        CpPackJob job = new CpUpdatePacksJob(Messages.CpPackInstaller_RefreshPacks, this,
                CmsisConstants.REPO_KEIL_PINDEX_FILE);
        job.setUser(true);
        fJobQueue.put(CmsisConstants.REPO_KEIL_PINDEX_FILE, job);
        job.schedule();
    }

    @Override
    public void updatePacks(IProgressMonitor monitor) {
        fMonitor = monitor;
        if (getMonitor().isCanceled())
            return;
        updatingPacks = true;
        CpPlugIn.getDefault().emitRteEvent(RteEvent.PACK_UPDATE_JOB_STARTED);
        updatePacks();
        updatingPacks = false;
        CpPlugIn.getDefault().emitRteEvent(RteEvent.PACK_UPDATE_JOB_FINISHED);
    }

    @Override
    public synchronized void jobFinished(String jobId, String jobTopic, RtePackJobResult jobResult) {
        CpPackJob job = fJobQueue.remove(jobId);
        String jobName = job != null ? job.getName() : Messages.CpPackInstaller_Processing;

        // job is finished, update the resolving status (fResolvingPacks)
        updateResolvingPacks(jobId);

        String output = new SimpleDateFormat("HH:mm:ss").format(new Date()); //$NON-NLS-1$
        output += ": " + jobName; //$NON-NLS-1$
        if (jobResult != null && jobResult.isSuccess()) {
            output += Messages.CpPackInstaller_Completed;
            printInConsole(output, ConsoleType.OUTPUT);

            if (job != null && job.installRequiredPacks()) {
                Collection<String> reqPacks = installRequiredPacks(jobResult.getPack());
                if (reqPacks != null && !reqPacks.isEmpty()) {
                    fResolvingPacks.put(jobId, reqPacks);
                }
            }
            if (jobTopic != null) {
                CpPlugIn.getDefault().emitRteEvent(jobTopic, jobResult);
            }
        } else {
            output += Messages.CpPackInstaller_WasNotSuccessful;
            if (jobResult != null && jobResult.getErrorString() != null) {
                output += ": " + jobResult.getErrorString(); //$NON-NLS-1$
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

        if (!isBusy())
            return false;
        return fJobQueue.containsKey(packId) || fResolvingPacks.containsKey(packId);
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
    public boolean isSuppressMessages() {
        return bSuppressMessages || !PlatformUI.isWorkbenchRunning();
    }

    @Override
    public void setSuppressMessages(boolean bSuppress) {
        bSuppressMessages = bSuppress;
    }

    @Override
    public void printInConsole(String message, ConsoleType type) {
        if (isSuppressMessages())
            return;
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
        default:
            break;
        }
    }

    /***************** Here begins the Update Packs part *****************/
    protected void updatePacks() {
        if (CpPlugIn.getPackManager().getCmsisPackRootDirectory() == null
                || CpPlugIn.getPackManager().getCmsisPackRootDirectory().isEmpty()) {
            // if this is not an automatic update, print the message on the console
            if (!(getMonitor() instanceof NullProgressMonitor)) {
                printInConsole(Messages.CpPackInstaller_SetCmsisPackRootFolderAndTryAgain, ConsoleType.ERROR);
            }
            return;
        }

        boolean success = true;

        String indexUrl = null;
        ICpPackRootProvider packRootProvider = CpPreferenceInitializer.getCmsisRootProvider();
        if (packRootProvider != null)
            indexUrl = packRootProvider.getPackIndexUrl();
        if (indexUrl == null || indexUrl.isEmpty()) {
            indexUrl = CmsisConstants.REPO_KEIL_INDEX_URL;
        }
        List<String[]> indexList = new ArrayList<>();

        try {
            boolean needsUpdate = false;
            int count = getRepoServiceProvider().readIndexFile(indexUrl, indexList, getMonitor());

            // collect all pdsc references in this site
            if (count > 0) {
                needsUpdate = true;
            } else if (count == -1) { // this index file is not correctly downloaded/parsed
                success = false;
            }

            // Set total number of work units to the number of pdsc files
            getMonitor().beginTask(Messages.CpPackInstaller_RefreshAllPacks, indexList.size() + 7);

            // Read all .pdsc files and collect summary if index.pidx's time stamp changes
            if (needsUpdate) {
                updatePdscFiles(indexList);
            }

            getMonitor().worked(1); // Should reach 100% now

        } catch (Exception e) {
            printInConsole(e.toString(), ConsoleType.ERROR);
            success = false;
        }

        if (getMonitor().isCanceled()) {
            printInConsole(Messages.CpPackInstaller_JobCancelled, ConsoleType.WARNING);
        } else if (success) {
            updateWebAndLocalFolders(indexList);
            if (!getMonitor().isCanceled()) {
                CpPreferenceInitializer.updateLastUpdateTime(true);
            }
        }
        printInConsole(Messages.CpPackInstaller_PackUpdatesCompleted, ConsoleType.INFO);

        CpPlugIn.getPackManager().reload();
    }

    /**
     * Updates .Web and .Local folders Remove .pdsc files that are in .Web folder
     * but not listed in index.pidx file Remove .pdsc files that are in .Local
     * folder but listed in index.pidx file Update .pdsc files in .Local folder if
     * needed
     *
     * @param indexList a list of .pdsc files in index.pidx, each item is { url,
     *                  name, version }
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
            indexPdscFiles.add(entry[1]);
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
        ICpPackCollection allPacks = manager.getPacks();
        if (allPacks == null)
            return;
        Map<String, ICpPackFamily> families = allPacks.getFamilies();
        for (Entry<String, ICpPackFamily> entry : families.entrySet()) {
            if (getMonitor().isCanceled()) {
                return;
            }
            ICpPackFamily family = entry.getValue();
            ICpPack latestPack = family.getPack();
            if (latestPack == null || latestPack.isDeprecated())
                continue;
            String familyId = entry.getKey();
            final String pdscName = familyId + CmsisConstants.EXT_PDSC;
            if (indexPdscFiles.contains(pdscName)) {
                continue;
            }
            if (latestPack.getPackState() != PackState.INSTALLED) {
                continue;
            }
            final String pdscUrl = latestPack.getUrl();
            final String destFileName = localFolder.append(pdscName).toOSString();
            downloadPdscFile(pdscUrl, pdscName, destFileName);
        }
    }

    /**
     * Downloads pdsc files that need to be updated
     *
     * @param list List of packs, each entry is like {url, name, version}
     */
    protected void updatePdscFiles(List<String[]> list) {

        IPath webFolder = new Path(CpPlugIn.getPackManager().getCmsisPackWebDir());

        // repo keys: { "type", "url", "list" }
        String pdscServer = null;
        ICpPackRootProvider packRootProvider = CpPreferenceInitializer.getCmsisRootProvider();
        if (packRootProvider != null) {
            // take pdsc download URL from ICpPackRootProvider,
            // the default returns CmsisConstants.REPO_KEIL_PACK_SERVER :
            // "https://www.keil.com/pack/"

            pdscServer = packRootProvider.getPackPdscUrl();
            if (pdscServer == null || pdscServer.isEmpty())
                pdscServer = null;
        }

        // String[] { url, name, version }
        for (int i = 0; i < list.size(); i++) {
            String[] pdsc = list.get(i);

            if (getMonitor().isCanceled()) {
                break;
            }

            final String pdscUrl = (pdscServer != null) ? pdscServer : pdsc[0];
            final String pdscName = pdsc[1];
            final String pdscVersion = pdsc[2];
            final String packFamilyId = Utils.extractBaseFileName(pdscName);

            String destFileName = webFolder.append(pdscName).toOSString();

            // if this is not .idx file and we have already higher version, skip
            if (pdscName.endsWith(CmsisConstants.EXT_PDSC) && new File(destFileName).exists()
                    && skipUpdate(pdscUrl, packFamilyId, pdscVersion)) {
                getMonitor().worked(1);
                continue;
            }
            downloadPdscFile(pdscUrl, pdscName, destFileName);
            getMonitor().worked(1);
        }
    }

    /**
     * Download the pdsc file with url and name
     *
     * @param pdscUrl      URL of the pdsc file
     * @param pdscName     pdsc file name
     * @param destFileName destination file name
     * @return true if this pdsc file is successfully downloaded, false otherwise
     *         False if this pdsc file needs to be downloaded again.
     */
    protected boolean downloadPdscFile(String pdscUrl, String pdscName, String destFileName) {

        pdscUrl = Utils.addTrailingSlash(pdscUrl); // ensure traling slash
        while (true) { // while for timeout
            if (getMonitor().isCanceled())
                return false;
            try {
                fRepoServiceProvider.getPdscFile(pdscUrl, pdscName, destFileName, getMonitor());
            } catch (FileNotFoundException e) {
                String url = pdscUrl + pdscName;
                printInConsole(NLS.bind(Messages.CpPackInstallJob_FileNotFound, url), ConsoleType.ERROR);
                return false;
            } catch (UnknownHostException e) {
                printInConsole(NLS.bind(Messages.CpPackInstallJob_UnknownHostException, e.getMessage()),
                        ConsoleType.ERROR);
                return false;
            } catch (SocketTimeoutException e) {
                int wait = timeoutQuestion(pdscUrl);
                if (wait == 1) { // No
                    return false;
                } else if (wait == 0) { // Yes
                    printInConsole(NLS.bind(Messages.CpPackInstaller_TimeoutConsoleMessage, pdscName, pdscUrl),
                            ConsoleType.WARNING);
                    continue;
                } else { // Cancel
                    getMonitor().setCanceled(true);
                    return false;
                }
            } catch (InterruptedIOException e) {
                printInConsole(e.getMessage(), ConsoleType.ERROR);
                return false;
            } catch (IOException e) {
                printInConsole(NLS.bind(Messages.CpPackInstaller_ErrorWhileRefreshingIgnored, pdscUrl, e.getMessage()),
                        ConsoleType.WARNING);
                return false;
            }
            break;
        }
        // One more unit completed
        getMonitor().worked(1);
        return true;
    }

    /***************** Here begins utility function part *****************/
    protected int timeoutQuestion(String pdscUrl) {
        if (isSuppressMessages())
            return 0; // do not wait

        RunnableWithIntResult runnable = new RunnableWithIntResult() {
            @Override
            public void run() {
                MessageDialog dialog = new MessageDialog(null, Messages.CpPackInstaller_Timout, null,
                        NLS.bind(Messages.CpPackInstaller_TimeoutMessage, pdscUrl, TIME_OUT / 1000),
                        MessageDialog.QUESTION_WITH_CANCEL, new String[] { IDialogConstants.YES_LABEL,
                                IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL },
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
     * @param pdscUrl      pdsc file's url
     * @param packFamilyId pack family id
     * @param pdscVersion  pdsc file's version
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
        return VersionComparator.versionCompare(latestPpack.getVersion(), pdscVersion) >= 0;
    }

    /**
     * Update the resolving pack's status
     *
     * @param packId Pack ID of the pack that just finished installation (maybe
     *               unsuccessful)
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
