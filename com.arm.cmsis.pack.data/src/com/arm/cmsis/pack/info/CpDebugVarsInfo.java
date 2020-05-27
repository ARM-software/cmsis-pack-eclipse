/*******************************************************************************
* Copyright (c) 2015-2018 ARM Ltd. and others
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
import com.arm.cmsis.pack.data.ICpFile;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.enums.EFileCategory;
import com.arm.cmsis.pack.enums.EFileRole;

/**
 * ICpFileInfo interface for CpDebugVars
 * 
 * @see ICpFileInfo
 * @see ICpFile
 */
public class CpDebugVarsInfo extends CpFileInfo {


	public CpDebugVarsInfo(ICpItem parent, ICpFile file) {
		super(parent, file);
		setFile(file);
	}

	public CpDebugVarsInfo(ICpItem parent, String tag) {
		super(parent, tag);
	}

	@Override
	public ICpPackInfo getPackInfo() {
		ICpDeviceInfo di = getParentOfType(ICpDeviceInfo.class);
		if (di != null) {
			return di.getPackInfo();
		}
		return null;
	}
	
	@Override
	public String getName() {
		return getAttribute(CmsisConstants.CONFIGFILE);
	}

	@Override
	public synchronized EFileCategory getCategory() {
		return EFileCategory.OTHER;
	}

	@Override
	public synchronized EFileRole getRole() {
		return EFileRole.CONFIG;
	}

}
