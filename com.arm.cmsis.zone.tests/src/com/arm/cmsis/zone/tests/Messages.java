package com.arm.cmsis.zone.tests;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.arm.cmsis.zone.tests.messages"; //$NON-NLS-1$
	public static String CmsisZoneTest_BiggerNumberofGoldenResults;
	public static String CmsisZoneTest_FzoneFileType;
	public static String CmsisZoneTest_LessNumberOfGoldenResults;
	public static String CmsisZoneTest_NoGoldenResults;
	public static String CmsisZoneTest_NoTestResults;
	public static String CmsisZoneTestData_AzoneCommand;
	public static String CmsisZoneTestData_AzoneFileType;
	public static String CmsisZoneTestData_PluginXML;
	public static String CmsisZoneTestReport_AdditionalLines;
	public static String CmsisZoneTestReport_DashLineBegin;
	public static String CmsisZoneTestReport_DashLineEnd;
	public static String CmsisZoneTestReport_GoldenResultsComparison;
	public static String CmsisZoneTestReport_LogTestResultsFile;
	public static String CmsisZoneTestReport_MissingLines;
	public static String CmsisZoneTestReport_StarsLineBegin;
	public static String CmsisZoneTestReport_StartsLineEnd;
	public static String CmsisZoneTestReport_Sucess;
	public static String CmsisZoneTestReport_TestCase;
	public static String RunTests_Finish;
	public static String RunTests_Ignored;
	public static String RunTests_TestRun;
	public static String RunTests_Time;
	public static String RunTests_TimeMs;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
