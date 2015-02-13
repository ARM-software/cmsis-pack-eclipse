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

import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.rte.components.IRteComponent;
import com.arm.cmsis.pack.rte.components.IRteComponentItem;

/**
 * Interface for dependency evaluation and resolving  
 */
public interface IRteDependency extends IRteDependencyItem {
	
	/**
	 * Checks if this dependency is resolved 
	 * @return true if resolved
	 */
	boolean isResolved();
	
	
	/**
	 * Returns dependency evaluation result for specific component candidate
	 * @return dependency evaluation result if component found, otherwise EEvaluationResult.UNDEFINED
	 */
	EEvaluationResult getEvaluationResult(IRteComponent component);

	
	/**
	 * Returns list of collected components which are candidates to resolve dependencies
	 * @return list of collected candidates to resolve dependencies  
	 */
	Collection<IRteComponent> getComponents();
	
	/**
	 * Returns component that best matches dependency
	 * @return list of collected candidates to resolve dependencies  
	 */
	IRteComponent getBestMatch();

	
	/**
	 * Adds component to the internal list of candidate components  
	 * @param component that is a candidate to fulfill dependency 
	 * @param result result of the evaluation showing to which extent the component fulfills the dependency  
	 */
	void addComponent(IRteComponent component, EEvaluationResult result);
	
	/**
	 * Adds component hierarchy item that stopped dependency evaluation    
	 * @param item a component hierarchy at which evaluation has stopped
	 * @param result reason why evaluation has stopped
	 */
	void addStopItem(IRteComponentItem item, EEvaluationResult result);
	
}
