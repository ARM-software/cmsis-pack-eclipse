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

package com.arm.cmsis.pack.events;

import com.arm.cmsis.pack.data.ICpPack;

/**
 * The result of a pack job, e.g. install, unpack, remove...
 */
public class RtePackJobResult {

	private String packId;
	private ICpPack pack;
	private ICpPack newPack;
	private boolean success;
	private String errorString;
	private String sourceFilePath;
	
	/**
	 * Constructor with the processing pack's id
	 * @param packId
	 */
	public RtePackJobResult(String packId) {
		this.packId = packId;
	}
	
	/**
	 * @return the packId
	 */
	public String getPackId() {
		return packId;
	}

	/**
	 * @return the pack
	 */
	public ICpPack getPack() {
		return pack;
	}

	/**
	 * @param pack the pack to set
	 */
	public void setPack(ICpPack pack) {
		this.pack = pack;
	}

	/**
	 * @return the newPack
	 */
	public ICpPack getNewPack() {
		return newPack;
	}

	/**
	 * @param newPack the newPack to set
	 */
	public void setNewPack(ICpPack newPack) {
		this.newPack = newPack;
	}

	/**
	 * @return the success
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * @param success the success to set
	 */
	public void setSuccess(boolean success) {
		this.success = success;
	}

	/**
	 * @return the sourceFilePath
	 */
	public String getSourceFilePath() {
		return sourceFilePath;
	}
	
	/**
	 * @param sourceFilePath the source file path
	 */
	public void setSourceFilePath(String sourceFilePath) {
		this.sourceFilePath = sourceFilePath;
	}

	/**
	 * @return the errorString
	 */
	public String getErrorString() {
		return errorString;
	}

	/**
	 * @param errorString the errorString to set
	 */
	public void setErrorString(String errorString) {
		this.errorString = errorString;
	}


}
