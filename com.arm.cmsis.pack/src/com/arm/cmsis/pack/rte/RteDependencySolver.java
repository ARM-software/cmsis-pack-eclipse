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

package com.arm.cmsis.pack.rte;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpConditionContext;
import com.arm.cmsis.pack.data.ICpComponent;
import com.arm.cmsis.pack.data.ICpCondition;
import com.arm.cmsis.pack.data.ICpExpression;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.info.ICpComponentInfo;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.rte.components.IRteComponent;
import com.arm.cmsis.pack.rte.components.IRteComponentGroup;
import com.arm.cmsis.pack.rte.components.IRteComponentItem;
import com.arm.cmsis.pack.rte.dependencies.IRteDependency;
import com.arm.cmsis.pack.rte.dependencies.IRteDependencyItem;
import com.arm.cmsis.pack.rte.dependencies.IRteDependencyResult;
import com.arm.cmsis.pack.rte.dependencies.RteDependency;
import com.arm.cmsis.pack.rte.dependencies.RteDependencyResult;
import com.arm.cmsis.pack.rte.dependencies.RteMissingComponentResult;
import com.arm.cmsis.pack.rte.dependencies.RteMissingDeviceResult;
import com.arm.cmsis.pack.utils.AlnumComparator;

/**
 * Class responsible for evaluating component dependencies and resolving them 
 */
public class RteDependencySolver extends CpConditionContext implements IRteDependencySolver {

	protected IRteModel rteModel = null;

	//results of evaluating expressions
	protected Map<ICpExpression, IRteDependency> fDependencies = null;
	protected Map<ICpExpression, IRteDependency> fDenyDependencies = null;
	
	// collected results for selected components
	protected Map<IRteComponentItem, IRteDependencyItem> fDependencyItems = null;
	protected IRteDependencyResult tCurrentDependecyResult =  null; // component being evaluated 
	
	// temporary collection of selected components
	protected Collection<IRteComponent> tSelectedComponents = null; 
	
	protected Map<IRteComponentItem, EEvaluationResult> fEvaluationResults = null;

	 /**
	 *  Helper class to compare component by evaluation result (descending) and component name (acceding) 
	 */
	class ComponentResultComparator implements Comparator<IRteComponent> {
		@Override
		public int compare(IRteComponent c0, IRteComponent c1) {
			int res0 = getEvaluationResult(c0).ordinal();
			int res1 = getEvaluationResult(c1).ordinal();
			int res = res0 - res1;
			if(res != 0)  
				return res;
			String name0 = c0.getActiveCpItem().getName();
			String name1 = c1.getActiveCpItem().getName();
			return AlnumComparator.alnumCompare(name0, name1);
		}
	 };
	
	/**
	 * Default constructor 
	 */
	public RteDependencySolver(IRteModel model) {
		rteModel = model;
	}

	
	@Override
	public void resetResult() {
		super.resetResult();
		fDependencies = null;
		fDenyDependencies = null;
		fEvaluationResults = null;
		fDependencyItems = null;
		tSelectedComponents = null;
	}
	
	protected Collection<IRteComponent> getSelectedComponents(){
		if(tSelectedComponents == null) {
			if(rteModel != null)
				tSelectedComponents = rteModel.getSelectedComponents();
		}
		return tSelectedComponents;
	}

	protected Collection<IRteComponent> getUsedComponents(){
		if(rteModel != null)
			return rteModel.getUsedComponents();
		return null;
	}
	

