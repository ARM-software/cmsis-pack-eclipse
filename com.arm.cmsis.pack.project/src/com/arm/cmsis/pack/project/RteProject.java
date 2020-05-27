/*******************************************************************************
 * Copyright (c) 2015-2020 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/

package com.arm.cmsis.pack.project;

import java.util.Collection;
import java.util.Map;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;

import com.arm.cmsis.pack.build.settings.RteToolChainAdapterInfo;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.configuration.IRteConfiguration;
import com.arm.cmsis.pack.info.ICpFileInfo;

/**
 * Default implementation of IRteProject interface
 */
public class RteProject extends PlatformObject implements IRteProject {

	private String fName = null;
	protected IRteConfiguration fRteConfiguration = null;
	protected RteProjectStorage fRteProjectStorage = null;
	private boolean bUpdateCompleted = false;
	private boolean fbInstallMissingPacksOnUpdate = false;

	/**
	 * Constructs RteProject for given project
	 */
	public RteProject(IProject project) {
		setName(project.getName());
		fRteProjectStorage = new RteProjectStorage();
		fbInstallMissingPacksOnUpdate = true; // set initial value to true
	}

	@Override
	public void destroy() {
		fRteConfiguration = null;
		fRteProjectStorage = null;
	}

	@Override
	public synchronized boolean isUpdateCompleted() {
		return bUpdateCompleted;
	}

	@Override
	public synchronized void setUpdateCompleted(boolean completed) {
		bUpdateCompleted = completed;
	}


	@Override
	public boolean isInstallMissingPacksOnUpdate() {
		return fbInstallMissingPacksOnUpdate;
	}

	@Override
	public void setInstallMissingPacksOnUpdate(boolean bInstall) {
		fbInstallMissingPacksOnUpdate = bInstall;
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

	@Override
	public IProject getProject() {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(getName());
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
	public void setRteConfigurationName(String rteConfigName) {
		fRteProjectStorage.setRteConfigurationName(rteConfigName);
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
		update(RteProjectUpdater.LOAD_CONFIGS|RteProjectUpdater.CAUSE_PROJECT_LOAD);
	}

	@Override
	public void init() {
		update(RteProjectUpdater.CAUSE_PROJECT_CREATED);
	}

	@Override
	public void reload() {
		update(RteProjectUpdater.LOAD_CONFIGS | RteProjectUpdater.UPDATE_TOOLCHAIN | RteProjectUpdater.CAUSE_PROJECT_RESET);
	}

	@Override
	public void refresh() {
		update(RteProjectUpdater.LOAD_CONFIGS);
	}

	@Override
	public void refresh(int cause) {
		update(RteProjectUpdater.LOAD_CONFIGS | cause);
	}
	
	@Override
	public void cleanup() {
		update(RteProjectUpdater.LOAD_CONFIGS | RteProjectUpdater.CLEANUP_RTE_FILES);
	}

	protected void update(int updateFlags) {
		CpProjectPlugIn.getRteProjectManager().updateProject(this, updateFlags);
	}

	protected void processRteStorages(boolean save) throws CoreException {
		IProject project = getProject();
		CoreModel model = CoreModel.getDefault();
		ICProjectDescription projDes = model.getProjectDescription(project);
		if (projDes == null) {
			// not a MBS project
			return;
		}
		if (save) {
			saveRteStorage(projDes);
			model.setProjectDescription(project, projDes);
		} else {
			loadRteStorage(projDes);
		}
	}

	protected void saveRteStorage(ICProjectDescription projDes) throws CoreException {
		if (fRteProjectStorage != null) {
			fRteProjectStorage.save(projDes);
		}
	}

	protected void loadRteStorage(ICProjectDescription projDes) throws CoreException {
		if (fRteProjectStorage == null) {
			fRteProjectStorage = new RteProjectStorage();
		}
		fRteProjectStorage.load(projDes);
	}

	@Override
	public boolean isFileUsed(String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			return false;
		}
		if (fileName.equals(CmsisConstants.RTE_RTE_Components_h)) {
			return true;
		}
		if (fRteConfiguration == null) {
			return false;
		}

		if (fileName.equals(CmsisConstants.RTE_Pre_Include_Global_h)) {
			Collection<String> globals = fRteConfiguration.getGlobalPreIncludeStrings();
			return globals != null && !globals.isEmpty();
		}

		if (fileName.startsWith(CmsisConstants.RTE_Pre_Include_)) {
			Map<String, String> locals = fRteConfiguration.getLocalPreIncludeStrings();
			if(locals == null || locals.isEmpty())
				return false;
			return locals.containsKey(fileName.substring(CmsisConstants.RTEDIR.length()));
		}

		return fRteConfiguration.getProjectFileInfo(fileName) != null;
	}

	@Override
	public ICpFileInfo getProjectFileInfo(String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			return null;
		}
		if (fRteConfiguration != null) {
			return fRteConfiguration.getProjectFileInfo(fileName);
		}
		return null;
	}

	@Override
	public ICpFileInfo[] getProjectFileInfos(String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			return null;
		}
		if (fRteConfiguration != null) {
			return fRteConfiguration.getProjectFileInfos(fileName);
		}
		return null;
	}

}
