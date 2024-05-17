/*******************************************************************************
* Copyright (c) 2022 ARM Ltd. and others
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

package com.arm.cmsis.pack.rte.components;

import com.arm.cmsis.pack.CpStrings;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.info.ICpBoardInfo;

public class RteSelectedBoardClass extends RteComponentClass {

    ICpBoardInfo fBoardInfo = null;

    public RteSelectedBoardClass(IRteComponentItem parent, ICpBoardInfo boardInfo) {
        super(parent, boardInfo.getName());
        fBoardInfo = boardInfo;
    }

    @Override
    public boolean purge() {
        return false;
    }

    @Override
    public ICpItem getActiveCpItem() {
        return fBoardInfo;
    }

    @Override
    public String getDescription() {
        if (fBoardInfo.getBoard() == null) {
            return CpStrings.BoardNotFound;
        }
        return fBoardInfo.getBoard().getDescription();
    }

    @Override
    public String getUrl() {
        return fBoardInfo.getUrl();
    }

    @Override
    public String getActiveVendor() {
        return fBoardInfo.getVendor();
    }

    @Override
    public String getActiveVersion() {
        return fBoardInfo.getRevision();
    }

    @Override
    public String getKey() {
        // Artificial empty key to make the item always on top.
        return CmsisConstants.EMPTY_STRING;
    }

    @Override
    public boolean isUseLatestVersion() {
        return true;
    }

    @Override
    public boolean isSelected() {
        return true; // board is always selected
    }

}