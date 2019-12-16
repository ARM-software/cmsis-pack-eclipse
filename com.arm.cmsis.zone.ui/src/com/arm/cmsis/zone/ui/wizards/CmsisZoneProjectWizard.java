/*******************************************************************************
* Copyright (c) 2017 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.zone.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackManager;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;
import com.arm.cmsis.pack.ui.wizards.RteDeviceSelectorPage;
import com.arm.cmsis.zone.project.CmsisZoneProjectCreator;
import com.arm.cmsis.zone.ui.Messages;

/**
 * 
 */
public class CmsisZoneProjectWizard extends Wizard implements INewWizard {

	CmsisZoneProjectMainPage mainPage;
	RteDeviceSelectorPage devicePage;
	IStructuredSelection fSelection = null;	
	CmsisZoneProjectRzoneFilePage resourceSourcePage;
	protected CmsisZoneProjectCreator cmsisZoneProjectCreator =  null;

	public CmsisZoneProjectWizard() {
		setWindowTitle(Messages.CmsisZoneProjectWizard_WindowTitle);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		fSelection  = selection;
		setNeedsProgressMonitor(true);
	}


	@Override
	public void addPages() {
		//Add main page
		mainPage = new CmsisZoneProjectMainPage(Messages.CmsisZoneProjectWizard_MainPageTitle);
		mainPage.setTitle(Messages.CmsisZoneProjectWizard_MainPageTitle);
		mainPage.setDescription(Messages.CmsisZoneProjectWizard_MainPageDescription);
		addPage(mainPage);
		
		//Add resource source page
		cmsisZoneProjectCreator = new CmsisZoneProjectCreator();
		resourceSourcePage = new CmsisZoneProjectRzoneFilePage(cmsisZoneProjectCreator, Messages.CmsisZoneProjectWizard_CmsisProject);
		resourceSourcePage.setTitle(Messages.CmsisZoneProjectWizard_MainPageTitle);
		resourceSourcePage.setDescription(Messages.CmsisZoneProjectWizard_ResourceFileSpecification);
		addPage(resourceSourcePage);
				
		//Add device page
		devicePage = new RteDeviceSelectorPage(false);
		ICpPackManager packManager = CpPlugIn.getPackManager();
		if (packManager == null) {
			devicePage.updateStatus(Messages.CmsisZoneProjectWizard_PackManagerAvailability);
			return;
		}
		IRteDeviceItem devices = packManager.getInstalledDevices();
		devicePage.setDevices(devices);
		addPage(devicePage);		
	}
	
	@Override
	public boolean performFinish() {		
		//Get info from CmsisZoneProjectMainPage
		IPath projectLocation = mainPage.getLocationPath();
		IProject project = mainPage.getProjectHandle();
		
		//Set wizard pages info to CmsisZoneProjectCreator class
		cmsisZoneProjectCreator.setProject(project);
		cmsisZoneProjectCreator.setProjectLocation(projectLocation);
		
		//Set device info depending on selected radio button (either 'Select device' or 'Use existing resource file')
		boolean bDeviceSelected = cmsisZoneProjectCreator.isDeviceSelected();
		if(bDeviceSelected) {
			//Get info from RteDeviceSelectorPage
			ICpDeviceInfo deviceInfo = devicePage.getDeviceInfo();
			cmsisZoneProjectCreator.setDeviceInfo(deviceInfo);
		}
		// the source rzone file name is set to cmsisZoneProjectCreator by the page  

		IRunnableWithProgress op = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(false, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), Messages.CmsisZoneProjectWizard_Error, realException.getMessage());
			return false;
		}	
		return true;
	}
	
	void doFinish(IProgressMonitor monitor) throws CoreException {
		
		//Create project		
		IFile aZoneFile = cmsisZoneProjectCreator.createProject(monitor);
		
		// Open .azone file in workspace
		if(aZoneFile != null) {
			IWorkbench wb = PlatformUI.getWorkbench();
			if(wb != null) {
				IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
				if(window != null) {
					IWorkbenchPage page = window.getActivePage();
					if(page != null) {
						try {
							IDE.openEditor(page, aZoneFile);
						} catch (PartInitException e) {						
							e.printStackTrace();
						}
					}
				}
			}			
		}		
	}
	
		
	@Override
	public boolean canFinish() {
		
		if(!mainPage.isPageComplete())
			return false;
		if(!resourceSourcePage.isPageComplete())
			return false;
		if(resourceSourcePage.isDeviceSelected()) {
			return devicePage.isPageComplete();
		}
		return true;
	}

	@Override
	public void dispose() {
		super.dispose();
	}
	
}
