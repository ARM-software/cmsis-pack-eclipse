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

package com.arm.cmsis.pack.info;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpFile;
import com.arm.cmsis.pack.data.ICpFile;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.enums.EFileRole;

/**
 * Default implementation of ICpFileInfo interface
 * @see ICpFileInfo
 * @see CpFile
 */
/**
 *
 */
public class CpFileInfo extends CpFile implements ICpFileInfo {

	ICpFile fFile = null;
	
	public CpFileInfo(ICpItem parent, ICpFile file) {
		super(parent, file.getTag());
		setFile(file);
		updateInfo();
	}
	
	public CpFileInfo(ICpItem parent, String tag) {
		super(parent, tag);
	}

	@Override
	public ICpFile getFile() {
		return fFile;
	}


	@Override
	public void setFile(ICpFile file) {
		fFile = file;
	}


	@Override
	public void updateInfo() {
		if(fFile != null) {
			attributes().setAttributes(fFile.attributes());
			if(fFile.getRole() == EFileRole.CONFIG) {
				// ensure we have the version for config files
				String version = fFile.getVersion();
				attributes().setAttribute(CmsisConstants.VERSION, version);
			}
			if(fFile.isDeviceDependent())
				attributes().setAttribute(CmsisConstants.DEVICE_DEPENDENT, true);
			else
				attributes().removeAttribute(CmsisConstants.DEVICE_DEPENDENT);
		}
	}

	@Override
	public ICpComponentInfo getComponentInfo() {
		for( ICpItem parent = getParent(); parent != null; parent = parent.getParent()){
			if(parent instanceof ICpComponentInfo)
				return (ICpComponentInfo)parent;
		}
		return null;
	}


	@Override
	public ICpPackInfo getPackInfo() {
		ICpComponentInfo ci = getComponentInfo();
		if(ci != null)
			return ci.getPackInfo();
		return null;
	}

	
}
