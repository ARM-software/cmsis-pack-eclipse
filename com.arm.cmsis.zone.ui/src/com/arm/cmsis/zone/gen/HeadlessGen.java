/*******************************************************************************
* Copyright (c) 2019 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.zone.gen;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.parser.ICpXmlParser;
import com.arm.cmsis.pack.ui.console.RteConsole;
import com.arm.cmsis.pack.utils.Utils;
import com.arm.cmsis.zone.data.ICpRootZone;
import com.arm.cmsis.zone.parser.CpZoneParser;
import com.arm.cmsis.zone.ui.Messages;

/**
 * Headless application to process *.azone files, generate corresponding *.fzone file and process templates   
 */
public class HeadlessGen implements IApplication {

	public static final String APP_ID  = "com.arm.cmsis.zone.ui.headlessgen";    //$NON-NLS-1$	
	public static final Integer EXIT_ERROR = new Integer(4);   // important work is not done, processing is aborted 
	public static final Integer EXIT_WARNING = new Integer(2); // work is only partially done, some of the tasks did not succeed  
	public static final Integer EXIT_INFO = new Integer(1); 
	public static final String helpArg  = "-help"; //$NON-NLS-1$;
	
	protected List<String> fInputFiles = new ArrayList<>();
	protected String templateFolder = null;
	protected String outputFolder = null;
	private FmGenerator fGenerator;
	RteConsole fConsole = null;
	
	
	@Override
	public Object start(IApplicationContext context) throws Exception {
		
		String timestamp =  Utils.getCurrentTimeStamp();
		String msg = timestamp + Messages.HeadlessGen_CmsisZoneTemplateProcessor;
		fConsole = RteConsole.openConsole();
		fConsole.outputInfo(msg);
		
		// Handle user provided arguments
		Integer result = getArguments((String[])context.getArguments().get(IApplicationContext.APPLICATION_ARGS)); 
		
		if (result != EXIT_OK)
			return result;
		
		fGenerator = new FmGenerator();
		fGenerator.setCmsisConsole(fConsole);

		for(String aZone : fInputFiles) {
			msg = Messages.HeadlessGen_GeneratingFiles + aZone + Messages.HeadlessGen_Ellipsis; 
			if(!fGenerator.processZoneFile(aZone, templateFolder, outputFolder))
				result = EXIT_ERROR;
		}
		if(result == EXIT_OK)
			fConsole.outputInfo(Messages.HeadlessGen_GenerationCompleted);
		else 
			fConsole.outputError(Messages.HeadlessGen_GenerationFailed);
		fConsole.output(CmsisConstants.EMPTY_STRING);
		
		return result;
	}


	public Integer getArguments(String[] args) {
		try{
			for (int i = 0; i < args.length; i++) {
				String a = args[i];
				if(helpArg.equals(a)){
					help();
					return EXIT_INFO;
				}
				if(i + 1 >= args.length) {
					printUsage(args, null);
					return EXIT_ERROR;
				}
				if(Messages.HeadlessGen_CommandAzone.equals(a)){ 
					i++;
					fInputFiles.add(args[i]);
					continue;
				}

				if(Messages.HeadlessGen_CommandFtl.equals(a)){ 
					i++;
					templateFolder = args[i];
					continue; 
				}
				if(Messages.HeadlessGen_CommandFtlGen.equals(a)){
					i++;
					outputFolder = args[i];
					continue; 
				}
				printUsage(args, a);
				return EXIT_ERROR;
				
			}
		} catch(Exception e) {
			printUsage(args, null);			
			return EXIT_ERROR;
		}

		if(fInputFiles.isEmpty()) {
			printUsage(args, null);			
			return EXIT_ERROR;
		}

		return EXIT_OK;
	}
		
	protected ICpRootZone loadZoneFile(String file) {
		if(file == null)
			return null;
		ICpXmlParser parser = new CpZoneParser();
		ICpItem root = parser.parseFile(file);
		if(root instanceof ICpRootZone) {
			return(ICpRootZone)root;
		}
		return null;
	}
	
	
			
	@Override
	public void stop() {
		// does nothing
	}

	protected void printUsage(String[] args, String a) {
		if (args == null || args.length == 0){
			fConsole.outputError(Messages.HeadlessGen_NoArgumentsSpecified);
		} else if( a != null ) {
			fConsole.outputError(Messages.HeadlessGen_Unknowargument + a);
		} else {
			fConsole.outputError(Messages.HeadlessGen_InvalidCommandLine);
		}
		help();
	}
	 /**
     * Prints commands usage
     */
    protected void help(){
        // Print usage
        fConsole.output(Messages.HeadlessGen_Usage);
        fConsole.output(Messages.HeadlessGen_UsageAzoneCommand);
        fConsole.output(Messages.HeadlessGen_UsageFtlCommand) ;        
        fConsole.output(Messages.HeadlessGen_UsageFtlGenCommand);        
        fConsole.output(Messages.HeadlessGen_UsageHelpCommand);
    }
	
}
