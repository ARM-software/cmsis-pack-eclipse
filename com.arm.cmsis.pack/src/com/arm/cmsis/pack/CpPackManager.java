/*******************************************************************************
 * Copyright (c) 2015 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 * A snippet for file change monitoring is taken from:
 * http://stackoverflow.com/questions/16251273/can-i-watch-for-single-file-change-with-watchservice-not-the-whole-directory
 *******************************************************************************/

package com.arm.cmsis.pack;


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

import com.arm.cmsis.pack.ICpPackInstaller.ConsoleType;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpPack;
import com.arm.cmsis.pack.data.CpPackCollection;
import com.arm.cmsis.pack.data.CpPackFamily;
import com.arm.cmsis.pack.data.ICpBoard;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPack.PackState;
import com.arm.cmsis.pack.data.ICpPackCollection;
import com.arm.cmsis.pack.data.ICpPackFamily;
import com.arm.cmsis.pack.events.IRteEventProxy;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.events.RteEventListener;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.parser.CpPidxParser;
import com.arm.cmsis.pack.parser.ICpXmlParser;
import com.arm.cmsis.pack.parser.PdscParser;
import com.arm.cmsis.pack.preferences.CpPreferenceInitializer;
import com.arm.cmsis.pack.repository.CpRepositoryList;
import com.arm.cmsis.pack.repository.RtePackJobResult;
import com.arm.cmsis.pack.rte.boards.IRteBoardItem;
import com.arm.cmsis.pack.rte.boards.RteBoardItem;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;
import com.arm.cmsis.pack.rte.devices.IRteDeviceRoot;
import com.arm.cmsis.pack.rte.devices.RteDeviceRoot;
import com.arm.cmsis.pack.rte.examples.IRteExampleItem;
import com.arm.cmsis.pack.rte.examples.RteExampleItem;
import com.arm.cmsis.pack.utils.FileChangeWatcher;
import com.arm.cmsis.pack.utils.Utils;
import com.arm.cmsis.pack.utils.VersionComparator;

/**
 * Default simple CMSIS-Pack manager
 */
public class CpPackManager extends RteEventListener implements ICpPackManager {

	protected ICpPackCollection allPacks = null; // global pack collection
	protected ICpPackCollection allInstalledPacks = null; // all installed pack collection
	protected ICpPackCollection allGenericPacks = null; // generic pack collection
	protected ICpPackCollection allDevicePacks = null; // device-specific pack collection
	protected ICpPackFamily allErrorPacks = null; // error pack collection

	protected ICpXmlParser pdscParser = null;
	protected IRteDeviceRoot allDevices = null;
	protected IRteDeviceRoot allInstalledDevices = null;
	protected Map<String, ICpBoard> allBoards = null;
	protected IRteBoardItem allRteBoards = null;
	protected IRteExampleItem allExamples = null;
	protected String cmsisPackRootDirectory = null;
	protected URI cmsisPackRootURI = null;

	protected boolean bPacksLoaded = false;
	protected boolean bReloading = false; // reload is in progress
	protected boolean bReloadPending = false; // reload is requested, but pack installer is still busy
	protected boolean bCheckForUpdates = false;; 

	protected IRteEventProxy fRteEventProxy = null;
	protected ICpPackInstaller fPackInstaller = null;
	protected CpRepositoryList fRepoList = null;

	protected Map<String, ICpPack> fGeneratedPacks = null;

	protected ICpPack.PackState packState = PackState.UNKNOWN;

	protected PackIdxWatcher packIdxWatcher = null;
	protected GpdscWatcher gpdscWatcher = new GpdscWatcher();

	class PackIdxWatcher extends FileChangeWatcher {

		public PackIdxWatcher(){
			super(FileChangeWatcher.ALL);
		}

