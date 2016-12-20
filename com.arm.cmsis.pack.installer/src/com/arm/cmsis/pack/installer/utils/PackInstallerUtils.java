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

package com.arm.cmsis.pack.installer.utils;


import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPack.PackState;
import com.arm.cmsis.pack.utils.VersionComparator;

/**
 * Utilities used in Pack Installer
 */
public class PackInstallerUtils {

	/**
	 * Loads new latest pack when existing latest pack is deleted.
	 * @param deletedPack deleted pack
	 * @return newly loaded pack if it is the latest available one, otherwise null
	 */
	public static ICpPack loadLatesPack(ICpPack deletedPack) {

		ICpItem parent = deletedPack.getParent();
		ICpPack newPack = null;
		// Deleted the latest version of this pack family
		if (deletedPack == parent.getFirstChild()) {
			String webDir = CpPlugIn.getPackManager().getCmsisPackWebDir();
			if(webDir == null || webDir.isEmpty()) {
				return null;
			}
			IPath webPdscFilePath = new Path(webDir).append(deletedPack.getPackFamilyId() + CmsisConstants.EXT_PDSC);
			if (!webPdscFilePath.toFile().exists()) {
				return null;
			}
			newPack = CpPlugIn.getPackManager().readPack(webPdscFilePath.toOSString());

			if (newPack != null) {
				newPack.setPackState(PackState.AVAILABLE);
				String newPackVersion = newPack.getVersion();
				if (parent.getPack() != null) {
					String lastestPackVersion = parent.getPack().getVersion();
					if (VersionComparator.versionCompare(newPackVersion, lastestPackVersion) < 0) {
						// new pack's version < latest pack's version. ignore this new pack
						newPack = null;
					}
				}
			}
		}
		return newPack;
	}

}
