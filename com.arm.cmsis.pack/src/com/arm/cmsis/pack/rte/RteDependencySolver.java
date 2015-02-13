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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.arm.cmsis.pack.data.CpConditionContext;
import com.arm.cmsis.pack.data.ICpComponent;
import com.arm.cmsis.pack.data.ICpCondition;
import com.arm.cmsis.pack.data.ICpExpression;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.info.ICpComponentInfo;
import com.arm.cmsis.pack.rte.components.IRteComponent;
import com.arm.cmsis.pack.rte.components.IRteComponentGroup;
import com.arm.cmsis.pack.rte.components.IRteComponentItem;

/**
 * Class responsible for evaluating component dependencies and resolving them 
 */
public class RteDependencySolver extends CpConditionContext implements IRteDependencySolver {

	protected IRteConfiguration rteConfiguration = null;

	protected Map<ICpItem, EEvaluationResult> fDenyResults = null; 
	
	protected Map<ICpExpression, IRteDependency> fDependencies = null;
	protected Map<ICpExpression, IRteDependency> fDenyDependencies = null;
	
	protected Map<IRteComponentItem, IRteDependencyItem> fDependencyItems = null;
	protected IRteDependencyResult tCurrentDependecyResult =  null; // component being evaluated 
	
	protected Collection<IRteComponent> tSelectedComponents = null; 
	
	protected Map<IRteComponentItem, EEvaluationResult> fEvaluationResults = null;
	/**
	 * Default constructor 
	 */
	public RteDependencySolver(IRteConfiguration config) {
		rteConfiguration = config;
	}

	
	@Override
	public void resetResult() {
		super.resetResult();
		fDenyResults = null;
		fDependencies = null;
		fDenyDependencies = null;
		fEvaluationResults = null;
		fDependencyItems = null;
		tSelectedComponents = null;
	}
	
	protected Collection<IRteComponent> getSelectedComponents(){
		if(tSelectedComponents == null) {
			if(rteConfiguration != null)
				tSelectedComponents = rteConfiguration.getSelectedComponents();
		}
		return tSelectedComponents;
	}

	protected Collection<IRteComponent> getUsedComponents(){
		if(rteConfiguration != null)
			return rteConfiguration.getUsedComponents();
		return null;
	}
	
	
	@Override
	protected EEvaluationResult getCachedResult(ICpItem item) {
		if(tbDeny) { // cache deny results separately
			if(fDenyResults != null)
				return fDenyResults.get(item);
			return null;
		}
		return super.getCachedResult(item);
	 }

	@Override
	protected void putCachedResult(ICpItem item, EEvaluationResult res) {
		 if(fDenyResults == null)
			 fDenyResults = new HashMap<ICpItem, EEvaluationResult>();
		 fDenyResults.put(item, res);
	 }
	

	@Override
	public EEvaluationResult evaluateCondition(ICpCondition condition) {
		EEvaluationResult result =  super.evaluateCondition(condition);
		
		if(tResultAccept != EEvaluationResult.UNDEFINED && !tResultAccept.isFulfilled()) {
			// collect results of accept expressions, select only those with the results equal to tResultAccept
			Collection<? extends ICpItem> children = condition.getChildren();
			for(ICpItem child :  children) {
				if(!(child instanceof ICpExpression))
					continue;
				ICpExpression expr = (ICpExpression)child;
				if(expr.getExpressionType() == ICpExpression.ACCEPT_EXPRESSION && 
				   expr.getExpressionDomain() == ICpExpression.COMPONENT_EXPRESSION) {
				   IRteDependency dependency = getDependency(expr);
					if(dependency == null)
						continue;
					if(dependency.getEvaluationResult() != tResultAccept)
						continue;
					tCurrentDependecyResult.addDependency(dependency);
				}
			}
		}
		return result;
	}


