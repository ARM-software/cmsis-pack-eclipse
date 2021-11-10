/*******************************************************************************
 * Copyright (c) 2021 ARM Ltd. and others
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
import java.io.IOException;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPack.PackState;
import com.arm.cmsis.pack.utils.Utils;
import com.arm.cmsis.pack.utils.VersionComparator;

/**
 * Utilities used in Pack Installer
 */
public class PackInstallerUtils {

    /**
     * Loads new latest pack when existing latest pack is deleted.
     *
     * @param deletedPack deleted pack
     * @return newly loaded pack if it is the latest available one, otherwise null
     */
    public static ICpPack loadLatestPack(ICpPack deletedPack) {

        ICpPack newPack = null;
        ICpItem family = deletedPack.getParent();
        ICpPack latestPack = family.getPack();
        // Deleted the latest version of this pack family
        if (deletedPack == latestPack) {
            String webDir = CpPlugIn.getPackManager().getCmsisPackWebDir();
            if (webDir == null || webDir.isEmpty()) {
                return null;
            }
            String pdscFile = deletedPack.getPackFamilyId() + CmsisConstants.EXT_PDSC;
            IPath pdscFilePath = new Path(webDir).append(pdscFile);
            if (!pdscFilePath.toFile().exists()) {
                String localDir = CpPlugIn.getPackManager().getCmsisPackLocalDir();
                pdscFilePath = new Path(localDir).append(pdscFile);
                if (!pdscFilePath.toFile().exists()) {
                    return null;
                }
            }
            newPack = CpPlugIn.getPackManager().readPack(pdscFilePath.toOSString());

            if (newPack != null) {
                newPack.setPackState(PackState.AVAILABLE);
                String newPackVersion = newPack.getVersion();
                String lastestPackVersion = latestPack.getVersion();
                if (VersionComparator.versionCompare(newPackVersion, lastestPackVersion) < 0) {
                    // new pack's version < latest pack's version. ignore this new pack
                    newPack = null;
                }
            }
        }
        return newPack;
    }

    /**
     * Copy from one directory to another
     *
     * @param srcDir    source directory
     * @param dstDir    destination directory
     * @param ignoreDir directories that should ignore during copy (directories w/
     *                  absolute path)
     * @param monitor   progress monitor
     * @throws IOException
     */
    public static void copyDirectoryWithProgress(File srcDir, File dstDir, Set<String> ignoreDir,
            IProgressMonitor monitor) throws IOException {
        if (srcDir == null || (ignoreDir != null && ignoreDir.contains(srcDir.getAbsolutePath()))) {
            return;
        }
        if (srcDir.isDirectory()) {
            String[] children = srcDir.list();
            if (children == null) {
                return;
            }
            for (String child : children) {
                copyDirectoryWithProgress(new File(srcDir, child), new File(dstDir, child), ignoreDir, monitor);
            }
        } else {
            if (!dstDir.getParentFile().exists()) {
                dstDir.getParentFile().mkdirs();
            }
            Utils.copy(srcDir, dstDir);
            monitor.worked(1);
        }
    }

    /**
     * Delete the folder recursively with progress monitor: first file, then folder
     *
     * @param folder  the folder
     * @param monitor progress monitor
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
            if (files == null) {
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

}
