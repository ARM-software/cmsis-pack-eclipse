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

package com.arm.cmsis.pack.item;

import java.util.Collection;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.item.ICmsisVisitor.VisitResult;

/**
 *	Base class for all CMSIS items
 */
public class CmsisItem implements ICmsisItem {

	protected String fTag  = CmsisConstants.EMPTY_STRING;    
	protected String fText = CmsisConstants.EMPTY_STRING;
	protected volatile Object[] cachedChildArray = null;

	/**
	 * Default constructor    
	 */
	public CmsisItem() {
	}
	
	@Override
	public void clear() {
		cachedChildArray = null;
	}

	@Override
	public void destroy() {
		clear();
		fTag = null;
		fText = null;
	}
	
	@Override
	public void invalidate() {
		cachedChildArray = null;
	}

	@Override
	public boolean purge() {
		return isRemoved(); 
	}
	
	
	@Override
	public Object getParent() {
		return null; // default has no parent
	}
	
	@Override
	public String getTag() {
		return fTag;
	}

	@Override
	public void setTag(String tag) {
		fTag = tag;
	}

	@Override
	public String getText() {
		return fText;
	}

	@Override
	public void setText(String text) {
		fText = text;
	}

	@Override
	public String getName() {
		return getTag();
	}

	@Override
	public String getEffectiveName() {
		return getName();
	}

	@Override
	public String getDescription() {
		return getText();
	}

	@Override
	public String getUrl() {
		return null;
	}

	@Override
	public String getDoc() {
		return null;
	}


	@Override
	public Collection<? extends ICmsisItem> getChildren() {
		// default has no children
		return null;
	}

	
	@Override
	public boolean isExclusive() {
		return false; // default is non-exclusive
	}
	
	@Override
	public boolean hasChildren() {
		Collection<? extends ICmsisItem> collection = getChildren();
		return collection != null && !collection.isEmpty();
	}

	@Override
	public int getChildCount() {
		Collection<? extends ICmsisItem> collection = getChildren();
		if(collection != null) {
			return collection.size();
		}
		return 0;
	}

	@Override
	public synchronized Object[] getChildArray() {
		if(cachedChildArray == null) {
			cachedChildArray = createChildArray();
		}
		return cachedChildArray;
	}


	/**
	 * Create child array that in general can be different than actual children.
	 * Default creates array out of child collection 
	 * @return created array
	 */
	protected Object[] createChildArray() {
		Collection<? extends ICmsisItem> collection = getChildren();
		if(collection != null) {
			return collection.toArray();
		}
		return EMPTY_OBJECT_ARRAY;
	}	

	@Override
	public VisitResult accept(ICmsisVisitor visitor) {
		if(visitor == null) {
			return VisitResult.CANCEL;
		}
		switch( visitor.visit(this)) {
		case  CANCEL: 
			return VisitResult.CANCEL;
		case SKIP_CHILDREN:
			return VisitResult.CONTINUE; // skip children, but parent should continue
		case  SKIP_LEVEL:
			return VisitResult.SKIP_CHILDREN; // instruct parent to skip its remaining children
		default:
			break;
		}
		// process children
		return accept(visitor, getItemsToVisit());
	}
	
	/**
	 * Returns collection of items to visit
	 * @return collection of items to visit
	 */
	protected Collection<? extends ICmsisItem> getItemsToVisit(){
		// default returns children
		return getChildren();
	}
	
	
	/**
	 * Visit collection of items (usually children)   
	 * @param visitor ICmsisVisitor to visit
	 * @param items collection   
	 * @return VisitResult
	 */
	protected VisitResult accept(ICmsisVisitor visitor, Collection<? extends ICmsisItem> items) {
		if(items != null) {
			for(ICmsisItem item : items){
				VisitResult result = VisitResult.CONTINUE;
				if(item != null) {
					result = item.accept(visitor);
				} else {
					result = visitor.visit(null);
				}				
				switch( result) {
				case  CANCEL: 
					return VisitResult.CANCEL;
				case SKIP_CHILDREN: 
				case SKIP_LEVEL:
					return  VisitResult.CONTINUE; // skip children, but parent should continue 
				default:
					break;
				}
			}
		}
		return VisitResult.CONTINUE;
	}
	
}
