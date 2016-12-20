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

package com.arm.cmsis.pack.installer.ui.views;

import org.eclipse.jface.action.IContributionManager;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

import com.arm.cmsis.pack.ui.CpPlugInUI;

/**
 * Responsible for common code in the views
 */
public class PackInstallerViewUtils {

	/**
	 * Add the Management Commands to the Local Tool bar
	 * @param viewPart the view part, e.g. PacksView, ExamplesView...
	 * @param manager the contributionManager
	 */
	public static void addManagementCommandsToLocalToolBar(IViewPart viewPart, IContributionManager manager) {
		String[] commandIds = { "com.arm.cmsis.pack.installer.commands.updateCommand", //$NON-NLS-1$
				"com.arm.cmsis.pack.installer.commands.importPackCommand", //$NON-NLS-1$
				"com.arm.cmsis.pack.installer.commands.reloadCommand", //$NON-NLS-1$
				"com.arm.cmsis.pack.installer.commands.importFolderPacksCommand" }; //$NON-NLS-1$
		String[] commandImages = { CpPlugInUI.ICON_CHECK4UPDATE, CpPlugInUI.ICON_RTE_UNPACK,
				CpPlugInUI.ICON_REFRESH, CpPlugInUI.ICON_IMPORT_FOLDER };

		for (int i = 0; i < commandIds.length; i++) {
			String commandId = commandIds[i];
			CommandContributionItemParameter p = new CommandContributionItemParameter(viewPart.getSite(), commandId, commandId,
					CommandContributionItem.STYLE_PUSH);
			p.icon = CpPlugInUI.getImageDescriptor(commandImages[i]);
			CommandContributionItem item = new CommandContributionItem(p);
			manager.add(item);
		}
	}
}
