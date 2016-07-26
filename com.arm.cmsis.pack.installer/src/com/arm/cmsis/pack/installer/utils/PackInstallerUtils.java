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

package com.arm.cmsis.pack.installer.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPack.PackState;
import com.arm.cmsis.pack.data.ICpPackCollection;
import com.arm.cmsis.pack.utils.VersionComparator;

/**
 * Utilities used in Pack Installer
 */
public class PackInstallerUtils {

	/**
	 * Get the Full Pack Id with version: PackFamilyId.Version
	 *
	 * @param cpItem the cmsis pack item
	 * @return full pack id or an empty string
	 */
	public static String getCpItemVersion(ICpItem cpItem) {
		if (cpItem == null) {
			return CmsisConstants.EMPTY_STRING;
		}

		String installingVersion = CmsisConstants.EMPTY_STRING;

		if (cpItem.getTag().equals(CmsisConstants.RELEASE_TAG)) {
			installingVersion = cpItem.getAttribute(CmsisConstants.VERSION);
		} else {
			installingVersion = cpItem.getPack().getVersion();
		}

		return installingVersion;
	}

	/**
	 * Return this {@link ICpItem}'s release date
	 *
	 * @param cpItem the cmsis pack item
	 * @return a string of the date or an empty string
	 */
	public static String getCpItemDate(ICpItem cpItem) {
		if (cpItem == null || cpItem instanceof ICpPackCollection) {
			return CmsisConstants.EMPTY_STRING;
		}

		String date = CmsisConstants.EMPTY_STRING;
		if (CmsisConstants.RELEASE_TAG.equals(cpItem.getTag())) {
			date = cpItem.getAttribute(CmsisConstants.DATE);
		} else {
			String version = cpItem.getPack().getVersion();
			Collection<? extends ICpItem> releases = cpItem.getPack().getGrandChildren(CmsisConstants.RELEASES_TAG);
			if (releases == null) {
				return date;
			}
			for (ICpItem release : releases) {
				if (release.getAttribute(CmsisConstants.VERSION).equals(version)) {
					date = release.getAttribute(CmsisConstants.DATE);
					break;
				}
			}
		}

		return date;
	}

	/**
	 * Get the Full Pack ID of this {@link ICpItem}
	 *
	 * @param cpItem the cmsis pack item
	 * @return a String like Vendor.Pack.Version
	 */
	public static String getFullPackId(ICpItem cpItem) {
		return cpItem.getPackFamilyId() + "." + getCpItemVersion(cpItem); //$NON-NLS-1$
	}

	/**
	 * Replace Vendor.Pack.Version with Vendor/Pack/Version
	 *
	 * @param fullPackId the pack's id
	 * @return the relative installation directory. e.g. ARM/CMSIS/4.5.0
	 */
	public static String getPackRelativeInstallDir(String fullPackId) {
		int iv = fullPackId.indexOf('.');
		if (iv == -1) {
			return fullPackId;
		}
		String vendor = fullPackId.substring(0, iv);
		int ip = fullPackId.indexOf('.', iv + 1);
		if (ip == -1) {
			return fullPackId;
		}
		String pack = fullPackId.substring(iv + 1, ip);
		String version = fullPackId.substring(ip + 1);
		return vendor + File.separator + pack + File.separator + version;
	}

	/**
	 * Return Download Directory of all the Packs
	 *
	 * @return Download Directory of all the Packs
	 */
	public static String getPacksDownloadDir() {
		return new Path(CpPlugIn.getPackManager().getCmsisPackRootDirectory()).append(".Download") //$NON-NLS-1$
				.toOSString();
	}

	/**
	 * Return Web Directory of all the Packs
	 *
	 * @return Web Directory of all the Packs
	 */
	public static String getPacksWebDir() {
		return new Path(CpPlugIn.getPackManager().getCmsisPackRootDirectory()).append(".Web") //$NON-NLS-1$
				.toOSString();
	}

	/**
	 * @param archiveFile the zip file
	 * @return the number of files contained in this zip file
	 * @throws IOException
	 */
	public static int getFilesCount(File archiveFile) throws IOException {
		ZipInputStream zipInput;
		zipInput = new ZipInputStream(new FileInputStream(archiveFile));
		ZipEntry zipEntry = zipInput.getNextEntry();
		int count = 0;
		while (zipEntry != null) {
			if (!zipEntry.isDirectory()) {
				count++;
			}
			zipEntry = zipInput.getNextEntry();
		}
		zipInput.closeEntry();
		zipInput.close();

		return count;
	}

