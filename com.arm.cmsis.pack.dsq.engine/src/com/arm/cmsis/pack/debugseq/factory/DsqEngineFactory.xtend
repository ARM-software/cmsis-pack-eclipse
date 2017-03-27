/*******************************************************************************
* Copyright (c) 2016 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.debugseq.factory

import com.arm.cmsis.pack.dsq.IDsqEngineFactory
import com.arm.cmsis.pack.info.ICpDeviceInfo
import com.arm.cmsis.pack.dsq.IDsqLogger
import com.arm.cmsis.pack.debugseq.engine.DebugSeqEngine
import com.arm.cmsis.pack.dsq.IDsqClient

/**
 * 
 */
class DsqEngineFactory implements IDsqEngineFactory {
	
	override create(ICpDeviceInfo di, IDsqClient dce, IDsqLogger log) {
		if (di === null)
			return null
		else
			new DebugSeqEngine(di, dce, log)
	}
	
}