/**
 * Copyright (c) 2016 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 */
package com.arm.cmsis.pack.debugseq.typing;

import com.arm.cmsis.pack.debugseq.debugSeq.Expression;
import com.arm.cmsis.pack.debugseq.debugSeq.Statement;
import com.arm.cmsis.pack.debugseq.debugSeq.StringConstant;
import com.arm.cmsis.pack.debugseq.debugSeq.VariableDeclaration;
import com.arm.cmsis.pack.debugseq.debugSeq.VariableRef;
import com.arm.cmsis.pack.debugseq.typing.DebugSeqType;
import com.arm.cmsis.pack.debugseq.typing.IntType;
import com.arm.cmsis.pack.debugseq.typing.StringType;
import com.google.common.base.Objects;
import java.util.Arrays;

@SuppressWarnings("all")
public class DebugSeqTypeProvider {
  public static final StringType stringType = new StringType();
  
  public static final IntType intType = new IntType();
  
  protected DebugSeqType _typeFor(final Expression e) {
    DebugSeqType _switchResult = null;
    boolean _matched = false;
    if (e instanceof StringConstant) {
      _matched=true;
      _switchResult = DebugSeqTypeProvider.stringType;
    }
    if (!_matched) {
      _switchResult = DebugSeqTypeProvider.intType;
    }
    return _switchResult;
  }
  
  protected DebugSeqType _typeFor(final VariableDeclaration variable) {
    return this.typeFor(variable.getValue());
  }
  
  protected DebugSeqType _typeFor(final VariableRef varRef) {
    VariableDeclaration _variable = varRef.getVariable();
    boolean _equals = Objects.equal(_variable, null);
    if (_equals) {
      return null;
    } else {
      return this.typeFor(varRef.getVariable());
    }
  }
  
  public boolean isInt(final DebugSeqType type) {
    return Objects.equal(type, DebugSeqTypeProvider.intType);
  }
  
  public boolean isString(final DebugSeqType type) {
    return Objects.equal(type, DebugSeqTypeProvider.stringType);
  }
  
  public DebugSeqType typeFor(final Statement varRef) {
    if (varRef instanceof VariableRef) {
      return _typeFor((VariableRef)varRef);
    } else if (varRef instanceof Expression) {
      return _typeFor((Expression)varRef);
    } else if (varRef instanceof VariableDeclaration) {
      return _typeFor((VariableDeclaration)varRef);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(varRef).toString());
    }
  }
}
