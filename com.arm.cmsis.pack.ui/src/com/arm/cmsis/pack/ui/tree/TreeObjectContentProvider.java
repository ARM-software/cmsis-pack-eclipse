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

package com.arm.cmsis.pack.ui.tree;

import org.eclipse.jface.viewers.ITreeContentProvider;

import com.arm.cmsis.pack.generic.ITreeObject;

/**
 *
 */
public class TreeObjectContentProvider implements ITreeContentProvider {

    ITreeObject getTreeObject(Object element) {
        if (element instanceof ITreeObject)
            return (ITreeObject) element;
        return null;

    }

    @Override
    public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        ITreeObject treeObject = getTreeObject(parentElement);
        if (treeObject != null)
            return treeObject.getChildArray();

        return ITreeObject.EMPTY_OBJECT_ARRAY;
    }

    @Override
    public Object getParent(Object element) {
        ITreeObject treeObject = getTreeObject(element);
        if (treeObject != null)
            return treeObject.getParent();
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        ITreeObject treeObject = getTreeObject(element);
        if (treeObject != null)
            return treeObject.hasChildren();
        return false;
    }

}
