/*******************************************************************************
 * Copyright (c) 2014 ARM Ltd and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/

package com.arm.cmsis.pack.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.arm.cmsis.pack.base.CmsisConstants;
import com.arm.cmsis.pack.enums.EDeviceHierarchyLevel;
import com.arm.cmsis.pack.generic.IAttributes;


/**
 *
 */
public class CpDeviceItem extends CpDeviceItemContainer implements ICpDeviceItem {

	private EDeviceHierarchyLevel level = null;
	protected List<ICpDeviceItem> deviceItems = null;
	protected Map<String, ICpItem > effectiveProperties = null;
	/**
	 * Effective processors (might be defined at family or sub-family level)
	 */
	protected Map<String, ICpItem> processors = null;

	
	/**
	 * @param parent
	 * @param tag
	 */
	public CpDeviceItem(ICpItem parent, String tag) {
		super(parent, tag);
	}

	@Override
	public ICpDeviceItem getDeviceItemParent() {
		ICpItem parent = getParent();
		if (parent != null && parent instanceof ICpDeviceItem)
			return (ICpDeviceItem) parent;
		return null;
	}

	@Override
	public List<ICpDeviceItem> getDeviceItems() {
		return deviceItems;
	}

	@Override
	public void addChild(ICpItem item) {
		if(item instanceof ICpDeviceItem) {
			if(deviceItems == null) 
				deviceItems = new LinkedList<ICpDeviceItem>();
			deviceItems.add((ICpDeviceItem) item);
		} else { // property
			List<ICpItem> children = children();
			String pname = item.getProcessorName(); 
			if(pname != null && !pname.isEmpty())
				children.add(0, item);  // add properties with Pname attribute always to front ( makes collecting effective properties easier)
			else 
				children.add(item);
		}
	}

	@Override
	public EDeviceHierarchyLevel getLevel() {
		if (level == null) {
			level = EDeviceHierarchyLevel.fromString(getTag());
		}
		return level;
	}

	
	@Override
	public ICpItem getEffectiveParent() {
		ICpItem parent = getParent();
		if(parent != null && parent instanceof ICpDeviceItem)
			return parent;
		return null;	
	}

	@Override
	public int getProcessorCount() {
		return getProcessors().size();
	}
	
	@Override
	public Map<String, ICpItem> getProcessors() {
		if(processors == null) {
			processors = new HashMap<String, ICpItem>();
			for(ICpDeviceItem deviceItem = this; deviceItem != null; deviceItem = deviceItem.getDeviceItemParent()){	 
				Collection<? extends ICpItem> children = deviceItem.getChildren();
				if(children == null)
					continue;
				for(ICpItem item : children) {
					if(item.getTag().equals(CmsisConstants.PROCESSOR_TAG)) {
						ICpDeviceProperty p = (ICpDeviceProperty)item;
						String pname = p.getProcessorName();
						
						ICpItem inserted = processors.get(pname);
						if(inserted == null) {
							processors.put(pname,  p);
						} else {
							// add missing attributes, but do not replace existing ones (we go down-up)
							inserted.mergeEffectiveContent(p);
						}
					}
				}
			}
		}
		return processors;
	}

	@Override
	public ICpItem getEffectiveProperties(String processorName) {
		if(effectiveProperties == null) {
			// ensure filled processor collection
			getProcessors();
			// collect properties for all processors
			effectiveProperties = new HashMap<String, ICpItem >();
			for(Entry<String, ICpItem> e : processors.entrySet()) {
				String pname = e.getKey();
				ICpItem props = new CpItem(this);
				// add processor attributes to the properties
				ICpItem pItem = e.getValue();
				props.attributes().setAttributes(pItem.attributes().getAttributesAsMap());
				effectiveProperties.put(pname, props);
				// directly insert processor property since it is already collected
				props.addChild(e.getValue());
				// add other properties
				collectEffectiveProperties(pname, props);
			}
		}
		return effectiveProperties.get(processorName);
	}
	
	protected void collectEffectiveProperties(String pname, ICpItem props) {
		// insert properties starting from this item and going up in parent chain 
		for(ICpDeviceItem deviceItem = this; deviceItem != null; deviceItem = deviceItem.getDeviceItemParent()){
			props.attributes().mergeAttributes(deviceItem.attributes());
			Collection<? extends ICpItem> children = deviceItem.getChildren();
			if(children == null)
				continue;
			for(ICpItem p : children) {
				String itemPname = p.getProcessorName();
				if(pname.isEmpty() || itemPname.isEmpty() || itemPname.equals(pname)) {
					props.mergeProperty(p);
				}			
			}
		}
	}


	@Override
	public String getName() {
		switch(getLevel()) {
		case DEVICE:
			return attributes().getAttribute("Dname",IAttributes.EMPTY_STRING);
		case FAMILY:
			return attributes().getAttribute("Dfamily",IAttributes.EMPTY_STRING);
		case SUBFAMILY:
			return attributes().getAttribute("DsubFamily",IAttributes.EMPTY_STRING);
		case VARIANT:
			return attributes().getAttribute("Dvariant", IAttributes.EMPTY_STRING);
		default:
			break;
		}
		return super.getName();
	}


	@Override
	public String constructId() {
		return getName();
	}
}
