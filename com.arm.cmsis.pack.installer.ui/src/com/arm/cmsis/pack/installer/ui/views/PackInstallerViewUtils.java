/*******************************************************************************
 * Copyright (c) 2021 ARM Ltd. and others
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

    private static final String[][] VIEW_MENU_COMMANDS = { // id, image
            { "com.arm.cmsis.pack.installer.commands.reloadCommand", CpPlugInUI.ICON_REFRESH }, //$NON-NLS-1$
            { "com.arm.cmsis.pack.installer.commands.updateCommand", CpPlugInUI.ICON_CHECK4UPDATE }, //$NON-NLS-1$
            { "com.arm.cmsis.pack.installer.commands.importPackCommand", CpPlugInUI.ICON_RTE_UNPACK }, //$NON-NLS-1$
            { "com.arm.cmsis.pack.installer.commands.importFolderPacksCommand", CpPlugInUI.ICON_IMPORT_FOLDER }, //$NON-NLS-1$
            { "com.arm.cmsis.pack.installer.commands.manLocalRepoCommand", CpPlugInUI.ICON_MAN_LOCAL_REPO } //$NON-NLS-1$
    };

    /**
     * Add the Management Commands to the Local Tool bar
     *
     * @param viewPart the view part, e.g. PacksView, ExamplesView...
     * @param manager  the contributionManager
     */
    public static void addManagementCommandsToLocalToolBar(IViewPart viewPart, IContributionManager manager) {
        for (int i = 0; i < VIEW_MENU_COMMANDS.length; i++) {
            String commandId = VIEW_MENU_COMMANDS[i][0];
            CommandContributionItemParameter p = new CommandContributionItemParameter(viewPart.getSite(), commandId,
                    commandId, CommandContributionItem.STYLE_PUSH);
            p.icon = CpPlugInUI.getImageDescriptor(VIEW_MENU_COMMANDS[i][1]);
            CommandContributionItem item = new CommandContributionItem(p);
            manager.add(item);
        }
    }
}
