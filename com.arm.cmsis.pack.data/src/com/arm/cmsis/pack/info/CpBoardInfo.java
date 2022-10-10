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

package com.arm.cmsis.pack.info;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpBoard;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;

/**
 * Default implementation of ICpDeviceInfo interface
 */
public class CpBoardInfo extends CpItemInfo implements ICpBoardInfo {

    protected ICpBoard fBoard = null;

    /**
     * Constructs CpBoardInfo from supplied ICpBoard
     *
     * @param parent parent ICpItem
     * @param board  ICpBoard to construct from
     */
    public CpBoardInfo(ICpItem parent, ICpBoard board) {
        super(parent, CmsisConstants.BOARD_TAG);
        setBoard(board);
    }

    /**
     * Default constructor
     *
     * @param parent parent ICpItem
     */
    public CpBoardInfo(ICpItem parent) {
        super(parent, CmsisConstants.BOARD_TAG);
    }

    /**
     * Constructs CpDeviceInfo from parent and tag
     *
     * @param parent parent ICpItem
     * @param tag
     */
    public CpBoardInfo(ICpItem parent, String tag) {
        super(parent, tag);
    }

    @Override
    public ICpBoard getBoard() {
        return fBoard;
    }

    @Override
    public ICpPack getPack() {
        if (fBoard != null) {
            return fBoard.getPack();
        }
        return super.getPack();
    }

    @Override
    public String getPackId() {
        if (fBoard != null) {
            return fBoard.getPackId();
        }
        return super.getPackId();
    }

    @Override
    public String getName() {
        return getAttribute(CmsisConstants.BNAME);
    }

    @Override
    public String getDescription() {
        if (fBoard != null) {
            return fBoard.getDescription();
        }
        return CmsisConstants.EMPTY_STRING;
    }

    @Override
    public String constructId() {
        return ICpBoard.constructBoardId(this);
    }

    @Override
    public synchronized String getUrl() {
        if (fBoard != null) {
            return fBoard.getUrl();
        }
        return getAttribute(CmsisConstants.URL);
    }

    @Override
    public void setBoard(ICpBoard board) {
        fBoard = board;
        updateInfo();
    }

    @Override
    public void updateInfo() {
        if (fBoard != null) {
            setAttribute(CmsisConstants.BNAME, fBoard.getName());
            setAttribute(CmsisConstants.BVENDOR, fBoard.getVendor());
            setAttribute(CmsisConstants.BREVISION, fBoard.getRevision());
            String url = fBoard.getUrl();
            if (url != null && !url.isEmpty()) {
                attributes().setAttribute(CmsisConstants.URL, url);
            }
        }
        updatePackInfo(fBoard != null ? fBoard.getPack() : null);
    }
}
