/**
 * Copyright (c) 2021 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 */
package com.arm.cmsis.pack.debugseq.generator;

import com.arm.cmsis.pack.debugseq.debugSeq.DebugSeqModel;
import com.arm.cmsis.pack.dsq.IDsqGeneratorInfo;

@SuppressWarnings("all")
public interface IDsqScriptGenerator extends IDsqGeneratorInfo {
  /**
   * Generate script code for the debug sequence model
   * @param model Debug Sequence Model
   * @param header optional to provide existing code
   * @return generated script code as String
   */
  String generate(final DebugSeqModel model, final String header);
}