	@Override
	protected void putCachedResult(ICpItem item, EEvaluationResult res) {
		// only cache positive result, do not-reevaluate fulfilled and ignored conditions,
		// for other results do trigger calls to evaluateDependency(), it has its own cache
		if(res.isFulfilled())
			super.putCachedResult(item, res);
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
		if(rteModel == null)
			return EEvaluationResult.IGNORED; // nothing to do
		
		IRteDependency dep = getDependency(expression); 
		if(dep == null){
			dep = new RteDependency(expression, tbDeny);
			IRteComponentItem components = rteModel.getComponents();
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
		if(rteModel == null)
			return EEvaluationResult.IGNORED; // nothing to do
		
		fDependencyItems = new LinkedHashMap<IRteComponentItem, IRteDependencyItem>();
		IRteComponentItem devClass = getSelectedDeviceClass();
		// first check if the selected device is available
		ICpDeviceInfo di = rteModel.getDeviceInfo();
		if(devClass != null && di != null) {
			if(di.getDevice() == null){
				fResult = EEvaluationResult.FAILED;
				tCurrentDependecyResult = new RteMissingDeviceResult(devClass, di);
				fDependencyItems.put(devClass, tCurrentDependecyResult);
				cacheConditionResult(devClass, fResult);
				return fResult; // missing device => no use to evaluate something else 
			}
			cacheConditionResult(devClass, EEvaluationResult.FULFILLED);
		}
		
		// report missing components 
		Collection<IRteComponent> usedComponents = getUsedComponents();
		if(usedComponents != null && !usedComponents.isEmpty()) {
			for(IRteComponent component : usedComponents){
				if(!component.isSelected()) {
					continue;
				}
				ICpComponentInfo ci = component.getActiveCpComponentInfo();
				if(ci == null)
					continue;
				if(ci.getComponent() != null)
					continue;
				EEvaluationResult r = ci.getEvaluationResult();
				tCurrentDependecyResult = new RteMissingComponentResult(component);
				tCurrentDependecyResult.setEvaluationResult(r);
				fDependencyItems.put(component, tCurrentDependecyResult);

				cacheConditionResult(component, r);
				cacheConditionResult(component.getParentClass(), r);
				cacheConditionResult(component.getParentGroup(), r);
				fResult = EEvaluationResult.FAILED;
			}
		}
		
		Collection<IRteComponent> selectedComponents = getSelectedComponents(); 
		if(selectedComponents == null || selectedComponents.isEmpty())
			return getEvaluationResult();
		
		// sorted map : MISSING comes earlier as SELECTABLE
		Map<IRteComponent, IRteDependencyResult> componentResults = new TreeMap<IRteComponent, IRteDependencyResult>(new ComponentResultComparator());
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

			cacheConditionResult(component, r);
			cacheConditionResult(component.getParentClass(), r);

			if(r.ordinal() < EEvaluationResult.FULFILLED.ordinal())
				componentResults.put(component, tCurrentDependecyResult);
			
			IRteComponentGroup g = component.getParentGroup();
			cacheConditionResult(g, r);
			//	 check for missing APIs and  API conflicts  
			ICpComponent api = g.getApi();
			if(api != null) {
				if(api instanceof ICpComponentInfo ) {
					ICpComponentInfo apiInfo = (ICpComponentInfo)api;
					if(apiInfo.getComponent() == null) {
						r = EEvaluationResult.MISSING_API;
						tCurrentDependecyResult = new RteMissingComponentResult(g);
						fDependencyItems.put(g, tCurrentDependecyResult);
						cacheConditionResult(g, r);
						cacheConditionResult(g.getParentClass(), r);
						fResult = EEvaluationResult.FAILED;
					}
				} else if(api.isExclusive()) {
					IRteDependency d = apiConflicts.get(g);
					if(d == null) {
						d = new RteDependency(api, true);
						apiConflicts.put(g, d);
					}
					d.addComponent(component, r);
				}
			}
		}
		if(!fDependencyItems.isEmpty())
			return getEvaluationResult(); // no need to evaluate further if components or APIs are missing 
		
		// add API evaluation results
		for(Entry<IRteComponentGroup, IRteDependency> e : apiConflicts.entrySet()) {
			IRteDependency d = e.getValue();
			if(d.getChildCount() > 1) {
				d.setEvaluationResult(EEvaluationResult.CONFLICT);
				IRteComponentGroup g = e.getKey(); 
				fDependencyItems.put(g, d);
				cacheConditionResult(g, EEvaluationResult.CONFLICT);
				cacheConditionResult(g.getParentClass(), EEvaluationResult.CONFLICT);
			}
		}
		
		// finally add sorted dependency results
		for(Entry<IRteComponent, IRteDependencyResult> e : componentResults.entrySet()) {
			IRteComponent c = e.getKey();
			IRteDependencyResult r = e.getValue();
			fDependencyItems.put(c, r);
		}
		
		return getEvaluationResult();

	}
	
	IRteComponentItem getSelectedDeviceClass(){
		return rteModel.getComponents().getFirstChild(CmsisConstants.EMPTY_STRING); // always first
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
			rteModel.selectComponent(c, 1);
			rteModel.evaluateComponentDependencies(); // re-evaluate dependencies to remove resolved ones
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
