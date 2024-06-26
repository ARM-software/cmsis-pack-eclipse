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

package com.arm.cmsis.pack.installer.jobs;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackInstaller;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.parser.PdscParser;
import com.arm.cmsis.pack.repository.RtePackJobResult;
import com.arm.cmsis.pack.utils.Utils;
import com.arm.cmsis.pack.utils.VersionComparator;

/**
 * Basic class for the pack manager's job
 */
public abstract class CpPackJob extends Job {

    protected String fJobId;
    protected ICpPackInstaller fPackInstaller;
    protected RtePackJobResult fResult;

    /**
     * @param name          The job's name
     * @param packInstaller the Pack installer
     * @param jobId         the job's id
     */
    public CpPackJob(String name, ICpPackInstaller packInstaller, String jobId) {
        super(name);
        fJobId = jobId;
        fPackInstaller = packInstaller;
        fResult = new RtePackJobResult(jobId);
    }

    /**
     * Return true if this job requires install the required packs
     *
     * @return True if this job requires install the required packs
     */
    public boolean installRequiredPacks() {
        return false;
    }

    @Override
    public boolean belongsTo(Object family) {
        return getName().equals(family);
    }

    /**
     * Checks if pack is a local one (does not have a pdsc entry in .web folder)
     *
     * @param pack ICpPack to check
     * @return true if pack is local
     */
    protected boolean isLocalPack(ICpPack pack) {
        return CpPlugIn.getPackManager().isLocalPack(pack);
    }

    protected void copyToLocal(ICpPack pack) throws IOException {
        String pdscFile = pack.getFileName();
        String familyId = pack.getPackFamilyId();
        String existingLocalVersion = null;
        IPath localPath = new Path(CpPlugIn.getPackManager().getCmsisPackLocalDir())
                .append(familyId + CmsisConstants.EXT_PDSC);
        File localFile = localPath.toFile();
        if (localFile.exists()) {
            PdscParser parser = new PdscParser();
            ICpItem existingLocalPack = parser.parseFile(localFile.toString());
            if (existingLocalPack != null)
                existingLocalVersion = existingLocalPack.getVersion();
        }
        // check if existing version is lower than the imported one
        if (existingLocalVersion == null
                || VersionComparator.versionCompare(existingLocalVersion, pack.getVersion()) < 0) {
            Utils.copy(new File(pdscFile), localPath.toFile());
        }
    }

}
