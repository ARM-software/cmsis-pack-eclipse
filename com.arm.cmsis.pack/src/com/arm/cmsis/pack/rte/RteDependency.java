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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.rte.components.IRteComponent;
import com.arm.cmsis.pack.rte.components.IRteComponentItem;

/**
 * Default implementation of IRteDependency interface
 */
public class RteDependency extends RteDependencyItem implements IRteDependency {

	protected ICpItem fCpItem = null; // component attributes to search for
	// collection to store candidates to resolve dependency  
	protected Map<IRteComponent, IRteDependencyItem> fComponentEntries = new LinkedHashMap<IRteComponent, IRteDependencyItem>();
	
	// list of component items that stop the search  
	protected Map<IRteComponentItem, IRteDependencyItem> fStopItems = null;
	
	boolean fbDeny = false;
	
	/**
	 * 
	 */
	public RteDependency( ICpItem item, boolean bDeny) {
		fCpItem = item;
		fbDeny = bDeny;
	}

	
	@Override
	public boolean isDeny() {
		return fbDeny;
	}

	@Override
	public boolean isResolved() {
		if(fResult == EEvaluationResult.IGNORED)
			return true;
		if(fResult == EEvaluationResult.FULFILLED)
			return !isDeny();
		return isDeny();      
	}
	
	@Override
	public Collection<IRteComponent> getComponents() {
		return fComponentEntries.keySet();
	}

	@Override
	public ICpItem getCpItem() {
		return fCpItem;
	}
	
	 
	@Override
	public EEvaluationResult getEvaluationResult() {
		EEvaluationResult result = super.getEvaluationResult();
//		if(isDeny() && result == EEvaluationResult.FULFILLED) {
//			result = EEvaluationResult.FAILED;
//		}
		return result;
	}


	@Override
	public EEvaluationResult getEvaluationResult(IRteComponent component) {
		IRteDependencyItem entry = fComponentEntries.get(component);
		if(entry != null) {
			EEvaluationResult result = entry.getEvaluationResult();
			if(isDeny() && result == EEvaluationResult.FULFILLED) {
				result = EEvaluationResult.FAILED;
			}
			return result;
		}
		return EEvaluationResult.UNDEFINED;
	}

	@Override
	public IRteComponent getBestMatch() {
		// TODO add bundle and variant calculations
		IRteComponent bestComponent = null;
		//EEvaluationResult bestResult = EEvaluationResult.MISSING;
		for(Entry<IRteComponent, IRteDependencyItem> e : fComponentEntries.entrySet()) {
			IRteComponent c = e.getKey();
			EEvaluationResult r = e.getValue().getEvaluationResult();
			if(r == EEvaluationResult.FULFILLED) {
				return c;
			} else if(r == EEvaluationResult.SELECTABLE) {
				if(bestComponent == null)
					bestComponent = c;
				else
					return null;
			}
		}
		return bestComponent;
	}

	@Override
	public void addComponent(IRteComponent component, EEvaluationResult result) {
		IRteDependencyItem de = new RteDependencyItem(component, result);
		fComponentEntries.put(component, de);
		if(fResult.ordinal() < result.ordinal())
			fResult = result;
	}

	@Override
	public void addStopItem(IRteComponentItem item, EEvaluationResult result) {
		if(fStopItems == null) 
			fStopItems = new LinkedHashMap<IRteComponentItem, IRteDependencyItem>();

		fStopItems.put(item, new RteDependencyItem(item, result));
		if(fResult.ordinal() < result.ordinal())
			fResult = result;
	}

	@Override
	public Collection<? extends IRteDependencyItem> getChildren() {
		return fComponentEntries.values();
	}


	@Override
	public String getDescription() {
		EEvaluationResult res = getEvaluationResult();
		if(isDeny() && res == EEvaluationResult.FULFILLED)
			res = EEvaluationResult.INCOMPATIBLE;
		switch(res) {
		case CONFLICT:
			return "Conflict, select exactly one component from list";
		case INCOMPATIBLE_API:
			return "Select or install compatible API";
		case INCOMPATIBLE:
		case INCOMPATIBLE_BUNDLE:
		case INCOMPATIBLE_VARIANT:
		case INCOMPATIBLE_VENDOR:
		case INCOMPATIBLE_VERSION:
			return "Select compatible component or unselect incompatible one";
		case INSTALLED:
			return "Update pack, bundle or variant selection and then select a component";
		case MISSING:
			return "Install missing component";
		case MISSING_API:
			return "Required API is missing";
		case MISSING_BUNDLE:
			return "Required bundle is missing";
		case MISSING_VARIANT:
			return "Required variant is missing";
		case MISSING_VENDOR:
			return "Required vendor is missing";
		case MISSING_VERSION:
			return "Required vendor is missing";
		case SELECTABLE:
			return "Select component from list";
		case UNAVAILABLE:
			return "Required component is not available for current device or toolchain";
		case UNAVAILABLE_PACK:
			return "Required pack is not selected";
		
		case FULFILLED:
		case UNDEFINED:
		case ERROR:
		case FAILED:
		case IGNORED:
		case INACTIVE:
		default:
			break;
		}
		return super.getDescription();
	}
}
