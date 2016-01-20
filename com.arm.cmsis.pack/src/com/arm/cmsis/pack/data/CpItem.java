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

package com.arm.cmsis.pack.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.enums.EVersionMatchMode;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.item.CmsisTreeItem;
import com.arm.cmsis.pack.utils.AlnumComparator;

/**
 * Default implementation of ICpItem interface
 */
public class CpItem extends CmsisTreeItem<ICpItem> implements ICpItem {
	
	protected IAttributes fAttributes = new CpAttributes(); 
	
	private ICpItem fCondition = null; // cached condition reference for quick access

	protected String fURL = null;
	protected String fId   = null;

	
	/**
	 * Default hierarchy constructor
	 * @param parent parent ICpItem
	 */
	public CpItem(ICpItem parent){
		super(parent);
	}

	/**
	 * Constructs item for the given parent and tag
	 * @param parent parent ICpItem 
	 * @param tag item's tag
	 */
	public CpItem(ICpItem parent, String tag) {
		super(parent);
		setTag(tag);
	}
	
	
	
	@Override
	public IAttributes attributes() {
		return fAttributes;
	}

	@Override
	public String getAttribute(String key) {
		 // Proxy method to get attribute from attributes 
		return attributes().getAttribute(key, CmsisConstants.EMPTY_STRING);
	}

	@Override	
	public boolean hasAttribute(String key) {
		return attributes().hasAttribute(key);
	}

	
	@Override
	public synchronized String getId() {
		if (fId == null) {
			fId = constructId();
		}
		return fId;
	}

	@Override
	protected String constructName() {
		if(hasAttribute(CmsisConstants.NAME)) {
			return getAttribute(CmsisConstants.NAME);
		}
		return super.constructName();
	}

	/**
	 * Constructs element Id
	 * default implementation tries to get "id" attribute
	 * if it is not successful, returns <code>"tag.name"<\code>
	 * derived classes may implement other algorithms 
	 * @return constructed element Id
	 */
	public String constructId() {
		if(hasAttribute(CmsisConstants.ID)) {
			return getAttribute(CmsisConstants.ID);
		}
		// if not successful, returns "tag.name" 
		String id = getTag();
		if(hasAttribute(CmsisConstants.NAME)) {
			id += ":" + getAttribute(CmsisConstants.NAME); //$NON-NLS-1$
		}
		return id;
	}


	@Override
	public ICpItem getParent(String tag) {
		for(ICpItem item = getParent(); item != null; item = item.getParent()) {
			if(item.getTag().equals(tag)) {
				return item;
			}
		}
		return null;
	}
	
	
	
	@Override
	public ICpComponent getParentComponent() {
		if(getParent() != null) {
			return getParent().getParentComponent();
		}
		return null;
	}

	@Override
	public ICpPack getPack() {
		if(getParent() != null) {
			return getParent().getPack();
		}
		return null;
	}
	
	@Override
	public String getPackId() {
		ICpPack pack = getPack();
		if(pack != null) {
			return pack.getId();
		}
		return CmsisConstants.EMPTY_STRING;
	}

	@Override
	public String getPackFamilyId() {
		ICpPack pack = getPack();
		if(pack != null) {
			return pack.getPackFamilyId();
		}
		return CmsisConstants.EMPTY_STRING;
	}


	@Override
	public String getItemKey(ICpItem item) {
		if(item != null) {
			return item.getTag();
		}
		return null;
	}

	@Override
	public ICpItem getProperty(String id) {
		Collection<? extends ICpItem> children = getChildren();
		if(children != null) {
			for(ICpItem item : children){
				if(item.getId().equals(id)) {
					return item;
				}
			}
		}
		return null;
	}

	@Override
	public Collection<? extends ICpItem> getGrandChildren(String tag) {
		ICpItem child = getFirstChild(tag);
		if(child != null) {
			return child.getChildren();
		}
		return null;
	}

	
	@Override
	public Collection<ICpItem> getChildren(String tag) {
		List<ICpItem> tagChildren = new LinkedList<ICpItem>();
		Collection<? extends ICpItem> children = getChildren();
		if(children != null) {
			for(ICpItem item: children) {
				if(item.getTag().equals(tag)) {
					tagChildren.add(item);
				}
			}
		}
		return tagChildren;
	}

	@Override
	protected List<ICpItem> children() {
		return (List<ICpItem>)super.children();
	}
	
	@Override
	public ICpItem createItem(ICpItem parent, String tag) {
		ICpItem item = createChildItem(tag);
		return item;
	}


