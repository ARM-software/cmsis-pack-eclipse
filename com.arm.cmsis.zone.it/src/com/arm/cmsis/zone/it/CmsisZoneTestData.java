package com.arm.cmsis.zone.it;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.project.utils.CDTUtils;
import com.arm.cmsis.pack.project.utils.ProjectUtils;
import com.arm.cmsis.pack.utils.Utils;


public class CmsisZoneTestData {
	
	private IProject project = null;
	private String fTestProject = "CmsisZoneTest"; //$NON-NLS-1$
	private String fTestResultsFolder = "TestResults";	//$NON-NLS-1$
	private String fGoldenResultsFolder = "GoldenResults"; //$NON-NLS-1$
	private String fTestInputDataFolder = "TestInputData"; //$NON-NLS-1$
	private List<String> fInputCommandLine = new ArrayList<>();	//list of .azone files with -azone command	
	private List<String> fAzoneFiles = new ArrayList<>();	
	
	
	/**
	 * Constructor
	 */
	public CmsisZoneTestData(){

	}
	
	/**
	 * Creates 'CmsisZoneTest' project in the workspace
	 * @return
	 */
	public IProject createProject(){    	    	
		project = null; 
		
		String destinationProjectPath = Utils.addTrailingSlash(getWorkspacePath()) + fTestProject;
	
		//Set destination URI
		File file = new File(destinationProjectPath);
		URI destinationURI = file.toURI();    	

		IProgressMonitor monitor= new NullProgressMonitor();

		//Create project in workspace
		try {
			project = CDTUtils.createProject(fTestProject, destinationURI, monitor);
		} catch (OperationCanceledException e) {			
			e.printStackTrace();
		} catch (CoreException e) {			
			e.printStackTrace();
		}
		return project;    	
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
	

	/**
	 * Calls 'createFolderStructure(File source, String target, IProject project, int i)' method to create folder structure of 'CmsisZoneTest' project
	 * @param project
	 */
	public void createTestFolderStructure(IProject project){    	

		//Get resources directory of this test plugin
		File testPlugin = new File(Messages.CmsisZoneTestData_PluginXML);		
		String pluginPath = testPlugin.getAbsolutePath();
		IPath resourcesPath = new Path(pluginPath);
		resourcesPath = resourcesPath.removeLastSegments(1);
		String srcPath = resourcesPath.toString();

		//Create folder' structure
		File sourcePath = new File(srcPath);
		createFolderStructure(sourcePath, CmsisConstants.EMPTY_STRING,project);
	}
	    

	/**
	 * Extracts structure of 'TestInputData' and 'GoldenData' directories from the 'com.arm.cmsis.zone.it' plug-in to be created in 'CmsisZoneTest' project 
	 * @param source
	 * @param target
	 * @param project
	 * @param i
	 */
	public void createFolderStructure(File source, String target, IProject project){  
		//Check if source is a directory
		if (source.isDirectory()) {            	
			String[] children = source.list();
			if(children == null || children.length == 0) {
				//return; // do not copy empty directories;
			}   
			for (String child : children) { 			
				//Creates file with its path from directory
				File sourceFile = new File(source, child);

				//Creates target file name
				String targetFile = target + File.separator + child;

				//Call to this method itself to copy file                    
				createFolderStructure(sourceFile, targetFile, project);
			}
			return;
		}
		// source is a regular file

		//Get source file with its path to be copied
		String sourceFileName = source.toString();

		//Get file name to be copied
		String targetFileName = target.substring(1);        

		//Copy file to the project folder created in eclipse workspace.
		try {
			if(sourceFileName.contains(fGoldenResultsFolder) || sourceFileName.contains(fTestInputDataFolder)){            	
				ProjectUtils.copyFile(project, sourceFileName, targetFileName, -1, new NullProgressMonitor(), true);
			}

		} catch (CoreException e) {		
			e.printStackTrace();
		}
	}
	
	
	public void copyFolderContent(File source, String target, IProject project) throws IOException{  		
		/*** Check if source is a directory ***/
		if (source.isDirectory()) {            	
			String[] children = source.list();   
			for (String child : children) { 			
				//Creates file with its path from directory
				File sourceFile = new File(source, child);

				//Creates target file name
				String targetFile = target + File.separator + child;

				//Call to this method itself to copy file                    
				copyFolderContent(sourceFile, targetFile, project);
			}
			return;
		}
		/*** source is a regular file ***/

		//Get source file with its path to be copied
		String sourceFileName = source.toString();

		//Copy file to the project folder created in eclipse workspace.
		try {
			ProjectUtils.copyFile(project, sourceFileName, target, -1, new NullProgressMonitor(), true);
		} catch (CoreException e) {		
			e.printStackTrace();
		}		
	}
	
	
	
	
	/**
	 * Creates command line for the CMSIS-Zone headless mode
	 * @param project: created 'CmsisZoneTest' in workspace
	 */
	public void createInputCommandLine(IProject project){
		List<String> aZoneFiles = new ArrayList<>();
		List<String> inputCommandLine = new ArrayList<>();

		//Collect azone files
		aZoneFiles = collectAzoneFiles(project);
		if(!aZoneFiles.isEmpty()){
			for (int i = 0; i < aZoneFiles.size(); i++) {
				String cmsisCommand = Messages.CmsisZoneTestData_AzoneCommand; 			
				inputCommandLine.add(cmsisCommand);
				String aZoneFile = aZoneFiles.get(i);
				inputCommandLine.add(aZoneFile);				
			}
		}		
		if(!inputCommandLine.isEmpty()){
			//Save list
			setInputCommandLine(inputCommandLine);
		}		
	}

	/**
	 * Collect list of azone files
	 * @param project 'CmsisZoneTest'
	 * @return
	 */
	public List<String> collectAzoneFiles(IProject project){
		List<String> aZoneFiles = new ArrayList<>();
		if(project!=null){
			try {
				processCmsisTestContainer(project);
				aZoneFiles = fAzoneFiles;
				setAzoneFilesList(aZoneFiles);
			} catch (CoreException e) {			
				e.printStackTrace();
			}
		}		
		return aZoneFiles;
	}
	
	
	/**
	 * Gets azone list files from 'CmsisZoneTest' project
	 * @param container 'CmsisZoneTest' project
	 * @throws CoreException
	 */
	private void processCmsisTestContainer(IContainer container) throws CoreException {
		IResource [] members = container.members();
		String fileName = CmsisConstants.EMPTY_STRING;	      

		for (IResource member : members){	    	   
			if (member instanceof IContainer){        	  

				//Get container's parent
				IContainer parentContainer = member.getParent();

				//Get full path of container's parent
				IPath pathContainer = parentContainer.getFullPath();
				String pathContainerName = pathContainer.toString();

				//Create container path
			    String fparentPath = pathContainerName.replace("/", File.separator) + File.separator + member.getName() + File.separator;  //$NON-NLS-1$
								
				if(fparentPath.contains(fTestResultsFolder)){
					//Read content
					processCmsisTestContainer((IContainer)member);		            
				} 
			} else if (member instanceof IFile){	        	   
				//Save file
				fileName =  member.getName();

				//Extract file extension
				String ext = Utils.extractFileExtension(fileName);
				if(ext == null)
					ext = CmsisConstants.EMPTY_STRING;
				ext = ext.toLowerCase();

				if(ext.equals(Messages.CmsisZoneTestData_AzoneFileType)){//Save file name if it has CMSIS-Zone project extension 'azone'		            	  
					IPath memberFullPath = member.getFullPath();
					String fullPath =  memberFullPath.toString();	            	            	  
					String destinationProjectPath = getWorkspacePath();	
					fileName = destinationProjectPath + fullPath;
					//Add file to the 'azone' files list	          
					fAzoneFiles.add(fileName);
				}
			}
		} 
	}

	/**
	 * Creates 'TestResults' folder
	 * @param project	
	 */
	public void createTestResultsFolder(IProject project) {		

		//Get workspace path
		String wsPath = getWorkspacePath();
		//Get project path
		IPath projectPath = project.getFullPath();
		String projectPathName = projectPath.toString();         
		projectPathName = Utils.addTrailingSlash(projectPathName);
		//Full test folder path	
		String testResultsFolderpath = wsPath + projectPathName + fTestResultsFolder;
	
		//Create 'TestResults' folder         
		File testResultsFolder = new File(testResultsFolderpath);        
		boolean result =  testResultsFolder.mkdir();		
				
		//Copy 'TestInputData' content to 'TestResults' directory
		if(result) { 				
			String testInputFolderpath = wsPath + projectPathName + fTestInputDataFolder;
			File testInputFolder = new File(testInputFolderpath);
			try {
				copyFolderContent(testInputFolder,fTestResultsFolder, project);
			} catch (IOException e) {				
				e.printStackTrace();
			}
		}
		
	}
	

	/***getters***/  

	public List<String> geInputCommandLine() {
		return fInputCommandLine;
	}

	/***setters***/
	private void setInputCommandLine(List<String> inputCommandLine) {
		this.fInputCommandLine = inputCommandLine;
	}

	private void setAzoneFilesList(List<String> azoneFilesList) {
		this.fAzoneFiles = azoneFilesList;
	}
    
}
