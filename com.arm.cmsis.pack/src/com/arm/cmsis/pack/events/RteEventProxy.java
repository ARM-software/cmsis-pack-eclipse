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

package com.arm.cmsis.pack.events;

import com.arm.cmsis.pack.generic.GenericListenerList;

/**
 * Default implementation of IRteEventProxy interface
 */
public class RteEventProxy extends GenericListenerList<IRteEventListener, RteEvent> implements IRteEventProxy {

	@Override
	public void emitRteEvent(final String topic, Object data) {
		notifyListeners(new RteEvent(topic, data));
	}
	
}
