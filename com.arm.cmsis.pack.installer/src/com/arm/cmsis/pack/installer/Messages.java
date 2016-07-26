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

package com.arm.cmsis.pack.installer;

import org.eclipse.osgi.util.NLS;

/**
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.arm.cmsis.pack.installer.messages"; //$NON-NLS-1$
	public static String CopyExampleDialog_AttentionMessage;
	public static String CopyExampleDialog_CopyExampleTitle;
	public static String CopyExampleDialog_Example;
	public static String CopyExampleDialog_Pack;
	public static String CopyExampleDialog_ProjectName;
	public static String CopyExampleDialog_ReplaceExistingProject;
	public static String CopyExampleDialog_ProjectLocation;
	public static String CpPackInstaller_Completed;
	public static String CpPackInstaller_CreatingNewProject;
	public static String CpPackInstaller_DeletingPack;
	public static String CpPackInstaller_ErrorWhileCopyingExample;
	public static String CpPackInstaller_ErrorWhileOverwritingExistingProject;
	public static String CpPackInstaller_ErrorWhileReadingProjectDescriptionFile;
	public static String CpPackInstaller_ErrorWhileRefreshingCheckFolder;
	public static String CpPackInstaller_ErrorWhileRefreshingIgnored;
	public static String CpPackInstaller_ExistsQuestion;
	public static String CpPackInstaller_FailedImportFilesFromFolder;
	public static String CpPackInstaller_FileNotFound;
	public static String CpPackInstaller_FinishingOperation;
	public static String CpPackInstaller_ImportingPack;
	public static String CpPackInstaller_InstallingPack;
	public static String CpPackInstaller_JobCancelled;
	public static String CpPackInstaller_NoPacksFound;
	public static String CpPackInstaller_OpenPackManagerToUpdatePacks;
	public static String CpPackInstaller_PackFamilyNotFound;
	public static String CpPackInstaller_Parsing;
	public static String CpPackInstaller_Processing;
	public static String CpPackInstaller_ProjectAlreadyExists;
	public static String CpPackInstaller_ProjectWillBeCreated;
	public static String CpPackInstaller_RefreshAllPacks;
	public static String CpPackInstaller_ReloadPacksAndManagerMessage;
	public static String CpPackInstaller_ReloadPacksAndManagerTitle;
	public static String CpPackInstaller_RemovingPack;
	public static String CpPackInstaller_RepoTypeNotSupported;
	public static String CpPackInstaller_TimeoutConsoleMessage;
	public static String CpPackInstaller_TimeoutMessage;
	public static String CpPackInstaller_Timout;
	public static String CpPackInstaller_UnpackingPack;
	public static String CpPackInstaller_Updating;
	public static String CpPackInstaller_WasNotSuccessful;

	public static String CpPackJob_CancelledByUser;

	public static String CpPackInstallJob_CannotFindPdscFile;
	public static String CpPackInstallJob_ConnectingTo;
	public static String CpPackInstallJob_DownloadingFrom;
	public static String CpPackInstallJob_FileNotFound;
	public static String CpPackInstallJob_InstallingPack;
	public static String CpPackInstallJob_MalformedURL;
	public static String CpPackInstallJob_TimeoutConsoleMessage;
	public static String CpPackInstallJob_UnzippingAndParsing;
	public static String CpPackRemoveJob_DeletingFilesFromFolder;
	public static String CpPackRemoveJob_RemovingPack;

	public static String CpPackUnpackJob_FailedToUnzipFile;
	public static String CpPackUnpackJob_InvalidOperation;
	public static String CpPackUnpackJob_OverwriteQuery;
	public static String CpPackUnpackJob_PathAlreadyExists;
	public static String CpPackUnpackJob_FailToParsePdscFile;
	public static String CpPackUnpackJob_PdscFileNotFoundInFolder;
	public static String CpPackUnpackJob_SourceFileCannotBeFound;
	public static String CpPackUnpackJob_Unpacking;

	public static String LicenseDialog_AgreeText;
	public static String LicenseDialog_GuidanceText;
	public static String LicenseDialog_LicenseAgreement;
	public static String LicenseDialog_LicenseDialogTitle;
	public static String OverwriteQuery_ExistsQuestion;
	public static String OverwriteQuery_OverwriteNameAndPathQuestion;
	public static String OverwriteQuery_Question;

	public static String PackInstallerUtils_PleaseAgreeLicenseAgreement;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
