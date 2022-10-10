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
