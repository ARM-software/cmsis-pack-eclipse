/*******************************************************************************
* Copyright (c) 2016 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.installer.jobs;

import org.eclipse.core.runtime.jobs.Job;

import com.arm.cmsis.pack.ICpPackInstaller;
import com.arm.cmsis.pack.events.RtePackJobResult;

/**
 * Basic class for the pack manager's job
 */
public abstract class CpPackJob extends Job {

	protected String fJobId;
	protected ICpPackInstaller fPackInstaller;
	protected RtePackJobResult fResult;

	/**
	 * @param name The job's name
	 * @param packInstaller the Pack installer
	 * @param jobId the job's id
	 */
	public CpPackJob(String name, ICpPackInstaller packInstaller, String jobId) {
		super(name);
		fJobId = jobId;
		fPackInstaller = packInstaller;
		fResult = new RtePackJobResult(jobId);
	}

	/**
	 * Return true if this job requires install the required packs
	 * @return True if this job requires install the required packs
	 */
	public boolean installRequiredPacks() {
		return false;
	}

	@Override
	public boolean belongsTo(Object family) {
		return getName().equals(family);
	}

}
