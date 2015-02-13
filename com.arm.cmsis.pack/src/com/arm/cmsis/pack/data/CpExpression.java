/*******************************************************************************
 * Copyright (c) 2014 ARM Ltd and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/

package com.arm.cmsis.pack.data;

import com.arm.cmsis.pack.enums.EEvaluationResult;

/**
 *
 */
public class CpExpression extends CpItem implements ICpExpression {

	protected char expressionType = 0;  // 'D' Device), 
										// 'C' Component), 
									    // 'T' Toolchain), 
										// 'R' Reference to condition
										// 'E' Error (mixed attribute types)
										// 'U' Unknown
	
	/**
	 * Hierarchical constructor
	 * @param parent parent ICpItem
	 * @param tag item's tag
	 */
	public CpExpression(ICpItem parent, String tag) {
		super(parent, tag);
	}

	@Override
	public char getExpressionType() {
		return getTag().charAt(0);
	}

	@Override
	public String constructId() {
		return getTag() + " " + attributes().getAttributesAsString();
	}

	@Override
	public char getExpressionDomain() {
		if(expressionType == 0) {
			if(hasCondition())
				expressionType = 'R';
			else if(attributes().containsAttribute("C*"))
				expressionType = 'C';
			else if(attributes().containsAttribute("D*"))
				expressionType = 'D';
			else if(attributes().containsAttribute("T*"))
				expressionType = 'T';
			else 
				expressionType = 'U';
		}
		return expressionType;
	}

	@Override
	public EEvaluationResult evaluate(ICpConditionContext context) {
		return context.evaluateExpression(this);
	}

	
	@Override
	public String getName() {
		return getId();
	}

	
	@Override
	public int hashCode() {
		if(getExpressionDomain() != REFERENCE_EXPRESSION) {
			return getId().hashCode();
		}
		return super.hashCode();
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if(super.equals(obj))
			return true;
		if(obj instanceof ICpExpression) {
			ICpExpression expr = (ICpExpression)obj;
			if(expr.getId().equals(getId()))
				return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return getId();
	}
	
}
