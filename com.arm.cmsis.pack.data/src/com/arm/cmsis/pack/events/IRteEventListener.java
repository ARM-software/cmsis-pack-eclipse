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

import com.arm.cmsis.pack.generic.IGenericListener;

/**
 * Listener to handle an RTE event
 */
public interface IRteEventListener extends IGenericListener<RteEvent> {
	// the interface is used for simplicity
	/**
	 * Sets IRteEventProxy to be used by this object to fire notifications
	 * @param rteEventProxy IRteEventProxy object
	 */
	default void setRteEventProxy(IRteEventProxy rteEventProxy) { ;
		if(rteEventProxy != null) {
			rteEventProxy.addListener(this);
		}
	}

	/**
	 * Returns IRteEventProxy object set by setRteEventProxy()
	 * @return IRteEventProxy object or null if none has been set
	 * @see #setRteEventProxy(IRteEventProxy)
	 */
	default IRteEventProxy getRteEventProxy() { return null;}
	
	
	/**
	 * Creates RteEvent and notifies listeners
	 * @param topic event topic
	 * @param data  event data
	 */
	default void emitRteEvent(final String topic, Object data) {
		IRteEventProxy rteEventProxy = getRteEventProxy();
		if(rteEventProxy != null) {
			rteEventProxy.notifyListeners(new RteEvent(topic, data));
		}
	}


	/**
	 * Creates RteEvent and notifies listeners
	 * @param topic event topic
	 */
	default void emitRteEvent(final String topic) {
		emitRteEvent(topic, null);
	}

	
}
