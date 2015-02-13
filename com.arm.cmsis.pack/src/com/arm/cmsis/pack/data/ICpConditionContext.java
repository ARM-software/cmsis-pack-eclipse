/*******************************************************************************
* Copyright (c) 2014 ARM Ltd.
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/

package com.arm.cmsis.pack.data;

import com.arm.cmsis.pack.base.IEvaluationResult;
import com.arm.cmsis.pack.enums.EEvaluationResult;
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
	
}