		public void restartWatch() {
			clearWatch();
			String idxFile = getPackIdxFile();
			if(idxFile == null || idxFile.isEmpty()) {
				return;
			}
			File file = new File(idxFile);
			if (!file.exists()) {
				try {
					// ensure directory exists
					String dir = getCmsisPackRootDirectory();
					FileChangeWatcher.createDirectories(dir);
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
			registerFile(idxFile);
			startWatch();
		}

		@Override
		protected void action(String file, int kind) {
			if (fPackInstaller == null || !fPackInstaller.isBusy()) {
				if(!isReloadPending()) {
					reload();
				}
			}
		}
	}


	class GpdscWatcher extends FileChangeWatcher {

		public GpdscWatcher(){
			super(FileChangeWatcher.ALL);
		}

		@Override
		public synchronized void registerFile(String file) {
			super.registerFile(file);
			startWatch();
		}

		@Override
		protected void action(String file, int kind) {
			refreshGpdsc(file, kind);
		}
	}


	/**
	 * Start watch the pack.idx file in the pack root folder
	 */
	protected void startPackIdxWatcher() {
		if (packIdxWatcher == null) {
			packIdxWatcher = new PackIdxWatcher();
		}
		packIdxWatcher.restartWatch();
	}

	/**
	 * Stop watch the pack.idx file in the pack root folder
	 */
	protected void stopPackIdxWatcher() {
		if (packIdxWatcher != null) {
			packIdxWatcher.stopWatch();
		}
	}

	/**
	 * Stop watch the pack.idx file in the pack root folder
	 */
	protected void clearPackIdxWatcher() {
		if (packIdxWatcher != null) {
			packIdxWatcher.clearWatch();
			packIdxWatcher = null;
		}
	}

	/**
	 *  Default pack manager implementation
	 */
	public CpPackManager() {
	}

	public String getPackIdxFile() {
		String idxFile = getCmsisPackRootDirectory();
		if(idxFile != null && !idxFile.isEmpty()) {
			idxFile = Utils.addTrailingSlash(idxFile) + CmsisConstants.PACK_IDX;
		}
		return idxFile;
	}


	@Override
	public void setPackInstaller(ICpPackInstaller packInstaller) {
		fPackInstaller = packInstaller;
	}

	@Override
	public ICpPackInstaller getPackInstaller() {
		return fPackInstaller;
	}



	@Override
	public boolean initParser(String xsdFile){
		if(pdscParser == null) {
			pdscParser = new PdscParser(xsdFile);
		} else {
			pdscParser.setXsdFile(xsdFile);
		}
		return pdscParser.init();
	}

	@Override
	synchronized public void clear() {
		allPacks = null;
		allErrorPacks = null;
		allInstalledPacks = null;
		allGenericPacks = null;
		allDevicePacks = null;
		allDevices = null;
		allInstalledDevices = null;
		allBoards = null;
		allRteBoards = null;
		allExamples = null;
		fGeneratedPacks = null;
		bPacksLoaded = false;
		if(pdscParser != null) {
			pdscParser.clear();
		}
	}

	@Override
	public void reload() {
		if(isReloading()) {
			return;
		}
		setReloading(true);

		clear();
		getPacks(); // triggers load
		if(fPackInstaller != null) {
			fPackInstaller.reset();
		}
		emitRteEvent(RteEvent.PACKS_RELOADED);

		setReloadPending(false);
		setReloading(false);
	}

	protected synchronized boolean isReloading() {
		return bReloading;
	}

	protected synchronized void setReloading(boolean loading) {
		bReloading = loading;
	}


	synchronized boolean isReloadPending() {
		return bReloadPending;
	}

	synchronized void setReloadPending(boolean pending) {
		bReloadPending = pending;
	}


	@Override
	public void destroy() {

		clear();
		pdscParser = null;
		bReloading = false;
		bReloadPending = false;
		clearPackIdxWatcher();
		gpdscWatcher.clearWatch();
		gpdscWatcher = null;
	}

	@Override
	synchronized public ICpPackCollection getPacks() {
		if(allPacks == null) {
			bPacksLoaded = loadPacks(cmsisPackRootDirectory);
		}
		return allPacks;
	}

	@Override
	synchronized public ICpPackCollection getInstalledPacks() {
		if(allPacks == null) {
			bPacksLoaded = loadPacks(cmsisPackRootDirectory);
		}
		return allInstalledPacks;
	}

	@Override
	synchronized public ICpPackCollection getDevicePacks() {
		if(allPacks == null) {
			bPacksLoaded = loadPacks(cmsisPackRootDirectory);
		}
		return allDevicePacks;
	}

	@Override
	synchronized public ICpPackCollection getGenericPacks() {
		if(allPacks == null) {
			bPacksLoaded = loadPacks(cmsisPackRootDirectory);
		}
		return allGenericPacks;
	}

	@Override
	synchronized public ICpPackFamily getErrorPacks() {
		return allErrorPacks;
	}

	@Override
	synchronized public IRteDeviceItem getDevices() {
		getPacks(); // ensure allPacks are loaded
		if(allDevices == null && bPacksLoaded && allPacks != null)  {
			allDevices = RteDeviceRoot.createTree(allPacks.getLatestEffectivePacks());
		}
		return allDevices;
	}

	@Override
	synchronized public IRteDeviceItem getInstalledDevices() {
		getPacks(); // ensure allPacks are loaded
		if(allInstalledDevices == null && bPacksLoaded && allInstalledPacks != null)  {
			allInstalledDevices = RteDeviceRoot.createTree(allInstalledPacks.getLatestInstalledPacks());
		}
		return allInstalledDevices;
	}

	@Override
	synchronized public Map<String, ICpBoard> getBoards() {
		getPacks(); // ensure allPacks are loaded
		if(allBoards == null && bPacksLoaded && allPacks != null)  {
			collectBoards();
		}
		return allBoards;
	}
	
	@Override
	public ICpBoard getBoard(String boardId) {
		if(boardId == null || boardId.isEmpty())
			return null;
		
		Map<String, ICpBoard> allBoards = getBoards();
		if (allBoards != null) {
			ICpBoard item = allBoards.get(boardId);
			return item;
		}
		return null;
	}

	

	protected void collectBoards() {
		allBoards = new HashMap<String, ICpBoard>();
		Collection<ICpPack> packs = allPacks.getPacks();
		for(ICpPack pack: packs) {
			addBoards(pack);
		}
	}

	@Override
	synchronized public IRteBoardItem getRteBoards() {
		getPacks(); // ensure allPacks are loaded
		if(allRteBoards == null && bPacksLoaded && allPacks != null)  {
			allRteBoards = RteBoardItem.createTree(allPacks.getLatestEffectivePacks());
		}
		return allRteBoards;
	}

	@Override
	public Collection<ICpBoard> getCompatibleBoards(IAttributes deviceAttributes) {
		List<ICpBoard> boards =  new LinkedList<ICpBoard>();
		getBoards();
		if(allBoards == null || allBoards.isEmpty()) {
			return boards;
		}
		for(ICpBoard b : allBoards.values()){
			if(b.hasCompatibleDevice(deviceAttributes)) {
				boards.add(b);
			}
		}
		return boards;
	}

	@Override
	synchronized public IRteExampleItem getExamples() {
		getPacks(); // ensure allPacks are loaded
		if(allExamples == null && bPacksLoaded && allPacks != null)  {
			allExamples = RteExampleItem.createTree(allPacks.getPacks());
		}
		return allExamples;
	}

	@Override
	synchronized public boolean loadPacks(final String rootDirectory){
		if(rootDirectory == null || rootDirectory.isEmpty()) {
			return false;
		}

		File root = new File(rootDirectory);
		if( !root.exists()) {
			return false;
		}

		packState = PackState.AVAILABLE;
		File webFile = new File(getCmsisPackWebDir());
		Collection<String> availableFileNames = Utils.findPdscFiles(webFile, null, 0);
		loadPacks(availableFileNames);
		
		File localFile = new File(getCmsisPackLocalDir());
		Collection<String> localFileNames = Utils.findPdscFiles(localFile, null, 0);
		loadPacks(localFileNames);

		packState = PackState.DOWNLOADED;
		File downloadFile = new File(getCmsisPackDownloadDir());
		Collection<String> downloadedFileNames = Utils.findPdscFiles(downloadFile, null, 0);
		loadPacks(downloadedFileNames);

		packState = PackState.LOCAL;
		Collection<String> localRepoistoryFileNames = CpPidxParser.getLocalRepositoryFileNames(getCmsisPackLocalDir());
		loadPacks(localRepoistoryFileNames);
		
		packState = PackState.INSTALLED;		
		Collection<String> installedFileNames = Utils.findPdscFiles(root, null, 3);
		loadPacks(installedFileNames);

		packState = PackState.UNKNOWN;

		return true;
	}


	@Override
	public boolean loadPacks(final Collection<String> fileNames){
		if(fileNames == null || fileNames.isEmpty()) {
			return true; // nothing to load => success
		}

		boolean success = true;
		for(String f : fileNames) {
			if(loadPack(f) == null) {
				success = false;
			}
		}
		return success;
	}

	@Override
	public ICpPack readPack(String file){
		if (pdscParser == null) {
			initParser(null);
		}
		ICpItem item = pdscParser.parseFile(file);
		if(item != null && item instanceof ICpPack) {
			return (ICpPack)item;
		}

		if(pdscParser.getErrorCount() > 0) {
			List<String> errors = pdscParser.getErrorStrings();
			if(errors != null && !errors.isEmpty()) {
				for(String msg : errors) {
					if(msg != null && !msg.isEmpty()){
						getRteEventProxy().emitRteEvent(RteEvent.PRINT_ERROR, msg);
					}
				}
			}
		}

		return null;
	}

	protected ICpPack loadPack(String file){
		if(allPacks == null) {
			allPacks = new CpPackCollection();
		}
		if (allGenericPacks == null) {
			allGenericPacks = new CpPackCollection(CmsisConstants.GENERIC);
		}
		if (allDevicePacks == null) {
			allDevicePacks = new CpPackCollection(CmsisConstants.DEVICE_SPECIFIC);
		}
		if (allInstalledPacks == null) {
			allInstalledPacks = new CpPackCollection();
		}

		ICpPack pack  = readPack(file);
		if (pack != null && CmsisConstants.PACKAGE_TAG.equals(pack.getTag())) {
			pack.setPackState(packState);
			allPacks.addChild(pack);
			if (packState.isInstalledOrLocal()) {
				allInstalledPacks.addChild(pack);
			}
			if (pack.isDevicelessPack()) {
				allGenericPacks.addChild(pack);
			} else {
				allDevicePacks.addChild(pack);
			}
		} else {
			if (allErrorPacks == null) {
				allErrorPacks = new CpPackFamily(null, CmsisConstants.ERRORS);
			}
			pack = new CpPack(allErrorPacks);
			pack.setFileName(file);
			pack.setText(Utils.extractFileName(file));
			pack.setTag(file);
			pack.setPackState(PackState.ERROR);
			allErrorPacks.addChild(pack);
			String errorString;
			if (!pdscParser.getErrorStrings().isEmpty()) {
				errorString = pdscParser.getErrorStrings().get(0);
			} else if (!CmsisConstants.PACKAGE_TAG.equals(pack.getTag())) {
				errorString = pack.getFileName() + ": " //$NON-NLS-1$
						+ CpStrings.CpPackManager_UnrecognizedFileFormatError;
			} else {
				errorString = pack.getFileName() + ": " //$NON-NLS-1$
						+ CpStrings.CpPackManager_DefaultError;
			}
			if(fPackInstaller != null) {
				fPackInstaller.printInConsole(CpStrings.CpPackManager_ErrorWhileParsing + errorString,
						ConsoleType.ERROR);
			}
		}
		return pack;
	}

	@Override
	public String getCmsisPackRootDirectory() {
		return cmsisPackRootDirectory;
	}

	@Override
	synchronized public CpRepositoryList getCpRepositoryList() {
		if (fRepoList == null) {
			fRepoList = new CpRepositoryList();
		}
		return fRepoList;
	}

	@Override
	public URI getCmsisPackRootURI() {
		return cmsisPackRootURI;
	}

	@Override
	public void setCmsisPackRootDirectory(String packRootDirectory) {
		String normalizedPackRoot = null;
		if(packRootDirectory != null) {
			// normalize and convert to Unix format
			IPath p = new Path(Utils.removeTrailingSlash(packRootDirectory));
			normalizedPackRoot = p.toString();
		}

		if (cmsisPackRootDirectory == null) {
			if (normalizedPackRoot == null || normalizedPackRoot.isEmpty()) {
				return;
			}
		} else if (cmsisPackRootDirectory.equals(normalizedPackRoot) && bPacksLoaded) {
			return;
		}
		clearPackIdxWatcher();
		if (normalizedPackRoot == null || normalizedPackRoot.isEmpty()) {
			cmsisPackRootDirectory = null;
			cmsisPackRootURI = null;
		} else {
			cmsisPackRootDirectory = normalizedPackRoot;
			try {
				// ensure directory exists
				FileChangeWatcher.createDirectories(cmsisPackRootDirectory);
			} catch (IOException e) {
				e.printStackTrace();
			}
			File f = new File(cmsisPackRootDirectory);
			cmsisPackRootURI = f.toURI();
		}
		boolean bSchedulePackUpdate = initPackRoot();
		reload();
		startPackIdxWatcher(); // resume watching
		if(bSchedulePackUpdate) { 
			setCheckForUpdates(bSchedulePackUpdate);
		}
	}

	/**
	 * Initialize pack root
	 * @returns true if update is scheuled
	 */
	protected boolean initPackRoot() {
		ICpPackRootProvider packRootProvider = CpPreferenceInitializer.getCmsisRootProvider(); 
		if(packRootProvider == null)
			return  false;
		
		String cmsisPackRootDirectory = getCmsisPackRootDirectory();
		if(cmsisPackRootDirectory == null) 
			return  false;	
		
		IPath rootPath = new Path(getCmsisPackRootDirectory());
		IPath pidx = rootPath.append(CmsisConstants.DOT_WEB).append(CmsisConstants.REPO_KEIL_PINDEX_FILE);
		if(pidx.toFile().exists())
			return false; // nothing to do			
		try {
			packRootProvider.initPackRoot(getCmsisPackRootDirectory(), new NullProgressMonitor()); // progress monitor is reserved for future
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public boolean arePacksLoaded() {
		return bPacksLoaded;
	}

	@Override
	public void setParser(ICpXmlParser xmlParser) {
		pdscParser = xmlParser;
	}

	@Override
	public ICpXmlParser getParser() {
		return pdscParser;
	}

	@Override
	public void handle(RteEvent event) {
		if(event.getTopic().startsWith(RteEvent.PACK_JOB)) {
			processPackJob((RtePackJobResult) event.getData(), event.getTopic());
		}
	}
	protected void processPackJob(RtePackJobResult jobResult, String topic) {
		if(isReloading()) {
			return; // do not interfere with reload
		}

		if(!isReloadPending()) { // reload will do all those changes anyway
			switch (topic) {
			case RteEvent.PACK_INSTALL_JOB_FINISHED:
				processPackAdded(jobResult);
				break;
			case RteEvent.PACK_REMOVE_JOB_FINISHED:
			case RteEvent.PACK_DELETE_JOB_FINISHED:
				processPackRemoved(jobResult, topic);
				break;
			case RteEvent.PACK_IMPORT_FOLDER_JOB_FINISHED:
				if (jobResult.isSuccess()) {
					setReloadPending(true);
				}
				break;
			default:
				break;
			}
		}

		if(fPackInstaller != null && !fPackInstaller.isBusy()) {
			stopPackIdxWatcher(); // suspend interrupting change events
			FileChangeWatcher.touchFile(getPackIdxFile()); // let others know is that we also have made changed
			if(isReloadPending()) {
				reload(); // resumes pack index watching
			} else {
				emitRteEvent(RteEvent.PACKS_UPDATED);
			}
			startPackIdxWatcher();
		}
	}

	protected void processPackAdded(RtePackJobResult jobResult) {
		if (jobResult == null || !jobResult.isSuccess()) {
			return;
		}
		ICpPack pack = jobResult.getPack();
		if(pack != null) {
			processPackAdded(pack);
			allInstalledPacks.addChild(pack);
		}
	}

	protected void processPackAdded(ICpPack pack) {
		if(pack == null) {
			return;
		}
		// fix for GitHub Issue #44: NPE in CpPackManager.processPackAdded() when installing first Pack into empty CMSIS root folder
		if(getPacks() == null) { // ensure pack collections exist
			return; // should not happen sinse default implementation of getPacks() allocates allPacks if not null 
		}
		
		// Update pack collection
		allPacks.addChild(pack);
		if (pack.isDevicelessPack()) {
			allGenericPacks.addChild(pack);
		} else {
			allDevicePacks.addChild(pack);
		}

		// Update RteDevice Tree
		if (allDevices != null) {
			allDevices.addDevices(pack);
		}
		if (allInstalledDevices != null) {
			allInstalledDevices.addDevices(pack);
		}

		// Update Board Collection
		if (allRteBoards != null) {
			allRteBoards.addBoards(pack);
		}
		addBoards(pack);

		// Update Examples Collection
		if (allExamples != null) {
			allExamples.addExamples(pack);
		}
	}

	protected void processPackRemoved(RtePackJobResult jobResult, String topic) {
		if (jobResult== null || !jobResult.isSuccess()) {
			return;
		}
		ICpPack pack = jobResult.getPack();
		// if the deleted pack is an error pack, only need to remove it from allErrorPacks
		if (pack.getPackState() == PackState.ERROR) {
			allErrorPacks.removeChild(pack);
			pack.setParent(null);
			return;
		}

		// Collect the pack collections from which the pack should be removed
		Collection<ICpPackCollection> packCollections = new LinkedList<>();
		packCollections.add(allInstalledPacks);
		packCollections.add(allPacks);
		if (RteEvent.PACK_DELETE_JOB_FINISHED.equals(topic)) {
			if (pack.isDevicelessPack()) {
				packCollections.add(allGenericPacks);
			} else {
				packCollections.add(allDevicePacks);
			}
		}
		String familyId = pack.getPackFamilyId();
		for (ICpPackCollection packCollection : packCollections) {
			ICpPackFamily packFamily = packCollection.getFamily(familyId);
			if(packFamily != null) {
				packFamily.removeChild(pack);
			}
		}

		// Remove Device from device tree if pack is not the latest version of this pack family
		if (allDevices != null) {
			allDevices.removeDevices(pack);
		}
		if (allInstalledDevices != null) {
			allInstalledDevices.removeDevices(pack);
		}

		// Remove Board from board tree
		if (allRteBoards != null) {
			allRteBoards.removeBoards(pack);
		}

		// Remove Example from examples tree
		if (allExamples != null) {
			allExamples.removeExamples(pack);
		}
		
		// add the latest installed pack of this pack family to the device tree if any
		ICpPack installedPack = allInstalledPacks.getPack(familyId);
		if (installedPack != null) {
			if (allDevices != null)
				allDevices.addDevices(installedPack);
			if (allInstalledDevices != null)
				allInstalledDevices.addDevices(installedPack);
		}

		// Add new pack into the packs, which could be the new pdsc file in the .Web or the .Download folder
		ICpPack newPack = jobResult.getNewPack();
		processPackAdded(newPack);
	}


	protected void addBoards(ICpPack pack) {
		if (pack == null) {
			return;
		}
		Collection<? extends ICpItem> boards = pack.getGrandChildren(CmsisConstants.BOARDS_TAG);
		if (allBoards != null && boards != null) {
			for(ICpItem item : boards) {
				if(!(item instanceof ICpBoard)) {
					continue;
				}
				ICpBoard currentBoard = (ICpBoard)item;
				String id = currentBoard.getId();
				ICpBoard previousBoard = allBoards.get(id);
				if (previousBoard == null || isToReplaceExistingItem(previousBoard, currentBoard)) {
					allBoards.put(id, currentBoard);
				}
			}
		}
	}

	/**
	 * Checks if to replace an existing item in a collection with the current one depending item's pack states and versions
	 * @param previous existing item
	 * @param current current item
	 * @return true if to replace existing with current
	 */
	protected static boolean isToReplaceExistingItem(ICpItem previous, ICpItem current) {
		PackState ps1 = previous.getPack().getPackState();
		PackState ps2 = current.getPack().getPackState();
		if (ps1.ordinal() < ps2.ordinal()) {
			return false;
		} else if (ps1.ordinal() > ps2.ordinal()) {
			return true;
		} else {
			String pv1 = previous.getPack().getVersion();
			String pv2 = current.getPack().getVersion();
			if (VersionComparator.versionCompare(pv1, pv2) < 0) {
				return true;
			}
		}
		return false;
	}

	@Override
	public synchronized ICpPack loadGpdsc(String file) {
		if(file == null || file .isEmpty()) {
			return null;
		}

		if(fGeneratedPacks != null && fGeneratedPacks.containsKey(file)) {
			return fGeneratedPacks.get(file);
		}

		ICpPack pack = doLoadGpdsc(file);
		gpdscWatcher.registerFile(file); // register file to watch change, even if file does not exists
		return pack;
	}

	synchronized void refreshGpdsc(String file, int kind) {
		if(kind == FileChangeWatcher.DELETE) {
			fGeneratedPacks.put(file, null);
		} else {
			try {
				Thread.sleep(500); // let file system some time to finish writing
			} catch (InterruptedException e) {
				// ignore the exception
			}
			doLoadGpdsc(file);
		}
		getRteEventProxy().emitRteEvent(RteEvent.GPDSC_CHANGED, file);
	}

	synchronized protected ICpPack doLoadGpdsc(String file) {
		ICpPack pack  = readPack(file);
		if(pack != null) {
			pack.setPackState(PackState.GENERATED);
		}

		if(fGeneratedPacks == null) {
			fGeneratedPacks = new HashMap<String, ICpPack>();
		}
		fGeneratedPacks.put(file,  pack);

		return pack;
	}

	@Override
	public boolean isRequiredPacksInstalled(ICpPack pack) {
		if(pack == null)
			return true; // null pack has no required packs
		Collection<? extends ICpItem> requiredPacks = pack.getRequiredPacks();
		if (requiredPacks == null) {
			return true;
		}

		for (ICpItem requiredPack : requiredPacks) {
			ICpPackCollection installedPacks = getInstalledPacks();
			if (installedPacks == null || installedPacks.getPack(requiredPack.attributes()) == null) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isLocalRepository(ICpPack pack) {
		return pack.getDir(true).indexOf(getCmsisPackRootDirectory()) < 0;
	}

	@Override
	public boolean isCheckForUpdates() {
		// TODO Auto-generated method stub
		return bCheckForUpdates;
	}

	@Override
	public void setCheckForUpdates(boolean bCheck) {
		if(bCheckForUpdates == bCheck)
			return;
		bCheckForUpdates = bCheck;
		if(bCheckForUpdates)
			emitRteEvent(RteEvent.PACKS_UPDATE_PENDING);
	}
	
}
