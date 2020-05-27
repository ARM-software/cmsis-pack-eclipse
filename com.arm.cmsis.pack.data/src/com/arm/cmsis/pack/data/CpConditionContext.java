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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.arm.cmsis.pack.enums.EEvaluationResult;

/**
 *  Default implementation of ICpConditionContext interface
 */
public class CpConditionContext extends CpAttributes implements ICpConditionContext {

	private EEvaluationResult fResult = EEvaluationResult.IGNORED;
	private Map<ICpItem, EEvaluationResult> fResults = null;

	// temporary variables
	private Set<ICpCondition> tConditionsBeingEvaluated = new HashSet<>(); // to prevent recursion
	protected boolean tbDeny = false; // flag is set when deny expression is evaluated

	@Override
	public void resetResult() {
		fResult = EEvaluationResult.IGNORED;
		fResults = null;
		tbDeny = false;
		tConditionsBeingEvaluated.clear();
	}


	@Override
	public EEvaluationResult getEvaluationResult() {
		return fResult;
	}


	@Override
	public void setEvaluationResult(EEvaluationResult result) {
		fResult = result;
	}

	protected void updateEvaluationResult(EEvaluationResult result) {
		if(result.ordinal() < fResult.ordinal())
			fResult = result;
	}

	@Override
	public EEvaluationResult getEvaluationResult(ICpItem item) {
		if(fResults != null)
			return fResults.get(item);
		return null;
	}

	@Override
	public EEvaluationResult evaluate(ICpItem item) {
		if(item == null)
			return EEvaluationResult.IGNORED;
		if(fResults == null)
			fResults = new HashMap<>();

		EEvaluationResult res = getEvaluationResult(item);
		if(isEvaluate(res)) {
			res = item.evaluate(this);
			putCachedResult(item, res);
		}
		return res;
	}


	/**
	 * Checks if result to be (re-)evaluated
	 * @param res EEvaluationResult
	 * @return true if result to be (re-)evaluated
	 */
	protected boolean isEvaluate(EEvaluationResult res) {
		return res == null || res == EEvaluationResult.UNDEFINED;
	}


	 /**
	 * Puts evaluation result into cache
	 * @param item ICpItem for which to put the result
	 * @param res result value to cache
	 */
	protected void putCachedResult(ICpItem item, EEvaluationResult res) {
		 if(fResults == null)
			 fResults = new HashMap<>();
		fResults.put(item, res);
	 }


	@Override
	public EEvaluationResult evaluateExpression(ICpExpression expression) {
		if(expression == null)
			return EEvaluationResult.IGNORED;
		switch(expression.getExpressionDomain()) {
		case ICpExpression.COMPONENT_EXPRESSION:
			return EEvaluationResult.IGNORED;
		case ICpExpression.DEVICE_EXPRESSION:
		case ICpExpression.TOOLCHAIN_EXPRESSION:
			boolean b = matchCommonAttributes(expression.attributes());
			return  b ? EEvaluationResult.FULFILLED : EEvaluationResult.FAILED;
		case ICpExpression.REFERENCE_EXPRESSION:
			return evaluate(expression.getCondition());
		default:
			break;
		}
		return EEvaluationResult.ERROR;
	}

	@Override
	public EEvaluationResult evaluateCondition(ICpCondition condition) {
		if(tConditionsBeingEvaluated.contains(condition))
			return EEvaluationResult.ERROR; // recursion

		tConditionsBeingEvaluated.add(condition);
		EEvaluationResult resultRequire = EEvaluationResult.IGNORED;
		EEvaluationResult resultAccept = EEvaluationResult.UNDEFINED;
		// first check require and deny expressions
		Collection<? extends ICpItem> children = condition.getChildren();
		for(ICpItem child :  children) {
			if(!(child instanceof ICpExpression))
				continue;
			ICpExpression expr = (ICpExpression)child;
			boolean bDeny = tbDeny; // save deny context
			if(expr.getExpressionType() == ICpExpression.DENY_EXPRESSION)
				tbDeny = !tbDeny; // invert the deny context
			EEvaluationResult res =  evaluate(expr);
			tbDeny = bDeny; // restore deny context
			if(res == EEvaluationResult.IGNORED || res == EEvaluationResult.UNDEFINED )
				continue;
			else if(res == EEvaluationResult.ERROR)
				return res;
			if(expr.getExpressionType() == ICpExpression.ACCEPT_EXPRESSION) {
				if(res.ordinal() > resultAccept.ordinal()){
					resultAccept = res;
				}
			} else {
				if(res.ordinal() < resultRequire.ordinal()){
					resultRequire = res;
				}
			}
		}

		tConditionsBeingEvaluated.remove(condition);

		if(resultAccept != EEvaluationResult.UNDEFINED &&
		   resultAccept.ordinal() < resultRequire.ordinal()) {
			return resultAccept;
		}

		return resultRequire;
	}


	@Override
	public Collection<ICpItem> filterItems(Collection<? extends ICpItem> sourceCollection) {
		Collection<ICpItem> filtered = new LinkedList<ICpItem>();
		if(sourceCollection != null && ! sourceCollection.isEmpty()) {
			for(ICpItem item : sourceCollection) {
				EEvaluationResult res = item.evaluate(this);
				if(res.isFulfilled())
					filtered.add(item);
			}
		}
		return filtered;
	}

}
