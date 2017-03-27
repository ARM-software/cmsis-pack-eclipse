/*******************************************************************************
 * Copyright (c) 2015 ARM Ltd. and others
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
import com.arm.cmsis.pack.info.ICpComponentInfo;
import com.arm.cmsis.pack.info.ICpPackInfo;
import com.arm.cmsis.pack.rte.components.IRteComponentItem;

/**
 * The class represent a result of missing component/API
 */
public class RteMissingComponentResult extends RteDependencyResult {

	public RteMissingComponentResult(IRteComponentItem componentItem) {
		super(componentItem);
	}

	@Override
	public String getDescription() {
		EEvaluationResult res = getEvaluationResult();
		ICpComponentInfo ci = fComponentItem.getActiveCpComponentInfo();
		String component;
		if (ci != null && ci.isApi()) {
			component = CpStrings.API;
		} else {
			component = CpStrings.Component;
		}
		String state;
		switch (res) {
		case MISSING_API:
			component = CpStrings.API;
		case MISSING:
			state = CpStrings.IsMissing;
			break;
		case UNAVAILABLE:
		case UNAVAILABLE_PACK:
			state = CpStrings.IsNotAvailableFoCurrentConfiguration;
			break;
		default:
			return super.getDescription();
		}

		String reason = CmsisConstants.EMPTY_STRING;
		if (ci != null && res != EEvaluationResult.UNAVAILABLE) {
			reason = CpStrings.Pack + ' ';
			ICpPackInfo pi = ci.getPackInfo();
			if (pi.getPack() == null) {
				reason += CpStrings.IsNotInstalled;
			} else {
				reason += CpStrings.IsExcluded;
			}
			String packId = pi.isVersionFixed() ? pi.getId() : pi.getPackFamilyId();
			reason += ": " + packId; //$NON-NLS-1$
		}
		String s = component + " " + state + ". " + reason; //$NON-NLS-1$//$NON-NLS-2$
		return s;
	}

	@Override
	public boolean isMaster() {
		return true;
	}

}
