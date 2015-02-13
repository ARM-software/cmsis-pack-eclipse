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

import com.arm.cmsis.pack.base.ICmsisItem;
import com.arm.cmsis.pack.base.IEvaluationResult;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.rte.components.IRteComponentItem;

/**
 * Base interface for object constructing dependency tree 
 */
public interface IRteDependencyItem extends ICmsisItem, IEvaluationResult {

	/**
	 * Returns associated component item if any
	 * @return associated component item
	 */
	IRteComponentItem getComponentItem();

	/**
	 * Returns associated ICpItem that is:
	 *  <ul>
	 * <li> a source of dependency (an ICpExpresiion or an ICpApi) 
	 * <li> or an ICpComponent corresponding to associated  IRteComponentItem
	 * </ul>
	 * @return ICpItem that is source of dependency or underlying ICpCompoent 
	 */
	ICpItem getCpItem();
	
	
	/**
	 * Checks if this item is evaluated in negative context: it denies the matching components  
	 * @return if the dependency is a deny one
	 */
	boolean isDeny();

	@Override
	Collection<? extends IRteDependencyItem> getChildren();
	
}
