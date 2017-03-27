/*******************************************************************************
* Copyright (c) 2017 ARM Ltd. and others
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
import com.arm.cmsis.pack.utils.VersionComparator;

/**
 *  Comparator to sort packs by their IDs (any vendor < Keil )  
 */
public class CpPackIdComparator extends VersionComparator {

	
	public CpPackIdComparator() {
		super();
	}

	public CpPackIdComparator(boolean descending) {
		super(descending);
	}

	public CpPackIdComparator(boolean descending, boolean caseSensitive) {
		super(descending, caseSensitive);
	}

	protected static int compareKeil(String packId0, String packId1) {
		boolean bKeil0 = packId0.startsWith(CmsisConstants.KEIL);
		boolean bKeil1 = packId1.startsWith(CmsisConstants.KEIL);
		if(bKeil0 != bKeil1) {
			int res = bKeil0 ? 1 : -1;
			return res;
		}
		return 0;
	}

	@Override
	protected int compare(String packId1, String packId2, boolean cs) {
		int res = compareKeil(packId1, packId2);
		if(res != 0) {
			return res;
		}
		return super.compare(packId1, packId2, cs);
	}
	
	public static int comparePackIds(String packId1, String packId2, boolean cs) {
		int res = compareKeil(packId1, packId2);
		if(res != 0) {
			return res;
		}
		return versionCompare(packId1, packId2, cs);
	}
}
