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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.xml.sax.SAXException;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.enums.ESeverity;
import com.arm.cmsis.pack.parser.CpXmlParser;
import com.arm.cmsis.pack.parser.ICpXmlParser;
import com.arm.cmsis.pack.ui.console.RteErrorCollection;
import com.arm.cmsis.pack.utils.Utils;
import com.arm.cmsis.zone.data.ICpRootZone;
import com.arm.cmsis.zone.error.CmsisZoneError;
import com.arm.cmsis.zone.parser.CpZoneParser;
import com.arm.cmsis.zone.project.CmsisZoneProjectCreator;
import com.arm.cmsis.zone.project.CmsisZoneValidator;
import com.arm.cmsis.zone.project.ICmsisZoneValidator;
import com.arm.cmsis.zone.ui.Messages;

import freemarker.core.ParseException;
import freemarker.ext.dom.NodeModel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * Class to call FreeMarker Template processing 
 */
public class FmGenerator extends RteErrorCollection {

	private Configuration fCfg; // FreeMarker Configuration
	
	private NodeModel fDataModel = null; // data model for FreeMarker
	
	private IProgressMonitor fProgressMonitor = null;
	

	public FmGenerator() {
		fCfg = new Configuration(Configuration.VERSION_2_3_22);
		fCfg.setDefaultEncoding("UTF-8"); //$NON-NLS-1$
		fCfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		fCfg.setLogTemplateExceptions(false);
	}

	public FmGenerator(IProgressMonitor progressMonitor) {
		this();
		setProgressMonitor(progressMonitor);
	}

	
	public void setProgressMonitor(IProgressMonitor progressMonitor) {
		fProgressMonitor = progressMonitor;
	}
	
	public IProgressMonitor getProgressMonitor() {
		if(fProgressMonitor == null) {
			fProgressMonitor = new NullProgressMonitor();
		}
		return fProgressMonitor;
	}


	
	protected String createFZoneFile(ICpRootZone aZone) {
		if(aZone == null){
			return null;
		}
		
		File f = new File (Utils.changeFileExtension(aZone.getRootFileName(), CmsisConstants.FZONE));
		String fZoneFile = f.getAbsolutePath();
		getCmsisConsole().output(Messages.FmGenerator_GenFreemarketInputFile +  fZoneFile);
		
		ICpItem ftlModel  = aZone.toFtlModel(null);
		ICpXmlParser parser = new CpXmlParser();
		parser.setXsdFile("fzone.xsd"); //$NON-NLS-1$
		parser.writeToXmlFile(ftlModel, fZoneFile);
		if(parser.getSevereErrorCount() > 0) {
			getCmsisConsole().outputError(Messages.FmGenerator_ErrorCreatingFile + fZoneFile); 
			getCmsisConsole().outputErrors(parser.getErrors());
			return null;
		}
		return fZoneFile;
	}
	