	/**
	 * Creates an item depending on its tag
	 * @param tag child's XML tag   
	 * @return
	 */
	protected ICpItem createChildItem(String tag) {
		switch(tag) {
		case CmsisConstants.CONDITION:
			return new CpCondition(this, tag);
		case CmsisConstants.API_TAG:
		case CmsisConstants.COMPONENT_TAG:
			return new CpComponent(this, tag);
		case CmsisConstants.BOARD_TAG:
			return new CpBoard(this, tag);
		case CmsisConstants.FILE_TAG:
			return new CpFile(this, tag);
		case CmsisConstants.DEVICES_TAG:
			return new CpDeviceItemContainer(this, tag);
		case CmsisConstants.TAXONOMY_TAG:
			return new CpTaxonomyContainer(this, tag);
		case CmsisConstants.EXAMPLE_TAG:
			return new CpExample(this, tag);
		case CmsisConstants.BUNDLE_TAG:
			return new CpItem(this, tag);
		default:
			break;
		}
		return new CpItem(this, tag);
	}

	@Override
	public boolean hasCondition() {
		return hasAttribute(CmsisConstants.CONDITION);
	}

	
	@Override
	public String getConditionId() {
		return getAttribute(CmsisConstants.CONDITION);
	}


	@Override
	public synchronized ICpItem getCondition() {
		if(fCondition == null) { // not cached yet
			String conditionID = getConditionId();
			if(conditionID != null && !conditionID.isEmpty()) {
				ICpPack pack = getPack();
				if(pack != null) {
					fCondition = pack.getCondition(conditionID);
				}
			}
		}
		return fCondition;
	}

	@Override
	public boolean isDeviceDependent() {
		if(attributes().getAttributeAsBoolean(CmsisConstants.DEVICE_DEPENDENT, false)) {
			return true;
		}

		ICpItem condition = getCondition();
		if(condition != null) {
			return condition.isDeviceDependent();
		}
		return false; 
	}

	
	@Override
	public EEvaluationResult evaluate(ICpConditionContext context) {
		ICpItem condition = getCondition();
		if(condition != null) {
			return context.evaluate(condition);
		}
		return EEvaluationResult.IGNORED;
	}

	@Override
	public String getEffectiveAttribute(final String key) {
		if(hasAttribute(key)) {
			return getAttribute(key);
		}
		ICpItem effectiveParent = getEffectiveParent(); 
		if(effectiveParent != null) {
			return effectiveParent.getEffectiveAttribute(key);
		}
		return null;
	}

	@Override
	public Map<String, String> getEffectiveAttributes(Map<String, String> m) {
		if(m == null) {
			m = new HashMap<String, String>();
		}
		
		Map<String, String> attributesMap = attributes().getAttributesAsMap();
		if(attributesMap != null) {
			for(Entry<String, String> e: attributesMap.entrySet()) {
				String key = e.getKey();
				if(!m.containsKey(key)) {
					m.put(key, e.getValue());
				}
			}
		}
		ICpItem effectiveParent = getEffectiveParent(); 
		if(effectiveParent != null) {
			effectiveParent.getEffectiveAttributes(m);
			return m;
		}
		return m;
	}

	@Override
	public void mergeProperty(ICpItem p, String processorName) {
		String id = p.getId();
		ICpItem inserted = getProperty(id);
		if(inserted == null || !p.isUnique()) {
			addChild(p); // insert only unique properties or descriptions
			if(p.providesEffectiveContent()) {
				p.mergeEffectiveContent(p, processorName);
			}
		} else if (inserted  != null) {
			// add missing attributes, but do not replace existing ones (we go down-up)
			// process sub-properties as well
			inserted.mergeEffectiveContent(p, processorName);
		}
	}
	
	/**
	 * Searches supplied list for a property with given ID
	 * @param id property id to find
	 * @param props list of properties
	 * @return device property if found, null otherwise
	 */
	 public static ICpItem getItemFromList(String id, List<ICpItem> props) {
        for(ICpItem p : props){
        	if(p.getId().equals(id)) {
				return p;
			}
        }
		return null;
	}

	
	@Override
	public void mergeEffectiveContent(ICpItem property, String processorName) {
		attributes().mergeAttributes(property.attributes()); // always merge attributes
	}

	@Override
	public boolean providesEffectiveContent() {
		return false; // default returns false
	}

	@Override
	public boolean isUnique() {
		return false; // default is false
	}

	@Override
	public ICpItem getEffectiveContent() {
		return null;
	}

	@Override
	public String getVendor() {
		String vendor = null;
		if(hasAttribute(CmsisConstants.DVENDOR)) {
			vendor = getAttribute(CmsisConstants.DVENDOR);
		} else if(hasAttribute(CmsisConstants.VENDOR)) {
			return getAttribute(CmsisConstants.VENDOR);
		}	
		if(vendor != null && !vendor.isEmpty()) {
			return vendor;
		}
		ICpItem parent = getParent();
		if(parent != null) {
			return parent.getVendor();
		}
		return null;
	}

	@Override
	public String getVersion() {
		if(hasAttribute(CmsisConstants.VERSION)) {
			return getAttribute(CmsisConstants.VERSION);
		}
		ICpItem parent = getParent();
		if(parent != null) {
			return parent.getVersion();
		}
		return null;
	}

