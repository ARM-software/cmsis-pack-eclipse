package com.arm.cmsis.zone.it;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.junit.Test;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.utils.AlnumComparator;
import com.arm.cmsis.pack.utils.Utils;

public class CmsisZoneTest {
	
	private String fTestProject = "CmsisZoneTest"; //$NON-NLS-1$
	private String fTestResultsFolder = "TestResults"; //$NON-NLS-1$	
	private String fGoldenResultsFolder = "GoldenResults"; //$NON-NLS-1$	
	private Map<String, String> fGoldenResults = new HashMap<String, String>();
	private Map<String, String> fTestResults = new HashMap<String, String>();
	private boolean fIsThereADefect = false;	
	private CmsisZoneTestReport fCmsisZoneTestReport = new CmsisZoneTestReport();
	private Set<String> fProjectFiles = new HashSet<>();

	
	public CmsisZoneTest() {
		
	}
	
	@Test
	public void testFzoneFilesContent() {	
		//Set test result
		boolean result = validateContent();	

		//Asserts that condition is false i.e. there is not a defect. If it isn't (i.e. there is a defect) it throws an AssertionError without a message.
		assertFalse(result);		
	}
	
	
	/**
	 * Finds difference between golden result files and test result files
	 * @return
	 */
	private boolean validateContent(){		
		
		//Create map to save copy comparison
		Map<String, ArrayList<Set<String>>> contentComparison = new HashMap<String, ArrayList<Set<String>>>();
		
		//Collect golden result files and test result files
		collectFilesResults();

		//Get golden results files from 'workspacePath\CmsisZoneTest\GoldenResults' 
		Map<String, String> goldenResults = new HashMap<String, String>();
		goldenResults = getGoldenResults();		
		//Map< LPC55S69.azone, workspacePath\CmsisZoneTest\GoldenResults\TC1\LPC55S69.azone > 
		
		//Get test results files from 'workspacePath\UvImporterTest\TestResults'
		Map<String, String> testResults = new HashMap<String, String>();
		testResults = getTestResults();
		//Map< LPC55S69.azone, workspacePath\CmsisZoneTest\TestResults\TC1\LPC55S69.azone > 
		
		//Validate maps data
		String validation = validate();
		fCmsisZoneTestReport.setContentValidation(validation);
		
		if(validation.isEmpty()){//Proceed with files comparison
			
			String goldenResultFile = CmsisConstants.EMPTY_STRING;
    		String testResultFile = CmsisConstants.EMPTY_STRING;
			
			//Iterate over golden results map, get key and with this key look for file in test result map, then make comparison of files
        	for (Map.Entry<String, String> entryGoldenResults : goldenResults.entrySet()) {
        		//Get key of golden results map
        		String keyGD = entryGoldenResults.getKey();
        		//Get golden result file
        		goldenResultFile = entryGoldenResults.getValue(); 
        		//Get test result file
        		testResultFile = testResults.get(keyGD); 
        		
        		//Compare files 
        		try {
        			ArrayList<Set<String>> comparisonResult = new ArrayList<Set<String>>();
        			comparisonResult = CmsisZoneCompareFiles.compareFiles(goldenResultFile, testResultFile);
        			
        			if(!comparisonResult.isEmpty() && comparisonResult.size() == 2){
        				//Write plus lines        			
        				Set<String> plusLines = new TreeSet<>();
        				plusLines = comparisonResult.get(0);          				
        				int plusLinesSize = plusLines.size();
        			        				
        				//Get no project files
        				Set<String>  noProjectFiles = new TreeSet<>(new AlnumComparator(false));
        				noProjectFiles = getNoProjectFiles(plusLines);
        				int noProjectFilesSize = noProjectFiles.size();
            			
            			//Write missing lines           			
            			Set<String> minusLines = new TreeSet<>();
            			minusLines = comparisonResult.get(1);
            			int minusLinesSize = minusLines.size();
            			
            			comparisonResult.clear(); //Reset arrayList
        				comparisonResult.add(noProjectFiles); //add set of no project files
        				comparisonResult.add(minusLines); //add minus lines           			
                 			
            			//Save comparison result e.g. <TC1_FastModels.txt, comparisonResult>            			
            			contentComparison.put(keyGD, comparisonResult);   
            			
            			if(plusLinesSize > 0 || minusLinesSize > 0 || noProjectFilesSize > 0){ 
            				fIsThereADefect = true;
            			}            			
        			}      			        			

        		} catch (Exception e) {			
        			e.printStackTrace();
        		}        		
        	}        	        
        	fCmsisZoneTestReport.setContentComparison(contentComparison);
        	
		}else{//Maps data are wrong
			fIsThereADefect = true;			
		}		
		//Validation flag to indicate a difference between golden files and golden results 
		return fIsThereADefect;
	}
	
	
	 /**
     * Collects golden data and test results
     */
    private void collectFilesResults(){    	
    	//Get golden result files
		processGoldenResultsContainer();
    	
		//Get test result files			
		processTestResultsContainer(); 		
	}
	
    
    private void processGoldenResultsContainer(){
    	String workSpace = getWorkspacePath();
    	String tesGoldenData = Utils.addTrailingSlash(workSpace) + Utils.addTrailingSlash(fTestProject) + Utils.addTrailingSlash(fGoldenResultsFolder);
    	File[] files = new File(tesGoldenData).listFiles();
    	goldenResultFiles(files);
    }
    
