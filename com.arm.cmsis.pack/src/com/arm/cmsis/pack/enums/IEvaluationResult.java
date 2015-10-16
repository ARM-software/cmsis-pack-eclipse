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

package com.arm.cmsis.pack.enums;


/**
 * Interface for items that can hold EEvaluationResult value
 * @see com.arm.cmsis.pack.enums.EEvaluationResult 
 */
public interface IEvaluationResult {
	/**
	 * Returns evaluation result for this object
	 * @return evaluation result
	 */
	EEvaluationResult getEvaluationResult();
	
	
	/**
	 * Sets evaluation result.   
	 * @param result to set
	 */
	void setEvaluationResult(EEvaluationResult result);

}
