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

import com.arm.cmsis.pack.debugseq.typing.DebugSeqType;

@SuppressWarnings("all")
public class IntType implements DebugSeqType {
  @Override
  public String toString() {
    return "int";
  }
}
