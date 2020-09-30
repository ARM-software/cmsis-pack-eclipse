package com.arm.cmsis.pack;

import org.eclipse.osgi.util.NLS;

public class CpStrings extends NLS {
	private static final String BUNDLE_NAME = "com.arm.cmsis.pack.CpStrings"; //$NON-NLS-1$
	public static String API;
	public static String APIversion;
	public static String Component;
	public static String Device;
	public static String ICpPackInstaller_NON_UNICODE_FILES;
	public static String IsMissing;
	public static String IsNotAvailableForCurrentConfiguration;
	public static String Latest;
	public static String Fixed;
	public static String Excluded;
	public static String Pack;
	public static String Version;
	public static String Selection;
	public static String Configuration;
	public static String Required_Gpdsc_File;
	public static String Failed_To_Load;
	public static String CpComponentInfo_ComponentMissing;
	public static String CpPackManager_DefaultError;
	public static String CpPackManager_ErrorWhileParsing;
	public static String CpPackManager_UnrecognizedFileFormatError;
	public static String CpXmlParser_Error;
	public static String CpXmlParser_ErrorCreatingXML;
	public static String CpXmlParser_ErrorParserInit;
	public static String CpXmlParser_ErrorParsingFile;
	public static String CpXmlParser_ErrorSchemaInit;
	public static String CpXmlParser_FatalError;
	public static String CpXmlParser_Warning;
	public static String EvalResult_Confilct;
	public static String EvalResult_Incompatible;
	public static String EvalResult_IncompatibleApi;
	public static String EvalResult_IncompatibleBundle;
	public static String EvalResult_IncompatibleVariant;
	public static String EvalResult_IncompatibleVendor;
	public static String EvalResult_IncompatibleVersion;
	public static String EvalResult_MissingApi;
	public static String EvalResult_MissingApiVersion;
	public static String EvalResult_MissingBundle;
	public static String EvalResult_MissingComponent;
	public static String EvalResult_MissingVariant;
	public static String EvalResult_MissingVendor;
	public static String EvalResult_MissingVersion;
	public static String EvalResult_SelectComponent;
	public static String EvalResult_UnavailableComponent;
	public static String EvalResult_UnavaliablePack;
	public static String RteDependency_ComponentNotAvailable;
	public static String RteDependency_Conflict;
	public static String RteDependency_InstallMissingComponent;
	public static String RteDependency_MissingAPI;
	public static String RteDependency_MissingAPIVersion;
	public static String RteDependency_MissingBundle;
	public static String RteDependency_MissingVariant;
	public static String RteDependency_MissingVendor;
	public static String RteDependency_MissingVersion;
	public static String RteDependency_PackNotSelected;
	public static String RteDependency_SelectCompatibleAPI;
	public static String RteDependency_SelectCompatibleComponent;
	public static String RteDependency_SelectComponentFromList;
	public static String RteDependency_UpdatePackVariantOrBundleSelection;
	public static String RteDependencyResult_AdditionalComponentRequired;
	public static String RteDependencyResult_ComponentConficts;
	public static String RteComponentVendorAny;
	public static String RteComponentVersionLatest;
	public static String RteMissingComponentResult_APIDefIsMissingNoPack;
	public static String RteMissingComponentResult_IsFoundInPack;
	public static String RteMissingComponentResult_NoComponentFoundMatchingDeviceCompiler;
	public static String RteMissingComponentResult_orHigherIsRequired;
	public static String RtePackIsNotInstalled;
	public static String PackIdIsNotAvailable;
	public static String IsNotInstalled;
	public static String IsExcluded;
	public static String PackFilterInInEffectComponentsFiltered;
	public static String SelectedDevice;
	public static String DeviceNotFound;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, CpStrings.class);
	}

	private CpStrings() {
	}
}