	@Override
	public EEvaluationResult evaluateExpression(ICpExpression expression) {
		if(expression == null)
			return EEvaluationResult.IGNORED;
		
		switch(expression.getExpressionDomain()) {
		case ICpExpression.COMPONENT_EXPRESSION:
			return evaluateDependency(expression);
		case ICpExpression.REFERENCE_EXPRESSION:
			return evaluate(expression.getCondition()); 
		
		case ICpExpression.DEVICE_EXPRESSION:
		case ICpExpression.TOOLCHAIN_EXPRESSION:
			return EEvaluationResult.IGNORED;
		default: 
			break;
		}
		return EEvaluationResult.ERROR;
	}


	protected EEvaluationResult evaluateDependency( ICpExpression expression) {
		if(rteConfiguration == null)
			return EEvaluationResult.IGNORED; // nothing to do
		
		IRteDependency dep = getDependency(expression); 
		if(dep == null){
			dep = new RteDependency(expression, tbDeny);
			IRteComponentItem components = rteConfiguration.getComponents();
			if(components != null){
				components.findComponents(dep);
			}
			putDependency(expression, dep);
		}
		
		EEvaluationResult result = dep.getEvaluationResult();
		if(expression.getExpressionType() != ICpExpression.ACCEPT_EXPRESSION) {
			if(!dep.isResolved())
				tCurrentDependecyResult.addDependency(dep);
		}
		return result;
	}

	protected IRteDependency getDependency(ICpExpression expression) {
		if(tbDeny) { // cache deny results separately
			if(fDenyDependencies != null)
				return fDenyDependencies.get(expression);
		} else if(fDependencies != null) {
			return fDependencies.get(expression);
		}
		return null;
	}

	protected void putDependency(ICpExpression expression,  IRteDependency dep ) {
		if(tbDeny) { // cache deny results separately
			if(fDenyDependencies == null)
				fDenyDependencies = new HashMap<ICpExpression, IRteDependency>();
			fDenyDependencies.put(expression, dep);
			
		} else {
			if(fDependencies == null)
				fDependencies = new HashMap<ICpExpression, IRteDependency>();
			fDependencies.put(expression, dep);
		}
	}


	@Override
	public IRteDependencyItem getDependencyItem(IRteComponentItem componentItem) {
		if(fDependencyItems != null)
			return fDependencyItems.get(componentItem);
		return null;
	}


	@Override
	public EEvaluationResult evaluateDependencies() {
		resetResult();
		if(rteConfiguration == null)
			return EEvaluationResult.IGNORED; // nothing to do
		
		fDependencyItems = new LinkedHashMap<IRteComponentItem, IRteDependencyItem>();
		// first report missing components 
		Collection<IRteComponent> usedComponents = getUsedComponents();
		if(usedComponents != null && !usedComponents.isEmpty()) {
			for(IRteComponent component : usedComponents){
				ICpComponentInfo ci = component.getActiveCpComponentInfo();
				if(ci == null)
					continue;
				EEvaluationResult r = ci.getEvaluationResult();
				if(r.isFulfilled())
					continue;
				tCurrentDependecyResult = new RteDependencyResult(component);
				tCurrentDependecyResult.setEvaluationResult(r);
				if(component.isSelected())
					fDependencyItems.put(component, tCurrentDependecyResult);

				cacheConditionResult(component, r);
				cacheConditionResult(component.getParentClass(), r);
				cacheConditionResult(component.getParentGroup(), r);
			}
		}
		
		Collection<IRteComponent> selectedComponents = getSelectedComponents(); 
		if(selectedComponents == null || selectedComponents.isEmpty())
			return getEvaluationResult();
		
		Map<IRteComponentGroup, IRteDependency> apiConflicts = new HashMap<IRteComponentGroup, IRteDependency>();
		for(IRteComponent component : selectedComponents){
			ICpComponent c = component.getActiveCpComponent();
			if(c == null || c instanceof ICpComponentInfo)
				continue;
			tCurrentDependecyResult = new RteDependencyResult(component);
			EEvaluationResult r = evaluate(c);
			tCurrentDependecyResult.setEvaluationResult(r);
			
			if(r.ordinal() < fResult.ordinal())
				fResult = r;

			if(r.ordinal() < EEvaluationResult.FULFILLED.ordinal())
				fDependencyItems.put(component, tCurrentDependecyResult);
			
			cacheConditionResult(component, r);
			cacheConditionResult(component.getParentClass(), r);
			
			IRteComponentGroup g = component.getParentGroup();
			cacheConditionResult(g, r);
			//	 check for API conflicts  
			ICpComponent api = g.getApi();
			if(api != null && api.isExclusive()) {
				IRteDependency d = apiConflicts.get(g);
				if(d == null) {
					d = new RteDependency(api, true);
					apiConflicts.put(g, d);
				}
				d.addComponent(component, r);
			}
		}
		// add API evaluation results
		for(Entry<IRteComponentGroup, IRteDependency> e : apiConflicts.entrySet()) {
			IRteDependency d = e.getValue();
			if(d.getChildCount() > 1) {
				d.setEvaluationResult(EEvaluationResult.CONFLICT);
				IRteComponentGroup g = e.getKey(); 
				fDependencyItems.put(e.getKey(), d);
				cacheConditionResult(g, EEvaluationResult.CONFLICT);
			}
		}
		
		return getEvaluationResult();

	}

