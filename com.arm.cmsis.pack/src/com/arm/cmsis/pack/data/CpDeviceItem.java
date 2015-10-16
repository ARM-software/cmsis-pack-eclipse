/*******************************************************************************
 * Copyright (c) 2015 ARM Ltd and others.
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

import com.arm.cmsis.pack.DeviceVendor;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.enums.EDeviceHierarchyLevel;


/**
 * Default implementation of ICpDeviceItem interface 
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
	public boolean hasDeviceItems() {
		return deviceItems != null && !deviceItems.isEmpty();
	}	

	
	@Override
	public List<ICpDeviceItem> getDeviceItems() {
		return deviceItems;
	}

	@Override
	public void addChild(ICpItem item) {
		cachedChildArray = null; // invalidate
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
	public synchronized Map<String, ICpItem> getProcessors() {
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
	public synchronized ICpItem getEffectiveProperties(String processorName) {
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
	protected String constructName() {
		switch(getLevel()) {
		case DEVICE:
			return getAttribute(CmsisConstants.DNAME);
		case FAMILY:
			return getAttribute(CmsisConstants.DFAMILY);
		case SUBFAMILY:
			return getAttribute(CmsisConstants.DSUBFAMILY);
		case VARIANT:
			return getAttribute(CmsisConstants.DVARIANT);
		default:
			break;
		}
		return super.constructName();
	}


	@Override
	public String constructId() {
		return getName();
	}

	
	@Override
	public boolean hasChildren() {
		return getEffectiveChildCount() > 0;
	}

	@Override
	public boolean hasEffectiveChildren() {
		return getEffectiveChildCount() > 0;
	}

	
	@Override
	public Collection<? extends ICpItem> getEffectiveChildren() {
		// combination of direct properties and device children
		List<ICpItem> effectiveChildren = new LinkedList<ICpItem>();
		if(hasChildren()) {
			effectiveChildren.addAll(getChildren());
		}
		if(hasDeviceItems()) {
			effectiveChildren.addAll(getDeviceItems());
		}
		return effectiveChildren;
	}

	@Override
	public int getEffectiveChildCount() {
		int count = getChildCount(); // count of direct properties
		if(hasDeviceItems())
			count += getDeviceItems().size();
		return count;
	}
	
	
	@Override
	protected Object[] createChildArray() {
		// use cached array for effective children, while direct children are rarely needed in GUI
		Collection<? extends ICpItem> collection = getEffectiveChildren();
		if(collection != null && !collection.isEmpty())
			return collection.toArray();
		return EMPTY_OBJECT_ARRAY;
	}

	@Override
	protected Collection<? extends ICpItem> getItemsToVisit() {
		return getEffectiveChildren();
	}

	@Override
	public synchronized String getUrl() {
		if(fURL == null) {
			fURL = DeviceVendor.getVendorUrl(getVendor());
			if(!fURL.isEmpty()) {
				fURL += '/';
				fURL += DeviceVendor.adjutsToUrl(getName());  
			}
		}
		return fURL;
	}

	@Override
	public String getDoc() {
		//get first book
		String doc = null;
		ICpItem bookItem = getFirstChild(CmsisConstants.BOOK_TAG);
		if(bookItem != null)
			doc = bookItem.getDoc();
		if(doc == null || doc.isEmpty())
			doc = super.getDoc();
		return doc;
	}
}
