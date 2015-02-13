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
import java.util.HashSet;

import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.enums.EFileCategory;
import com.arm.cmsis.pack.generic.IAttributes;


/**
 *
 */
public class CpComponent extends CpItem implements ICpComponent {
	/**
	 * @param parent
	 * @param tag
	 */
	public CpComponent(ICpItem parent, String tag) {
		super(parent, tag);
		ICpItem bundle = getParent("bundle");
		// inherit attributes from bundle
		if(bundle != null)
			attributes().mergeAttributes(bundle.attributes());
	}

	@Override
	public String constructId() {
		// construct Component ID in the form "PackId::Vendor::Cclass.Cgroup.Cvariant(condition).Version"
		String id = IAttributes.EMPTY_STRING;
		if(!isApi())
			id += getPackId();
		id += "::";
		id += getName();
		if(hasCondition()){
			id += "(";
			id += getConditionId();
			id += ")";
		}
		id += ":";
		id += getVersion();
		return id; 
	}


	@Override
	public String getName() {
		
		String name = IAttributes.EMPTY_STRING;
		if(!isApi())
			name = getVendor();
		name += "::";
		if(hasAttribute("Cbundle")) {
			name += ".";
			name += getAttribute("Cbundle");
		}
		
		name += getAttribute("Cclass");
		name += ".";
		
		name += getAttribute("Cgroup");
		if(hasAttribute("Csub")) {
			name += ".";
			name += getAttribute("Csub");
		}
		if(hasAttribute("Cvariant")) {
			name += ".";
			name += getAttribute("Cvariant");
		}
		return name;
	}

	@Override
	public String getVendor() {
		if(hasAttribute("Cvendor"))
			return getAttribute("Cvendor");
		return super.getVendor();
	}

	@Override
	public String getVersion() {
		if(isApi())
			return attributes().getAttribute("Capiversion", IAttributes.EMPTY_STRING);
		else
			return attributes().getAttribute("Cversion", IAttributes.EMPTY_STRING);
	}


	@Override
	public boolean isApi() {
		return getTag().equals("api");
	}

	
	@Override
	public int getMaxInstances() {
		return attributes().getAttributeAsInt("maxInstances", 1);
	}

	@Override
	public Collection<ICpFile> getFilteredFiles(ICpConditionContext context) {
		Collection<ICpFile> filteredFiles = new HashSet<ICpFile>();
		Collection<? extends ICpItem> allFiles = getChildren("files");
		if(allFiles != null && ! allFiles.isEmpty()) {
			for(ICpItem item : allFiles) {
				if(item instanceof ICpFile) {
					ICpFile f = (ICpFile)item;
					if(f.evaluate(context) != EEvaluationResult.FAILED)
						filteredFiles.add(f);
				}
			}
		}
		return filteredFiles;
	}
	
	protected String getDocLink() {
		Collection<? extends ICpItem> allFiles = getChildren("files");
		if(allFiles != null && ! allFiles.isEmpty()) {
			for(ICpItem item : allFiles) {
				if(item instanceof ICpFile) {
					ICpFile f = (ICpFile)item;
					if(f.getCategory() == EFileCategory.DOC)
						return f.getUrl();
				}
			}
		}
		return null;
	}
	
}
