package com.arm.cmsis.zone.it;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import com.arm.cmsis.pack.common.CmsisConstants;

public class CmsisZoneTestReport {
	
	private Map< String, ArrayList<Set<String>>> fContentComparison = new HashMap<String, ArrayList<Set<String>>>();
	private String fTestProject = "CmsisZoneTest"; //$NON-NLS-1$
	private String fContentValidation = CmsisConstants.EMPTY_STRING;
	private PrintWriter fWriter = null;
	private String fTestResultJUnit = CmsisConstants.EMPTY_STRING;
	
	public CmsisZoneTestReport() {    
		RunTests.setTestReport(this);		
    }

	 /**
     * Writes test report in console
     * @throws IOException in case of write error
     */
    public void writeTestReport() throws IOException{   
    	String logFilePath = getWorkspacePath() + '/' + fTestProject + '/' + Messages.CmsisZoneTestReport_LogTestResultsFile;

    	File file = new File(logFilePath);
    	fWriter =  new PrintWriter(file);

    	//Write report's content
    	writeContent(); 

    	fWriter.flush();
    	fWriter.close();        
    } 
	
    /**
     * Writes test report's content
     * @throws IOException
     */
    public void writeContent()  throws IOException { 
    	//Print JUnit result
    	println(Messages.CmsisZoneTestReport_StarsLineBegin);
    	println(getTestResultJUnit());
       	println(Messages.CmsisZoneTestReport_StartsLineEnd);
    	
    	//Print test report
    	println();    	
        println(Messages.CmsisZoneTestReport_DashLineBegin);           
      
        println(Messages.CmsisZoneTestReport_GoldenResultsComparison);
        writeFilesComparison();
        println();
                
        println();
        println(Messages.CmsisZoneTestReport_DashLineEnd);
        println();
    }
    
    private void println(){
    	System.out.print(System.lineSeparator());
    	fWriter.write(System.lineSeparator()); 
    }
    
    private void println(String s){
    	System.out.println(s);
    	fWriter.write(s);
    	fWriter.write(System.lineSeparator());
    }
    
    /**
     * Writes comparison of golden and test files
     * @throws IOException
     */
    public void writeFilesComparison() throws IOException {
    	//Get validation
    	String contentValidation = getContentValidation();

    	if(contentValidation.isEmpty()){ //Proceed with comparison writing
    		//Iterate over map and comparison sets
    		Map<String, ArrayList<Set<String>>> contentComparison = new HashMap<String, ArrayList<Set<String>>>();
    		contentComparison = getContentComparison();    		 

    		for (Map.Entry<String, ArrayList<Set<String>>> entryCopyComparison : contentComparison.entrySet()){
    			//Get test case file name
    			String file = entryCopyComparison.getKey();  
    			ArrayList<Set<String>> comparisonResult = new ArrayList<Set<String>>();
    			comparisonResult = contentComparison.get(file); 
    			if(comparisonResult.size() == 2){ // there are plusLines and minusLines
    				Set<String> plusLines = new TreeSet<>();
    				Set<String> minusLines = new TreeSet<>();
    				plusLines = comparisonResult.get(0);
    				minusLines = comparisonResult.get(1);
    				if(plusLines.size() == 0 && minusLines.size() == 0){
    					println(Messages.CmsisZoneTestReport_TestCase + file);
    					println(Messages.CmsisZoneTestReport_Sucess);    					
    				}else{
    					println(Messages.CmsisZoneTestReport_TestCase +CmsisConstants.SPACE+ file);
    					if(!plusLines.isEmpty()){    						        					
        					println(Messages.CmsisZoneTestReport_AdditionalLines);
        					for(String s : plusLines) {
        						println(s);
        					}         					
    					}
    					if(!minusLines.isEmpty()){
    						println(Messages.CmsisZoneTestReport_MissingLines);
        					for(String s : minusLines){
        						println(s);
        					}        					
    					}
    				}
    			}
    		}
    	}else{ //Write validation message
    		println(fContentValidation);
    	}    	
    }
    
    
    /**
     * Gets workspace path
     * @return String with workspace path
     */
    public String getWorkspacePath() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		if(root == null)
			return null;
		return root.getLocation().toString();
	}
   

    /***getters***/   
    
	public String getContentValidation() {
		return fContentValidation;
	}    
	
	public Map<String, ArrayList<Set<String>>> getContentComparison() {
		return fContentComparison;
	}
	
	public String getTestResultJUnit() {
		return fTestResultJUnit;
	}
	
	/***setters***/
	
	public void setContentValidation(String contentValidation) {
		this.fContentValidation = contentValidation;
	}
    
	public void setContentComparison(Map<String, ArrayList<Set<String>>> contentComparison) {
		this.fContentComparison = contentComparison;
	}
	
	public void setTestResultJUnit(String ftestResultJUnit) {
		this.fTestResultJUnit = ftestResultJUnit;
	}	
}
