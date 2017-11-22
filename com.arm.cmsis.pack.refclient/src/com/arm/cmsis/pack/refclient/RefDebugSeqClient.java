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

package com.arm.cmsis.pack.refclient;

import java.util.List;

import com.arm.cmsis.pack.dsq.DsqException;
import com.arm.cmsis.pack.dsq.IDsqClient;
import com.arm.cmsis.pack.dsq.IDsqCommand;

/**
 * Reference Debug Sequence Executor implementation, rotates command result for each execution
 */
public class RefDebugSeqClient implements IDsqClient {

	private long commandResult = 1;

	@Override
	public void execute(List<IDsqCommand> commands, boolean atomic) throws DsqException {
		for (IDsqCommand cmd : commands) {
			cmd.setOutput(commandResult);
			commandResult = Long.rotateLeft(commandResult, 1);
		}
	}

	@Override
	public long query(long type, String message, long defaultValue) throws DsqException {
		return 1;
	}

}
