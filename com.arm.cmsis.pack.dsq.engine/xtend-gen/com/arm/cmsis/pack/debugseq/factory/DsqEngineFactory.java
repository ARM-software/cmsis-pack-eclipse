/**
 * Copyright (c) 2021 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 */
package com.arm.cmsis.pack.debugseq.factory;

import com.arm.cmsis.pack.debugseq.engine.DebugSeqEngine;
import com.arm.cmsis.pack.dsq.IDsqClient;
import com.arm.cmsis.pack.dsq.IDsqEngine;
import com.arm.cmsis.pack.dsq.IDsqEngineFactory;
import com.arm.cmsis.pack.dsq.IDsqLogger;
import com.arm.cmsis.pack.info.ICpDeviceInfo;

@SuppressWarnings("all")
public class DsqEngineFactory implements IDsqEngineFactory {
  @Override
  public IDsqEngine create(final ICpDeviceInfo di, final IDsqClient dce, final IDsqLogger log) {
    DebugSeqEngine _xifexpression = null;
    if ((di == null)) {
      return null;
    } else {
      _xifexpression = new DebugSeqEngine(di, dce, log);
    }
    return _xifexpression;
  }
}
