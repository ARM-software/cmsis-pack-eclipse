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

package com.arm.cmsis.pack.enums;

import com.arm.cmsis.pack.common.CmsisConstants;

/**
 * Enumeration value corresponding <code>"category"</code> attribute in pdsc
 * file That represents
 *
 * @see ICpFile
 */
public enum EFileCategory {

    DOC, HEADER, INCLUDE, LIBRARY, OBJECT, SOURCE, SOURCE_ASM, SOURCE_C, SOURCE_CPP, LINKER_SCRIPT, UTILITY, IMAGE, SVD,
    SRC, // library source paths
    PRE_INCLUDE_GLOBAL, PRE_INCLUDE_LOCAL, OTHER;

    /**
     * @param str value of <code>"category"</code> attribute
     * @return corresponding enumeration value
     */
    public static EFileCategory fromString(final String str) {
        if (str == null)
            return OTHER;
        switch (str) {
        case CmsisConstants.DOC:
            return DOC;
        case CmsisConstants.HEADER:
            return HEADER;
        case CmsisConstants.INCLUDE:
            return INCLUDE;
        case CmsisConstants.LIBRARY:
            return LIBRARY;
        case CmsisConstants.OBJECT:
            return OBJECT;
        case CmsisConstants.SOURCE:
            return SOURCE;
        case CmsisConstants.SOURCE_ASM:
            return SOURCE_ASM;
        case CmsisConstants.SOURCE_C:
            return SOURCE_C;
        case CmsisConstants.SOURCE_CPP:
            return SOURCE_CPP;
        case CmsisConstants.LINKER_SCRIPT:
            return LINKER_SCRIPT;
        case CmsisConstants.UTILITY:
            return UTILITY;
        case CmsisConstants.IMAGE:
            return IMAGE;
        case CmsisConstants.SVD:
            return SVD;
        case CmsisConstants.SRC:
            return SRC;
        case CmsisConstants.PRE_INCLUDE_GLOBAL:
            return PRE_INCLUDE_GLOBAL;
        case CmsisConstants.PRE_INCLUDE_LOCAL:
            return PRE_INCLUDE_LOCAL;
        default:
            return OTHER;
        }
    }

    /**
     * Checks if file category is header
     *
     * @return true if header
     */
    public boolean isHeader() {
        return this == HEADER;
    }

    /**
     * Checks if file category is source
     *
     * @return true if source
     */
    public boolean isSource() {
        switch (this) {
        case SOURCE:
        case SOURCE_ASM:
        case SOURCE_C:
        case SOURCE_CPP:
            return true;
        case DOC:
        case HEADER:
        case IMAGE:
        case INCLUDE:
        case LIBRARY:
        case LINKER_SCRIPT:
        case OBJECT:
        case OTHER:
        case PRE_INCLUDE_GLOBAL:
        case PRE_INCLUDE_LOCAL:
        case SRC:
        case SVD:
        case UTILITY:
        default:
            break;

        }
        return false;
    }

}
