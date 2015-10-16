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

package com.arm.cmsis.pack.ui.tree;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.arm.cmsis.pack.generic.ITreeObject;

/**
 * 
 */
public class TreeObjectContentProvider implements ITreeContentProvider {

	ITreeObject getTreeObject(Object element){
		if(element instanceof ITreeObject)
			return (ITreeObject)element;
		return null;
		
	}
	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		ITreeObject treeObject = getTreeObject(parentElement);
		if(treeObject != null)
			return treeObject.getChildArray();
		
		return ITreeObject.EMPTY_OBJECT_ARRAY;
	}

	@Override
	public Object getParent(Object element) {
		ITreeObject treeObject = getTreeObject(element);
		if(treeObject != null)
			return treeObject.getParent();
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		ITreeObject treeObject = getTreeObject(element);
		if(treeObject != null)
			return treeObject.hasChildren();
		return false;
	}

}
