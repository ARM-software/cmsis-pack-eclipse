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

package com.arm.cmsis.pack.rte.boards;

import java.util.Collection;

import com.arm.cmsis.pack.data.ICpBoard;
import com.arm.cmsis.pack.data.ICpPack;

/**
 * Interface base element for BoardDevice elements. This first level are boards,
 * then mounted and compatible devices of this board
 */
public interface IRteBoardItem extends IRteBoardDeviceItem {

    /**
     * Add board
     *
     * @param item
     */
    void addBoard(ICpBoard item);

    /**
     * Add boards from supplied pack
     *
     * @param pack IcpPack to add boards from
     */
    void addBoards(ICpPack pack);

    /**
     * Remove board
     *
     * @param item
     */
    void removeBoard(ICpBoard item);

    /**
     * Remove boards from supplied pack
     *
     * @param pack IcpPack to remove boards from
     */
    void removeBoards(ICpPack pack);

    /**
     * Return the board with the id
     *
     * @param boardId
     * @return board with the id, or null
     */
    IRteBoardItem findBoard(String boardId);

    /**
     * Find boards matching partialBoardId
     *
     * @param partialBoardId partial boardId
     * @return collection of boards matching partialBoardId
     */
    Collection<IRteBoardItem> findBoards(String partialBoardId);

    /**
     * Get boards from all the packs
     *
     * @return boards from all the packs
     */
    Collection<ICpBoard> getBoards();

    /**
     * Get a set of mounted devices of this board
     *
     * @return a set of mounted devices of this board
     */
    IRteBoardDeviceItem getMountedDevices();

    /**
     * Get a set of compatible devices of this board
     *
     * @return a set of compatible devices of this board
     */
    IRteBoardDeviceItem getCompatibleDevices();

}
