/*******************************************************************************
 * Copyright (c) 2016 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/

package com.arm.cmsis.pack.rte;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.arm.cmsis.pack.data.CpPack;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPack.PackState;
import com.arm.cmsis.pack.info.ICpItemInfo;
import com.arm.cmsis.pack.info.ICpPackInfo;
import com.arm.cmsis.pack.rte.dependencies.IRteDependencyItem;

public class RteModelUtils {

	/**
	 * Return a collection of missing pack IDs
	 * @param model the RTE model
	 * @return a collection of missing pack IDs or an empty collection
	 */
	public static Collection<String> getMissingPacks(IRteModel model) {
		Set<String> missingPacks = new HashSet<String>();
		if (model == null) {
			return missingPacks;
		}
		Collection<? extends IRteDependencyItem> results = model.getDependencyItems();
		for(IRteDependencyItem item : results){
			ICpItem cpItem = item.getCpItem();
			if (cpItem instanceof ICpItemInfo) {
				ICpItemInfo ci = (ICpItemInfo) cpItem;
				ICpPackInfo pi = ci.getPackInfo();
				ICpPack pack = pi.getPack();
				if (pack == null || pack.getPackState() != PackState.INSTALLED) {
					missingPacks.add(CpPack.constructPackId(pi.attributes()));
				}
			}
		}
		return missingPacks;
	}
}
