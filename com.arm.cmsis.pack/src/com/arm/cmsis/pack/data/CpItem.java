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

package com.arm.cmsis.pack.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.arm.cmsis.pack.base.CmsisConstants;
import com.arm.cmsis.pack.base.CmsisTreeItem;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.enums.EVersionMatchMode;
import com.arm.cmsis.pack.generic.Attributes;
import com.arm.cmsis.pack.generic.IAttributes;

/**
 * Default implementation of ICpItem interface
 */
public class CpItem extends CmsisTreeItem<ICpItem> implements ICpItem {
	
	protected IAttributes fAttributes = new Attributes(); 
	
	private ICpItem fCondition = null; // cached condition reference for quick access

	protected String fURL = null;
	protected String fId   = null;

	
	/**
	 * Default hierarchy constructor
	 * @param parent parent ICpItem
	 */
	public CpItem(ICpItem parent){
		setParent(parent);
	}

	/**
	 * Constructs item for the given parent and tag
	 * @param parent parent ICpItem 
	 * @param tag item's tag
	 */
	public CpItem(ICpItem parent, String tag) {
		setParent(parent);
		setTag(tag);
	}
	
	
	
	@Override
	public IAttributes attributes() {
		return fAttributes;
	}

	/**
	 * Proxy method to get attribute from attributes 
	 * @param key attribute key
	 * @return attribute value
	 */
	protected String getAttribute(String key) {
		return attributes().getAttribute(key);
	}

	/**
	 * Proxy method to get attribute from attributes 
	 * @param key attribute key
	 * @return true if the item has an attribute
	 */
	protected boolean hasAttribute(String key) {
		return attributes().hasAttribute(key);
	}

	
	@Override
	public String getId() {
		if (fId == null) {
			fId = constructId();
		}
		return fId;
	}
	
	

	@Override
	public String getName() {
		if(hasAttribute("name"))
			return getAttribute("name");
		return super.getName();
	}

	/**
	 * Constructs element Id
	 * default implementation tries to get "id" attribute
	 * if it is not successful, returns <code>"tag.name"<\code>
	 * derived classes may implement other algorithms 
	 * @return constructed element Id
	 */
	public String constructId() {
		String id = getAttribute("id");
		if(id == null) {
			// if not successful, returns "tag.name" 
			id = getTag();
			if(hasAttribute("name")) {
				id += ":" + getAttribute("name");
			}
		}
		return id;
	}


	@Override
	public ICpItem getParent(String tag) {
		for(ICpItem item = getParent(); item != null; item = item.getParent()) {
			if(item.getTag().equals(tag))
				return item;
		}
		return null;
	}
	
	
	@Override
	public ICpPack getPack() {
		if(getParent() != null) 
			return getParent().getPack();
		return null;
	}
	
	@Override
	public String getPackId() {
		ICpPack pack = getPack();
		if(pack != null)
			return pack.getId();
		return IAttributes.EMPTY_STRING;
	}

	@Override
	public String getPackFamilyId() {
		ICpPack pack = getPack();
		if(pack != null)
			return pack.getPackFamilyId();
		return IAttributes.EMPTY_STRING;
	}


	@Override
	public String getItemKey(ICpItem item) {
		if(item != null)
			return item.getTag();
		return null;
	}

	@Override
	public ICpItem getProperty(String id) {
		Collection<? extends ICpItem> children = getChildren();
		if(children != null) {
			for(ICpItem item : children){
				if(item.getId().equals(id))
					return item;
			}
		}
		return null;
	}

