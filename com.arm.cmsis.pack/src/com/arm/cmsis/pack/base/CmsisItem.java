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

package com.arm.cmsis.pack.base;

import java.util.Collection;

import com.arm.cmsis.pack.base.ICmsisVisitor.VisitResult;
import com.arm.cmsis.pack.generic.IAttributes;

/**
 *
 */
public class CmsisItem implements ICmsisItem {

	protected String fTag  = IAttributes.EMPTY_STRING;    
	protected String fText = IAttributes.EMPTY_STRING;
	protected volatile Object[] cachedChildArray = null;

	/**
	 * Default constructor    
	 */
	public CmsisItem() {
	}

	@Override
	public Object getParent() {
		// default has no parent
		return null;
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
		if(collection != null)
			return collection.size();
		return 0;
	}

	@Override
	public Object[] getChildArray() {
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
		if(collection != null)
			return collection.toArray();
		return EMPTY_OBJECT_ARRAY;
	}	

	@Override
	public VisitResult accept(ICmsisVisitor visitor) {
		if(visitor == null)
			return VisitResult.CANCEL;
		switch( visitor.visit(this)) {
		case  CANCEL: 
			return VisitResult.CANCEL;
		case SKIP_CHILDREN:
			return VisitResult.CONTINUE; // skip fChildMap, but parent should continue
		case  SKIP_LEVEL:
			return VisitResult.SKIP_CHILDREN; // instruct parent to skip its remaining children
		default:
			break;
		}
		Collection<? extends ICmsisItem> children = getChildren();  
		// process children
		if(children != null) {
			for(ICmsisItem item : children){
				VisitResult result = VisitResult.CONTINUE;
				if(item != null)
					result = item.accept(visitor);
				else
					result = visitor.visit(null);				
				
				switch( result) {
				case  CANCEL: 
					return VisitResult.CANCEL;
				case SKIP_CHILDREN: 
				case SKIP_LEVEL:
					return  VisitResult.CONTINUE; // skip fChildMap, but parent should continue 
				default:
					break;
				}
			}
		}
		return VisitResult.CONTINUE; 
	}
	
}