	protected void cacheConditionResult(IRteComponentItem item, EEvaluationResult res) {
		if(item == null)
			return;
		if(getEvaluationResult(item).ordinal() <= res.ordinal())
			return;

		if(fEvaluationResults == null)
			fEvaluationResults = new HashMap<IRteComponentItem, EEvaluationResult>(); 
		fEvaluationResults.put(item, res);
	}
	
	
	@Override
	public EEvaluationResult getEvaluationResult(IRteComponentItem item) {
		if(fEvaluationResults != null) {
			EEvaluationResult res = fEvaluationResults.get(item);
			if(res != null)
				return res;
		}
		return EEvaluationResult.IGNORED;
	}


	@Override
	public EEvaluationResult resolveDependencies() {
		// try to run resolve iteration until all dependencies are resolved or no resolution is available
		while(fDependencyItems != null && getEvaluationResult().ordinal() < EEvaluationResult.FULFILLED.ordinal())
		{	
			if(resolveIteration() == false)
				break;
		}
		return getEvaluationResult();
	}
	
	/**
	 * Tries to resolve SELECTABLE dependencies 
	 * @return true if one of dependencies gets resolved => the state changes 
	 */
	protected boolean resolveIteration(){
		for(IRteDependencyItem depItem : fDependencyItems.values()) {
			if(resolveDependency(depItem))
				return true;
		}
		return false;
	}
	
	
	protected boolean resolveDependency(IRteDependencyItem depItem){
		if(depItem.getEvaluationResult() != EEvaluationResult.SELECTABLE)
			return false;
		if(depItem instanceof IRteDependencyResult) { 
			IRteDependencyResult depRes = (IRteDependencyResult)depItem; 
			Collection<IRteDependency> deps = depRes.getDependencies();
			if(deps == null)
				return false;
			for(IRteDependency d : deps) {
				if(resolveDependency(d))
					return true;
			}
		}
		return false;
	}
	
	
	protected boolean resolveDependency(IRteDependency dependency){
		if(dependency.getEvaluationResult() != EEvaluationResult.SELECTABLE)
			return false;
		
		IRteComponent c = dependency.getBestMatch();
		if(c != null) {
			rteConfiguration.selectComponent(c, 1);
			return true;
		}
		return false;
	}


	@Override
	public Collection<? extends IRteDependencyItem> getDependencyItems() {
		if(fDependencyItems != null) 
			return fDependencyItems.values();
		return null;
	}
}
