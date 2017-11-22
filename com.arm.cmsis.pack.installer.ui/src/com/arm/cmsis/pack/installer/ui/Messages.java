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

package com.arm.cmsis.pack.installer.ui;

import org.eclipse.osgi.util.NLS;

/**
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.arm.cmsis.pack.installer.ui.messages"; //$NON-NLS-1$
	public static String BoardsView_AvailableInPack;
	public static String BoardsView_Boards;
	public static String BoardsView_DeprecatedBoard;
	public static String PackInstallerView_CheckForUpdatesLink;
	public static String PackInstallerView_Help;
	public static String BoardsView_RemoveSelection;
	public static String BoardsView_SearchBoard;
	public static String CollapseAll;
	public static String CollapseAllNodes;
	public static String CollapseSelected;
	public static String CollapseSelectedNode;
	public static String CpInstallerPlugInUI_ExitEclipse;
	public static String CpInstallerPlugInUI_ExitEclipseMessage;
	public static String CpInstallerPlugInUI_OFFLINE;
	public static String CpInstallerPlugInUI_ONLINE;
	public static String DevicesView_1Device;
	public static String DevicesView_AvailableInPack;
	public static String DevicesView_DeprecatedDevice;
	public static String DevicesView_Devices;
	public static String DevicesView_Processor;
	public static String DevicesView_RemoveSelection;
	public static String DevicesView_SearchDevice;
	public static String ExamplesView_Board;
	public static String ExamplesView_CopyExampleInstallPack;
	public static String ExamplesView_Device;
	public static String ExamplesView_OnlyShowInstalledPack;
	public static String ExamplesView_Pack;
	public static String ExamplesView_SearchExample;
	public static String ExpandAll;
	public static String ExpandAllNodes;
	public static String ExpandSelected;
	public static String ExpandSelectedNode;
	public static String Help;
	public static String ImportPacksHandler_DialogText;
	public static String ImportFolderPacksHandler_Message;
	public static String ImportFolderPacksHandler_Title;
	public static String PackPropertyView_CopyAction;
	public static String PackPropertyView_CopyTooltip;
	public static String PackPropertyView_InstallAction;
	public static String PackInstallerView_InstallRequiredPacks;
	public static String PackInstallerView_InstallRequiredPacksToolTip;
	public static String PackInstallerView_OpenPreferenceLink;
	public static String PackPropertyView_InstallTooltip;
	public static String PackPropertyView_UnpackAction;
	public static String PackPropertyView_UnpackTooltip;
	public static String PacksExamplesViewFilter_NoBoards;
	public static String PacksExamplesViewFilter_NoDevices;
	public static String PacksView_1Pack;
	public static String PacksView_CannotLoadPdscFiles;
	public static String PacksView_CheckForUpdate;
	public static String PacksView_CheckForUpdateOnWeb;
	public static String PacksView_Remove;
	public static String PacksView_RemovePlusDelete;
	public static String PacksView_RemoveSelectedPack;
	public static String PacksView_Delete;
	public static String PacksView_Delete_;
	public static String PacksView_DeleteAllTooltip;
	public static String PacksView_DeleteSelectedPack;
	public static String PacksView_DeprecatedOn;
	public static String PacksView_GenericPacksDescription;
	public static String PacksView_InstallSinglePack;
	public static String PacksView_InstallSinglePackTooltip;
	public static String PacksView_Location;
	public static String PacksView_Packs;
	public static String PacksView_PreviousPackVersions;
	public static String PacksView_ReplacedBy;
	public static String PacksView_RequiredPacks;
	public static String PacksView_ResolveRequiredPacks;
	public static String PacksView_SearchPack;
	public static String PacksView_Selected;
	public static String PacksView_ShowPacksOutline;
	public static String PacksView_UnpackSinglePack;
	public static String PacksView_Version;
	public static String ReloadPacksHandler_RefreshPacks;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
