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

package com.arm.cmsis.pack.project.template;

import java.io.File;

import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.arm.cmsis.pack.build.settings.RteToolChainAdapterFactory;
import com.arm.cmsis.pack.build.settings.RteToolChainAdapterInfo;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.configuration.IRteConfiguration;
import com.arm.cmsis.pack.configuration.RteConfiguration;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.info.CpConfigurationInfo;
import com.arm.cmsis.pack.info.ICpConfigurationInfo;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.parser.ConfigParser;
import com.arm.cmsis.pack.project.CpProjectPlugIn;
import com.arm.cmsis.pack.project.IRteProject;
import com.arm.cmsis.pack.project.Messages;
import com.arm.cmsis.pack.project.RteProjectManager;
import com.arm.cmsis.pack.project.utils.ProjectUtils;

/**
 * Process runner that creates new RTE Project with default RTE configuration  
 */
public class CreateRteProject extends ProcessRunner {
	
	@Override
	public void process(TemplateCore template, ProcessArgument[] args, String processId, IProgressMonitor monitor) throws ProcessFailureException {
		String projectName 	= args[0].getSimpleValue();
		String compiler 	= args[1].getSimpleValue();
		String output	 	= args[2].getSimpleValue();
		String adapterId 	= args[3].getSimpleValue();
		
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if(project == null) {
			String msg = Messages.CreateRteProject_ErrorCreatingRteProject;
			msg += 	Messages.CreateRteProject_EclipseProjectNotExists;
			msg += projectName;
			throw new ProcessFailureException(getProcessMessage(processId, IStatus.ERROR, msg));
		}
		
		RteToolChainAdapterInfo adapterInfo = createToolChainAdapter(adapterId);
		if(adapterInfo == null){
			String msg = Messages.CreateRteProject_ErrorCreatingRteProject;
			msg += 	Messages.CreateRteProject_ToolchainAdapterNotFound;
			msg += adapterId;
			throw new ProcessFailureException(getProcessMessage(processId, IStatus.ERROR, msg));
		}
		String rteConfigName = projectName + CmsisConstants.DOT_RTECONFIG;
		IRteConfiguration rteConf = createRteConfiguration(compiler, output);
		try {
			IFile iFile = ProjectUtils.createFile(project, rteConfigName, monitor);
			iFile.refreshLocal(IResource.DEPTH_ONE, null);
			project.refreshLocal(IResource.DEPTH_INFINITE, null);

			ConfigParser confParser = new ConfigParser(); 
			IPath location = iFile.getLocation();
			if(location!= null) {
				File file =  location.toFile();
				confParser.writeToXmlFile(rteConf.getConfigurationInfo(), file.getAbsolutePath());
			}
			// open Rte configuration file 
			IWorkbench wb = PlatformUI.getWorkbench();		
			if(wb != null) {
				IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
				if(window != null) {
					IWorkbenchPage page = window.getActivePage();
					if(page != null) {
						try {
							IDE.openEditor(page, iFile);
						} catch (PartInitException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		} catch (CoreException e) {
			String msg = Messages.CreateRteProject_ErrorCreatingConfigFile;
			msg += 	e.getMessage();
			throw new ProcessFailureException(getProcessMessage(processId, IStatus.ERROR, msg), e); 
		}

		IRteProject rteProject = createRteProject(project, adapterInfo); // never fails
		rteProject.setRteConfiguration(rteConfigName, rteConf);
		rteProject.update();
	}

	protected IRteProject createRteProject(IProject project, RteToolChainAdapterInfo adapterInfo) {
		RteProjectManager rteProjectManager = CpProjectPlugIn.getRteProjectManager();
		IRteProject rteProject = rteProjectManager.createRteProject(project);
		rteProject.setToolChainAdapterInfo(adapterInfo);
		return rteProject;

	}

	protected RteToolChainAdapterInfo createToolChainAdapter(String adapterId) {
		RteToolChainAdapterFactory adapterFactory = RteToolChainAdapterFactory.getInstance(); 
		return adapterFactory.getAdapterInfo(adapterId);
	}

	protected IRteConfiguration createRteConfiguration(String compiler, String output) {
		
		ICpDeviceInfo deviceInfo = RteProjectTemplate.getSelectedDeviceInfo();
		ICpItem toolchainInfo = RteProjectTemplate.createToolChainInfo(compiler, output);
		ICpConfigurationInfo cpInfo = new CpConfigurationInfo(deviceInfo, toolchainInfo);

		IRteConfiguration rteConf = new RteConfiguration();
		rteConf.setConfigurationInfo(cpInfo);
		
		return rteConf;
	}

}
