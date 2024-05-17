/*******************************************************************************
* Copyright (c) 2021 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.generic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Default implementation of IGenericListenerList interface
 */
public class GenericListenerList<L extends IGenericListener<E>, E> implements IGenericListenerList<L, E> {

    protected Set<L> listeners;

    public GenericListenerList() {
        listeners = Collections.synchronizedSet(new LinkedHashSet<L>());
    }

    @Override
    public synchronized void addListener(L listener) {
        if (listener == null || listener == this) // avoid loops
            return;
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
        // make a copy to avoid add/remove conflicts
        List<L> workingList = new ArrayList<>(listeners);
        for (Iterator<? extends L> iterator = workingList.iterator(); iterator.hasNext();) {
            if (listeners.isEmpty())
                return;
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
