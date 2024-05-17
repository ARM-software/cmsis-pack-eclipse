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
package com.arm.cmsis.pack.project.ui;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.enums.EFileRole;
import com.arm.cmsis.pack.info.ICpFileInfo;
import com.arm.cmsis.pack.project.utils.ProjectUtils;

public class RteUpdateConfigFileTester extends PropertyTester {

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        if (property.equalsIgnoreCase("canupdate") || //$NON-NLS-1$
                property.equalsIgnoreCase("canmerge")) { //$NON-NLS-1$
            IFile file = ProjectUtils.getRteFileResource(receiver);
            if (file == null || file.isLinked()) {
                return false;
            }
            if (CmsisConstants.RTECONFIG.equals(file.getFileExtension())
                    || CmsisConstants.RTE_RTE_Components_h.equals(file.getProjectRelativePath().toString())) {
                return false;
            }
            ICpFileInfo fi = ProjectUtils.getCpFileInfo(file);
            if (fi == null) {
                return false;
            }
            if (property.equalsIgnoreCase("canmerge") && fi.getRole() != EFileRole.CONFIG) { //$NON-NLS-1$
                return false;
            }
            int versionDiff = fi.getVersionDiff();
            if (versionDiff > 2 || versionDiff < 0) {
                return true;
            }
        }
        return false;
    }

}