    private void goldenResultFiles(File[] files) {
		for (File file : files) {		
			if (file.isDirectory()) {
				//Read content
				goldenResultFiles(file.listFiles());
			} else {
				String fileName = file.getName();
				String filePath = file.getAbsolutePath();
						        
		        String ext = Utils.extractFileExtension(fileName);
		        if(ext != null)
		        	ext = ext.toLowerCase();
		        if(ext.contains(Messages.CmsisZoneTest_FzoneFileType)) { 
		        	//Add file name and file path 
					fGoldenResults.put(fileName, filePath);
		        }				 					
			}
		}
	}
    
    
    private void processTestResultsContainer(){
    	String workSpace = getWorkspacePath();
    	String testResultData = Utils.addTrailingSlash(workSpace) + Utils.addTrailingSlash(fTestProject) + Utils.addTrailingSlash(fTestResultsFolder);
    	File[] files = new File(testResultData).listFiles();
    	testResultFiles(files);
    }
    
    private void testResultFiles(File[] files) {
		for (File file : files) {		
			if (file.isDirectory()) {
				//Read content
				testResultFiles(file.listFiles());
			} else {
				String fileName = file.getName();
				String filePath = file.getAbsolutePath();
				
				String ext = Utils.extractFileExtension(fileName);
		        if(ext != null)
		        	ext = ext.toLowerCase();
		        if(ext.contains(Messages.CmsisZoneTest_FzoneFileType)) {
		        	//Add file name and file path 
					fTestResults.put(fileName, filePath); 	
		        }							
			}
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
    
    
    private String validate(){
		String result = CmsisConstants.EMPTY_STRING;
		
	    //Validate maps data
        String validateMapsData =  validateMapsData();
        if(!validateMapsData.isEmpty())
            return validateMapsData;
        
		return result;		
	}
	
	private String validateMapsData(){
		String result = CmsisConstants.EMPTY_STRING;

		//Get golden results files (workspacePath/CmsisZoneTest/GoldenResults)
		Map<String, String> goldenResults = new HashMap<String, String>();
		goldenResults = getGoldenResults();
		
		//Get list of test results files (workspacePath/CmsisZoneTest/TestResults)
		Map<String, String> testResults = new HashMap<String, String>();
		testResults = getTestResults();
		
		if(goldenResults.isEmpty())
        	return Messages.CmsisZoneTest_NoGoldenResults;
        
        if(testResults.isEmpty())
        	return Messages.CmsisZoneTest_NoTestResults;
        
        if(goldenResults.size() < testResults.size())
        	return Messages.CmsisZoneTest_LessNumberOfGoldenResults;
        
        if(goldenResults.size() > testResults.size())
        	return Messages.CmsisZoneTest_BiggerNumberofGoldenResults;
		
		return result;	
	}
	
	
	 private Set<String> getNoProjectFiles(Set<String> plusFiles){
	    	Set<String>  noProjectFiles = new TreeSet<>(new AlnumComparator(false));
	    	for(String file: plusFiles){
	    		 String ext = Utils.extractFileExtension(file);
	    	        if(ext == null)
	    	            ext = CmsisConstants.EMPTY_STRING;
	    	        ext = ext.toLowerCase();
	    		
	    		if(!fProjectFiles.contains(ext)){
	    			noProjectFiles.add(file);
	    		}    		
	    	}
	    	return noProjectFiles;
	    }
    
    private Map<String, String> getGoldenResults() {
    	return fGoldenResults;
    }

    private Map<String, String> getTestResults() {
    	return fTestResults;
    }    
    
}
