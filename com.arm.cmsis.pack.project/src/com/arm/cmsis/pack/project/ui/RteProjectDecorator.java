/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.arm.cmsis.pack.project.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.configuration.IRteConfiguration;
import com.arm.cmsis.pack.info.ICpComponentInfo;
import com.arm.cmsis.pack.info.ICpFileInfo;
import com.arm.cmsis.pack.project.CpProjectPlugIn;
import com.arm.cmsis.pack.project.IRteProject;
import com.arm.cmsis.pack.project.RteProjectManager;
import com.arm.cmsis.pack.project.utils.ProjectUtils;
import com.arm.cmsis.pack.ui.CpPlugInUI;

/**
 * Class to decorate RTE items in Project explorer for RTE projects 
 * 
 * @see ILightweightLabelDecorator
 */
public class RteProjectDecorator implements ILightweightLabelDecorator {

	static public final String ID = "com.arm.cmsis.pack.project.decorators.RteProjectDecorator"; //$NON-NLS-1$
	@Override
	public void decorate(Object element, IDecoration decoration) {
		IResource resource = ProjectUtils.getRteResource(element);
		if(resource == null)
			return;
		int type = resource.getType();
		if(type != IResource.FOLDER && type != IResource.FILE)
			return;
		
		IPath path = resource.getProjectRelativePath();
		IProject project = resource.getProject();
		RteProjectManager rteProjectManager = CpProjectPlugIn.getRteProjectManager();
		IRteProject rteProject = rteProjectManager.getRteProject(project);
		
		if(type == IResource.FOLDER || resource.getFileExtension().equals(CmsisConstants.RTECONFIG)) { 
			if(path.segmentCount() == 1) { // RTE folder itself 
				IRteConfiguration rteConf = rteProject != null ? rteProject.getRteConfiguration() : null;
				if(rteConf == null  || !rteConf.isValid() )
					addOverlay(decoration, CpPlugInUI.ICON_RTE_ERROR_OVR);
				else if(type == IResource.FOLDER)
					addOverlay(decoration, CpPlugInUI.ICON_RTE_OVR);
			}
		}
		
		if(rteProject != null && type == IResource.FILE) {
			ICpFileInfo fi = rteProject.getProjectFileInfo(path.toString());
			if(fi != null) {
				ICpComponentInfo ci = fi.getComponentInfo();
				String suffix = " [" + ci.getName() + "]";  //$NON-NLS-1$//$NON-NLS-2$
				decoration.addSuffix(suffix);
				if(ci.getComponent() == null)
					addOverlay(decoration, CpPlugInUI.ICON_RTE_ERROR_OVR);	
				return;
			}
		} 
	}

	private void addOverlay(IDecoration decoration, String iconFile) {
		ImageDescriptor descriptor = CpPlugInUI.getImageDescriptor(iconFile); 
		if (descriptor == null)
			return;
		decoration.addOverlay(descriptor, IDecoration.TOP_LEFT);
	}
	
	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}
	
	/**
	 *  Refreshes decoration of all RTE resources 
	 */
	static public void refresh() {
		// Decorate using current UI thread
		Display.getDefault().asyncExec(new Runnable()
		  {
		    public void run()
		    {
		  	  IDecoratorManager decoratorManager = PlatformUI.getWorkbench().getDecoratorManager();
		  	  if(decoratorManager != null)
		  		decoratorManager.update(ID);
		    }
		  });
	}
}