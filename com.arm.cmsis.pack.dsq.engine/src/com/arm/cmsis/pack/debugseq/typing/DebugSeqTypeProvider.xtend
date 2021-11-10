/*******************************************************************************
* Copyright (c) 2021 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.debugseq.typing

import com.arm.cmsis.pack.debugseq.debugSeq.Expression
import com.arm.cmsis.pack.debugseq.debugSeq.VariableRef
import com.arm.cmsis.pack.debugseq.debugSeq.VariableDeclaration
import com.arm.cmsis.pack.debugseq.debugSeq.StringConstant

class DebugSeqTypeProvider {
	public static val stringType = new StringType
	public static val intType = new IntType
	
	def dispatch DebugSeqType typeFor(Expression e) {
		switch (e) {
			StringConstant: stringType
			default: intType
		}
	}
	
	def dispatch DebugSeqType typeFor(VariableDeclaration variable) {
		return variable.value.typeFor
	}
	
	def dispatch DebugSeqType typeFor(VariableRef varRef) {
		if (varRef.variable == null) {
			return null
		} else {
			return varRef.variable.typeFor
		}
	}
	
	def isInt(DebugSeqType type) { type == intType }
	def isString(DebugSeqType type) { type == stringType }
}