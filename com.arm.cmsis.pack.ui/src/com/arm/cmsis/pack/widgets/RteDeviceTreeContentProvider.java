/*******************************************************************************
* Copyright (c) 2014 ARM Ltd.
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/

package com.arm.cmsis.pack.widgets;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;
import com.arm.cmsis.pack.ui.CpPlugInUI;

/**
 *
 */
public class RteDeviceTreeContentProvider extends LabelProvider implements ITreeContentProvider  {

	IRteDeviceItem devices = null;
	
	/**
	 * 
	 */
	public RteDeviceTreeContentProvider() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void dispose() {
		devices = null;
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if(oldInput != newInput) {
			if(newInput != null && newInput instanceof IRteDeviceItem)
				devices = (IRteDeviceItem) newInput; 
			else
				devices = null;
		}
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IRteDeviceItem)
			return ((IRteDeviceItem)parentElement).getChildArray();
		return null;
		}

	@Override
	public Object getParent(Object childElement) {
		if (childElement instanceof IRteDeviceItem) {
			return ((IRteDeviceItem)childElement).getParent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof IRteDeviceItem)
			return ((IRteDeviceItem)element).hasChildren();
		return false;
	}

	@Override
	public Image getImage(Object element) {
		if(hasChildren(element))
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
		return CpPlugInUI.getImage(CpPlugInUI.ICON_DEVICE);
	}

	@Override
	public String getText(Object element) {
		if(element instanceof IRteDeviceItem) {
			IRteDeviceItem item = (IRteDeviceItem)element;
			return item.getName();
		}
		return IAttributes.EMPTY_STRING;	
	}
}
