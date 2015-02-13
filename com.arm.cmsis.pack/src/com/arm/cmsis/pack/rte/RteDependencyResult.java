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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.rte.components.IRteComponentItem;

/**
 * Default implementation of IRteDependencyResult
 */
public class RteDependencyResult extends RteDependencyItem implements IRteDependencyResult {

	Set<IRteDependency> fDependencies = new LinkedHashSet<IRteDependency>();
	
	/**
	 *	Constructs DependencyResult with  associated component item 
	 */
	public RteDependencyResult(IRteComponentItem componentItem) {
		super(componentItem);
	}
	
	
	@Override
	public Collection<IRteDependency> getChildren() {
		return getDependencies();
	}

	
	@Override
	public Collection<IRteDependency> getDependencies() {
		return fDependencies;
	}


	@Override
	public void addDependency(IRteDependency dependency) {
		if(dependency == null)
			return;
		if(fDependencies.contains(dependency))
			return;
		if(dependency.isResolved())
			return;
		fDependencies.add(dependency);
	}


	@Override
	public void setEvaluationResult(EEvaluationResult result) {
		super.setEvaluationResult(result);
		purgeDependencies();
		
	}

	/**
	 *  Removes dependencies that are less than overall result since they are irrelevant
	 */
	private void purgeDependencies() {
		int thisOrdinal = getEvaluationResult().ordinal();
		for (Iterator<IRteDependency> iterator = fDependencies.iterator(); iterator.hasNext();) {
			IRteDependency d = iterator.next();
			EEvaluationResult r = d.getEvaluationResult();
			if(d.isDeny() && r == EEvaluationResult.FULFILLED)
				r = EEvaluationResult.FAILED;
			if(r.ordinal() < thisOrdinal)
				iterator.remove();
		}
	}


	@Override
	public String getDescription() {
		EEvaluationResult res = getEvaluationResult();
		if(!fDependencies.isEmpty()) {
			switch(res) {
			case INSTALLED:
			case MISSING:
			case SELECTABLE:
			case UNAVAILABLE:
			case UNAVAILABLE_PACK:
				return "Additional software components required";
			case INCOMPATIBLE:
			case FAILED:
				return "Component conflicts with other selected components";
			default:
				break;
			}
		}
		String s = res.getDescription();
		if(s != null)
			return s;
		return super.getDescription();
	}
}
