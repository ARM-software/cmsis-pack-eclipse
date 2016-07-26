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

package com.arm.cmsis.pack.data;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.arm.cmsis.pack.common.CmsisConstants;

/**
 *  Convenience class to access information under "debug" device property
 */
public class CpDebug extends CpDeviceProperty implements ICpDebug {

	public CpDebug(ICpItem parent, String tag) {
		super(parent, tag);
	}

	
	@Override
	public String constructId() {
		String id = super.constructId();
		int index = getPunitIndex();
		if(index > 0) {
			String punit = getAttribute(CmsisConstants.PUNIT);
			id += '.' + punit;
		}
		return id;
	}


	@Override
	public String getSvdFile() {
		return getAbsolutePath(attributes().getAttribute(CmsisConstants.SVD));
	}

	@Override
	public Collection<ICpDataPatch> getDataPacthes() {
		List<ICpDataPatch> patches = new LinkedList<ICpDataPatch>();
		Collection<? extends ICpItem> children = getChildren();
		if(children != null) {
			for(ICpItem item : children) {
				if(item instanceof ICpDataPatch)
					patches.add((ICpDataPatch)item);
			}
		}
		return patches;
	}
	
}
