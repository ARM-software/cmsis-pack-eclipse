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

package com.arm.cmsis.pack.project;

import org.eclipse.osgi.util.NLS;

/**
 * @author edriouk
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.arm.cmsis.pack.project.messages"; //$NON-NLS-1$
	public static String CreateRteProject_EclipseProjectNotExists;
	public static String CreateRteProject_ErrorCreatingConfigFile;
	public static String CreateRteProject_ErrorCreatingRteProject;
	public static String CreateRteProject_ToolchainAdapterNotFound;
	public static String ProjectUtils_CannotCopyFile;
	public static String ProjectUtils_DoesNotExistsOrNotAccessible;
	public static String ProjectUtils_Project;
	public static String ProjectUtils_ProjectfolderMustBeRelative;
	public static String ProjectUtils_TheFile;
	public static String ProjectUtils_to;
	public static String RteConfigRenameResourceChange_RenamingFile;
	public static String RteConfiguration_ComponentSelection;
	public static String RteConfiguration_DeviceHeader;
	public static String RteProjectRenameParticipant_CheckingPreconditions;
	public static String RteProjectRenameParticipant_CMSIS_RTE_project_rename_handler;
	public static String RteProjectRenameParticipant_CreatingChange;
	public static String RteProjectRenameParticipant_RenameIsNotAllowed;
	public static String RteProjectRenameParticipant_RenameOfRteFolderIsNotAllowed;
	public static String RteProjectTemplate_CMSIS_RTE_Project;
	public static String RteProjectUpdater_ErrorLoadinConfigFile;
	public static String RteProjectUpdater_ErrorConfigFileNotExist;
	public static String RteProjectUpdater_ErrorParsingFailed; 
	public static String RteProjectUpdater_ErrorProjectIsNull;
	public static String RteProjectUpdater_ErrorUpdatingRteProject;
	public static String RteProjectUpdater_LoadingRteConfiguration;
	public static String RteProjectUpdater_ProjectUpdated;
	public static String RteProjectUpdater_UpdatingBuildSettings;
	public static String RteProjectUpdater_UpdatingProject;
	public static String RteProjectUpdater_UpdatingResources;
	public static String RteProjectUpdater_Success;
	public static String RteProjectUpdater_Fail;
	public static String RtePropertyPage_Component;
	public static String RtePropertyPage_ComponentNotFound;
	public static String RtePropertyPage_Description;
	public static String RtePropertyPage_Location;
	public static String RtePropertyPage_Path;
	public static String RtePropertyPage_ResolvedVersion;
	public static String RtePropertyPage_SoftwarePack;
	public static String RtePropertyPage_Type;
	public static String RtePropertyPage_URL;
	public static String RtePropertyPage_Version;
	public static String RteTemplateCmsisProjectPage_NoTollchainAvailable;
	public static String RteTemplateCmsisProjectPage_Output;
	public static String RteTemplateCmsisProjectPage_SelectedProjectType;
	public static String RteTemplateDeviceSelectorPage_NoDevicesAreAvailable;
	public static String RteTemplateDeviceSelectorPage_NoPackManagerIsAvailble;
	public static String RteTemplateCmsisProjectPage_CreateDefaultMain;
	public static String RteTooolChainAdapterSelector_Adapter;
	public static String RteTooolChainAdapterSelector_Family;
	public static String RteTooolChainAdapterSelector_FamilyToolTipAndDescription;
	public static String RteTooolChainAdapterSelector_NoToolchainAdapterAvailable;
	public static String RteTooolChainAdapterSelector_PassedviaTCompiler;
	public static String RteTooolChainAdapterSelector_SelectFamily;
	public static String RteTooolChainAdapterSelector_Toolchain;
	public static String RteTooolChainAdapterSelector_ToolchainAdapter;
	public static String RteTooolChainAdapterSelector_ToolChainAdapterIsReponsibleFor;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
