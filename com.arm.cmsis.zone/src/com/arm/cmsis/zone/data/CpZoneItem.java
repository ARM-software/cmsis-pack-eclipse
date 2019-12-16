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

package com.arm.cmsis.zone.data;

import java.util.Map;
import java.util.Map.Entry;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.error.CmsisError;
import com.arm.cmsis.pack.generic.Attributes;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.permissions.IMemoryAccess;
import com.arm.cmsis.pack.permissions.IMemoryPermissions;
import com.arm.cmsis.pack.permissions.IMemoryPriviledge;

/* 
 * Base class for CMSIS Zone elements 
 */
public class CpZoneItem extends CpItem implements ICpZoneItem {
	

	public CpZoneItem(ICpItem parent) {
		super(parent);
	}

	public CpZoneItem(ICpItem parent, String tag) {
		super(parent, tag);
	}
	
	@Override
	protected ICpItem createChildItem(String tag) {
		return new CpZoneItem(this, tag);
	}

	@Override
	protected String constructName() {
		if(hasAttribute(CmsisConstants.NAME)) {
			return getAttribute(CmsisConstants.NAME);
		}
		return getTag();
	}
	
	@Override
	public String getDescription() {
		CmsisError error = getFirstError();
		if(error != null) {
			return error.getFormattedMessage();
		}
		return getInfo();
	}
	
	
	@Override
	public synchronized String getUrl() {
		return null;
	}

	@Override
	public void setName(String name) {
		fName = name;
		setAttribute(CmsisConstants.NAME,  name);
	}
	
	@Override
	public String getItemKey(ICpItem item) {
		String key = null;
		if(item != null) {
			key = item.getName();
			if(key == null || key.isEmpty())
				key = getTag();
		}
		return key;
	}
	
	@Override
	public ICpItem toFtlModel(ICpItem ftlParent, String key, String value) {
		switch(key) {
		case CmsisConstants.ACCESS:
			return createFtlFlagsElement(ftlParent, key, value, IMemoryPermissions.ACCESS_FLAGS);
		case CmsisConstants.SECURITY:
			return createFtlFlagsElement(ftlParent, key, value, IMemoryPermissions.SECURITY_FLAGS);
		case CmsisConstants.PRIVILEGE:
			return createFtlFlagsElement(ftlParent, key, value, IMemoryPriviledge.PRIVILEGE_FLAGS);
			default:
				break;
		}
		
		ICpItem item = new CpItem(ftlParent, key);
		item.setText(value);
		return item;
	}
	
	protected ICpItem createFtlFlagsElement(ICpItem ftlParent, String key, String value, String flags) {
		ICpItem flagsItem = new CpItem(ftlParent, key);
		for(int i = 0 ; i < flags.length(); i++ ){
			char ch = flags.charAt(i);
			String k = CmsisConstants.EMPTY_STRING + ch; 
			String v = IMemoryAccess.isAccessSet(ch, value) ? CmsisConstants.ONE : CmsisConstants.ZERO;
			ICpItem a = new CpItem(flagsItem, k);
			a.setText(v);
			flagsItem.addChild(a);
		}
		return flagsItem;
	}
	
	/**
	 * Return attributes that need to expanded as child items.  
	 * @return the attributes as IAttributes
	 */
	protected IAttributes getAttributesForFtlModel() {
		// base implementation returns copy of all attributes
		return new Attributes(attributes());
	}

	@Override
	public ICpItem toFtlModel(ICpItem ftlParent) {
		String tag = getTag();
		if(tag == null || tag.isEmpty())
			return null; // no element without tag
		ICpItem item = new CpItem(ftlParent, tag);
		// add text if no children or attributes 
		String text = getText();
		if(text != null && !text.isEmpty()) {
			if(hasChildren() || attributes().hasAttributes()){
				// cannot add text to item directly , create a separate "text" one
				ICpItem t = new CpItem(item, CmsisConstants.TEXT);
				t.setText(text);
				item.addChild(t);
			} else{
				item.setText(text);
			}
		}
		// expand attributes
		if(attributes().hasAttributes()) {
			Map<String, String> attributesMap = getAttributesForFtlModel().getAttributesAsMap();
			if(attributesMap != null) {
				for(Entry<String, String> e: attributesMap.entrySet()) {
					String key = e.getKey();
					String val = e.getValue();
					if(key.indexOf(CmsisConstants.COLON) >=0 ) {
						// retain attributes with namespaces , e.g. xs:xmnls
						item.setAttribute(key, val);
					} else {
						ICpItem a = toFtlModel(item,  e.getKey(),  e.getValue());
						item.addChild(a);
					}
				}
			}
		}
		return item;
	}

	@Override
	public void addError(CmsisError error) {
		if(error == null ) {
			return;
		}
		
		if(error.getItem() == null) {
			error.setItem(this);
		}
		String file = error.getFile();
		if(file == null || file.isEmpty()) {
			error.setFile(getRootFileName());
		}
		super.addError(error);
	}

	@Override
	public boolean isValid() {
		if(getSevereErrorCount() > 0 ) {
			return false;
		}
		return super.isValid();
	}
	
}
