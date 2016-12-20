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

package com.arm.cmsis.pack.generic;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Default implementation of IGenericListenerList interface 
 */
public class GenericListenerList<L extends IGenericListener<E>, E> implements IGenericListenerList<L, E> {

	protected Set<L> listeners = Collections.synchronizedSet(new LinkedHashSet<L>()); 

	public GenericListenerList() {
	}

	@Override
	public synchronized void addListener(L listener) {
		if(listener != null && listener != this) //avoid loops
			listeners.add(listener);
	}

	@Override
	public synchronized void removeListener(L listener) {
		listeners.remove(listener);
	}

	@Override
	public synchronized void removeAllListeners() {
		listeners.clear();
	}


	@Override
	public synchronized void notifyListeners(E event) {
		for (Iterator<? extends L> iterator = listeners.iterator(); iterator.hasNext();) {
			L listener = iterator.next();
			try {
				listener.handle(event);
			} catch (Exception ex) {
				ex.printStackTrace();
				iterator.remove();
			}
		}
	}

}
