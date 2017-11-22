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

package com.arm.cmsis.pack.data;

import java.util.Collection;

import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.enums.IEvaluationResult;
import com.arm.cmsis.pack.generic.IAttributes;

/**
 * Context for component/file filtering and dependency evaluation. 
 * <p>
 * Caches overall evaluation result as well as results or individual conditions and expressionsf   
 * <p>
 * Underlying IAttributes provides methods to manage device and toolchain filter attributes  
 * @see EEvaluationResult  
 */
public interface ICpConditionContext extends IAttributes, IEvaluationResult {
	/**
	 *  Resets cached results to UNDEFINED
	 */
	void resetResult();
	
	
	/**
	 * Returns cached result for particular item 
	 * @return result for particular ICpItem 
	 */
	EEvaluationResult getEvaluationResult(ICpItem item);

	
	/**
	 * Evaluates supplied item (component, file, etc.) for given context and returns its result   
	 * @param item ICpItem to evaluate  
	 * @return evaluation result  
	 */
	EEvaluationResult evaluate(ICpItem item);
	
	/**
	 * Evaluates supplied condition
	 * @param condition to evaluate  
	 * @return evaluation result
	 */
	EEvaluationResult evaluateCondition(ICpCondition condition);
	
	
	/**
	 * Evaluates supplied expression
	 * @param expression to evaluate  
	 * @return evaluation result
	 */
	EEvaluationResult evaluateExpression(ICpExpression expression);
	
	
	/**
	 * Filters supplied collection of items (for example ICpFile items) filtered according to the context 
	 * @param sourceCollection source collection to filter 
	 * @return filtered collection
	 */
	Collection<ICpItem> filterItems(Collection<? extends ICpItem> sourceCollection);
	
}