	protected ICpRootZone loadZoneFile(String aZoneFile) {
		if(aZoneFile == null || aZoneFile.isEmpty())
			return null;
		
		getCmsisConsole().output(Messages.FmGenerator_LoadingZoneFile +  aZoneFile); 

		ICpXmlParser parser = new CpZoneParser();
		File file = new File(aZoneFile);
		ICpItem root = parser.parseFile(file.getAbsolutePath());
		if(root instanceof ICpRootZone) {
			return(ICpRootZone)root;
		}
		if(parser.getSevereErrorCount() > 0) {
			getCmsisConsole().outputError(Messages.FmGenerator_ErrorReadingFile + aZoneFile);
			getCmsisConsole().outputErrors(parser.getErrors());
			for(String err : parser.getErrorStrings()) {
				getCmsisConsole().output(err);
			}
		}
		return null;
	}

	
	public boolean processZoneFile(String aZoneFile, String templateFolder, String outputFolder){
		ICpRootZone aZone = loadZoneFile(aZoneFile);
		if(aZone == null) {
			return false;
		}
		aZone.init();
		// validate zone
		ICmsisZoneValidator validator = new CmsisZoneValidator();
		boolean bValid =  validator.validate(aZone);
		getCmsisConsole().outputErrors(validator.getErrors());
		if(!bValid) {
			return false;
		} 
		try {
			return processZone(aZone, templateFolder, outputFolder);
		} catch (CoreException e) {
			e.printStackTrace();
			CmsisZoneError err = new CmsisZoneError(e, ESeverity.Error, CmsisZoneError.Z501);
			err.setFile(aZoneFile);
			getCmsisConsole().outputError(err);
			addError(err);
			return false;
		} 
	}
	
	
	public boolean processZone(ICpRootZone aZone, String templateFolder, String outputFolder) throws CoreException {
		
		// generate sub-zone files
		CmsisZoneProjectCreator.createZoneFiles(aZone, getCmsisConsole(), getProgressMonitor());
		String fZoneFile = createFZoneFile(aZone);
		if(fZoneFile == null)
			return false;
		return processTemplates(fZoneFile, templateFolder, outputFolder); 
	}
	
	
	public boolean processTemplates(String fZoneFile, String templateFolder, String outputFolder) {
		String fZoneFilePath = Utils.extractPath(fZoneFile, true); 
		if(templateFolder == null) {
			templateFolder = CmsisConstants.FTL;
		}
		File tFolder = new File(templateFolder);
		if(!tFolder.isAbsolute()) {
			tFolder = new File(fZoneFilePath + templateFolder);
		}
		if(!tFolder.exists())
			tFolder.mkdirs();
		
		if(outputFolder == null) {
			outputFolder = CmsisConstants.FTL_GEN;
		} 
		File oFolder = new File(outputFolder);
		if(!oFolder.isAbsolute()) {
			oFolder = new File(fZoneFilePath + outputFolder);
		}

		if(!oFolder.exists())
			oFolder.mkdirs();
		
		getCmsisConsole().output(Messages.FmGenerator_ProcessingTemplates + tFolder.getAbsolutePath());
		
		/* Create a data-model */
		fDataModel = null;
		try {
			fDataModel = NodeModel.parse(new File(fZoneFile));
			fCfg.setDirectoryForTemplateLoading(tFolder);
		} catch (SAXException | IOException | ParserConfigurationException e) {			
			e.printStackTrace();
			CmsisZoneError err = new CmsisZoneError(e, ESeverity.Error, CmsisZoneError.Z601);
			err.setFile(fZoneFile);
			addError(err);
			getCmsisConsole().outputError(err);
			return false;
		}
		if(fDataModel == null || fDataModel.getNode() == null || fDataModel.getNode().getFirstChild() == null) {
			CmsisZoneError err = new CmsisZoneError(ESeverity.Error, CmsisZoneError.Z601);
			err.setFile(fZoneFile);
			addError(err);
			return false;
		}
		
		Collection<String> templateFiles = Utils.findFiles(tFolder, CmsisConstants.FTL, null, 0);
		if(templateFiles == null || templateFiles.isEmpty()) { 
			CmsisZoneError err = new CmsisZoneError(ESeverity.Warning, CmsisZoneError.Z604);
			err.setFile(tFolder.getAbsolutePath());
			getCmsisConsole().outputError(err);
			return true;
		}
		if(!tFolder.exists())
			tFolder.mkdirs();

		boolean success = true;
		for(String templateFile : templateFiles) {
			String template = Utils.extractFileName(templateFile); 
			String out = oFolder.getAbsolutePath() + '/' + Utils.removeFileExtension(template);
			try {
				doGenerate(template, out);
			} catch (TemplateException e) {
				e.printStackTrace();
				CmsisZoneError err = new CmsisZoneError(e, ESeverity.Error, CmsisZoneError.Z602);
				err.setFile(templateFile);
				err.setLine(e.getLineNumber());
				err.setColumn(e.getColumnNumber());
				addError(err);
				success = false;
			} catch (ParseException e) {
				CmsisZoneError err = new CmsisZoneError(e, ESeverity.Error, CmsisZoneError.Z602);
				err.setFile(templateFile);
				err.setLine(e.getLineNumber());
				err.setColumn(e.getColumnNumber());
				addError(err);
				success = false;
			} catch (IOException e) {
				e.printStackTrace();
				CmsisZoneError err = new CmsisZoneError(e, ESeverity.Error, CmsisZoneError.Z602);
				err.setFile(templateFile);
				addError(err);
				success = false;
			}
		}
		return success;
	}
	
	
	protected void doGenerate(String template, String output) throws IOException, TemplateException  {
		getCmsisConsole().output(CmsisConstants.SPACES8  + template + Messages.FmGenerator_Arrow+ output);
		Writer out = null;
		try {
			Template ftl = fCfg.getTemplate(template);
			out = new FileWriter(output);		
			ftl.process(fDataModel.getNode().getFirstChild(), out);
		} catch(TemplateException e) {
			throw e;
		} finally {
			if(out != null) {
				out.flush();
				out.close();
			}
		}
	}
	
}
