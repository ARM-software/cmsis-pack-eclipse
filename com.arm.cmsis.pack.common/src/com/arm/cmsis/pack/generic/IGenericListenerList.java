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

/**
 * Generic listener list to register IGenericListener listeners and notify them with events
 *
 * @param <L> listener type
 * @param <E> event type
 */
public interface IGenericListenerList<L extends IGenericListener<E>, E> {
	
	/**
	 * Adds listener to the internal collection
	 * @param listener listener to add 
	 */
	void addListener(L listener);
	
	/**
	 * Removes from to the internal collection
	 * @param listener listener to remove 
	 */
	void removeListener(L listener);
	

	/**
	 * Removes all listeners
	 */
	void removeAllListeners();
	
	
	/**
	 * Notifies registered listeners about an event
	 * @param event event to fire
	 */
	void notifyListeners(E event);
	
	
}
