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
 * Basic class for the pack installer's job
 */
public abstract class CpPackJob extends Job {

	protected ICpPackInstaller fPackInstaller;
	protected RtePackJobResult fResult;

	/**
	 * @param name The job's name
	 * @param packInstaller the Pack installer
	 * @param packId the pack's id
	 */
	public CpPackJob(String name, ICpPackInstaller packInstaller, String packId) {
		super(name);
		fPackInstaller = packInstaller;
		fResult = new RtePackJobResult(packId);
	}

	@Override
	public boolean belongsTo(Object family) {
		return getName().equals(family);
	}

}
