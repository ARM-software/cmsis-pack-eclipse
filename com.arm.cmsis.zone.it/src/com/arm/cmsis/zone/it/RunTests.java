package com.arm.cmsis.zone.it;

import java.io.IOException;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

public class RunTests {
	
	private static CmsisZoneHeadlessMode headlessModeCmsisZone = null; 
	private static CmsisZoneTestReport fTestReport = null; 
	
	/**
	 * Constructor 
	 */
	public RunTests(){
		
	}
	
	
	/**
	 * Calls all classes under test
	 */
	public void runTests(){
		JUnitCore junit = new JUnitCore();
		Result result = junit.run(CmsisZoneTest.class); 
		resultReport(result);
	}
	
	
	/**
	 * Prints test report
	 * @param result
	 */
	public static void resultReport(Result result) {	
		String testResultJUnit = Messages.RunTests_Finish + result.getFailureCount() + 
				                 Messages.RunTests_Ignored + result.getIgnoreCount() +
				                 Messages.RunTests_TestRun + result.getRunCount()  + 
				                 Messages.RunTests_Time + result.getRunTime() + Messages.RunTests_TimeMs;	
		//Write detailed test report
		CmsisZoneTestReport testReport = getTestReport();		
		try {	
			testReport.setTestResultJUnit(testResultJUnit);
			testReport.writeTestReport();
		} catch (IOException e) {			
			e.printStackTrace();
		}	    
	}
	
	
	/***getters***/
	public static CmsisZoneHeadlessMode getHeadlessModeCmsisZone() {
		return headlessModeCmsisZone;
	}
	
	public static CmsisZoneTestReport getTestReport() {
		return fTestReport;
	}
	
	
	/***setters***/
	public static void setHeadlessModeCmsisZone(CmsisZoneHeadlessMode headlessMode) {
		headlessModeCmsisZone = headlessMode;
	}	
	
	public static void setTestReport(CmsisZoneTestReport testReport) {
		fTestReport = testReport;
	}
	
}
