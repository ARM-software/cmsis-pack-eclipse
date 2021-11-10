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

package com.arm.cmsis.pack.data;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.enums.EFileCategory;
import com.arm.cmsis.pack.enums.EFileRole;
import com.arm.cmsis.pack.utils.Utils;

/**
 * Interface describing component's file item
 */
public interface ICpFile extends ICpItem {

    /**
     * Returns file category corresponding "category" attribute in pdsc file
     *
     * @return file category as EFileCategory value
     * @see EFileCategory
     */
    EFileCategory getCategory();

    /**
     * Returns file role corresponding "attr" attribute in pdsc file
     *
     * @return file role as EFileRole enum value
     * @see EFileRole
     */
    EFileRole getRole();

    /**
     * Returns original absolute include path specified by this file (only if
     * header)
     *
     * @return original absolute include path as String
     */
    default String getFilePath() {
        if (hasAttribute(CmsisConstants.PATH)) {
            return Utils.removeTrailingSlash(getAbsolutePath(getAttribute(CmsisConstants.PATH)));
        }
        return Utils.extractPath(getAbsolutePath(getName()), false);
    }

    /**
     * Returns filename, can potentially contain several; segments if "path"
     * attribute is specified
     *
     * @return filename
     */
    default String getFileName() {
        if (hasAttribute(CmsisConstants.PATH)) {
            String path = getFilePath() + '/';
            String fileName = getAbsolutePath(getName()).replace('\\', '/');
            if (!path.isEmpty() && fileName.startsWith(path)) {
                return fileName.substring(path.length());
            }
        }
        return Utils.extractFileName(getName());
    }

}
