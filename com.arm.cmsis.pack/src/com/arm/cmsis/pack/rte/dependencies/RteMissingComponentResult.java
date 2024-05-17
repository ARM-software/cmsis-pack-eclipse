/*******************************************************************************
 * Copyright (c) 2021 - 2019 ARM Ltd. and others
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

package com.arm.cmsis.pack.rte.dependencies;

import com.arm.cmsis.pack.CpStrings;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpPack;
import com.arm.cmsis.pack.data.ICpComponent;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.info.ICpComponentInfo;
import com.arm.cmsis.pack.info.ICpPackInfo;
import com.arm.cmsis.pack.rte.components.IRteComponentGroup;
import com.arm.cmsis.pack.rte.components.IRteComponentItem;
import com.arm.cmsis.pack.utils.VersionComparator;

/**
 * The class represent a result of missing component/API
 */
public class RteMissingComponentResult extends RteDependencyResult {

    public RteMissingComponentResult(IRteComponentItem componentItem) {
        super(componentItem);
    }

    @Override
    public String getDescription() {
        ICpComponentInfo ci = fComponentItem.getActiveCpComponentInfo();
        EEvaluationResult res = getEvaluationResult();
        String descr = getElementType(ci, res) + CmsisConstants.SPACE;

        switch (res) {
        case MISSING_API:
        case MISSING_API_VERSION:
            descr = getMissingApiDescription(ci, res);
            break;
        case MISSING:
            descr += CpStrings.IsMissing + ". " + getPackReason(ci); //$NON-NLS-1$
            break;
        case UNAVAILABLE:
            descr += CpStrings.IsNotAvailableForCurrentConfiguration + ". " + getPackReason(ci); //$NON-NLS-1$ ;
            break;
        case UNAVAILABLE_PACK:
            descr += CpStrings.IsNotAvailableForCurrentConfiguration + ". " + getPackReason(ci); //$NON-NLS-1$
            break;
        default:
            descr = super.getDescription();
        }

        return descr;
    }

    /**
     * Returns reason for missing API
     *
     * @param ci  ICpComponentInfo
     * @param res EEvaluationResult
     * @return description string
     */
    private String getMissingApiDescription(ICpComponentInfo ci, EEvaluationResult res) {

        String requiredApiVersion = ci != null ? ci.getAttribute(CmsisConstants.CAPIVERSION)
                : CmsisConstants.EMPTY_STRING;
        ICpComponent availableApi = null;
        if (res == EEvaluationResult.MISSING_API_VERSION) {
            IRteComponentGroup g = getComponentItem().getParentGroup();
            availableApi = g != null ? g.getApi() : null;
        }

        String description = CpStrings.APIversion;
        description += " '" + requiredApiVersion + "' "; //$NON-NLS-1$ //$NON-NLS-2$
        description += CpStrings.RteMissingComponentResult_orHigherIsRequired + ". "; //$NON-NLS-1$
        if (availableApi == null) {
            description += CpStrings.RteMissingComponentResult_APIDefIsMissingNoPack;
        } else {
            description += CpStrings.Version;
            description += " '" + availableApi.getVersion() + "' "; //$NON-NLS-1$ //$NON-NLS-2$
            description += CpStrings.RteMissingComponentResult_IsFoundInPack;
            description += " '" + availableApi.getPackId() + "' "; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return description;
    }

    protected String getElementType(ICpComponentInfo ci, EEvaluationResult res) {
        if (res.isMissingApi() || (ci != null && ci.isApi())) {
            return CpStrings.API;
        }
        return CpStrings.Component;
    }

    /**
     * Reason for the missing result if any
     *
     * @return reason string
     */
    protected String getPackReason(ICpComponentInfo ci) {
        if (ci == null) {
            return CmsisConstants.EMPTY_STRING;
        }
        String reason = CpStrings.Pack + ' ';
        ICpPackInfo pi = ci.getPackInfo();
        boolean bReportPackVersion = pi.isVersionFixed();
        ICpPack pack = ci.getPack();
        if (getEvaluationResult() == EEvaluationResult.UNAVAILABLE_PACK) {
            // as reason tells : pack is excluded (either certain version or the entire
            // family)
            reason += CpStrings.IsExcluded;
        } else if (pack != null) {
            // check if the installed version differs: it might explain why the the
            // component is not found
            int versionDiff = VersionComparator.versionCompare(pack.getVersion(), CpPack.versionFromId(pi.getId()));
            if (versionDiff < 2 && versionDiff > -2) {
                // almost the same pack => the component is probably filtered out by
                // device/compiler configuration
                return CpStrings.RteMissingComponentResult_NoComponentFoundMatchingDeviceCompiler;
            } else {
                // pack differs in minor or major version, report pack as not installed
                bReportPackVersion = true;
                reason += CpStrings.IsNotInstalled;
            }
        } else {
            // pack was not resolved => it is not installed
            reason += CpStrings.IsNotInstalled;
        }

        String packId = bReportPackVersion ? pi.getId() : pi.getPackFamilyId();
        reason += ": " + packId; //$NON-NLS-1$
        return reason;
    }

    @Override
    public boolean isMaster() {
        return true;
    }

}
