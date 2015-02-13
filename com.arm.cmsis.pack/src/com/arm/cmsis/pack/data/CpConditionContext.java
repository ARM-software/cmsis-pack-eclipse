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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.generic.Attributes;

/**
 *  Default implementation of ICpConditionContext interface
 */
public class CpConditionContext extends Attributes implements ICpConditionContext {

	protected EEvaluationResult fResult = EEvaluationResult.IGNORED;
	protected Map<ICpItem, EEvaluationResult> fResults = null;
	
	// temporary variables
	protected EEvaluationResult tResultAccept = EEvaluationResult.UNDEFINED; // keeps last (the best) accept result
	protected boolean tbDeny = false; // flag is set when deny expression is evaluated  
	
	
	/**
	 * 
	 */
	public CpConditionContext() {
	}

	/**
	 * @param tag
	 */
	public CpConditionContext(String tag) {
		super(tag);
	}

	
	
	@Override
	public void resetResult() {
		fResult = EEvaluationResult.IGNORED;
		fResults = null;
	}

	@Override
	public EEvaluationResult getEvaluationResult() {
		return fResult;
	}

	
	@Override
	public void setEvaluationResult(EEvaluationResult result) {
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
		EEvaluationResult res = EEvaluationResult.UNDEFINED;
		if(fResults == null)
			fResults = new HashMap<ICpItem, EEvaluationResult>();

		res = getCachedResult(item);
		if(res == null || res == EEvaluationResult.UNDEFINED) {
			res = item.evaluate(this);
			putCachedResult(item, res);
		}
		return res;
	}

	
	 /**
	 * Retrieves cached result for the given item if already in cache
	 * @param item ICpItem for which to retrieve result
	 * @return cached result or null if not yet in cache
	 */
	protected EEvaluationResult getCachedResult(ICpItem item) {
		 if(fResults != null)
			 return fResults.get(item);
		 return null;
	 }

	 /**
	 * Puts evaluation result into cache
	 * @param item ICpItem for which to put the result
	 * @param res result value to cache
	 */
	protected void putCachedResult(ICpItem item, EEvaluationResult res) {
		 if(fResults == null)
			 fResults = new HashMap<ICpItem, EEvaluationResult>();
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
		tResultAccept = resultAccept; 
		
		if(resultAccept != EEvaluationResult.UNDEFINED && 
		   resultAccept.ordinal() < resultRequire.ordinal()) {  
			return resultAccept;
		}
		return resultRequire;
	}
	
}