	/**
	 * Delete the folder recursively with progress monitor: first file, then folder
	 *
	 * @param folder the folder
	 */
	public static void deleteFolderRecursiveWithProgress(File folder, IProgressMonitor monitor) {

		if (folder == null) {
			return;
		}

		if (folder.isFile()) {
			folder.setWritable(true, false);
			folder.delete();
			return;
		}

		if (folder.exists()) {
			File[] files = folder.listFiles();
			if(files == null) {
				return;
			}
			for (File f : files) {
				if (f.isDirectory()) {
					deleteFolderRecursiveWithProgress(f, monitor);
					f.setWritable(true, false);
					f.delete();
				} else {
					f.setWritable(true, false);
					f.delete();
					monitor.worked(1);
				}
			}
			folder.setWritable(true, false);
			folder.delete();
		}
	}

	/**
	 * Count the number of files in specific folder
	 * @param folder the root folder
	 * @return the number of files in folder
	 */
	public static int countFiles(File folder) {
		if (folder == null) {
			return 0;
		}

		if (folder.isFile()) {
			return 1;
		}

		int count = 0;
		if (folder.exists()) {
			File[] files = folder.listFiles();
			if(files == null) {
				return 0;
			}
			for (File f : files) {
				if (f.isDirectory()) {
					count += countFiles(f);
				} else {
					count++;
				}
			}
		}
		return count;
	}

	/**
	 * Clear the read-only flag
	 *
	 * @param folder the root folder
	 * @param extension extension of the files whose read-only flag should be cleared,
	 * use an empty string to clear the read-only flag on all the files
	 */
	public static void clearReadOnly(File folder, String extension) {
		if (folder == null) {
			return;
		}

		if (folder.exists()) {
			File[] files = folder.listFiles();
			if(files == null) {
				return;
			}
			for (File f : files) {
				if (f.isDirectory()) {
					clearReadOnly(f, extension);
					f.setWritable(true, false);
				} else if (extension.isEmpty() || f.getName().endsWith(extension)) {
					f.setWritable(true, false);
				}
			}
			folder.setWritable(true, false);
		}
	}

	/**
	 * Set the read-only flag
	 *
	 * @param folder the folder
	 */
	public static void setReadOnly(File folder) {
		if (folder == null) {
			return;
		}

		if (folder.exists()) {
			File[] files = folder.listFiles();
			if(files == null) {
				return;
			}
			for (File f : files) {
				if (f.isDirectory()) {
					setReadOnly(f);
					f.setReadOnly();
				} else {
					f.setReadOnly();
				}
			}
			folder.setReadOnly();
		}
	}

	/**
	 * Update the {@link ICpPackFamily} status of this {@link ICpPack} when this
	 * pack is deleted.
	 *
	 * @param pack
	 *            The pack
	 * @param monitor
	 *            progress monitor
	 */
	public static ICpPack updatePackFamily(ICpPack pack, IProgressMonitor monitor) {

		SubMonitor progress = SubMonitor.convert(monitor, 100);

		ICpItem parent = pack.getParent();
		ICpPack newPack = null;
		// Deleted the latest version of this pack family
		if (pack == parent.getFirstChild()) {
			progress.worked(80);

			IPath webPdscFilePath = new Path(getPacksWebDir()).append(pack.getPackFamilyId() + ".pdsc"); //$NON-NLS-1$
			if (!webPdscFilePath.toFile().exists()) {
				return null;
			}
			newPack = (ICpPack) CpPlugIn.getPackManager().getParser().parseFile(webPdscFilePath.toOSString());

			if (newPack != null) {
				newPack.setPackState(PackState.AVAILABLE);
				String newPackVersion = newPack.getVersion();
				if (parent.getPack() != null) {
					String lastestPackVersion = parent.getPack().getVersion();
					if (new VersionComparator().compare(newPackVersion, lastestPackVersion) > 0) {
						// new pack's version < latest pack's version. ignore this new pack
						newPack = null;
					}
				}
			}
		}

		progress.setWorkRemaining(0);

		return newPack;
	}

}
