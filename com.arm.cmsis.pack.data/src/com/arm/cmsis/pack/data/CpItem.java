/*******************************************************************************
* Copyright (c) 2015 - 2019 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.data;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.enums.EVersionMatchMode;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.item.CmsisTreeItem;
import com.arm.cmsis.pack.utils.AlnumComparator;
import com.arm.cmsis.pack.utils.Utils;

/**
 * Default implementation of ICpItem interface
 */
public class CpItem extends CmsisTreeItem<ICpItem> implements ICpItem {
	
	protected IAttributes fAttributes = null; 
	
	protected ICpItem fCondition = null; // cached condition reference for quick access

	protected String fURL = null;
	protected String fId   = null;

	public static final Collection<ICpItem> EMPTY_LIST = new LinkedList<>();
	
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
		if(fAttributes == null)
			fAttributes = createAttributes(); 
		return fAttributes;
	}

	@Override
	public IAttributes createAttributes() {
		return new CpAttributes();
	}

	
	@Override
	public void invalidate() {
		fId = null;
		super.invalidate();
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
	public ICpRootItem getRootItem() {
		if(getParent() != null) {
			return getParent().getRootItem();
		}
		return null;
	}
	
	@Override
	public String getRootDir(boolean keepSlash) {
		ICpRootItem root = getRootItem();
		if(root != null)
			return root.getDir(keepSlash);
		return null;
	}

	@Override
	public String getRootFileName() {
		ICpRootItem root = getRootItem();
		if(root != null)
			return root.getFileName();
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
	protected List<ICpItem> children() {
		return (List<ICpItem>)super.children();
	}
	
	@Override
	public Collection<? extends ICpItem> getChildren() {
		if(fChildren != null) {
			return fChildren;
		}
		return EMPTY_LIST;
	}
	
	@Override
	public Collection<? extends ICpItem> getGrandChildren(String tag) {
		ICpItem child = getFirstChild(tag);
		if(child != null) {
			return child.getChildren();
		}
		return EMPTY_LIST;
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
	public ICpItem clone() {
		ICpItem clonedItem = createItem();
		if(clonedItem != null && attributes().hasAttributes()) {
			clonedItem.attributes().setAttributes(attributes());
		}
		return clonedItem;
	}

	/**
	 * Creates a an item of the same class as this one, called by clone()  
	 * @return new ICpItem 
	 */
	protected ICpItem createItem() {
		ICpItem parent = getParent();
		if(parent != null) {
			return parent.createItem(parent, getTag());
		} 
		return createItem(parent, getTag());
	}
	
	@Override
	public ICpItem copyTo(ICpItem parent, boolean copyChildren) {
		ICpItem clonedItem = clone();
		clonedItem.setParent(parent);
		if(parent != null) {
			parent.addChild(clonedItem);
		}
		if(copyChildren)
			copyChildrenTo(clonedItem);
		return clonedItem;
	}

	
	@Override
	public void copyChildrenTo(ICpItem newParent) {
		if(!hasChildren())
			return;
		for(ICpItem child : getChildren()) {
			child.copyTo(newParent);
		}
	}

	@Override
	public boolean updateItem(ICpItem other) {
		if(other == null)
			return false;
		boolean bModified = updateAttributes(other);
		Collection<? extends ICpItem> otherChildren = other.getChildren();
		Collection<? extends ICpItem> children = getChildren();
		if(otherChildren.size() != children.size()) {
			// replace all children
			fChildren = null;
			other.copyChildrenTo(this);
			return true;
		}
		
		Iterator<? extends ICpItem> it = children.iterator();
		Iterator<? extends ICpItem> itOther = otherChildren.iterator();
	    while (it.hasNext() && itOther.hasNext()) {
	        ICpItem child = it.next();
	        ICpItem otherChild = itOther.next();
	    	if(child.updateItem(otherChild))
	    		bModified = true;
	    }
		
		return bModified;
	}
	

	
	@Override
	public ICpItem createItem(ICpItem parent, String tag) {
		ICpItem item = createChildItem(tag);
		if(parent != this) {
			item.setParent(parent);
		}
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
		case CmsisConstants.GENERATOR_TAG:
			return new CpGenerator(this, tag);
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
		if(p == inserted)
			return; // do not insert the same item twice (can happen when merging effective content)
		if(inserted == null || !p.isUnique()) {
			addChild(p); // insert only unique properties or descriptions
			if(p.providesEffectiveContent()) {
				p.mergeEffectiveContent(p, processorName);
			}
		} else {
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
	public boolean isGenerated() {
		ICpItem parent = getParent();
		if(parent != null)
			return parent.isGenerated(); 
		return false;
	}

	@Override
	public boolean isDeprecated() {
		ICpPack pack = getPack();
		if(pack != null)
			return pack.isDeprecated();
		return false;
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
		} else if(hasAttribute(CmsisConstants.VENDOR)) {
			return getAttribute(CmsisConstants.CVENDOR);
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
		if(hasAttribute(CmsisConstants.INFO))
			return getAttribute(CmsisConstants.INFO);
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
		String basePath = getRootDir(true);
		if(basePath != null && !basePath.isEmpty()) {
			String absPath = basePath + relPath; 
			Path path = Paths.get(absPath);
			return path.toString().replace('\\', '/'); 
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
	public int getPunitIndex() {
		return getPunitIndex(attributes());
	}

	@Override
	public int getPunitsCount() {
		return getPunitsCount(attributes());
	}
	
	/**
	 * Returns "Punit" attribute as integer
	 * @param attributes attributes to extract Punit
	 * @return processor unit index (0 is default) 
	 */
	static public int getPunitIndex(IAttributes attributes) {
		return attributes.getAttributeAsInt(CmsisConstants.PUNIT, 0);
	}

	/**
	 * Returns "Punits" attribute as integer
	 * @param attributes attributes to extract "Punits"
	 * @return processor unit count (1 is default) 
	 */
	static public int getPunitsCount(IAttributes attributes) {
		return attributes.getAttributeAsInt(CmsisConstants.PUNITS, 1);
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
				if(doc == null || doc.isEmpty() || books.containsKey(doc)) {
					continue;
				}
				String title = book.getAttribute(CmsisConstants.TITLE);
				books.put(title, book);
			}
		}
		return books.values();
	}

	@Override
	public boolean isDefaultVariant() {
		return attributes().getAttributeAsBoolean(CmsisConstants.IS_DEFAULT_VARIANT, false);
	}

	@Override
	public boolean matchesHost() {
		String host = getAttribute(CmsisConstants.HOST);
		return host == null || host.isEmpty() || host.equals(CmsisConstants.ALL) || host.equals(Utils.getHostType());
	}

	@Override
	public boolean isRemoved() {
		return getAttributeAsBoolean(CmsisConstants.REMOVED, false);
	}

	@Override
	public ICpItem toSimpleTree(ICpItem parent) {
		String tag = getTag();
		if(tag == null || tag.isEmpty())
			return null; // no element without tag
		ICpItem item = new CpItem(parent, tag);
		if(parent != null) {
			parent.addChild(item); 
		}
		// add text if no children or attributes 
		String text = getText();
		if(text != null && !text.isEmpty()) {
			if(hasChildren() || attributes().hasAttributes()){
				// cannot add text to item directly , reate a separate "text" one
				ICpItem t = new CpItem(item, CmsisConstants.TEXT);
				t.setText(text);
				item.addChild(t);
			} else{
				item.setText(text);
			}
		}
		// expand attributes
		if(attributes().hasAttributes()) {
			Map<String, String> attributesMap = attributes().getAttributesAsMap();
			for(Entry<String, String> e: attributesMap.entrySet()) {
				ICpItem a = toSimpleItem(item,  e.getKey(),  e.getValue());
				item.addChild(a);
			}
		}
		if(hasChildren()) {
			for(ICpItem c: getChildren() ){ 
				item.addChild(c.toSimpleTree(item)); 
			}
		}
		return item;
	}
	
	@Override
	public ICpItem toSimpleItem(ICpItem parent, String key, String value) {
		if(key == null)
			return null; // cannot create an item without tag
		ICpItem item = new CpItem(parent, key);
		item.setText(value);
		return item;
	}
	
}
