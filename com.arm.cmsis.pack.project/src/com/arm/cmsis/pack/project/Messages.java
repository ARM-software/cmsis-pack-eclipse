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
	public static String CmsisCodeTemplate_Browse;
	public static String CmsisCodeTemplate_Component;
	public static String CmsisCodeTemplate_ContainerNotExist;
	public static String CmsisCodeTemplate_CreatingFile;
	public static String CmsisCodeTemplate_Description;
	public static String CmsisCodeTemplate_Error;
	public static String CmsisCodeTemplate_FileAlreadyExist;
	public static String CmsisCodeTemplate_FileContainerNotExist;
	public static String CmsisCodeTemplate_FileContainerNotSpecified;
	public static String CmsisCodeTemplate_FileExtensionNotConsistent;
	public static String CmsisCodeTemplate_FileName;
	public static String CmsisCodeTemplate_FileNameNotSpecified;
	public static String CmsisCodeTemplate_FileNameNotValid;
	public static String CmsisCodeTemplate_FileUnderRTEFolder;
	public static String CmsisCodeTemplate_Location;
	public static String CmsisCodeTemplate_Name;
	public static String CmsisCodeTemplate_OpeningFileForEditing;
	public static String CmsisCodeTemplate_OverwriteExistingFile;
	public static String CmsisCodeTemplate_ProjectNotWritable;
	public static String CmsisCodeTemplate_RefreshRTEProject;
	public static String CmsisCodeTemplate_SelectFolder;
	public static String CmsisCodeTemplate_Title;
	public static String CmsisCodeTemplate_WindowTitle;
	public static String CmsisCodeTemplate_WizardPage;
	public static String CmsisCodeTemplateNewWizardPage_LocationNotUnderProject;
	public static String CmsisCodeTemplateNewWizardPage_Project;
	public static String CmsisCodeTemplateNewWizardPage_ProjectMustBeSpecified;
	public static String CmsisCodeTemplateNewWizardPage_ProjectMustExist;	
	public static String CmsisCodeTemplate_NoTemplates;
	public static String CmsisHeadlessBuilder_CmsisPackManagerNotAvailable;
	public static String CmsisHeadlessBuilder_cmsisPackRootUsage;
	public static String CmsisHeadlessBuilder_HelpUsage;
	public static String CmsisHeadlessBuilder_NoCmsisPackRoot;
	public static String CmsisHeadlessBuilder_NoInstalledPacks;	
	public static String CreateRteProject_EclipseProjectNotExists;
	public static String CreateRteProject_ErrorCreatingConfigFile;
	public static String CreateRteProject_ErrorCreatingRteProject;
	public static String CreateRteProject_ToolchainAdapterNotFound;
	public static String MergeConfigFileHandler_Merge;
	public static String ProjectSelectionDialog_NoRteProjectFound;
	public static String ProjectSelectionDialog_RteProjectSelectionDialog;
	public static String ProjectSelectionDialog_SelectRteProject;
	public static String ProjectUtils_CannotCopyFile;
	public static String ProjectUtils_DoesNotExistsOrNotAccessible;
	public static String ProjectUtils_Project;
	public static String ProjectUtils_ProjectfolderMustBeRelative;
	public static String ProjectUtils_TheFile;
	public static String ProjectUtils_to;
	public static String RteConfigRenameResourceChange_RenamingFile;
	public static String RteConfiguration_ComponentSelection;
	public static String RteConfiguration_DeviceHeader;
	public static String RteProjectManager_ReloadRteProjectMessage;
	public static String RteProjectManager_ReloadRteProjectTitle;
	public static String RteProjectNature_AddRteNature;
	public static String RteProjectRenameParticipant_CheckingPreconditions;
	public static String RteProjectRenameParticipant_CMSIS_RTE_project_rename_handler;
	public static String RteProjectRenameParticipant_CreatingChange;
	public static String RteProjectRenameParticipant_RenameIsNotAllowed;
	public static String RteProjectRenameParticipant_RenameOfRteFolderIsNotAllowed;
	public static String RteProjectTemplate_CMSIS_RTE_Project;
	public static String RteProjectUpdater_Cause;
	public static String RteProjectUpdater_ConfigChanged;
	public static String RteProjectUpdater_ErrorLoadinConfigFile;
	public static String RteProjectUpdater_ErrorConfigFileNotExist;
	public static String RteProjectUpdater_ErrorParsingFailed;
	public static String RteProjectUpdater_ErrorCmisPackRootNotSet;
	public static String RteProjectUpdater_ErrorProjectIsNull;
	public static String RteProjectUpdater_ErrorUpdatingRteProject;
	public static String RteProjectUpdater_LoadingRteConfiguration;
	public static String RteProjectUpdater_PacksInstalled;
	public static String RteProjectUpdater_ProjectCreated;
	public static String RteProjectUpdater_ProjectRefresh;
	public static String RteProjectUpdater_ProjectReset;
	public static String RteProjectUpdater_ProjectUpdated;
	public static String RteProjectUpdater_UpdatingBuildSettings;
	public static String RteProjectUpdater_UpdatingProject;
	public static String RteProjectUpdater_UpdatingResources;
	public static String RteProjectUpdater_Success;
	public static String RteProjectUpdater_Fail;
	public static String RteProjectUpdater_GpdscChanged;
	public static String RteProjectUpdater_ImportCompleted;
	public static String RteProjectUpdater_InitialProjectLoad;
	public static String RteProjectUpdater_InstallMissinPacksMessage;
	public static String RteProjectUpdater_InstallMissinPacksTitle;
	public static String RteProjectUpdater_isChanged;
	public static String RteProjectUpdater_isSaved;
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
	public static String UpdateConfigFileHandler_Downgrade;
	public static String UpdateConfigFileHandler_UpdateSelectedFiles;
	public static String UpdateConfigFileHandler_Upgrade;
	public static String CpEclipseExampleImporter_ErrorWhileCopyingExample;
	public static String CpEclipseExampleImporter_ErrorWhileOverwritingExistingProject;
	public static String CpEclipseExampleImporter_ErrorWhileReadingProjectDescriptionFile;
	public static String CpEclipseExampleImporter_ExistsQuestion;
	public static String CpEclipseExampleImporter_FailedImportFilesFromFolder;
	public static String CopyExampleDialog_AttentionMessage;
	public static String CopyExampleDialog_CopyExampleTitle;
	public static String CopyExampleDialog_Example;
	public static String CopyExampleDialog_Pack;
	public static String CopyExampleDialog_ProjectName;
	public static String CopyExampleDialog_ReplaceExistingProject;
	public static String CopyExampleDialog_ProjectLocation;
	public static String OverwriteQuery_ExistsQuestion;
	public static String OverwriteQuery_OverwriteNameAndPathQuestion;
	public static String OverwriteQuery_Question;
    public static String RteProjectImporter_JobMessage;
    public static String RteProjectImporter_CancelImport;
    public static String RteProjectImporter_ErrorCDTProjectCreation;



	public static String RteProjectImporter_Import_Completed;
	public static String RteProjectImporter_Importing_Example;
	public static String RteProjectImporter_Importing_File;



	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
