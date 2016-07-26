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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationDataProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;

/**
 * The purpose of the class is to hook in project load process and get notified when .cproject is loaded.<br> 
 * That allows to adjust the project configurations to RTE configurations after load.<br>
 * <p>
 * This class is contributed to org.eclipse.cdt.core.CConfigurationDataProvider extension point.<br>
 * Therefore it gets constructed at the beginning of CCorePlugin initialization.<br>
 * We need to wait until ICProjectDescriptionManager and IIndexManager are become available in CCorePlugin.<br>
 * That is done by waiting for IIndexManager startup job is scheduled. Then we can install ICProjectDescriptionListener    
 * <p>  
 * CDT does not provide standard methods to register ICProjectDescriptionListener early enough (e.g. when Eclipse is just started) <br> 
 * It could be only done when CCorePlugin is fully initialized, but at this moment the projects are already loaded and indexed.<br> 
 * Thus this workaround is needed.<br>
 * <p> 
 * The CConfigurationDataProvider methods overridden by this class intentionally do nothing because they never get called.<br> 
 */
public class RteSetupHook extends CConfigurationDataProvider{

	/**
	 * Default constructor gets called at earlier stages of CCorePlugin initialization 
	 */
	public RteSetupHook() {

		// check if IIndexManager CCorePlugin is already initialized (very unlikely)
		IIndexManager indexManager = CCorePlugin.getIndexManager();
		if(indexManager != null) {
			initRteSetupParticipant(); 
		} else {
			// wait for IIndexManager initialization (get notified when the startup job is scheduled )
			IJobChangeListener jobListener= new IJobChangeListener() {
				@Override
				public void scheduled(IJobChangeEvent event) {
					Job job = event.getJob(); 
					// here we hope that start job never gets renamed...
					String name = CCorePlugin.getResourceString("CCorePlugin.startupJob");  //$NON-NLS-1$
					if(job != null && job.getName().equals(name)) {
						initRteSetupParticipant();

						IJobManager jobMan = Job.getJobManager();
						jobMan.removeJobChangeListener(this); // we do not need the listener any more
					}
				}
				// we are not interested in other events
				@Override
				public void aboutToRun(IJobChangeEvent event) { /*does nothing */ }
				@Override
				public void sleeping(IJobChangeEvent event) { /*does nothing */ }
				@Override
				public void running(IJobChangeEvent event) { /*does nothing */ }
				@Override
				public void done(IJobChangeEvent event) { /*does nothing */ }
				@Override
				public void awake(IJobChangeEvent event) { /*does nothing */ }
			};
			IJobManager jobMan = Job.getJobManager();
			jobMan.addJobChangeListener(jobListener);
		}
	}

	protected void initRteSetupParticipant() {
		CpProjectPlugIn.getRteProjectManager().initRteSetupParticipant();
	}

	@Override
	public CConfigurationData loadConfiguration(
			ICConfigurationDescription cfgDescription, IProgressMonitor monitor)
			throws CoreException {
		// does nothing because is not called
		return null;
	}

	@Override
	public CConfigurationData createConfiguration(
			ICConfigurationDescription cfgDescription,
			ICConfigurationDescription baseCfgDescription,
			CConfigurationData baseData, boolean clone, IProgressMonitor monitor)
			throws CoreException {
		// does nothing because is not called
		return null;
	}

	@Override
	public void removeConfiguration(ICConfigurationDescription cfgDescription,
			CConfigurationData data, IProgressMonitor monitor) {
		// does nothing because is not called 
	}

}
