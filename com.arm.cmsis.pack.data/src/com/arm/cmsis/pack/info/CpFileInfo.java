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

package com.arm.cmsis.pack.info;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpFile;
import com.arm.cmsis.pack.data.ICpFile;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.enums.EFileRole;
import com.arm.cmsis.pack.utils.VersionComparator;

/**
 * Default implementation of ICpFileInfo interface
 *
 * @see ICpFileInfo
 * @see CpFile
 */
public class CpFileInfo extends CpFile implements ICpFileInfo {

    protected ICpFile fFile = null;
    protected int fVersionDiff = 0;

    public CpFileInfo(ICpItem parent, ICpFile file) {
        super(parent, file.getTag());
        setFile(file);
        updateInfo();
    }

    public CpFileInfo(ICpItem parent, String tag) {
        super(parent, tag);
    }

    @Override
    public ICpFile getFile() {
        return fFile;
    }

    @Override
    public void setFile(ICpFile file) {
        fFile = file;
    }

    @Override
    public void updateInfo() {
        if (fFile != null) {
            fVersionDiff = 0;
            attributes().setAttributes(fFile.attributes());
            if (fFile.getRole() == EFileRole.CONFIG) {
                // ensure we have the version for config files
                attributes().setAttribute(CmsisConstants.VERSION, fFile.getVersion());
            }
            if (fFile.isDeviceDependent()) {
                attributes().setAttribute(CmsisConstants.DEVICE_DEPENDENT, true);
            } else {
                attributes().removeAttribute(CmsisConstants.DEVICE_DEPENDENT);
            }
            if (fFile.isBoardDependent()) {
                attributes().setAttribute(CmsisConstants.BOARD_DEPENDENT, true);
            } else {
                attributes().removeAttribute(CmsisConstants.BOARD_DEPENDENT);
            }
        }
    }

    @Override
    public ICpComponentInfo getComponentInfo() {
        return getParentOfType(ICpComponentInfo.class);
    }

    @Override
    public ICpPackInfo getPackInfo() {
        ICpComponentInfo ci = getComponentInfo();
        if (ci != null) {
            return ci.getPackInfo();
        }
        return null;
    }

    @Override
    public void setVersion(String version) {
        attributes().setAttribute(CmsisConstants.VERSION, version);
        if (fFile != null) {
            fVersionDiff = VersionComparator.versionCompare(getVersion(), fFile.getVersion());
        } else {
            fVersionDiff = 0;
        }
    }

    @Override
    public int getVersionDiff() {
        if (fFile == null) {
            return 0;
        }
        return fVersionDiff;
    }

    @Override
    public boolean isGenerated() {
        if (fFile != null)
            return fFile.isGenerated();
        return super.isGenerated();
    }

}
