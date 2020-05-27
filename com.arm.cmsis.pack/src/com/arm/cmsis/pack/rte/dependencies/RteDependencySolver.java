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

package com.arm.cmsis.pack.rte.dependencies;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpEnvironmentProvider;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpConditionContext;
import com.arm.cmsis.pack.data.ICpComponent;
import com.arm.cmsis.pack.data.ICpExpression;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.info.ICpComponentInfo;
import com.arm.cmsis.pack.info.ICpConfigurationInfo;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.rte.IRteModel;
import com.arm.cmsis.pack.rte.components.IRteComponent;
import com.arm.cmsis.pack.rte.components.IRteComponentGroup;
import com.arm.cmsis.pack.rte.components.IRteComponentItem;
import com.arm.cmsis.pack.utils.AlnumComparator;
import com.arm.cmsis.pack.utils.VersionComparator;

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

	protected Map<IRteComponentItem, EEvaluationResult> fEvaluationResults = null;

	// temporary collections of selected components and results
	protected Collection<IRteComponent> tSelectedComponents = null;
	// sorted map : MISSING comes earlier than SELECTABLE
	protected Map<IRteComponent, IRteDependencyResult> tComponentResults = null;
	protected Map<IRteComponentGroup, IRteDependency> tApiConflicts = null;


	/**
	 *  Helper class to compare component by evaluation result (descending) and component name (acceding)
	 */
	class ComponentResultComparator implements Comparator<IRteComponent> {
		@Override
		public int compare(IRteComponent c0, IRteComponent c1) {
			int res0 = getEvaluationResult(c0).ordinal();
			int res1 = getEvaluationResult(c1).ordinal();
			int res = res0 - res1;
			if(res != 0) {
				return res;
			}
			String name0 = c0.getActiveCpItem().getName();
			String name1 = c1.getActiveCpItem().getName();
			return AlnumComparator.alnumCompare(name0, name1);
		}
	}

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
		tComponentResults = null;
		tApiConflicts = null;
	}

	protected Collection<IRteComponent> getSelectedComponents(){
		if(tSelectedComponents == null) {
			if(rteModel != null) {
				tSelectedComponents = rteModel.getSelectedComponents();
			} else {
				tSelectedComponents = Collections.emptyList();
			}
		}
		return tSelectedComponents;
	}

	@Override
	public Collection<? extends IRteDependencyItem> getDependencyItems() {
		return getDependencyItemMap().values();
	}

	protected Map<IRteComponentItem, IRteDependencyItem> getDependencyItemMap() {
		if(fDependencyItems == null) {
			fDependencyItems = new LinkedHashMap<>();
		}
		return fDependencyItems;
	}

	protected Map<IRteComponent, IRteDependencyResult> getComponentResults() {
		if(tComponentResults == null) {
			tComponentResults = new TreeMap<>(new ComponentResultComparator());
		}
		return tComponentResults;
	}

	protected Map<IRteComponentGroup, IRteDependency> getApiConflicts() {
		if(tApiConflicts == null) {
			tApiConflicts = new HashMap<>();
		}
		return tApiConflicts;
	}

	protected Collection<IRteComponent> getUsedComponents(){
		if(rteModel != null) {
			return rteModel.getUsedComponents();
		}
		return Collections.emptyList();
	}

	protected Map<String, ICpPack> getGeneratedPacks(){
		if(rteModel != null) {
			return rteModel.getGeneratedPacks();
		}
		return null;
	}

	@Override
	protected boolean isEvaluate(EEvaluationResult res) {
		if(super.isEvaluate(res)) {
			return true;
		}

		if(res == EEvaluationResult.ERROR) {
			return false;
		}
		// do not re-evaluate fulfilled and ignored conditions,
		// for other results do trigger calls to evaluateDependency(), it has its own cache
		return !res.isFulfilled();
	}

	protected void collectDependencies(IRteDependencyResult depRes, ICpItem condition, EEvaluationResult overallResult) {
		// first check require and deny expressions
		Collection<ICpExpression> children = condition.getChildrenOfType(ICpExpression.class);
		for(ICpExpression expr :  children) {
			EEvaluationResult res =  getEvaluationResult(expr);
			if(res.isFulfilled() || res.isUndefined()) {
				continue;
			}

			if(expr.getExpressionType() == ICpExpression.ACCEPT_EXPRESSION) {
				if(res.ordinal() < overallResult.ordinal()){
					continue; // ignored
				}
			} else {
				if(res.ordinal() > overallResult.ordinal()){
					continue;
				}
			}
			boolean bDeny = tbDeny; // save deny context
			if(expr.getExpressionType() == ICpExpression.DENY_EXPRESSION) {
				tbDeny = !tbDeny; // invert the deny context
			}
			if(expr.getExpressionDomain() == ICpExpression.REFERENCE_EXPRESSION) {
				collectDependencies(depRes, expr.getCondition(), overallResult);
			} else if(expr.getExpressionDomain() == ICpExpression.COMPONENT_EXPRESSION) {
				IRteDependency dep = getDependency(expr);
				if(dep != null) {
					depRes.addDependency(dep);
				}
			}
			tbDeny = bDeny; // restore deny context
		}
	}


	@Override
	public EEvaluationResult evaluateExpression(ICpExpression expression) {
		if(expression == null) {
			return EEvaluationResult.IGNORED;
		}

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
		{
			return EEvaluationResult.IGNORED; // nothing to do
		}

		IRteDependency dep = getDependency(expression);
		if(dep == null){
			dep = new RteDependency(expression, tbDeny);
			if(tbDeny) {
				EEvaluationResult res = evaluateDenyDependency(dep);
				dep.setEvaluationResult(res);
			} else {
				IRteComponentItem components = rteModel.getComponents();
				if(components != null){
					components.findComponents(dep);
				}
			}
			putDependency(expression, dep);
		}

		return dep.getEvaluationResult();
	}

	protected EEvaluationResult evaluateDenyDependency(IRteDependency dep) {
		EEvaluationResult res = EEvaluationResult.FULFILLED;
		Collection<IRteComponent> selectedComponents = getSelectedComponents();
		if(selectedComponents == null || selectedComponents.isEmpty()) {
			return res;
		}
		IAttributes attr = dep.getCpItem().attributes();
		for(IRteComponent rteComponent : selectedComponents) {
			ICpComponent c = rteComponent.getActiveCpComponent();
			if(c == null)
			{
				continue; // should not happen
			}

			if(attr.matchAttributes(c.attributes())) {
				res = EEvaluationResult.INCOMPATIBLE;
				dep.addComponent(rteComponent, res);
			}
		}
		return res;
	}


	protected IRteDependency getDependency(ICpExpression expression) {
		if(tbDeny) { // cache deny results separately
			if(fDenyDependencies != null) {
				return fDenyDependencies.get(expression);
			}
		} else if(fDependencies != null) {
			return fDependencies.get(expression);
		}
		return null;
	}

	protected void putDependency(ICpExpression expression,  IRteDependency dep ) {
		if(tbDeny) { // cache deny results separately
			if(fDenyDependencies == null) {
				fDenyDependencies = new HashMap<>();
			}
			fDenyDependencies.put(expression, dep);

		} else {
			if(fDependencies == null) {
				fDependencies = new HashMap<>();
			}
			fDependencies.put(expression, dep);
		}
	}


	@Override
	public IRteDependencyItem getDependencyItem(IRteComponentItem componentItem) {
		return getDependencyItemMap().get(componentItem);
	}


	@Override
	public EEvaluationResult evaluateDependencies() {
		resetResult();

		if(rteModel == null) {
			return EEvaluationResult.IGNORED; // nothing to do
		}

		if(evaluateDevice() == EEvaluationResult.FAILED) {
			setEvaluationResult(EEvaluationResult.FAILED);
			return getEvaluationResult();  // missing device => no use to evaluate something else
		}

		// Report missing components and gpdsc files
		evaluateUsedComponents();

		// Report unsatisfied dependencies and conflicts
		evaluateSelectedComponents();

		purgeResults();
		return getEvaluationResult();
	}

	/**
	 * Evaluates selected components and reports unsatisfied dependencies and conflicts
	 */
	protected void evaluateSelectedComponents() {
		Collection<IRteComponent> selectedComponents = getSelectedComponents();
		if(selectedComponents == null || selectedComponents.isEmpty()) {
			return; // nothing selected => nothing to evaluate
		}

		// sorted map : MISSING comes earlier than SELECTABLE

		for(IRteComponent component : selectedComponents){
			evaluateSelectedComponent(component);
		}
		if(!getDependencyItemMap().isEmpty())	{
			return; // no need to evaluate further if components or APIs are missing
		}

		// add API conflicts if any
		for(Entry<IRteComponentGroup, IRteDependency> e : getApiConflicts().entrySet()) {
			IRteDependency d = e.getValue();
			if(d.getChildCount() > 1) {
				d.setEvaluationResult(EEvaluationResult.CONFLICT);
				IRteComponentGroup g = e.getKey();
				getDependencyItemMap().put(g, d);
				cacheConditionResult(g, EEvaluationResult.CONFLICT);
				cacheConditionResult(g.getParentClass(), EEvaluationResult.CONFLICT);
				updateEvaluationResult(EEvaluationResult.CONFLICT);
			}
		}
		// finally add sorted dependency results
		for(Entry<IRteComponent, IRteDependencyResult> e : getComponentResults().entrySet()) {
			IRteComponent c = e.getKey();
			IRteDependencyResult r = e.getValue();
			getDependencyItemMap().put(c, r);
		}
	}


	/**
	 * Evaluates a selected component
	 * @param component IRteComponent to evaluate
	 */
	protected void evaluateSelectedComponent(IRteComponent component) {
		ICpComponent c = component.getActiveCpComponent();
		if(c == null || c instanceof ICpComponentInfo) {
			return; // already reported as missing
		}
		EEvaluationResult r = evaluate(c);

		updateEvaluationResult(r);

		cacheConditionResult(component, r);
		cacheConditionResult(component.getParentClass(), r);

		if(!r.isFulfilled()) {
			IRteDependencyResult  depRes = new RteDependencyResult(component);
			ICpItem condition = c.getCondition();
			if(r != EEvaluationResult.ERROR) {
				collectDependencies(depRes, condition, r);
			}
			depRes.setEvaluationResult(r);
			getComponentResults().put(component, depRes);
		}

		evaluateGroupApi(component, c, r);
	}


	/**
	 * Evaluates component group for API presence, API version and API conflicts
	 * @param component IRteComponent to evaluate
	 * @param c active ICpComponent
	 * @param r EEvaluationResult of component evaluation
	 */
	protected void evaluateGroupApi(IRteComponent component, ICpComponent c, EEvaluationResult r) {
		IRteComponentGroup g = component.getParentGroup();
		cacheConditionResult(g, r);
		//	 check for missing APIs and  API conflicts
		ICpComponent api = g.getApi();
		if(api instanceof ICpComponentInfo)  {
			ICpComponentInfo apiInfo = (ICpComponentInfo)api;
			if(apiInfo.getComponent() == null) {
				reportMissingApi(g, EEvaluationResult.MISSING);
			}
			return;
		}
		String requiredApiVersion = c.getAttribute(CmsisConstants.CAPIVERSION);
		if(!requiredApiVersion.isEmpty()) { // component requires API
			if(api == null) {
				reportMissingApi(component, EEvaluationResult.MISSING_API);
				return;
			}
			String apiVersion = api.getVersion();
			int versionDiff = VersionComparator.versionCompare(apiVersion, requiredApiVersion);
			if(versionDiff < -2) {
				reportMissingApi(component, EEvaluationResult.MISSING_API_VERSION);
				return;
			}
		}

		if(api != null && api.isExclusive()) {
			IRteDependency d = getApiConflicts().get(g);
			if(d == null) {
				d = new RteDependency(api, true);
				getApiConflicts().put(g, d);
			}
			d.addComponent(component, r);
		}
	}

	protected void reportMissingApi(IRteComponentItem componentItem, EEvaluationResult r) {
		IRteDependencyResult depRes = new RteMissingComponentResult(componentItem);
		depRes.setEvaluationResult(r);
		getDependencyItemMap().put(componentItem, depRes);
		cacheConditionResult(componentItem, r);
		IRteComponentGroup g = componentItem.getParentGroup();
		if(g != componentItem) {
			cacheConditionResult(g, r);
		}
		cacheConditionResult(componentItem.getParentClass(), r);
		updateEvaluationResult(r);
	}

	/**
	 * Evaluates used components and reports missing components and gpdsc files
	 */
	protected void evaluateUsedComponents() {
		ICpEnvironmentProvider ep  = CpPlugIn.getEnvironmentProvider();
		ICpConfigurationInfo configInfo = rteModel.getConfigurationInfo();

		Collection<IRteComponent> usedComponents = getUsedComponents();
		for(IRteComponent component : usedComponents){
			if(!component.isSelected()) {
				continue;
			}
			ICpComponentInfo ci = component.getActiveCpComponentInfo();
			if(ci == null) {
				continue;
			}
			EEvaluationResult r;
			IRteDependencyResult depRes = null;
			if(ci.getComponent() != null) {
				if(ci.isGenerated() || !ci.isSaved()) {
					continue;
				}
				String gpdsc = ci.getGpdsc();
				if(gpdsc == null || gpdsc.isEmpty()) {
					continue;
				}
				gpdsc = ep.expandString(gpdsc, configInfo, true);

				ICpPack pack = rteModel.getGeneratedPack(gpdsc);
				if(pack != null) {
					continue;
				}
				r = EEvaluationResult.MISSING_GPDSC;
				depRes = new RteMissingGpdscResult(component, gpdsc);
			} else {
				r = ci.getEvaluationResult();
				depRes = new RteMissingComponentResult(component);
				updateEvaluationResult(r);
			}

			depRes.setEvaluationResult(r);
			getDependencyItemMap().put(component, depRes);

			cacheConditionResult(component, r);
			cacheConditionResult(component.getParentClass(), r);
			cacheConditionResult(component.getParentGroup(), r);

			updateEvaluationResult(r);
		}
	}


	/**
	 * Checks if device info is available
	 * @return EEvaluationResult
	 */
	protected EEvaluationResult evaluateDevice() {
		IRteComponentItem devClass = getSelectedDeviceClass();
		// first check if the selected device is available
		ICpDeviceInfo di = rteModel.getDeviceInfo();
		if(devClass != null && di != null) {
			if(di.getDevice() == null){
				EEvaluationResult result = EEvaluationResult.FAILED;
				IRteDependencyResult depRes = new RteMissingDeviceResult(devClass, di);
				getDependencyItemMap().put(devClass, depRes);
				cacheConditionResult(devClass, result);
				return result; // missing device => no use to evaluate something else
			}
			cacheConditionResult(devClass, EEvaluationResult.FULFILLED);
		}
		return EEvaluationResult.IGNORED;
	}


	/**
	 * Removes all items that are higher than overall result
	 */
	protected void purgeResults() {
		Iterator<IRteDependencyItem> iterator = getDependencyItemMap().values().iterator();
		while(iterator.hasNext()) {
			IRteDependencyItem d = iterator.next();
			EEvaluationResult res = d.getEvaluationResult();
			if(res.ordinal() > getEvaluationResult().ordinal()) {
				iterator.remove();
			}
		}
	}



	protected IRteComponentItem getSelectedDeviceClass(){
		return rteModel.getComponents().getFirstChild(CmsisConstants.EMPTY_STRING); // always first
	}


	protected void cacheConditionResult(IRteComponentItem item, EEvaluationResult res) {
		if(item == null) {
			return;
		}
		if(getEvaluationResult(item).ordinal() <= res.ordinal()) {
			return;
		}

		if(fEvaluationResults == null) {
			fEvaluationResults = new HashMap<>();
		}
		fEvaluationResults.put(item, res);
	}


	@Override
	public EEvaluationResult getEvaluationResult(IRteComponentItem item) {
		if(fEvaluationResults != null) {
			EEvaluationResult res = fEvaluationResults.get(item);
			if(res != null) {
				return res;
			}
		}
		return EEvaluationResult.IGNORED;
	}


	@Override
	public EEvaluationResult resolveDependencies() {
		// try to run resolve iteration until all dependencies are resolved or no resolution is available
		while(!getEvaluationResult().isFulfilled())
		{
			if(!resolveIteration()) { // no dependency is resolved
				break;
			}
		}
		return getEvaluationResult();
	}

	/**
	 * Tries to resolve SELECTABLE dependencies
	 * @return true if one of dependencies gets resolved => the state changes
	 */
	protected boolean resolveIteration(){
		for(IRteDependencyItem depItem : getDependencyItemMap().values()) {
			if(resolveDependency(depItem)) {
				return true;
			}
		}
		return false;
	}


	protected boolean resolveDependency(IRteDependencyItem depItem){
		if(depItem.getEvaluationResult() != EEvaluationResult.SELECTABLE) {
			return false;
		}
		if(depItem instanceof IRteDependencyResult) {
			IRteDependencyResult depRes = (IRteDependencyResult)depItem;
			Collection<IRteDependency> deps = depRes.getDependencies();
			if(deps == null) {
				return false;
			}
			for(IRteDependency d : deps) {
				if(resolveDependency(d)) {
					return true;
				}
			}
		}
		return false;
	}


	protected boolean resolveDependency(IRteDependency dependency){
		if(dependency.getEvaluationResult() != EEvaluationResult.SELECTABLE) {
			return false;
		}

		IRteComponent c = dependency.getBestMatch();
		if(c != null) {
			ICpComponent cpComponent = c.getActiveCpComponent();
			if(cpComponent == null || cpComponent.isCustom()) {
				return false; // custom component cannot be selected automatically
			}

			rteModel.selectComponent(c, 1);
			rteModel.evaluateComponentDependencies(); // re-evaluate dependencies to remove resolved ones
			return true;
		}
		return false;
	}
}
