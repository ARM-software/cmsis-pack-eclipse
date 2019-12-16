/*******************************************************************************
 * Copyright (c) 2017 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/
package com.arm.cmsis.zone.widgets;

import java.util.LinkedList;
import java.util.List;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.generic.ITreeObject;
import com.arm.cmsis.pack.ui.tree.TreeObjectContentProvider;
import com.arm.cmsis.zone.data.ICpDeviceUnit;
import com.arm.cmsis.zone.data.ICpMemoryBlock;
import com.arm.cmsis.zone.data.ICpProcessorUnit;
import com.arm.cmsis.zone.data.ICpResourceContainer;
import com.arm.cmsis.zone.data.ICpResourceGroup;
import com.arm.cmsis.zone.data.ICpResourceItem;
import com.arm.cmsis.zone.data.ICpRootZone;


/**
 * Content provider for CmsisZontTreeWidget
 */
public class CmsisZoneContentProvider extends TreeObjectContentProvider {
	protected CmsisZoneTreeWidget fTreeWidget;
	
	public CmsisZoneContentProvider(CmsisZoneTreeWidget treeWidget) {
		super();
		fTreeWidget = treeWidget;
	}
	
	protected boolean isShowList() {
		return fTreeWidget.isShowList();
	}

	protected ICpDeviceUnit getTargetDevice() {
		return fTreeWidget.getTargetDevice();
	}
	protected ICpProcessorUnit getTargetProcessor() {
		return fTreeWidget.getTargetProcessor();
	}

	
	@Override
	public Object[] getChildren(Object parentElement) {
		if(fTreeWidget == null)
			return null;
	
		ICpResourceItem item = CmsisZoneColumnAdvisor.getResourceItem(parentElement);
		if(item instanceof ICpRootZone) {
			ICpRootZone root = (ICpRootZone)item;
			
			List<ICpItem> rootChildren = new LinkedList<>();
			ICpDeviceUnit du = root.getDeviceUnit();
			if(du != null)
				rootChildren.add(du);
			ICpResourceContainer resources = root.getResources();
			if(resources != null) {
				if(isShowList()) {
					rootChildren.add(resources);
				} else {//It' s a tree
					ICpResourceGroup memories = resources.getMemoryGroup();
					if(memories != null && memories.hasChildren())
						rootChildren.add(memories);
					boolean bShowPeripheral = root.getZoneOption(CmsisConstants.PERIPHERAL, CmsisConstants.SHOW);
					if(bShowPeripheral) {
						ICpResourceGroup peripherals = resources.getPeripheralsGroup();
						if(peripherals != null && peripherals.hasChildren())
							rootChildren.add(peripherals);
					}
				}
			}
			return rootChildren.toArray();
		} 
		if(item instanceof ICpResourceGroup) {
			return item.getEffectiveChildArray();
		}
		if(!isShowList()) {
			ICpMemoryBlock block = CmsisZoneColumnAdvisor.getMemoryBlock(item);
			if(block != null && block.hasSubBlocks()){
				ICpRootZone rootZone = block.getRootZone();
				if((block.isROM() && rootZone.getZoneOption(CmsisConstants.ROM, CmsisConstants.SHOW)) || 
				   (block.isRAM() && rootZone.getZoneOption(CmsisConstants.RAM, CmsisConstants.SHOW)) || 
				   (block.isPeripheral() && rootZone.getZoneOption(CmsisConstants.PERIPHERAL, CmsisConstants.SHOW))) {
					return block.getSubBlocks().toArray();
				}
			}
		}
		return ITreeObject.EMPTY_OBJECT_ARRAY;
	}

	@Override
	public Object getParent(Object element) {
		ICpResourceItem item = CmsisZoneColumnAdvisor.getResourceItem(element);
		if(item == null)
			return null;
		if(item instanceof ICpDeviceUnit) {
			return item.getRootZone();
		}
		if(isShowList()) {
			return item.getRootZone().getResources();
		}
		ICpItem parent = item.getParent();
		if(parent instanceof ICpResourceContainer) {
			item.getRootZone();
		} 
		return parent;
	}

	@Override
	public boolean hasChildren(Object element) {
		ICpResourceItem item = CmsisZoneColumnAdvisor.getResourceItem(element);
		if(item instanceof ICpResourceGroup) {
			return item.hasChildren();
		} 
		if(!isShowList()) {
			ICpMemoryBlock block = CmsisZoneColumnAdvisor.getMemoryBlock(item);
			if(block != null){
				return block.hasSubBlocks();
			}
		}
		return false;
	}
}