	@Override
	public String getDescription() {
		ICpItem descr = getFirstChild(CmsisConstants.DESCRIPTION);
		if(descr != null) {
			return descr.getText();
		}
		return CmsisConstants.EMPTY_STRING;
	}

	@Override
	public synchronized String getUrl() {
		if(fURL == null) {
			String url = getAttribute(CmsisConstants.URL);
			if(url == null || url.isEmpty()) {
				ICpItem urlItem = getFirstChild(CmsisConstants.URL);
				if(urlItem != null) {
					url = urlItem.getText();
				}
			}
			if(url == null || url.isEmpty()) {
				fURL = getDoc();				
			} else {   
				fURL = getAbsolutePath(url);
			}
		}
		return fURL;
	}

	@Override
	public String getAbsolutePath(String relPath) {
		if(relPath == null || relPath.isEmpty()) {
			return CmsisConstants.EMPTY_STRING;
		}
			
		if(relPath.startsWith("\\\\") || relPath.indexOf(":") == 1) { // Windows only: share or absolute with drive letter  //$NON-NLS-1$ //$NON-NLS-2$
			return relPath; // already absolute (windows)
		}
		// check if path is already absolute or is an URL
		//   absolute                  url without http:         // http: or https: or file:    
		if (relPath.startsWith("//")  || relPath.startsWith("www.") || relPath.indexOf(':') != -1 ) {   //$NON-NLS-1$ //$NON-NLS-2$
			return relPath;  // an URL => already absolute
		}
		// relative path => add pack installation directory
		ICpPack pack = getPack();
		if(pack != null) {
			String packPath = pack.getInstallDir(null);
			if(packPath != null) {
				String absPath = packPath + relPath; 
				IPath path = new Path(absPath);
				return path.toString(); 
			}
		}
		return relPath;
	}
	
	@Override
	public String getDoc() {
		String doc = getAttribute(CmsisConstants.DOC);
		if(doc == null || doc.isEmpty()) {
			doc = getAttribute(CmsisConstants.NAME);
		}
		if(doc == null || doc.isEmpty()) {
			ICpItem docItem = getFirstChild(CmsisConstants.DOC);
			if(docItem != null) {
				doc = docItem.getText();
			}
		}
		return getAbsolutePath(doc);
	}

	@Override
	public String getProcessorName() {
		return getProcessorName(attributes());
	}

	/**
	 * Returns "Pname" attribute of the element representing device property 
	 * @param attributes attributes to extract processor name  
	 * @return processor name or empty string if "pname" attribute not found
	 */
	static public String getProcessorName(IAttributes attributes) {
		return attributes.getAttribute(CmsisConstants.PNAME, CmsisConstants.EMPTY_STRING);
	}

	
	@Override
	public String getDeviceName() {
		return getDeviceName(attributes());
	}
	/**
	 * Returns full device name in form "Name:Pname" 
	 * @param attributes attributes to construct full device name  
	 * @return full device name or null if the attributes parameter does not represent device
	 */
	static public String getDeviceName(IAttributes attributes) {
		String deviceName = null;
		if(attributes.hasAttribute(CmsisConstants.DVARIANT)) {
			deviceName = attributes.getAttribute(CmsisConstants.DVARIANT);
		} else if(attributes.hasAttribute(CmsisConstants.DNAME)) {
			deviceName = attributes.getAttribute(CmsisConstants.DNAME);
		}
		
		if(deviceName != null) {
			String processorName = getProcessorName(attributes);
			if(!processorName.isEmpty()) {
				deviceName += ":" + processorName;  //$NON-NLS-1$
			}
		}
		return deviceName;
	}
	
	@Override
	public String getBundleName() {
		return getAttribute(CmsisConstants.CBUNDLE);
	}

	@Override
	public EVersionMatchMode getVersionMatchMode() {
		return EVersionMatchMode.fromString(getAttribute(CmsisConstants.VERSION_MODE));
	}

	@Override
	public void setVersionMatchMode(EVersionMatchMode mode) {
		String strValue = EVersionMatchMode.toString(mode);
		attributes().setAttribute(CmsisConstants.VERSION_MODE, strValue);
	}
	

	@Override
	public boolean isVersionFixed() {
		return getVersionMatchMode() == EVersionMatchMode.FIXED;
	}

	
	@Override
	public Collection<ICpItem> getBooks() {
		Map<String, ICpItem> books = new TreeMap<String, ICpItem>(new AlnumComparator(false, false));
		Collection<? extends ICpItem> children = getChildren();
		if(children != null && ! children.isEmpty()) {
			for(ICpItem book : children) {
				if(!book.getTag().equals(CmsisConstants.BOOK_TAG)) {
					continue;
				}
				String doc = book.getDoc();
				if(doc.isEmpty()) {
					continue;
				}
				if(doc == null || doc.isEmpty() || books.containsKey(doc)) {
					continue;
				}
				String title = book.getAttribute(CmsisConstants.TITLE);
				books.put(title, book);
			}
		}
		return books.values();
	}

}
