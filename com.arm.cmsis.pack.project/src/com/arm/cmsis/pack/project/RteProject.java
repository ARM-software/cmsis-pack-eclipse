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

package com.arm.cmsis.pack.project;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import com.arm.cmsis.pack.build.settings.RteToolChainAdapterInfo;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.configuration.IRteConfiguration;
import com.arm.cmsis.pack.info.ICpFileInfo;

/**
 * Default implementation of ICRteProject interface
 */
public class RteProject implements IRteProject {

	private String fName = null;
	protected IRteConfiguration fRteConfiguration = null; 

	protected RteProjectStorage fRteProjectStorage = null;
	
	private boolean bUpdateCompleted = false; 
	
	/**
	 *  Constructs RteProject for given project
	 */
	public RteProject(IProject project) {
		setName(project.getName());
		fRteProjectStorage = new RteProjectStorage();
	}

	@Override
	public void destroy() {
		fRteConfiguration = null;
		fRteProjectStorage = null;
	}

	@Override
	synchronized public boolean isUpdateCompleted() {
		return bUpdateCompleted;
	}
	
	
	@Override
	synchronized public void setUpdateCompleted(boolean completed) {
		bUpdateCompleted = completed;
		
	}

	@Override
	public String getName() {
		return fName;
	}

	@Override
	public void setName(String name) {
		fName = name;
	}

	@Override
	public RteToolChainAdapterInfo getToolChainAdapterInfo() {
		return fRteProjectStorage.getToolChainAdapterInfo();
	}

	@Override
	public void setToolChainAdapterInfo(RteToolChainAdapterInfo toolChainAdapterInfo) {
		fRteProjectStorage.setToolChainAdapterInfo(toolChainAdapterInfo);
	}

	
	protected void outputMessage(final String message)
	{
		// TODO: implement
	}
	
	@Override
	public IProject getProject() {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(getName());
		return project;
	}

	@Override
	public IRteConfiguration getRteConfiguration() {
		return fRteConfiguration;
	}

	
	@Override
	public void setRteConfiguration(String rteConfigName, IRteConfiguration rteConf) {
		fRteConfiguration = rteConf;
		fRteProjectStorage.setRteConfigurationName(rteConfigName);
	}

	@Override
	public String getRteConfigurationName() {
		return fRteProjectStorage.getRteConfigurationName();
	}

	
	@Override
	public RteProjectStorage getProjectStorage() {
		return fRteProjectStorage;
	}
	
	
	@Override
	public void save() throws CoreException {
		processRteStorages(true);
	}

	@Override
	public void load() throws CoreException {
		setUpdateCompleted(false);
		processRteStorages(false);
		reload(); 
	}


	@Override
	public void update() {
		update(RteProjectUpdater.UPDATE_ALL);
	}

	@Override
	public void reload() {
		update( RteProjectUpdater.LOAD_CONFIGS | RteProjectUpdater.UPDATE_ALL);
	}

	protected void update(int updateFlags) {
		setUpdateCompleted(false);
		RteProjectUpdater updater = new RteProjectUpdater(this, updateFlags);
		updater.schedule();
	}
	
	
	protected void processRteStorages(boolean save) throws CoreException {
		IProject project = getProject();
		CoreModel model = CoreModel.getDefault();
		ICProjectDescription projDes = model.getProjectDescription(project);
		if(save) {
			saveRteStorage(projDes);
			model.setProjectDescription(project, projDes);
		}else {
			loadRteStorage(projDes);
		}
	}


	protected void saveRteStorage(ICProjectDescription projDes) throws CoreException {
		if(fRteProjectStorage != null) {
			fRteProjectStorage.save(projDes);
		}
	}

	protected void loadRteStorage(ICProjectDescription projDes) throws CoreException {
		if(fRteProjectStorage == null)
			fRteProjectStorage = new RteProjectStorage();
		fRteProjectStorage.load(projDes);
	}


	@Override
	public boolean isFileUsed(String fileName) {
		if(fileName == null || fileName.isEmpty())
			return false;
		if(fileName.equals(CmsisConstants.RTE_RTE_Components_h))
			return true;
		if(fRteConfiguration != null)
			return fRteConfiguration.getProjectFileInfo(fileName) != null;
		return false;
	}

	@Override
	public ICpFileInfo getProjectFileInfo(String fileName) {
		if(fileName == null || fileName.isEmpty())
			return null;
		if(fRteConfiguration != null)
			return fRteConfiguration.getProjectFileInfo(fileName);
		return null;
	}

}