	@Override
	public Collection<? extends ICpItem> getChildren(String tag) {
		ICpItem child = getFirstChild(tag);
		if(child != null)
			return child.getChildren();
		return null;
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
		case "condition":
			return new CpCondition(this, tag);
		case "api":
		case "component":
			return new CpComponent(this, tag);
		case "file":
			return new CpFile(this, tag);
		case "devices":
			return new CpDeviceItemContainer(this, tag);
		case "taxonomy":
			return new CpTaxonomyContainer(this, tag);
		case "bundle":
			return new CpItem(this, tag);
		default:
			break;
		}
		return new CpItem(this, tag);
	}

	@Override
	public boolean hasCondition() {
		return hasAttribute("condition");
	}

	
	@Override
	public String getConditionId() {
		return attributes().getAttribute("condition", IAttributes.EMPTY_STRING);
	}


	@Override
	public ICpItem getCondition() {
		if(fCondition == null) { // not cached yet
			String conditionID = getConditionId();
			if(conditionID != null && !conditionID.isEmpty()) {
				ICpPack pack = getPack();
				if(pack != null)
					fCondition = pack.getCondition(conditionID);
			}
		}
		return fCondition;
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
		if(effectiveParent != null) 
			return effectiveParent.getEffectiveAttribute(key);
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
				if(!m.containsKey(key))
					m.put(key, e.getValue());
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
	public void mergeProperty(ICpItem p) {
		String id = p.getId();
		ICpItem inserted = getProperty(id);
		if(inserted == null || !p.isUnique()) {
			addChild(p); // insert only unique properties or descriptions
		} else if (inserted  != null) {
			// add missing attributes, but do not replace existing ones (we go down-up)
			// process sub-properties as well
			inserted.mergeEffectiveContent(p);
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
        	if(p.getId().equals(id))
        		return p;
        }
		return null;
	}

	
	@Override
	public void mergeEffectiveContent(ICpItem property) {
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
		if(hasAttribute("Dvendor")) 
			vendor = getAttribute("Dvendor");
		else if(hasAttribute("vendor"))
			return getAttribute("vendor");	
		if(vendor != null && !vendor.isEmpty())
			return vendor;
		ICpItem parent = getParent();
		if(parent != null)
			return parent.getVendor();
		return null;
	}

	@Override
	public String getVersion() {
		if(hasAttribute("version"))
			return getAttribute("version");
		ICpItem parent = getParent();
		if(parent != null)
			return parent.getVersion();
		return null;
	}

	@Override
	public String getDescription() {
		ICpItem descr = getFirstChild("description");
		if(descr != null)
			return descr.getText();
		return IAttributes.EMPTY_STRING;
	}

	@Override
	public String getUrl() {
		if(fURL == null) {
			fURL = attributes().getAttribute("url");
			if(fURL == null) {
				ICpItem urlItem = getFirstChild("url");
				if(urlItem != null)
					fURL = urlItem.getText();
			}
			if(fURL == null || fURL.isEmpty()) {
				fURL = getDocLink();				
			}
			if(fURL == null)
				fURL = IAttributes.EMPTY_STRING;				

			if(!fURL.isEmpty()) {
				// check if it is a relative path and not an url  
				if (fURL.indexOf(":") == -1 && fURL.indexOf("\\\\") != 0  &&// absolute
					fURL.indexOf("www.") != 0)  { // url without http:  
					ICpPack p = getPack();
					if(p != null) {
						String path = p.getInstallDir(null);
						if(path != null)
							fURL = path + fURL;
					}
				}
			}
		}
		return fURL;
	}

	
	/**
	 * Returns link to an associated document or URL if any 
	 * @return url link or path to document  
	 */
	protected String getDocLink() {
		String doc = getAttribute("doc");
		if(doc == null) {
			doc = getAttribute("name");
		}
		if(doc == null) {
			ICpItem docItem = getFirstChild("doc");
			if(docItem != null)
				doc = docItem.getText();
		}
		return doc;
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
		return attributes.getAttribute(CmsisConstants.PNAME, IAttributes.EMPTY_STRING);
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
		if(attributes.hasAttribute("Dvariant"))
			deviceName = attributes.getAttribute("Dvariant");
		else if(attributes.hasAttribute("Dname"))
			deviceName = attributes.getAttribute("Dname");
		
		if(deviceName != null) {
			String processorName = getProcessorName(attributes);
			if(!processorName.isEmpty()) {
				deviceName += ":" + processorName; 
			}
		}
		return deviceName;
	}
	
	@Override
	public String getBundleName() {
		return attributes().getAttribute(CmsisConstants.CBUNDLE, IAttributes.EMPTY_STRING);
	}

	@Override
	public EVersionMatchMode getVersionMatchMode() {
		return EVersionMatchMode.fromString(attributes().getAttribute("versionMatchMode"));
	}

	@Override
	public void setVersionMatchMode(EVersionMatchMode mode) {
		String strValue = EVersionMatchMode.toString(mode);
		attributes().setAttribute("versionMatchMode", strValue);
	}
	

	@Override
	public boolean isVersionFixed() {
		return getVersionMatchMode() == EVersionMatchMode.FIXED;
	}

	@Override
	public boolean isVendorFixed() {
		return attributes().getAttributeAsBoolean("vendorFixed", false);
	}

	@Override
	public void setVendorFixed(boolean fixed) {
		if(fixed)
			attributes().setAttribute("vendorFixed", "1");
		else
			attributes().removeAttribute("vendorFixed");
	}
}
