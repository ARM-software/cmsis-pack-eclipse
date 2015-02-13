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

import com.arm.cmsis.pack.enums.EEvaluationResult;

/**
 *
 */
public class CpCondition extends CpItem implements ICpCondition {

	private boolean bInCheck = false;   // flag to prevent recursion  
	/**
	 * @param parent
	 */
	public CpCondition(ICpItem parent) {
		super(parent);
	}

	/**
	 * @param parent
	 * @param tag
	 */
	public CpCondition(ICpItem parent, String tag) {
		super(parent, tag);
	}

	@Override
	protected ICpItem createChildItem(String tag) {
		ICpItem child = null;
		switch(tag){
		case "accept":
		case "require": 
			child = new CpExpression(this, tag);
			break;
		case "deny":
			child = new CpDenyExpresion(this, tag);
			break;
		default:
			child = super.createChildItem(tag);
		}
		return child;
	}

	@Override
	public EEvaluationResult evaluate(ICpConditionContext context) {
		EEvaluationResult result = EEvaluationResult.UNDEFINED;
		if(bInCheck) {
			result = EEvaluationResult.ERROR; // recursion error
		} else{
			bInCheck = true;
			try{
				result = context.evaluateCondition(this);
			} catch (NullPointerException e) {
				e.printStackTrace();
				result = EEvaluationResult.ERROR; // null pointer exception
			} finally {
				bInCheck = false;
			}
		}
		return result;		
	}

}
