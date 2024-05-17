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

import com.arm.cmsis.pack.data.ICpBoard;
import com.arm.cmsis.pack.enums.IEvaluationResult;

/**
 * Interface representing board used in the configuration
 */
public interface ICpBoardInfo extends ICpItemInfo, IEvaluationResult {

    /**
     * Returns actual board represented by this info
     *
     * @return actual board
     */
    ICpBoard getBoard();

    /**
     * Sets actual board to this info
     *
     * @param board actual board to set
     */
    void setBoard(ICpBoard board);

}
