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

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.enums.EEvaluationResult;

/**
 *
 */
public class CpCondition extends CpItem implements ICpCondition {

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
		case CmsisConstants.ACCEPT:
		case CmsisConstants.REQUIRE: 
			child = new CpExpression(this, tag);
			break;
		case CmsisConstants.DENY:
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
		try{
			result = context.evaluateCondition(this);
		} catch (NullPointerException e) {
			e.printStackTrace();
			result = EEvaluationResult.ERROR; // null pointer exception
		}
		return result;		
	}

}
