/*******************************************************************************
* Copyright (c) 2022 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.rte.dependencies;

import com.arm.cmsis.pack.CpStrings;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.info.ICpBoardInfo;
import com.arm.cmsis.pack.info.ICpPackInfo;
import com.arm.cmsis.pack.rte.components.IRteComponentItem;

public class RteMissingBoardResult extends RteDependencyResult {

    protected ICpBoardInfo fBoardInfo = null;

    public RteMissingBoardResult(IRteComponentItem componentItem, ICpBoardInfo boardInfo) {
        super(componentItem);
        fBoardInfo = boardInfo;
        setEvaluationResult(boardInfo.getEvaluationResult());
    }

    @Override
    public String getDescription() {
        EEvaluationResult res = fBoardInfo.getEvaluationResult();

        String state;
        switch (res) {
        case FAILED:
        case MISSING:
            state = CpStrings.IsMissing;
            break;
        case UNAVAILABLE_PACK:
            state = CpStrings.IsNotAvailableForCurrentConfiguration;
            break;
        default:
            return super.getDescription();
        }
        String reason = CmsisConstants.EMPTY_STRING;

        ICpPackInfo pi = fBoardInfo.getPackInfo();
        if (pi != null) {
            reason = CpStrings.Pack + " "; //$NON-NLS-1$
            String packId = pi.isVersionFixed() ? pi.getId() : pi.getPackFamilyId();
            if (pi.getPack() == null) {
                reason += CpStrings.IsNotInstalled;
            } else {
                reason += CpStrings.IsExcluded;
            }
            reason += ": " + packId; //$NON-NLS-1$
        }
        return CpStrings.RteMissingBoardResult_Board + " " + state + ". " + reason; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public boolean isMaster() {
        return true;
    }
}