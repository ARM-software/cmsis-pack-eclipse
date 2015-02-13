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

package com.arm.cmsis.pack.rte;

import java.util.Collection;

import com.arm.cmsis.pack.data.ICpConditionContext;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.rte.components.IRteComponentItem;

/**
 *
 */
public interface IRteDependencySolver extends ICpConditionContext{

	
	/**
	 * Evaluates dependencies for selected components
	 * @return worst dependency evaluation result
	 */
	EEvaluationResult evaluateDependencies();
	
	
	/**
	 * Tries to resolve component dependencies
	 * @return evaluation result after dependency resolving 
	 */
	EEvaluationResult resolveDependencies();
	
	
	/**
	 * Returns dependency item for given component item (bundle, group or component) 
	 * @param component IRteComponentItem for which to get result 
	 * @return dependency result or null if component item has no unresolved dependencies
	 */
	IRteDependencyItem getDependencyItem(IRteComponentItem componentItem); 
	
	/**
	 * Returns dependency evaluation result for given item (class, group or component) 
	 * @param item IRteComponentItem for which to get result 
	 * @return condition result or IGNORED if item has no result
	 */
	EEvaluationResult getEvaluationResult(IRteComponentItem item); 
	
	
	/**
	 * Returns collection of dependency results (items and dependencies)
	 * @return collection of dependency results
	 */
	Collection<? extends IRteDependencyItem> getDependencyItems();
	
}
